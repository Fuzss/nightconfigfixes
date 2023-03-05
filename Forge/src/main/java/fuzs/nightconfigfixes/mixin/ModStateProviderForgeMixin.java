package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.NightConfigFixes;
import fuzs.nightconfigfixes.config.ModConfigSpecUtil;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.core.ModStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ModStateProvider.class)
abstract class ModStateProviderForgeMixin {

    @Inject(method = "lambda$new$3(Lnet/minecraftforge/fml/ModList;)V", at = @At("HEAD"), remap = false)
    private static void run(ModList modList, CallbackInfo callback) {
        try {
            if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("correctConfigValuesFromDefaultConfig")) return;
            ConfigTracker.INSTANCE.fileMap().values().forEach(ModConfigSpecUtil::wrapSpec);
            ModList.get().applyForEachModContainer(Function.identity()).forEach(ModConfigSpecUtil::wrapConfigHandler);
        } catch (Exception e) {
            throw new RuntimeException("%s ran into a problem. DO NOT REPORT THIS TO FORGE!!!".formatted(NightConfigFixes.MOD_NAME), e);
        }
    }
}
