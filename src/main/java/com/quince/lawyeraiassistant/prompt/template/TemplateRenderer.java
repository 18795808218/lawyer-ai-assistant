package com.quince.lawyeraiassistant.prompt.template;

import java.util.Map;

/**
 * 项目内部统一的 Prompt 模板渲染接口。
 *
 * <p>
 * Prompt Builder 只依赖该接口，不直接依赖 Spring AI 的
 * PromptTemplate，从而隔离具体模板引擎实现。
 * </p>
 */
public interface TemplateRenderer {

    /**
     * 使用变量渲染模板。
     *
     * @param template  Prompt 模板文本
     * @param variables 模板变量
     * @return 渲染后的 Prompt 文本
     */
    String render(
            String template,
            Map<String, Object> variables
    );
}