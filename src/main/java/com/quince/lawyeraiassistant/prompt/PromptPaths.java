package com.quince.lawyeraiassistant.prompt;

/**
 * Prompt 资源路径。
 *
 * <p>
 * 当前仍作为兼容常量类保留。
 * Recovery Sprint 之后不再进行批量迁移。
 * </p>
 */
public final class PromptPaths {

    /**
     * 正式律师系统 Prompt。
     */
    public static final String LAWYER_SYSTEM = "classpath:prompts/system/lawyer-system.st";

    /**
     * 案件分析教学/Playground Prompt。
     */
    public static final String CASE_ANALYSIS = "classpath:prompts/legal/case-analysis.st";

    private PromptPaths() {
        throw new IllegalStateException(
                "PromptPaths is a constants class and cannot be instantiated");
    }
}