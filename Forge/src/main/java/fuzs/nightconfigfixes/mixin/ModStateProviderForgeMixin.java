package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.NightConfigFixes;
import fuzs.nightconfigfixes.config.ModConfigSpecUtil;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ModLoader.class)
abstract class ModStateProviderForgeMixin {

    @Inject(method = "loadMods", at = @At("HEAD"), remap = false)
    public void loadMods(final ModWorkManager.DrivenExecutor syncExecutor, final Executor parallelExecutor, final Function<Executor, CompletableFuture<Void>> beforeSidedEvent, final Function<Executor, CompletableFuture<Void>> afterSidedEvent, final Runnable periodicTask, CallbackInfo callback) {
        try {
            if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("correctConfigValuesFromDefaultConfig")) return;
            ConcurrentHashMap<String, ModConfig> fileMap = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "fileMap");
            if (fileMap == null) return;
            fileMap.values().forEach(ModConfigSpecUtil::applySpec);
        } catch (Exception e) {
            throw new RuntimeException(String.format("%s ran into a problem. DO NOT REPORT THIS TO FORGE!!!", NightConfigFixes.MOD_NAME), e);
        }
    }
}
