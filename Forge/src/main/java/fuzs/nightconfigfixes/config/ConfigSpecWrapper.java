package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.*;

import static com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction.*;

public class ConfigSpecWrapper {

    // accessed by JavaScript core mod
    public static int correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun, Map<List<String>, String> levelComments) {
        int count = 0;

        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();
        final Map<String, Object> defaultMap;
        if (config instanceof FileConfig) {
            defaultMap = CheckedConfigFileTypeHandler.DEFAULT_CONFIG_VALUES.get(((FileConfig) config).getNioPath().getFileName().toString().intern());
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
                    count += ConfigSpecWrapper.correct((Config) specValue, (CommentedConfig) configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun, levelComments);
                    if (count > 0 && dryRun) return count;
                } else if (dryRun) {
                    return 1;
                } else {
                    CommentedConfig newValue = config.createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += ConfigSpecWrapper.correct((Config) specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun, levelComments);
                }

                String newComment = levelComments.get(parentPath);
                String oldComment = config.getComment(key);
                if (!stringsMatchIgnoringNewlines(oldComment, newComment)) {
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
                if (!stringsMatchIgnoringNewlines(oldComment, valueSpec.getComment())) {
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

    private static boolean stringsMatchIgnoringNewlines(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 instanceof String && obj2 instanceof String) {
            String string1 = (String) obj1;
            String string2 = (String) obj2;

            if (string1.length() > 0 && string2.length() > 0) {
                return string1.replaceAll("\r\n", "\n")
                        .equals(string2.replaceAll("\r\n", "\n"));

            }
        }
        // Fallback for when we're not given Strings, or one of them is empty
        return Objects.equals(obj1, obj2);
    }
}
