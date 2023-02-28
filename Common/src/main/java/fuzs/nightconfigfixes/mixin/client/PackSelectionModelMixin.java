package fuzs.nightconfigfixes.mixin.client;

import fuzs.nightconfigfixes.ForwardingPackSelectionModelEntry;
import fuzs.nightconfigfixes.ResourceConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackSelectionModel.class)
abstract class PackSelectionModelMixin {
    @Shadow
    @Final
    private PackRepository repository;

    @Inject(method = "method_29640", at = @At("TAIL"), cancellable = true)
    public void getUnselected(Pack pack, CallbackInfoReturnable<PackSelectionModel.Entry> callback) {
        if (this.repository == Minecraft.getInstance().getResourcePackRepository()) {
            callback.setReturnValue(new ForwardingPackSelectionModelEntry(pack, callback.getReturnValue(), ResourceConfigHandler.getOverride(pack.getId())));
        }
    }

    @Inject(method = "method_29644", at = @At("TAIL"), cancellable = true)
    public void getSelected(Pack pack, CallbackInfoReturnable<PackSelectionModel.Entry> callback) {
        if (this.repository == Minecraft.getInstance().getResourcePackRepository()) {
            callback.setReturnValue(new ForwardingPackSelectionModelEntry(pack, callback.getReturnValue(), ResourceConfigHandler.getOverride(pack.getId())));
        }
    }
}
