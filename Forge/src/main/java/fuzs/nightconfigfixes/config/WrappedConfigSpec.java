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
//        ObfuscationReflectionHelper.setPrivateValue(ModConfig.class, this.modConfig, data, "configData");
        try {
            Field field = ModConfig.class.getDeclaredField("configData");
            field.setAccessible(true);
            MethodHandles.lookup().unreflectSetter(field).invoke(this.modConfig, data);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
