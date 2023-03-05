package fuzs.nightconfigfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ModConfigSpecUtil {
    private static final Unsafe UNSAFE;

    static {
        try {
            Constructor<?> constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            UNSAFE = (Unsafe) constructor.newInstance();
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void wrapSpec(ModConfig modConfig) {
        ForgeConfigSpec spec = modConfig.getSpec();
        ConfigSpecWrapper configSpecWrapper = new ConfigSpecWrapper(spec);
        setModConfigSpec(modConfig, configSpecWrapper);
    }

    public static void wrapConfigHandler(ModContainer container) {
        // replace configHandler field in ModContainer, so we can switch back to the original ForgeConfigSpec when any IConfigEvent is fired
        // mods might be holding on to the original ForgeConfigSpec instance and make a reference comparison
        // let's just hope mods don't change config data during this event which might trigger the file being corrected, after all this is the best we can do
        Optional<Consumer<ModConfig.ModConfigEvent>> optional = ObfuscationReflectionHelper.getPrivateValue(ModContainer.class, container, "configHandler");
        Objects.requireNonNull(optional, "config handler is null");
        optional.ifPresent(configHandler -> ObfuscationReflectionHelper.setPrivateValue(ModContainer.class, container, Optional.<Consumer<ModConfig.ModConfigEvent>>of(evt -> {
            ModConfig modConfig = evt.getConfig();
            ForgeConfigSpec spec = null;
            if (modConfig.getSpec() instanceof ConfigSpecWrapper) {
                spec = modConfig.getSpec();
                setModConfigSpec(modConfig, ((ConfigSpecWrapper) modConfig.getSpec()).getSpec());
            }
            configHandler.accept(evt);
            if (spec != null) {
                setModConfigSpec(modConfig, spec);
            }
        }), "configHandler"));
    }

    private static void setModConfigSpec(ModConfig modConfig, ForgeConfigSpec spec) {
        Field field = ObfuscationReflectionHelper.findField(ModConfig.class, "spec");
        // the field is final, so we have to use Unsafe to change it, not possible via reflection
        long fieldOffset = UNSAFE.objectFieldOffset(field);
        UNSAFE.putObject(modConfig, fieldOffset, spec);
    }
}
