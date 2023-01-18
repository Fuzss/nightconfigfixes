package fuzs.nightconfigfixes.config;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;

public class WrappedModConfig extends ModConfig {
    private final ModContainer container;
    private final ConfigFileTypeHandler configHandler;

    private WrappedModConfig(ModConfig other) {
        this(other, ModList.get().getModContainerById(other.getModId()).orElseThrow());
    }

    private WrappedModConfig(ModConfig other, ModContainer container) {
        super(other.getType(), other.getSpec(), container, other.getFileName());
        this.container = container;
        this.configHandler = CheckedConfigFileTypeHandler.TOML;
    }

    public static void replace(ModConfig modConfig) {
        // if the mod config we want to copy already has data set for some reason do nothing (which it shouldn't, as we copy everything before configs load which is when this is set)
        // mods do so much weird stuff with the config system, maybe it's a config for one of those mods that manually load their configs early to allow using them during registration events
        // just skip it, no point in supporting it, if the file is faulty it's been loaded already anyway and made the game crash
        if (modConfig.getConfigData() != null) return;
        unregister(modConfig);
        new WrappedModConfig(modConfig);
    }

    private static void unregister(ModConfig modConfig) {
        // file map is most important one to remove, otherwise an exception will be thrown as the file path is duplicated
        // also type sets just store everything via reference comparison, so the initial config won't be replaced when we add our copy, therefore remove manually
        // there is a third storage by mod id in an enum map, that's not an issue, the previous config will be replaced when the new one is inserted (also the map is only for retrieving the config file name which stays the same between copy and original)
        // lastly mod configs are also stored in ModContainer#configs, but the contents are neither accessible nor used anywhere, so we don't care
        if (ConfigTracker.INSTANCE.fileMap().remove(modConfig.getFileName()) == null || !ConfigTracker.INSTANCE.configSets().get(modConfig.getType()).remove(modConfig)) {
            throw new NullPointerException("Mod config %s has not previously been registered".formatted(modConfig.getFileName()));
        }
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return this.configHandler;
    }

    public void fireEvent(final IConfigEvent configEvent) {
        this.container.dispatchConfigEvent(configEvent);
    }
}
