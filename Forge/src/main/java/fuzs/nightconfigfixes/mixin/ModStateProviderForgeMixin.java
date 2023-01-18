package fuzs.nightconfigfixes.mixin;

import com.google.common.collect.ImmutableSet;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import fuzs.nightconfigfixes.config.WrappedModConfig;
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
        if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("recreateConfigsWhenParsingFails")) return;
        // store all configs in a new set first as we'll be modifying the underlying map
        Set<ModConfig> configs = ImmutableSet.copyOf(ConfigTracker.INSTANCE.fileMap().values());
        configs.forEach(WrappedModConfig::replace);
    }
}
