package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;
import fuzs.nightconfigfixes.NightConfigFixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Specify target class as string to maybe help when night config is not present on Fabric, not sure if it really makes a different, after all Mixin is very forgiving when the target class does not exist.
 * <p>Also we are forced to use {@link Overwrite} instead of {@link org.spongepowered.asm.mixin.injection.Inject} as the latter is not supported in interfaces.
 */
@Mixin(ConfigParser.class)
interface ConfigParserMixin<C extends Config> {

    @Shadow(remap = false)
    ConfigFormat<C> getFormat();

    @Overwrite(remap = false)
    default C parse(Path file, FileNotFoundAction nefAction, Charset charset) {
        NightConfigFixes.LOGGER.info("Running custom mixin logic...");
        try {
            if (Files.notExists(file) && !nefAction.run(file, this.getFormat())) {
                return this.getFormat().createConfig();
            }
            try (InputStream input = Files.newInputStream(file)) {
                return this.parse(input, charset);
            } catch (ParsingException e) {
                // we come in here and catch the ParsingException, just try to recreate the config via the FileNotFoundAction
                // if we fail just throw the ParsingException like we aren't even here
                try {
                    Files.delete(file);
                    if (nefAction.run(file, this.getFormat())) {
                        C config;
                        try (InputStream input = Files.newInputStream(file)) {
                            config = this.parse(input, charset);
                        }
                        NightConfigFixes.LOGGER.warn("Not enough data available for config file {}", file.toAbsolutePath());
                        return config;
                    }
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
                throw e;
            }
        } catch (IOException e) {
            throw new WritingException("An I/O error occurred", e);
        }
    }

    @Shadow(remap = false)
    C parse(InputStream input, Charset charset);

    @Overwrite(remap = false)
    default void parse(Path file, Config destination, ParsingMode parsingMode, FileNotFoundAction nefAction, Charset charset) {
        NightConfigFixes.LOGGER.info("Running custom mixin logic...");
        try {
            if (Files.notExists(file) && !nefAction.run(file, this.getFormat())) {
                return;
            }
            try (InputStream input = Files.newInputStream(file)) {
                this.parse(input, destination, parsingMode, charset);
            } catch (ParsingException e) {
                // we come in here and catch the ParsingException, just try to recreate the config via the FileNotFoundAction
                // if we fail just throw the ParsingException like we aren't even here
                try {
                    Files.delete(file);
                    if (nefAction.run(file, this.getFormat())) {
                        try (InputStream input = Files.newInputStream(file)) {
                            this.parse(input, destination, parsingMode, charset);
                        }
                        NightConfigFixes.LOGGER.warn("Not enough data available for config file {}", file.toAbsolutePath());
                        return;
                    }
                } catch (Throwable t) {
                    e.addSuppressed(t);
                }
                throw e;
            }
        } catch (IOException e) {
            throw new WritingException("An I/O error occurred", e);
        }
    }

    @Shadow(remap = false)
    void parse(InputStream input, Config destination, ParsingMode parsingMode, Charset charset);
}
