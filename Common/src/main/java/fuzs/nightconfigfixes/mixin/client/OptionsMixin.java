package fuzs.nightconfigfixes.mixin.client;

import fuzs.nightconfigfixes.ResourceConfigHandler;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Options.class)
abstract class OptionsMixin {
    @Shadow
    public List<String> resourcePacks;

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CallbackInfo callback) {
        // Add built-in resource packs if they are enabled by default only if the options file is blank.
        // Idea copied from Fabric Api.
        if (this.resourcePacks.isEmpty()) {
            this.resourcePacks.addAll(ResourceConfigHandler.getDefaultResourcePacks(false));
        }
    }
}
