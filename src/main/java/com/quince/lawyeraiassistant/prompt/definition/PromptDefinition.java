package com.quince.lawyeraiassistant.prompt.definition;

import com.quince.lawyeraiassistant.prompt.PromptNames;
import com.quince.lawyeraiassistant.prompt.PromptPaths;

/**
 * Prompt 元数据定义。
 *
 * 当前作为新的统一入口，
 * 内部仍复用 PromptNames / PromptPaths，
 * 保证 Recovery Sprint 期间不破坏现有代码。
 */
public enum PromptDefinition {

    LAWYER_SYSTEM(
            PromptNames.LAWYER_SYSTEM,
            PromptPaths.LAWYER_SYSTEM,
            "v1");

    private final String name;

    private final String location;

    private final String version;

    PromptDefinition(
            String name,
            String location,
            String version) {
        this.name = name;
        this.location = location;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getVersion() {
        return version;
    }
}