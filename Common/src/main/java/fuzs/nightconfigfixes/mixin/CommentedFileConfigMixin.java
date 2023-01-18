package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.file.NoFormatFoundException;
import fuzs.nightconfigfixes.NightConfigFixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

@Mixin(CommentedFileConfig.class)
public interface CommentedFileConfigMixin {

    @Overwrite(remap = false)
    static CommentedFileConfigBuilder builder(Path file) {
        NightConfigFixes.LOGGER.info("Running custom mixin logic...");
        ConfigFormat format = FormatDetector.detect(file);
        if (format == null) {
            throw new NoFormatFoundException("No suitable format for " + file.getFileName());
        } else if (!format.supportsComments()) {
            throw new NoFormatFoundException(
                    "The available format doesn't support comments for " + file.getFileName());
        }
        return builder(file, format);
    }

    static CommentedFileConfigBuilder builder(Path file, ConfigFormat<? extends CommentedConfig> format) {
        throw new RuntimeException();
    }
}
