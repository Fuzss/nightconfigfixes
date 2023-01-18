package fuzs.nightconfigfixes.config;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableMap;
import fuzs.nightconfigfixes.NightConfigFixes;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class NightConfigFixesConfig {
    public static final NightConfigFixesConfig INSTANCE;
    private static final String CONFIG_FILE_NAME = NightConfigFixes.MOD_ID + ".toml";
    private static final Map<String, Object> CONFIG_VALUES = ImmutableMap.<String, Object>builder().put("forceGlobalServerConfigs", false).put("recreateConfigsWhenParsingFails", true).build();
    private static final ConfigSpec CONFIG_SPEC;

    static {
        CONFIG_SPEC = new ConfigSpec();
        for (Map.Entry<String, Object> entry : CONFIG_VALUES.entrySet()) {
            CONFIG_SPEC.define(entry.getKey(), entry.getValue());
        }
        INSTANCE = new NightConfigFixesConfig();
    }

    private CommentedFileConfig configData;

    private NightConfigFixesConfig() {
        this.loadFrom(FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME));
    }

    // copied from FML config
    private void loadFrom(final Path configFile) {
        this.configData = CommentedFileConfig.builder(configFile, TomlFormat.instance()).sync().onFileNotFound(FileNotFoundAction.copyData(Objects.requireNonNull(this.getClass().getResourceAsStream("/" + CONFIG_FILE_NAME)))).autosave().autoreload().writingMode(WritingMode.REPLACE).build();
        try {
            this.configData.load();
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to load %s config from %s".formatted(NightConfigFixes.MOD_NAME, configFile), e);
        }
        if (!CONFIG_SPEC.isCorrect(this.configData)) {
            NightConfigFixes.LOGGER.warn("Configuration file {} is not correct. Correcting", configFile);
            CONFIG_SPEC.correct(this.configData, (action, path, incorrectValue, correctedValue) -> NightConfigFixes.LOGGER.warn("Incorrect key {} was corrected from {} to {}", path, incorrectValue, correctedValue));
        }
        this.configData.save();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        if (!CONFIG_VALUES.containsKey(key)) {
            throw new IllegalArgumentException("%s is not a know config value key".formatted(key));
        }
        return this.configData.<T>getOptional(key).orElse((T) CONFIG_VALUES.get(key));
    }
}
