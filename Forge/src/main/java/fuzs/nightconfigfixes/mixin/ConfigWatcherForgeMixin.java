package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fuzs.nightconfigfixes.util.ConfigLoadingUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraftforge.fml.config.ConfigFileTypeHandler$ConfigWatcher")
abstract class ConfigWatcherForgeMixin {

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;load()V"), remap = false)
    public void run$load(CommentedFileConfig configData) {
        ConfigLoadingUtil.tryLoadConfigFile(configData);
    }
}
