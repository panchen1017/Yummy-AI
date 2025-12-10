package cn.bugstack.knowledge.config;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@Configuration
public class OpenAIConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        // 文本分割器：将文档拆分为更小的片段，以便向量化与检索
        return new TokenTextSplitter();
    }


    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apikey) {
        // 构建 OpenAI API 客户端，使用配置中的 base-url 与 api-key
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apikey)
                .build();
    }

    @Bean("openAiSimpleVectorStore")
    public SimpleVectorStore vectorStore(OpenAiApi openAiApi) {
        // 内存型向量库：基于 OpenAI EmbeddingModel，适合快速试验与小规模数据
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * -- 删除旧的表（如果存在）
     * DROP TABLE IF EXISTS public.vector_store_openai;
     *
     * -- 创建新的表，使用UUID作为主键
     * CREATE TABLE public.vector_store_openai (
     *     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     *     content TEXT NOT NULL,
     *     metadata JSONB,
     *     embedding VECTOR(1536)
     * );
     *
     * SELECT * FROM vector_store_openai
     */
    @Bean("openAiPgVectorStore")
    public PgVectorStore pgVectorStore(OpenAiApi openAiApi, JdbcTemplate jdbcTemplate) {
        // 持久化向量库：使用 Postgres pgvector 存储嵌入，表名为 vector_store_openai
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("vector_store_openai")
                .build();
    }

//    /**
//     * 调用 openAI
//     * @param openAiChatModel
//     * @return
//     */
//    @Bean
//    public ChatClient.Builder chatClientBuilder(OpenAiChatModel openAiChatModel) {
//        // 构建 ChatClient.Builder：用于集成 MCP 工具并进行对话调用
//        return new DefaultChatClientBuilder(openAiChatModel, ObservationRegistry.NOOP, (ChatClientObservationConvention) null);
//    }

    /**
     * 处理重复 MCP 客户端并设置为主提供者
     * @param mcpClients
     * @return
     */
    @Bean("syncMcpToolCallbackProvider")
    @Primary
    public SyncMcpToolCallbackProvider syncMcpToolCallbackProvider(List<McpSyncClient> mcpClients) {
//        mcpClients.remove(0);

        // 用于记录 name 和其对应的 index
        Map<String, Integer> nameToIndexMap = new HashMap<>();
        // 用于记录重复的 index
        Set<Integer> duplicateIndices = new HashSet<>();

        // 遍历 mcpClients 列表
        for (int i = 0; i < mcpClients.size(); i++) {
            String name = mcpClients.get(i).getServerInfo().name();
            if (nameToIndexMap.containsKey(name)) {
                // 如果 name 已经存在，记录当前 index 为重复
                duplicateIndices.add(i);
            } else {
                // 否则，记录 name 和 index
                nameToIndexMap.put(name, i);
            }
        }

        // 删除重复的元素，从后往前删除以避免影响索引
        List<Integer> sortedIndices = new ArrayList<>(duplicateIndices);
        sortedIndices.sort(Collections.reverseOrder());
        for (int index : sortedIndices) {
            mcpClients.remove(index);
        }

        return new SyncMcpToolCallbackProvider(mcpClients);
    }

    /**
     * 构建客户端并挂工具
     * 启动时创建 ChatClient 实例，默认挂载工具集合（包含 CSDN 发帖工具）
     * @param openAiChatModel
     * @param tools
     * @return
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ToolCallbackProvider tools) {
        DefaultChatClientBuilder defaultChatClientBuilder = new DefaultChatClientBuilder(openAiChatModel, ObservationRegistry.NOOP, (ChatClientObservationConvention) null);
        return defaultChatClientBuilder
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .build())
                .build();
    }
}
