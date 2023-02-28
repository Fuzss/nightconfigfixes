package fuzs.nightconfigfixes.mixin.client;

import fuzs.nightconfigfixes.ForwardingPackSelectionModelEntry;
import fuzs.nightconfigfixes.ResourceConfigHandler;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackSelectionModel.class)
abstract class PackSelectionModelMixin {

    @Inject(method = "method_29640", at = @At("TAIL"), cancellable = true)
    public void getUnselected(Pack pack, CallbackInfoReturnable<PackSelectionModel.Entry> callback) {
        callback.setReturnValue(new ForwardingPackSelectionModelEntry(pack, callback.getReturnValue(), ResourceConfigHandler.getOverride(pack.getId())));
    }

    @Inject(method = "method_29644", at = @At("TAIL"), cancellable = true)
    public void getSelected(Pack pack, CallbackInfoReturnable<PackSelectionModel.Entry> callback) {
        callback.setReturnValue(new ForwardingPackSelectionModelEntry(pack, callback.getReturnValue(), ResourceConfigHandler.getOverride(pack.getId())));
    }
}
