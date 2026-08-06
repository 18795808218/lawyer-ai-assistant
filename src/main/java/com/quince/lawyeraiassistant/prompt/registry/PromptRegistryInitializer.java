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
 * 负责在应用启动时加载 PromptDefinition 中声明的全部
 * 正式 Prompt，并注册到 PromptRegistry。
 * </p>
 *
 * <pre>
 * PromptDefinition
 *      ↓
 * PromptLoader
 *      ↓
 * PromptFragment
 *      ↓
 * PromptRegistry
 * </pre>
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
         *
         * <p>
         * 后续新增 Prompt 时，只需要增加 PromptDefinition，
         * 不需要再次修改本初始化器。
         * </p>
         */
        @PostConstruct
        public void initialize() {
                for (PromptDefinition definition : PromptDefinition.values()) {

                        register(definition);
                }
        }

        /**
         * 加载并注册一个 PromptDefinition。
         */
        private void register(
                        PromptDefinition definition) {

                Objects.requireNonNull(
                                definition,
                                "PromptDefinition must not be null");

                PromptFragment loadedFragment = promptLoader.load(
                                definition.getLocation());

                PromptFragment normalizedFragment = normalizeFragment(
                                loadedFragment,
                                definition);

                promptRegistry.register(
                                normalizedFragment);
        }

        /**
         * 使用 PromptDefinition 中的逻辑名称和版本，
         * 避免依赖 Loader 根据文件名推断名称的隐式约定。
         */
        private PromptFragment normalizeFragment(
                        PromptFragment fragment,
                        PromptDefinition definition) {

                Objects.requireNonNull(
                                fragment,
                                "Loaded PromptFragment must not be null");

                Objects.requireNonNull(
                                definition,
                                "PromptDefinition must not be null");

                return PromptFragment.builder()
                                .name(
                                                definition.getName())
                                .content(
                                                fragment.getContent())
                                .version(
                                                definition.getVersion())
                                .source(
                                                fragment.getSource())
                                .build();
        }
}