package com.quince.lawyeraiassistant.prompt;

import com.quince.lawyeraiassistant.prompt.loader.PromptLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 旧 Prompt 资源访问方式的兼容层。
 *
 * <p>
 * 生产 Prompt Pipeline 已经通过：
 * </p>
 *
 * <pre>
 * PromptLoader
 *      ↓
 * PromptRegistry
 *      ↓
 * PromptFactory
 * </pre>
 *
 * <p>
 * 本类目前只为尚未迁移的教学接口提供兼容能力。
 * 后续 Playground 重构完成后，可以考虑删除。
 * </p>
 */
@Component
public class PromptResourceLoader {

    private final ResourceLoader resourceLoader;

    private final PromptLoader promptLoader;

    public PromptResourceLoader(
            ResourceLoader resourceLoader,
            PromptLoader promptLoader) {
        this.resourceLoader = Objects.requireNonNull(
                resourceLoader,
                "resourceLoader must not be null");

        this.promptLoader = Objects.requireNonNull(
                promptLoader,
                "promptLoader must not be null");
    }

    /**
     * 加载 Prompt 文本。
     *
     * <p>
     * 实际读取职责已经委托给 PromptLoader。
     * </p>
     *
     * @param location Prompt 资源位置
     * @return Prompt 文本内容
     */
    public String load(String location) {
        return promptLoader
                .load(location)
                .getContent();
    }

    /**
     * 获取原始 Spring Resource。
     *
     * <p>
     * 该方法目前仅用于旧的 PromptTemplate 教学接口。
     * 生产代码不应该直接依赖 Resource。
     * </p>
     *
     * @param location 资源位置
     * @return 可读取的 Resource
     */
    public Resource getResource(String location) {
        validateLocation(location);

        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Prompt resource does not exist: "
                            + location);
        }

        if (!resource.isReadable()) {
            throw new IllegalStateException(
                    "Prompt resource is not readable: "
                            + location);
        }

        return resource;
    }

    private void validateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt resource location must not be blank");
        }
    }
}