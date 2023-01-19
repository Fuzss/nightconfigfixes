/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.logging.LogUtils;
import fuzs.nightconfigfixes.NightConfigFixes;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Copied from Forge, only replaced {@link FileConfig#load()} calls with wrapped calls to better handle a possible {@link ParsingException}
 * <p>The only reason for this to extend the original Forge class is to be able to return this instance in {@link ModConfig#getHandler()}
 * <p>Huge props to the Corail Woodcutter mod where this whole idea comes from, the mod can be found here: <a href="https://www.curseforge.com/minecraft/mc-mods/corail-woodcutter">Corail Woodcutter</a>
 */
public class CheckedConfigFileTypeHandler extends ConfigFileTypeHandler {
    public static final ConfigFileTypeHandler TOML = new CheckedConfigFileTypeHandler();
    static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path DEFAULT_CONFIG_PATH = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());

    // Night Config Fixes: custom method from Forge Config API Port to better handle config loading when a ParsingException occurs
    private static void tryLoadConfigFile(FileConfig configData) {
        try {
            configData.load();
        } catch (ParsingException e) {
            try {
                Files.delete(configData.getNioPath());
                configData.load();
                NightConfigFixes.LOGGER.warn("Configuration file {} could not be parsed. Correcting", configData.getNioPath());
                return;
            } catch (Throwable t) {
                e.addSuppressed(t);
            }
            throw e;
        }
    }

    @Override
    public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
        return (c) -> {
            // Night Config Fixes: implement an option to make server configs global
            final Path configPath;
            if (NightConfigFixesConfig.INSTANCE.<Boolean>getValue("forceGlobalServerConfigs")) {
                configPath = FMLPaths.CONFIGDIR.get().resolve(c.getFileName());
            } else {
                configPath = configBasePath.resolve(c.getFileName());
            }
            final CommentedFileConfig configData = CommentedFileConfig.builder(configPath).sync().preserveInsertionOrder().autosave().onFileNotFound((newfile, configFormat) -> this.setupConfigFile(c, newfile, configFormat)).writingMode(WritingMode.REPLACE).build();
            LOGGER.debug(CONFIG, "Built TOML config for {}", configPath);
            try {
                // Night Config Fixes: wrap config loading to better handle com.electronwill.nightconfig.core.io.ParsingException: Not enough data available
                tryLoadConfigFile(configData);
            } catch (ParsingException ex) {
                throw new ConfigLoadingException(c, ex);
            }
            LOGGER.debug(CONFIG, "Loaded TOML config file {}", configPath);
            try {
                FileWatcher.defaultInstance().addWatch(configPath, new ConfigWatcher(c, configData, Thread.currentThread().getContextClassLoader()));
                LOGGER.debug(CONFIG, "Watching TOML config file {} for changes", configPath);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't watch config file", e);
            }
            return configData;
        };
    }

    @Override
    public void unload(Path configBasePath, ModConfig config) {
        // Night Config Fixes: implement an option to make server configs global
        final Path configPath;
        if (NightConfigFixesConfig.INSTANCE.<Boolean>getValue("forceGlobalServerConfigs")) {
            configPath = FMLPaths.CONFIGDIR.get().resolve(config.getFileName());
        } else {
            configPath = configBasePath.resolve(config.getFileName());
        }
        try {
            FileWatcher.defaultInstance().removeWatch(configBasePath.resolve(config.getFileName()));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to remove config {} from tracker!", configPath, e);
        }
    }

    private boolean setupConfigFile(final ModConfig modConfig, final Path file, final ConfigFormat<?> conf) throws IOException {
        Files.createDirectories(file.getParent());
        Path p = DEFAULT_CONFIG_PATH.resolve(modConfig.getFileName());
        if (Files.exists(p)) {
            LOGGER.info(CONFIG, "Loading default config file from path {}", p);
            Files.copy(p, file);
        } else {
            Files.createFile(file);
            conf.initEmptyFile(file);
        }
        return true;
    }

    private static class ConfigWatcher implements Runnable {
        private final ModConfig modConfig;
        private final CommentedFileConfig commentedFileConfig;
        private final ClassLoader realClassLoader;
        // Night Config Fixes: store found mod container, we need it for dispatching the config event
        private final ModContainer modContainer;

        ConfigWatcher(final ModConfig modConfig, final CommentedFileConfig commentedFileConfig, final ClassLoader classLoader) {
            this.modConfig = modConfig;
            this.commentedFileConfig = commentedFileConfig;
            this.realClassLoader = classLoader;
            // Night Config Fixes: store found mod container, we need it for dispatching the config event
            this.modContainer = ModList.get().getModContainerById(modConfig.getModId()).orElseThrow();
        }

        @Override
        public void run() {
            // Force the regular classloader onto the special thread
            Thread.currentThread().setContextClassLoader(this.realClassLoader);
            if (!this.modConfig.getSpec().isCorrecting()) {
                try {
                    // Night Config Fixes: wrap config loading to better handle com.electronwill.nightconfig.core.io.ParsingException: Not enough data available
                    tryLoadConfigFile(this.commentedFileConfig);
                    if (!this.modConfig.getSpec().isCorrect(this.commentedFileConfig)) {
                        LOGGER.warn(CONFIG, "Configuration file {} is not correct. Correcting", this.commentedFileConfig.getFile().getAbsolutePath());
                        ConfigFileTypeHandler.backUpConfig(this.commentedFileConfig);
                        this.modConfig.getSpec().correct(this.commentedFileConfig);
                        this.commentedFileConfig.save();
                    }
                } catch (ParsingException ex) {
                    throw new ConfigLoadingException(this.modConfig, ex);
                }
                LOGGER.debug(CONFIG, "Config file {} changed, sending notifies", this.modConfig.getFileName());
                this.modConfig.getSpec().afterReload();
                // Night Config Fixes: we don't have access to the package-private method on ModConfig, so just copy the implementation here, no need to get all fancy with reflection/method handle
                this.modContainer.dispatchConfigEvent(IConfigEvent.reloading(this.modConfig));
            }
        }
    }

    private static class ConfigLoadingException extends RuntimeException {

        public ConfigLoadingException(ModConfig config, Exception cause) {
            super("Failed loading config file " + config.getFileName() + " of type " + config.getType() + " for modid " + config.getModId(), cause);
        }
    }
}
