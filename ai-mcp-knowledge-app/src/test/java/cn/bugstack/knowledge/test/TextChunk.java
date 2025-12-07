package cn.bugstack.knowledge.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {
    /**
     * 唯一标识：用于父子块关联与追踪
     */
    private String id;

    /**
     * 文本内容：当前块的具体文本
     */
    private String text;

    /**
     * 父块 ID：用于层级分割时记录来源
     */
    private String parentId;

    /**
     * 元数据：存放额外信息（如来源、知识库名等）
     */
    private Map<String, Object> metadata;

    public TextChunk(String text) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.metadata = new HashMap<>();
    }
}
