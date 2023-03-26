package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fuzs.nightconfigfixes.config.CheckedConfigFileTypeHandler;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraftforge.resource.ResourceCacheManager$ResourceManagerBootCacheConfigurationHandler")
abstract class ResourceManagerBootCacheConfigurationHandlerMixin {

    @Redirect(method = "createConfiguration", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;load()V"), remap = false)
    private static void createConfiguration(CommentedFileConfig configData) {
        if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("recreateConfigsWhenParsingFails")) {
            configData.load();
        } else {
            CheckedConfigFileTypeHandler.tryLoadConfigFile(configData);
        }
    }
}
