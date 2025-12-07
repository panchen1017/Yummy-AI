package cn.bugstack.knowledge.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MCPTest {

    /**
     * 在 openAI Config 中注入
     */
    @Resource
    private ChatClient.Builder chatClientBuilder;

//    @Resource
//    private ChatClient chatClient;

    /**
     * 让 springAI 帮我加载下有哪些服务可以被使用
     */
    @Autowired
    private ToolCallbackProvider tools;

    /**
     * - 在配置里声明“要用哪些外部工具服务器”，Spring AI把这些工具接上
     * 测试里把这些工具交给聊天客户端；你提问时，模型发现有工具可用，让框架去执行，拿到结果后再把答案返回给你。
     * 先通过配置文件把需要调用的工具从 json 文件中读出来
     * 本次使用到的工具服务器是 filesystem 和 mcp-server-computer
     * filesystem 是一个现成的 MCP 服务器包 @modelcontextprotocol/server-filesystem ，它以Node包形式分发，跨平台、零安装调用方便。
     * - 配置工具源
     * springAI 这个包就去读取这些工具包，并且登记到 tools 就是 ToolCallbackProvider 中去。
     * <p>
     * 然后，拿着刚刚注册的 tools 工具和 Input请求，调用 openAI 的 API，发起对话。
     *   - 执行 `.prompt(userInput).call()` 时，请求里会附带“工具清单”，模型知道自己能用哪些工具。
     */
    @Test
    public void test_tool() {
        // 查询可用 MCP 工具，并通过 ChatClient 调用
        String userInput = "有哪些工具可以使用";
        var chatClient = chatClientBuilder
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    @Test
    public void test() {
        // 组合使用 MCP 工具：获取电脑配置，并写入指定路径文件
        String userInput = "获取电脑配置";
//        userInput = "在 /Users/fuzhengwei/Desktop 文件夹下，创建 电脑.txt";
        userInput = "获取电脑配置后，在 D:\\javaproject\\panchen\\testmcp 文件夹下，创建 电脑.txt 把电脑配置写入 电脑.txt";

        var chatClient = chatClientBuilder
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .build())
                .build();

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }
//    @Test
//    public void test_saveArticle() {
//        String userInput;
//        userInput = """
//                我需要你帮我生成一篇文章，要求如下；
//
//                1. 场景为互联网大厂java求职者面试
//                2. 面试管提问 Java 核心知识、JUC、JVM、多线程、线程池、HashMap、ArrayList、Spring、SpringBoot、MyBatis、Dubbo、RabbitMQ、xxl-job、Redis、MySQL、Linux、Docker、设计模式、DDD等不限于此的各项技术问题。
//                3. 按照故事场景，以严肃的面试官和搞笑的水货程序员谢飞机进行提问，谢飞机对简单问题可以回答，回答好了面试官还会夸赞。复杂问题胡乱回答，回答的不清晰。
//                4. 每次进行3轮提问，每轮可以有3-5个问题。这些问题要有技术业务场景上的衔接性，循序渐进引导提问。最后是面试官让程序员回家等通知类似的话术。
//                5. 提问后把问题的答案，写到文章最后，最后的答案要详细讲述出技术点，让小白可以学习下来。
//
//                根据以上内容，不要阐述其他信息，请直接提供；文章标题、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）
//
//                将以上内容发布文章到CSDN
//                """;
//
//        System.out.println("\n>>> QUESTION: " + userInput);
//        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
//    }

}
