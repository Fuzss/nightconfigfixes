package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ForgeConfigSpec.class)
abstract class ForgeConfigSpecForgeMixin extends UnmodifiableConfigWrapper<UnmodifiableConfig> {

    protected ForgeConfigSpecForgeMixin(UnmodifiableConfig config) {
        super(config);
    }
}
