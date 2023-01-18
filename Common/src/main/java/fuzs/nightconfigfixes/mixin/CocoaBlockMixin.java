package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.HelloPrinter;
import fuzs.nightconfigfixes.NightConfigFixes;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CocoaBlock.class)
public class CocoaBlockMixin implements HelloPrinter {

    @Unique
    public void printHello2222() {
        System.out.println("weho");
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(BlockBehaviour.Properties properties, CallbackInfo callback) {
        NightConfigFixes.LOGGER.warn("HELLO!");
    }
}
