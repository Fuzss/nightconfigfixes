package fuzs.nightconfigfixes.util;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import fuzs.nightconfigfixes.NightConfigFixes;
import fuzs.nightconfigfixes.config.NightConfigFixesConfig;

import java.nio.file.Files;
import java.util.function.BooleanSupplier;

public class ConfigLoadingUtil {

    public static void tryLoadConfigFile(FileConfig configData) {
        tryLoadConfigFile(configData, () -> NightConfigFixesConfig.INSTANCE.<Boolean>getValue("recreateConfigsWhenParsingFails"));
    }

    private static void tryLoadConfigFile(FileConfig configData, BooleanSupplier recreate) {
        try {
            configData.load();
        } catch (ParsingException e) {
            if (recreate.getAsBoolean()) {
                try {
                    Files.delete(configData.getNioPath());
                    configData.load();
                    NightConfigFixes.LOGGER.warn("Configuration file {} could not be parsed. Correcting", configData.getNioPath());
                    return;
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
            }
            throw e;
        }
    }
}
