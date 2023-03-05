package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import net.minecraftforge.common.ForgeConfigSpec;
import org.objectweb.asm.tree.InsnList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeConfigSpec.class, remap = false)
abstract class ForgeConfigSpecForgeMixin extends UnmodifiableConfigWrapper<UnmodifiableConfig> {

    protected ForgeConfigSpecForgeMixin(UnmodifiableConfig config) {
        super(config);
    }

    @Inject(method = "afterReload", at = @At("HEAD"))
    public void afterReload(CallbackInfo c) {
        System.out.println();
    }
}
