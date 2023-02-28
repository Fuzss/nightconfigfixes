package fuzs.nightconfigfixes.mixin.client;

import fuzs.nightconfigfixes.PackAwareSelectionEntry;
import fuzs.nightconfigfixes.ResourceConfigHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.stream.Stream;

@Mixin(PackSelectionScreen.class)
abstract class PackSelectionScreenMixin extends Screen {

    protected PackSelectionScreenMixin(Component component) {
        super(component);
    }

    @ModifyVariable(method = "updateList", at = @At("HEAD"))
    private Stream<PackSelectionModel.Entry> updateList(Stream<PackSelectionModel.Entry> models) {
        return models.filter(entry -> !(entry instanceof PackAwareSelectionEntry contextEntry) || !ResourceConfigHandler.getOverride(contextEntry.getPackId()).hidden());
    }
}
