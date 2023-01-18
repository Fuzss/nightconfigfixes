package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(ServerLifecycleHooks.class)
abstract class ServerLifecycleHooksForgeMixin {

    @Inject(method = "getServerConfigPath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getServerConfigPath(final MinecraftServer server, CallbackInfoReturnable<Path> callback) {
        if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("forceGlobalServerConfigs")) return;
        callback.setReturnValue(FMLPaths.CONFIGDIR.get());
    }
}
