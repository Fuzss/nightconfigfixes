package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import fuzs.nightconfigfixes.config.CheckedConfigFileTypeHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.electronwill.nightconfig.core.ConfigSpec.CorrectionAction.*;

@Mixin(value = ForgeConfigSpec.class, remap = false)
abstract class ForgeConfigSpecForgeMixin extends UnmodifiableConfigWrapper<UnmodifiableConfig> {
    @Shadow
    private Map<List<String>, String> levelComments;

    protected ForgeConfigSpecForgeMixin(UnmodifiableConfig config) {
        super(config);
    }

    @Shadow
    private int correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun) {
        throw new RuntimeException();
    }

    @Inject(method = "correct(Lcom/electronwill/nightconfig/core/UnmodifiableConfig;Lcom/electronwill/nightconfig/core/CommentedConfig;Ljava/util/LinkedList;Ljava/util/List;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Z)I", at = @At("HEAD"), cancellable = true)
    private void correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun, CallbackInfoReturnable<Integer> callback) {
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
                    count += this.correct((Config) specValue, (CommentedConfig) configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun) {
                        callback.setReturnValue(count);
                        return;
                    }
                } else if (dryRun) {
                    callback.setReturnValue(1);
                    return;
                } else {
                    CommentedConfig newValue = config.createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += this.correct((Config) specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = this.levelComments.get(parentPath);
                String oldComment = config.getComment(key);
                if (!this.stringsMatchIgnoringNewlines(oldComment, newComment)) {
                    if (commentListener != null)
                        commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, newComment);

                    if (dryRun) {
                        callback.setReturnValue(1);
                        return;
                    }

                    config.setComment(key, newComment);
                }
            } else {
                ForgeConfigSpec.ValueSpec valueSpec = (ForgeConfigSpec.ValueSpec) specValue;
                if (!valueSpec.test(configValue)) {
                    if (dryRun) {
                        callback.setReturnValue(1);
                        return;
                    }

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

                    if (dryRun) {
                        callback.setReturnValue(1);
                        return;
                    }

                    config.setComment(key, valueSpec.getComment());
                }
            }

            parentPath.removeLast();
        }

        // Second step: removes the unspecified values
        for (Iterator<Map.Entry<String, Object>> ittr = configMap.entrySet().iterator(); ittr.hasNext(); ) {
            Map.Entry<String, Object> entry = ittr.next();
            if (!specMap.containsKey(entry.getKey())) {
                if (dryRun) {
                    callback.setReturnValue(1);
                    return;
                }

                ittr.remove();
                parentPath.addLast(entry.getKey());
                listener.onCorrect(REMOVE, parentPathUnmodifiable, entry.getValue(), null);
                parentPath.removeLast();
                count++;
            }
        }
        callback.setReturnValue(count);
        return;
    }

    @Shadow
    private boolean stringsMatchIgnoringNewlines(@Nullable Object obj1, @Nullable Object obj2) {
        throw new RuntimeException();
    }
}
