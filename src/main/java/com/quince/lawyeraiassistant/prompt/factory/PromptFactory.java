package com.quince.lawyeraiassistant.prompt.factory;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;

/**
 * 面向业务层的 Prompt 获取入口。
 *
 * <p>
 * 业务代码不应该直接依赖 PromptRegistry，
 * 而应该通过 PromptFactory 获取所需 Prompt。
 * </p>
 */
public interface PromptFactory {

    /**
     * 根据 Prompt 的逻辑名称获取 PromptFragment。
     *
     * @param name Prompt 逻辑名称
     * @return 已注册的 PromptFragment
     */
    PromptFragment get(String name);

    /**
     * 获取律师助手的基础系统 Prompt。
     *
     * @return 律师系统 Prompt
     */
    PromptFragment lawyerSystem();
}