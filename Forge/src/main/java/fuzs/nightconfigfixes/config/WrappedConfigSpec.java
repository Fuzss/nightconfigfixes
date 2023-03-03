package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.base.Joiner;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction.*;
import static net.minecraftforge.fml.Logging.CORE;

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
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Joiner DOT_JOINER = Joiner.on(".");

    private final ModConfig modConfig;
    private final IConfigSpec<?> spec;
    private boolean isCorrecting;
    private boolean isSettingConfig;

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
        this.isSettingConfig = true;
        // prevent ForgeConfigSpec from correcting the config, we do this ourselves so our custom correction method may be used
        this.spec.acceptConfig(data);
        this.isSettingConfig = false;
        if (this.spec instanceof ForgeConfigSpec) {
            if (data != null && !this.isCorrect(data)) {
                String configName = data instanceof FileConfig ? ((FileConfig) data).getNioPath().toString() : data.toString();
                LOGGER.warn(CORE, "Configuration file {} is not correct. Correcting", configName);
                this.correct(data, (action, path, incorrectValue, correctedValue) -> LOGGER.warn(CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join(path), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : ""), (action, path, incorrectValue, correctedValue) -> LOGGER.debug(CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join(path)));

                if (data instanceof FileConfig) {
                    ((FileConfig) data).save();
                }
            }
            this.afterReload();
        }
    }

    @Override
    public boolean isCorrecting() {
        return this.isCorrecting;
    }

    @Override
    public boolean isCorrect(CommentedConfig commentedFileConfig) {
        if (!(this.spec instanceof ForgeConfigSpec)) {
            return this.spec.isCorrect(commentedFileConfig);
        }
        return this.isSettingConfig || this._isCorrect(commentedFileConfig);
    }

    @Override
    public int correct(CommentedConfig commentedFileConfig) {
        if (!(this.spec instanceof ForgeConfigSpec)) {
            return this.spec.correct(commentedFileConfig);
        }
        return this._correct(commentedFileConfig);
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

    public UnmodifiableConfig getSpec() {
        return this.spec instanceof ForgeConfigSpec forgeConfigSpec ? forgeConfigSpec.getSpec() : null;
    }

    public synchronized boolean _isCorrect(CommentedConfig config) {
        LinkedList<String> parentPath = new LinkedList<>();
        return this.correct(this.getSpec(), config, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {
        }, null, true) == 0;
    }

    public int _correct(CommentedConfig config) {
        return this.correct(config, (action, path, incorrectValue, correctedValue) -> {
        }, null);
    }

    public synchronized int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener) {
        return this.correct(config, listener, null);
    }

    public synchronized int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener) {
        LinkedList<String> parentPath = new LinkedList<>(); //Linked list for fast add/removes
        int ret = -1;
        try {
            this.isCorrecting = true;
            ret = this.correct(this.getSpec(), config, parentPath, Collections.unmodifiableList(parentPath), listener, commentListener, false);
        } finally {
            this.isCorrecting = false;
        }
        return ret;
    }

    private int correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun) {
        int count = 0;

        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();
        final Map<String, Object> defaultMap;
        if (config instanceof FileConfig fileConfig) {
            defaultMap = CheckedConfigFileTypeHandler.DEFAULT_CONFIG_VALUES.get(fileConfig.getNioPath().getFileName().toString().intern());
        } else {
            defaultMap = null;
        }

        for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
            final String key = specEntry.getKey();
            final Object specValue = specEntry.getValue();
            final Object configValue = configMap.get(key);
            final ConfigSpec.CorrectionAction action = configValue == null ? ADD : REPLACE;

            parentPath.addLast(key);

            if (specValue instanceof Config) {
                if (configValue instanceof CommentedConfig) {
                    count += this.correct((Config) specValue, (CommentedConfig) configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun) return count;
                } else if (dryRun) {
                    return 1;
                } else {
                    CommentedConfig newValue = config.createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += this.correct((Config) specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = ((ForgeConfigSpec) this.spec).getLevelComment(parentPath);
                String oldComment = config.getComment(key);
                if (!this.stringsMatchIgnoringNewlines(oldComment, newComment)) {
                    if (commentListener != null)
                        commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, newComment);

                    if (dryRun) return 1;

                    config.setComment(key, newComment);
                }
            } else {
                ForgeConfigSpec.ValueSpec valueSpec = (ForgeConfigSpec.ValueSpec) specValue;
                if (!valueSpec.test(configValue)) {
                    if (dryRun) return 1;

                    // Night Config Fixes: try to get the value from the default config first before falling back to the built-in default config value
                    Object newValue;
                    if (defaultMap != null && defaultMap.containsKey(key)) {
                        newValue = defaultMap.get(key);
                        if (!valueSpec.test(newValue)) {
                            newValue = valueSpec.correct(configValue);
                        }
                    } else {
                        newValue = valueSpec.correct(configValue);
                    }

                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                }
                String oldComment = config.getComment(key);
                if (!this.stringsMatchIgnoringNewlines(oldComment, valueSpec.getComment())) {
                    if (commentListener != null)
                        commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, valueSpec.getComment());

                    if (dryRun) return 1;

                    config.setComment(key, valueSpec.getComment());
                }
            }

            parentPath.removeLast();
        }

        // Second step: removes the unspecified values
        for (Iterator<Map.Entry<String, Object>> ittr = configMap.entrySet().iterator(); ittr.hasNext(); ) {
            Map.Entry<String, Object> entry = ittr.next();
            if (!specMap.containsKey(entry.getKey())) {
                if (dryRun) return 1;

                ittr.remove();
                parentPath.addLast(entry.getKey());
                listener.onCorrect(REMOVE, parentPathUnmodifiable, entry.getValue(), null);
                parentPath.removeLast();
                count++;
            }
        }
        return count;
    }

    private boolean stringsMatchIgnoringNewlines(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 instanceof String string1 && obj2 instanceof String string2) {
            if (string1.length() > 0 && string2.length() > 0) {
                return string1.replaceAll("\r\n", "\n").equals(string2.replaceAll("\r\n", "\n"));

            }
        }
        // Fallback for when we're not given Strings, or one of them is empty
        return Objects.equals(obj1, obj2);
    }
}
