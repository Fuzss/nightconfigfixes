package fuzs.nightconfigfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

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

    public static void applySpec(ModConfig modConfig) {
        IConfigSpec<?> spec = modConfig.getSpec();
        if (!(spec instanceof ForgeConfigSpec forgeConfigSpec)) return;
        Field field = ObfuscationReflectionHelper.findField(ModConfig.class, "spec");
        // the field is final, so we have to use Unsafe to change it, not possible via reflection
        long fieldOffset = UNSAFE.objectFieldOffset(field);
        ConfigSpecWrapper configSpecWrapper = new ConfigSpecWrapper(forgeConfigSpec);
        UNSAFE.putObject(modConfig, fieldOffset, configSpecWrapper);
    }
}
