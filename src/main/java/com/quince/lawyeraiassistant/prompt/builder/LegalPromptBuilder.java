package com.quince.lawyeraiassistant.prompt.builder;

import com.quince.lawyeraiassistant.prompt.factory.PromptFactory;
import com.quince.lawyeraiassistant.prompt.knowledge.KnowledgeFormatter;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import com.quince.lawyeraiassistant.prompt.template.TemplateRenderer;
import com.quince.lawyeraiassistant.prompt.template.TemplateVariables;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 法律场景 Prompt 构建器。
 *
 * <p>
 * 负责把静态 Prompt 模板与动态 PromptContext
 * 组装成 Spring AI Prompt。
 * </p>
 *
 * <p>
 * 当前构建链路：
 * </p>
 *
 * <pre>
 * PromptContext
 *      ↓
 * KnowledgeFormatter
 *      ↓
 * TemplateVariables
 *      ↓
 * TemplateRenderer
 *      ↓
 * SystemMessage + UserMessage
 *      ↓
 * Prompt
 * </pre>
 */
@Component
public class LegalPromptBuilder {

    private final PromptFactory promptFactory;

    private final TemplateRenderer templateRenderer;

    private final KnowledgeFormatter knowledgeFormatter;

    public LegalPromptBuilder(
            PromptFactory promptFactory,
            TemplateRenderer templateRenderer,
            KnowledgeFormatter knowledgeFormatter) {
        this.promptFactory = Objects.requireNonNull(
                promptFactory,
                "PromptFactory must not be null");

        this.templateRenderer = Objects.requireNonNull(
                templateRenderer,
                "TemplateRenderer must not be null");

        this.knowledgeFormatter = Objects.requireNonNull(
                knowledgeFormatter,
                "KnowledgeFormatter must not be null");
    }

    /**
     * 构建法律问答 Prompt。
     *
     * @param context Prompt 上下文
     * @return Spring AI Prompt
     */
    public Prompt build(PromptContext context) {
        validateContext(context);

        PromptFragment systemFragment = promptFactory.lawyerSystem();

        validateSystemFragment(systemFragment);

        TemplateVariables variables = TemplateVariables.from(
                context,
                knowledgeFormatter);

        List<Message> messages = new ArrayList<>();

        messages.add(
                buildSystemMessage(
                        systemFragment,
                        variables));

        messages.add(
                buildUserMessage(context));

        return new Prompt(messages);
    }

    /**
     * 渲染系统 Prompt 模板并构造 SystemMessage。
     */
    private SystemMessage buildSystemMessage(
            PromptFragment fragment,
            TemplateVariables variables) {
        String renderedSystemPrompt = templateRenderer.render(
                fragment.getContent(),
                variables.toMap());

        validateRenderedPrompt(
                renderedSystemPrompt);

        return new SystemMessage(
                renderedSystemPrompt);
    }

    /**
     * 用户问题继续作为独立 UserMessage。
     */
    private UserMessage buildUserMessage(
            PromptContext context) {
        return new UserMessage(
                context.getQuestion());
    }

    private void validateContext(
            PromptContext context) {
        Objects.requireNonNull(
                context,
                "PromptContext must not be null");

        if (context.getQuestion() == null
                || context.getQuestion().isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt question must not be blank");
        }
    }

    private void validateSystemFragment(
            PromptFragment fragment) {
        Objects.requireNonNull(
                fragment,
                "System PromptFragment must not be null");

        if (fragment.getContent() == null
                || fragment.getContent().isBlank()) {
            throw new IllegalArgumentException(
                    "System PromptFragment content must not be blank");
        }
    }

    private void validateRenderedPrompt(
            String renderedPrompt) {
        if (renderedPrompt == null
                || renderedPrompt.isBlank()) {
            throw new IllegalStateException(
                    "Rendered system prompt must not be blank");
        }
    }
}