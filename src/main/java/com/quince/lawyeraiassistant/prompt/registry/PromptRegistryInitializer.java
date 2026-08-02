package com.quince.lawyeraiassistant.prompt.registry;

import com.quince.lawyeraiassistant.prompt.definition.PromptDefinition;
import com.quince.lawyeraiassistant.prompt.loader.PromptLoader;
import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Prompt 注册表初始化器。
 *
 * <p>
 * 负责在应用启动时加载生产链路需要的 Prompt，
 * 并注册到 PromptRegistry。
 * </p>
 *
 * <p>
 * 本类只负责初始化编排：
 * </p>
 *
 * <pre>
 * PromptLoader
 *      ↓
 * PromptFragment
 *      ↓
 * PromptRegistry
 * </pre>
 *
 * <p>
 * 不负责读取文件细节，也不负责保存 Prompt。
 * </p>
 */
@Component
public class PromptRegistryInitializer {

    private final PromptLoader promptLoader;

    private final PromptRegistry promptRegistry;

    public PromptRegistryInitializer(
            PromptLoader promptLoader,
            PromptRegistry promptRegistry) {
        this.promptLoader = Objects.requireNonNull(
                promptLoader,
                "promptLoader must not be null");

        this.promptRegistry = Objects.requireNonNull(
                promptRegistry,
                "promptRegistry must not be null");
    }

    /**
     * 应用启动后初始化 PromptRegistry。
     */
    @PostConstruct
    public void initialize() {
        register(
                PromptDefinition.LAWYER_SYSTEM.getName(),
                PromptDefinition.LAWYER_SYSTEM.getLocation());
    }

    /**
     * 加载并注册一个 Prompt。
     *
     * @param expectedName Prompt 在 Registry 中的逻辑名称
     * @param location     Prompt 资源位置
     */
    private void register(
            String expectedName,
            String location) {
        PromptFragment loadedFragment = promptLoader.load(location);

        PromptFragment fragment = normalizeName(
                loadedFragment,
                expectedName);

        promptRegistry.register(fragment);
    }

    /**
     * 显式使用 PromptNames 中定义的逻辑名称。
     *
     * <p>
     * Loader 根据文件名解析出来的名称通常也是
     * lawyer-system，但初始化器不依赖这个隐式约定。
     * </p>
     */
    private PromptFragment normalizeName(
            PromptFragment fragment,
            String expectedName) {
        Objects.requireNonNull(
                fragment,
                "Loaded PromptFragment must not be null");

        return PromptFragment.builder()
                .name(expectedName)
                .content(fragment.getContent())
                .version(fragment.getVersion())
                .source(fragment.getSource())
                .build();
    }
}