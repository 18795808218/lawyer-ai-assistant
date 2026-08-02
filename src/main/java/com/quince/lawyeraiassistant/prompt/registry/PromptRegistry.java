package com.quince.lawyeraiassistant.prompt.registry;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;

public interface PromptRegistry {

    void register(PromptFragment fragment);

    PromptFragment find(String name);

    boolean contains(String name);

    void clear();

}