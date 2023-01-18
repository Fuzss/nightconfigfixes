package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.config.CheckedConfigFileTypeHandler;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        if (!NightConfigFixesConfig.INSTANCE.<Boolean>getValue("recreateConfigsWhenParsingFails")) return;
        // use the IMixinConfigPlugin to switch this field as early as possible, couldn't think of something else that loads this early and is easily accessible by the mod
        // also the TOML field not being final is very odd, let's just hope it stays like that
        // otherwise go back to the original approach with wrapping the ModConfig instances which is currently disabled
        ObfuscationReflectionHelper.setPrivateValue(ConfigFileTypeHandler.class, null, CheckedConfigFileTypeHandler.TOML, "TOML");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
