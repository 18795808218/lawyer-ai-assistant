package com.quince.lawyeraiassistant.prompt.loader;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;

/**
 * Prompt 加载器。
 *
 * 负责从指定来源读取 Prompt，
 * 并转换成 PromptFragment。
 *
 * 不负责缓存。
 * 不负责版本管理。
 */
public interface PromptLoader {

    /**
     * 加载 Prompt。
     *
     * @param location Prompt资源位置
     * @return PromptFragment
     */
    PromptFragment load(String location);

}