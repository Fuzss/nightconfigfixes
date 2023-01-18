package fuzs.nightconfigfixes.mixin;

import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModConfig.class)
public interface ModConfigForgeAccessor {

    @Accessor("configHandler")
    @Mutable
    void setConfigHandler(ConfigFileTypeHandler configHandler);
}
