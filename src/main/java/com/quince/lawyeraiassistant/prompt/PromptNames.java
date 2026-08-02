package com.quince.lawyeraiassistant.prompt;

/**
 * Prompt 在 Registry 中使用的逻辑名称。
 *
 * <p>这里保存的是 Prompt 名称，而不是资源文件路径。</p>
 *
 * <p>例如：</p>
 * <pre>
 * Prompt 名称：lawyer-system
 * 资源路径：classpath:prompts/system/lawyer-system.st
 * </pre>
 */
public final class PromptNames {

    /**
     * 律师助手基础身份和行为约束。
     */
    public static final String LAWYER_SYSTEM =
            "lawyer-system";

    /**
     * 法律知识引用规则。
     *
     * <p>只有项目中实际存在该 Prompt 时才使用。</p>
     */
    public static final String CITATION_RULES =
            "citation-rules";

    /**
     * 无法可靠回答时的拒答规则。
     *
     * <p>只有项目中实际存在该 Prompt 时才使用。</p>
     */
    public static final String REFUSE_RULES =
            "refuse-rules";

    private PromptNames() {
        throw new IllegalStateException(
                "PromptNames is a constants class and cannot be instantiated"
        );
    }
}