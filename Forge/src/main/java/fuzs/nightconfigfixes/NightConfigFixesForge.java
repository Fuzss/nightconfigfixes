package fuzs.nightconfigfixes;

import fuzs.nightconfigfixes.config.NightConfigFixesConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(NightConfigFixes.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NightConfigFixesForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        NightConfigFixesConfig.INSTANCE.load();
    }
}
