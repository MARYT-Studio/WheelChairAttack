package world.maryt.wheelchair_attack;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import world.maryt.wheelchair_attack.events.AttackingScaling;

@Mod(WheelchairAttack.MOD_ID)
public class WheelchairAttack {
    public static final String MOD_ID = "wheelchair_attack";
    private static final Logger LOGGER = LogUtils.getLogger();
    @SuppressWarnings("removal")
    public WheelchairAttack() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Event Handlers
        MinecraftForge.EVENT_BUS.register(AttackingScaling.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Cheer up! We believe you're The Only Champ forever!");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Let's rush! Grinding all those weak enemies down!");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Let's rush! Grinding all those weak enemies down! We believe you're The Only Champ forever!");
        }
    }
}
