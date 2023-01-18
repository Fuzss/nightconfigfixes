package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fuzs.nightconfigfixes.util.ConfigLoadingUtil;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConfigFileTypeHandler.class)
abstract class ConfigFileTypeHandlerForgeMixin {

    @Redirect(method = "lambda$reader$1(Ljava/nio/file/Path;Lnet/minecraftforge/fml/config/ModConfig;)Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;load()V"), remap = false)
    public void reader$load(CommentedFileConfig configData) {
        ConfigLoadingUtil.tryLoadConfigFile(configData);
    }
}
