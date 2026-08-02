package com.quince.lawyeraiassistant.prompt.registry;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的 PromptRegistry 默认实现。
 *
 * <p>
 * 负责 Prompt 的注册、查询和生命周期管理。
 * </p>
 */
@Component
public class DefaultPromptRegistry
        implements PromptRegistry {

    private final Map<String, PromptFragment> registry = new ConcurrentHashMap<>();

    @Override
    public void register(PromptFragment fragment) {
        Objects.requireNonNull(
                fragment,
                "Prompt fragment must not be null");

        String name = validateName(
                fragment.getName());

        PromptFragment previous = registry.putIfAbsent(
                name,
                fragment);

        if (previous != null) {
            throw new IllegalStateException(
                    "Prompt already registered: "
                            + name);
        }
    }

    @Override
    public PromptFragment find(String name) {
        String validatedName = validateName(name);

        PromptFragment fragment = registry.get(validatedName);

        if (fragment == null) {
            throw new IllegalArgumentException(
                    "Prompt not found: "
                            + validatedName);
        }

        return fragment;
    }

    @Override
    public boolean contains(String name) {
        String validatedName = validateName(name);

        return registry.containsKey(
                validatedName);
    }

    @Override
    public void clear() {
        registry.clear();
    }

    private String validateName(String name) {
        Objects.requireNonNull(
                name,
                "Prompt name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt name must not be blank");
        }

        return name;
    }
}