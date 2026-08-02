package com.quince.lawyeraiassistant.prompt.template;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * TemplateRenderer 的默认实现。
 *
 * <p>
 * 内部使用 Spring AI PromptTemplate 渲染模板。
 * </p>
 *
 * <p>
 * 默认变量格式：
 * </p>
 *
 * <pre>
 * {question}
 * {knowledge}
 * {conversationId}
 * </pre>
 */
@Component
public class DefaultTemplateRenderer implements TemplateRenderer {

    @Override
    public String render(
            String template,
            Map<String, Object> variables) {
        validateTemplate(template);

        Map<String, Object> safeVariables = variables == null
                ? Collections.emptyMap()
                : variables;

        PromptTemplate promptTemplate = new PromptTemplate(template);

        return promptTemplate.render(safeVariables);
    }

    /**
     * 校验模板文本。
     *
     * @param template 模板文本
     */
    private void validateTemplate(String template) {
        Objects.requireNonNull(
                template,
                "Prompt template must not be null");

        if (template.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt template must not be blank");
        }
    }
}