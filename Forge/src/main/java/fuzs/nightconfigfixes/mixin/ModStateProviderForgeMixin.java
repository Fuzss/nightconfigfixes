package fuzs.nightconfigfixes.mixin;

import com.google.common.collect.ImmutableSet;
import fuzs.nightconfigfixes.util.ConfigLoadingUtil;
import fuzs.nightconfigfixes.util.WrappedModConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.core.ModStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModStateProvider.class)
abstract class ModStateProviderForgeMixin {

    @Inject(method = "lambda$new$3(Lnet/minecraftforge/fml/ModList;)V", at = @At("HEAD"), remap = false)
    private static void run(ModList modList, CallbackInfo callback) {
        Set<ModConfig> configs = ImmutableSet.copyOf(ConfigTracker.INSTANCE.fileMap().values());
//        configs.forEach(ConfigLoadingUtil::unregisterModConfig);
        ConfigLoadingUtil.clearTrackedConfigs();
        configs.forEach(WrappedModConfig::copy);
    }
}
