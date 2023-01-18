package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A wrapped {@link net.minecraftforge.common.ForgeConfigSpec} for the only reason of hooking into {@link #acceptConfig(CommentedConfig)},
 * so we can 'listen' to it and update <code>configData</code> in the {@link ModConfig} we have wrapped in {@link WrappedModConfig}
 *
 * <p>The original <code>configData</code> field needs to be updated, as mods might be holding on to the original {@link ModConfig} instance they created for retrieving that field.
 * It's the only mutable field in {@link ModConfig}, so there is nothing else we need to worry about.
 *
 * <p>The only method that should ever be called on this in {@link ModConfig} is {@link #acceptConfig(CommentedConfig)}, as the getter is overridden to provide the original spec.
 */
public class WrappedConfigSpec implements IConfigSpec<WrappedConfigSpec> {
    private final ModConfig modConfig;
    private final IConfigSpec<?> spec;

    private WrappedConfigSpec(ModConfig modConfig) {
        this.modConfig = modConfig;
        this.spec = modConfig.getSpec();
    }

    public static IConfigSpec<?> of(ModConfig modConfig) {
        return new WrappedConfigSpec(modConfig);
    }

    @Override
    public void acceptConfig(CommentedConfig data) {
        ObfuscationReflectionHelper.setPrivateValue(ModConfig.class, this.modConfig, data, "configData");
        this.spec.acceptConfig(data);
    }

    @Override
    public boolean isCorrecting() {
        return this.spec.isCorrecting();
    }

    @Override
    public boolean isCorrect(CommentedConfig commentedFileConfig) {
        return this.spec.isCorrect(commentedFileConfig);
    }

    @Override
    public int correct(CommentedConfig commentedFileConfig) {
        return this.spec.correct(commentedFileConfig);
    }

    @Override
    public void afterReload() {
        this.spec.afterReload();
    }

    @Override
    public <T> T getRaw(List<String> path) {
        return this.spec.getRaw(path);
    }

    @Override
    public boolean contains(List<String> path) {
        return this.spec.contains(path);
    }

    @Override
    public int size() {
        return this.spec.size();
    }

    @Override
    public Map<String, Object> valueMap() {
        return this.spec.valueMap();
    }

    @Override
    public Set<? extends Entry> entrySet() {
        return this.spec.entrySet();
    }

    @Override
    public ConfigFormat<?> configFormat() {
        return this.spec.configFormat();
    }
}
