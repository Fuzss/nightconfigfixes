package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import com.google.common.base.Joiner;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction.*;
import static net.minecraftforge.fml.Logging.CORE;

/**
 * A wrapped {@link net.minecraftforge.common.ForgeConfigSpec} for the only reason of hooking into {@link #correct(UnmodifiableConfig, CommentedConfig, Map, LinkedList, List, ConfigSpec.CorrectionListener, ConfigSpec.CorrectionListener, boolean)},
 * so we can modify the behavior for restoring faulty config values, so that instead of directly retrieving the built-in default value from {@link ForgeConfigSpec.ValueSpec#correct(Object)},
 * we first check if there is a default value defined by a default config file found in <code>defaultconfigs</code>.
 */
public class ConfigSpecWrapper extends UnmodifiableConfigWrapper<ForgeConfigSpec> implements IConfigSpec<ConfigSpecWrapper> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Joiner DOT_JOINER = Joiner.on(".");

    private boolean isCorrecting;

    public ConfigSpecWrapper(ForgeConfigSpec config) {
        super(config);
    }

    public ForgeConfigSpec getSpec() {
        return this.config;
    }

    @Override
    public void acceptConfig(CommentedConfig data) {
        // do not call ForgeConfigSpec#acceptConfig as it triggers Forge's correction behavior
        ObfuscationReflectionHelper.setPrivateValue(ForgeConfigSpec.class, this.config, data, "childConfig");
        if (data != null && !this.isCorrect(data)) {
            String configName = data instanceof FileConfig ? ((FileConfig) data).getNioPath().toString() : data.toString();
            LOGGER.warn(CORE, "Configuration file {} is not correct. Correcting", configName);
            this.correct(data);

            if (data instanceof FileConfig) {
                ((FileConfig) data).save();
            }
        }
        this.afterReload();
    }

    @Override
    public boolean isCorrecting() {
        return this.isCorrecting;
    }

    @Override
    public boolean isCorrect(CommentedConfig commentedFileConfig) {
        synchronized (this) {
            LinkedList<String> parentPath = new LinkedList<>();
            return this.correct(this.config, commentedFileConfig, null, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {
            }, (action, path, incorrectValue, correctedValue) -> {
            }, true) == 0;
        }
    }

    // all methods below copied from net.minecraftforge.common.ForgeConfigSpec with only minor adjustments

    @Override
    public synchronized int correct(CommentedConfig config) {
        LinkedList<String> parentPath = new LinkedList<>(); //Linked list for fast add/removes
        int ret;
        try {
            this.isCorrecting = true;
            final Map<String, Object> defaultMap;
            if (config instanceof FileConfig fileConfig) {
                defaultMap = CheckedConfigFileTypeHandler.DEFAULT_CONFIG_VALUES.get(fileConfig.getNioPath().getFileName().toString().intern());
            } else {
                defaultMap = null;
            }
            ret = this.correct(this.config, config, defaultMap, parentPath, Collections.unmodifiableList(parentPath), (action, path, incorrectValue, correctedValue) -> {
                LOGGER.warn(CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join(path), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : "");
            }, (action, path, incorrectValue, correctedValue) -> {
                LOGGER.debug(CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join(path));
            }, false);
        } finally {
            this.isCorrecting = false;
        }
        return ret;
    }

    @Override
    public void afterReload() {
        this.config.afterReload();
    }

    private int correct(UnmodifiableConfig spec, CommentedConfig config, @Nullable Map<String, Object> defaultMap, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun) {
        int count = 0;

        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();

        for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
            final String key = specEntry.getKey();
            final Object specValue = specEntry.getValue();
            final Object configValue = configMap.get(key);
            final ConfigSpec.CorrectionAction action = configValue == null ? ADD : REPLACE;

            parentPath.addLast(key);

            if (specValue instanceof Config) {
                if (configValue instanceof CommentedConfig) {
                    count += this.correct((Config) specValue, (CommentedConfig) configValue, defaultMap != null && defaultMap.get(key) instanceof Config defaultConfig ? defaultConfig.valueMap() : null, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun) return count;
                } else if (dryRun) {
                    return 1;
                } else {
                    CommentedConfig newValue = config.createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += this.correct((Config) specValue, newValue, defaultMap != null && defaultMap.get(key) instanceof Config defaultConfig ? defaultConfig.valueMap() : null, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = this.config.getLevelComment(parentPath);
                String oldComment = config.getComment(key);
                if (!this.stringsMatchIgnoringNewlines(oldComment, newComment)) {
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
