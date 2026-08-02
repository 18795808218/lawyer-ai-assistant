package com.quince.lawyeraiassistant.prompt.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 表示一个可独立管理和复用的 Prompt 片段。
 *
 * <p>
 * PromptFragment 不是最终发送给大模型的 Prompt，
 * 而是构建最终 Prompt 时使用的一个静态组成部分。
 * </p>
 *
 * <p>
 * 例如：
 * </p>
 * <ul>
 * <li>律师身份定义</li>
 * <li>法律咨询业务规则</li>
 * <li>引用规范</li>
 * <li>拒答规则</li>
 * <li>输出格式要求</li>
 * </ul>
 */
@Getter
@Builder
@ToString
public class PromptFragment {

    /**
     * Prompt 片段的唯一名称。
     *
     * <p>
     * 该名称用于 PromptRegistry 注册和查询，
     * 不应该直接使用资源路径作为业务名称。
     * </p>
     *
     * <p>
     * 示例：
     * </p>
     * <ul>
     * <li>lawyer-identity</li>
     * <li>legal-consultation</li>
     * <li>citation-rules</li>
     * </ul>
     */
    private final String name;

    /**
     * Prompt 片段的实际文本内容。
     */
    private final String content;

    /**
     * Prompt 版本。
     *
     * <p>
     * 第一版可以统一使用 v1，未来支持 Prompt 灰度发布、
     * 版本切换和回滚时使用。
     * </p>
     */
    private final String version;

    /**
     * Prompt 的来源。
     *
     * <p>
     * 当前一般是 classpath 资源路径，例如：
     * </p>
     *
     * <pre>
     * classpath:prompts/v1/identity/lawyer.md
     * </pre>
     */
    private final String source;
}