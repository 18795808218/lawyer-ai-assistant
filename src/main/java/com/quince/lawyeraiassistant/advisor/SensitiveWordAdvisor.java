package com.quince.lawyeraiassistant.advisor;

import com.quince.lawyeraiassistant.exception.SensitiveWordException;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class SensitiveWordAdvisor implements CallAdvisor {

    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 200;

    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "诈骗",
            "洗钱",
            "赌博",
            "删库",
            "木马",
            "sql注入");

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        String userText = extractLatestUserText(request);

        findSensitiveWord(userText)
                .ifPresent(word -> {
                    throw new SensitiveWordException(word);
                });

        return chain.nextCall(request);
    }

    private String extractLatestUserText(ChatClientRequest request) {

        return request.prompt()
                .getInstructions()
                .stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .reduce((first, second) -> second)
                .map(Message::getText)
                .orElse("");
    }

    private Optional<String> findSensitiveWord(String userText) {

        if (userText == null || userText.isBlank()) {
            return Optional.empty();
        }

        String normalizedText = userText.toLowerCase(Locale.ROOT);

        return SENSITIVE_WORDS.stream()
                .filter(normalizedText::contains)
                .findFirst();
    }

    @Override
    public String getName() {
        return SensitiveWordAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}