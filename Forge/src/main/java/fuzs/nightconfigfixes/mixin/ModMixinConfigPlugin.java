package fuzs.nightconfigfixes.mixin;

import fuzs.nightconfigfixes.config.CheckedConfigFileTypeHandler;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.MavenVersionAdapter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    private static final VersionRange RESOURCE_CACHING_FORGE_VERSIONS = MavenVersionAdapter.createFromVersionSpec("[43.1.35,44.0.40)");

    @Override
    public void onLoad(String mixinPackage) {
        CheckedConfigFileTypeHandler.replaceDefaultConfigHandler();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if ("net.minecraftforge.resource.ResourceCacheManager$ResourceManagerBootCacheConfigurationHandler".equals(targetClassName)) {
            ModFileInfo modFileInfo = FMLLoader.getLoadingModList().getModFileById("forge");
            if (modFileInfo == null) return false;
            DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(modFileInfo.versionString());
            return RESOURCE_CACHING_FORGE_VERSIONS.containsVersion(artifactVersion);
        }
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
