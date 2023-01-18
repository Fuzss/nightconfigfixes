package fuzs.nightconfigfixes.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class WrappedModConfig extends ModConfig {
    private final ModContainer container;
    private final ConfigFileTypeHandler configHandler;
    @Nullable
    private final CommentedConfig initialConfigData;

    private WrappedModConfig(ModConfig other) {
        this(other, ModList.get().getModContainerById(other.getModId()).orElseThrow());
    }

    private WrappedModConfig(ModConfig other, ModContainer container) {
        super(other.getType(), other.getSpec(), container, other.getFileName());
        this.container = container;
        this.configHandler = SafeConfigFileTypeHandler.TOML;
        this.initialConfigData = other.getConfigData();
    }

    public static void copy(ModConfig other) {
        new WrappedModConfig(other);
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return this.configHandler;
    }

    @Override
    public CommentedConfig getConfigData() {
        // do this weird stuff in case the mod config we copied already has data set for some reason
        // (it really shouldn't as data is only set when configs are loaded and copying is done before configs load, but mods do so much weird stuff with the config system, so be careful,
        // maybe those mods that manually load their configs early to allow using them during registration events...)
        // so if there is something in super #setConfigData has been called on us and the initial config data is outdated
        // otherwise if null is returned, return initial data, it's likely null, but might just not be
        CommentedConfig configData = super.getConfigData();
        return configData != null ? configData : this.initialConfigData;
    }

    @Override
    public void save() {
        ((CommentedFileConfig) this.getConfigData()).save();
    }

    @Override
    public Path getFullPath() {
        return ((CommentedFileConfig) this.getConfigData()).getNioPath();
    }

    public void fireEvent(final IConfigEvent configEvent) {
        this.container.dispatchConfigEvent(configEvent);
    }
}
