package cn.bugstack.knowledge.test.Utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;

public class TokenTextSplitterWithContext {

    private final int chunkSize;
    private final int chunkOverlap;

    public TokenTextSplitterWithContext(int chunkSize, int chunkOverlap) {
        // 指定每块大小与重叠量，用于生成更平滑的上下文窗口
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<Document> split(List<Document> documents) {
        // 将输入文档按分词结果进行窗口切分，附带原始元数据
        List<Document> result = new ArrayList<>();
        for (Document doc : documents) {
            String[] tokens = tokenize(doc.getText());
            int start = 0;
            while (start < tokens.length) {
                int end = Math.min(start + chunkSize, tokens.length);
                StringBuilder chunkBuilder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    chunkBuilder.append(tokens[i]).append(" ");
                }
                String chunkText = chunkBuilder.toString().trim();
                Document chunkDoc = new Document(chunkText);
                chunkDoc.getMetadata().putAll(doc.getMetadata());
                result.add(chunkDoc);
                start += (chunkSize - chunkOverlap);
            }
        }
        return result;
    }

    private String[] tokenize(String text) {
        // 使用结巴分词进行中文分词，返回词序列
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> segTokens = segmenter.process(text, JiebaSegmenter.SegMode.INDEX);
        return segTokens.stream().map(token -> token.word).toArray(String[]::new);
    }


}
