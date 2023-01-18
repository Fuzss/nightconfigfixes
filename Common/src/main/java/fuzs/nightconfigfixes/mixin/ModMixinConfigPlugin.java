package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.NightConfigFixes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (true) return true;
        targetClassName = targetClassName.replace("/", ".");
        mixinClassName = mixinClassName.replace("/", ".");
        NightConfigFixes.LOGGER.info("Should apply mixin {} to {}: {}", mixinClassName, targetClassName, clazzExists(targetClassName, this.getClass().getClassLoader()));
        return clazzExists(targetClassName, this.getClass().getClassLoader());
    }

    private static boolean clazzExists(String clazzName, ClassLoader loader) {
        try {
            Class.forName(clazzName, false, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
