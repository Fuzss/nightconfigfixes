package fuzs.nightconfigfixes.config;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.*;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class WrappedModConfig extends ModConfig {
    private final IConfigSpec<?> spec;
    private final ModContainer container;
    private final ConfigFileTypeHandler configHandler;

    private WrappedModConfig(ModConfig other, ModContainer container) {
        super(other.getType(), WrappedConfigSpec.of(other), container, other.getFileName());
        this.spec = other.getSpec();
        this.container = container;
        this.configHandler = CheckedConfigFileTypeHandler.TOML;
    }

    public static void replace(ModConfig oldConfig) {
        // if the mod config we want to copy already has data set for some reason, do nothing (which it shouldn't, as we copy everything before configs load which is when this is set)
        // mods do so much weird stuff with the config system, maybe it's a config for one of those mods that manually load their configs early to allow using them during registration events
        // just skip it, no point in supporting it, if the file is faulty it's been loaded already anyway and should have made the game crash
        if (oldConfig.getConfigData() != null) return;
        unregister(oldConfig);
        ModContainer container = ModList.get().getModContainerById(oldConfig.getModId()).orElseThrow();
        ModConfig newConfig = new WrappedModConfig(oldConfig, container);
        wrapConfigHandler(container, newConfig, oldConfig);
    }

    private static void unregister(ModConfig oldConfig) {
        // file map is most important one to remove, otherwise an exception will be thrown as the file path is duplicated
        // also type sets just store everything via reference comparison, so the initial config won't be replaced when we add our copy, therefore remove manually
        // there is a third storage by mod id in an enum map, that's not an issue, the previous config will be replaced when the new one is inserted (also the map is only for retrieving the config file name which stays the same between copy and original)
        // lastly mod configs are also stored in ModContainer#configs, but the contents are neither accessible nor used anywhere, so we don't care
        if (ConfigTracker.INSTANCE.fileMap().remove(oldConfig.getFileName()) == null || !ConfigTracker.INSTANCE.configSets().get(oldConfig.getType()).remove(oldConfig)) {
            throw new RuntimeException("Mod config %s has not previously been registered".formatted(oldConfig.getFileName()));
        }
    }

    private static void wrapConfigHandler(ModContainer container, ModConfig newConfig, ModConfig oldConfig) {
        // replace configHandler field in ModContainer, so we can switch back to the original ModConfig when any IConfigEvent is fired
        // mods might be holding on to the original ModConfig instance and make a reference comparison
        // this will be wrapped multiple times, as it's replaced for every WrappedModConfig instance that is created
        Optional<Consumer<IConfigEvent>> optional = ObfuscationReflectionHelper.getPrivateValue(ModContainer.class, container, "configHandler");
        Objects.requireNonNull(optional, "config handler is null");
        optional.ifPresent(configHandler -> ObfuscationReflectionHelper.setPrivateValue(ModContainer.class, container, Optional.<Consumer<IConfigEvent>>of(evt -> {
            evt = switchModConfigs(evt, newConfig, oldConfig);
            configHandler.accept(evt);
        }), "configHandler"));
    }

    private static IConfigEvent switchModConfigs(IConfigEvent evt, ModConfig newConfig, ModConfig oldConfig) {
        // just create a new ModConfigEvent when switching ModConfigs, the event is just a single value holder basically
        // we are unable to replace the config in the existing event instance as the field is final and there is no way for setting a final field in Java 12+ without ASM
        if (evt.getConfig() == newConfig) {
            if (evt instanceof ModConfigEvent.Loading) {
                evt = IConfigEvent.loading(oldConfig);
            } else if (evt instanceof ModConfigEvent.Reloading) {
                evt = IConfigEvent.reloading(oldConfig);
            } else {
                throw new RuntimeException("%s is an unknown %s sub-type".formatted(evt.getClass(), ModConfigEvent.class));
            }
        }
        return evt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IConfigSpec<T>> IConfigSpec<T> getSpec() {
        return (IConfigSpec<T>) this.spec;
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return this.configHandler;
    }

    public void fireEvent(final IConfigEvent configEvent) {
        this.container.dispatchConfigEvent(configEvent);
    }
}
