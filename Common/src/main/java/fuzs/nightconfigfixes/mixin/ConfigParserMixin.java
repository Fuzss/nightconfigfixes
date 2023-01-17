package fuzs.nightconfigfixes.mixin;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(targets = "com.electronwill.nightconfig.core.io.ConfigParser")
interface ConfigParserMixin<C extends Config> {

    @Shadow
    ConfigFormat<C> getFormat();

    @Inject(method = "parse(Ljava/nio/file/Path;Lcom/electronwill/nightconfig/core/file/FileNotFoundAction;Ljava/nio/charset/Charset;)Lcom/electronwill/nightconfig/core/Config;", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.AFTER), cancellable = true, remap = false)
    default void parse(Path file, FileNotFoundAction nefAction, Charset charset, CallbackInfoReturnable<C> callback) {
        try {
            C config;
            try (InputStream input = Files.newInputStream(file)) {
                config = this.parse(input, charset);
            } catch (ParsingException e) {
                Files.delete(file);
                if (!nefAction.run(file, this.getFormat())) {
                    config = this.getFormat().createConfig();
                } else {
                    throw e;
                }
            }
            callback.setReturnValue(config);
        } catch (IOException e) {
            throw new WritingException("An I/O error occured", e);
        }
    }

    @Shadow
    C parse(InputStream input, Charset charset);

    @Inject(method = "parse(Ljava/nio/file/Path;Lcom/electronwill/nightconfig/core/Config;Lcom/electronwill/nightconfig/core/io/ParsingMode;Lcom/electronwill/nightconfig/core/file/FileNotFoundAction;Ljava/nio/charset/Charset;)V", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.AFTER), cancellable = true, remap = false)
    default void parse(Path file, Config destination, ParsingMode parsingMode, FileNotFoundAction nefAction, Charset charset, CallbackInfo callback) {
        try {
            InputStream input = null;
            try {
                input = Files.newInputStream(file);
                this.parse(input, destination, parsingMode, charset);
            } catch (ParsingException e) {
                Files.delete(file);
                if (input != null && !nefAction.run(file, this.getFormat())) {
                    this.parse(input, destination, parsingMode, charset);
                } else {
                    throw e;
                }
            } finally {
                if (input != null) input.close();
            }
            callback.cancel();
        } catch (IOException e) {
            throw new WritingException("An I/O error occured", e);
        }
    }

    @Shadow
    void parse(InputStream input, Config destination, ParsingMode parsingMode, Charset charset);
}
