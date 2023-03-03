package fuzs.nightconfigfixes;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.List;

@Mod(NightConfigFixes.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NightConfigFixesForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("This is my value").define("important", false).next()
                .comment("This is my other value").defineInRange("number", 10, 1, 100).next()
                .push(List.of("section", "number", "three"))
                .comment("This is the third value").defineInRange("another", 7, 1, 99).next()
                .pop(3);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
    }
}
