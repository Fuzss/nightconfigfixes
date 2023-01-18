package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.util.SafeConfigFileTypeHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.core.ModStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModStateProvider.class)
abstract class ModStateProviderForgeMixin {

    @Inject(method = "lambda$new$3(Lnet/minecraftforge/fml/ModList;)V", at = @At("HEAD"), remap = false)
    private static void run(ModList modList, CallbackInfo callback) {
        ConfigTracker.INSTANCE.fileMap().values().forEach(modConfig -> {
            ((ModConfigForgeAccessor) modConfig).setConfigHandler(SafeConfigFileTypeHandler.TOML);
        });
    }
}
