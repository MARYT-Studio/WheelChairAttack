package world.maryt.wheelchair_attack;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = WheelchairAttack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue PLAYER_DAMAGE_CALC_METHOD = BUILDER.comment("0 (Default): Player Max Damage in a combat x mobShouldDieWithinThisAttacks. This makes the player have most beautiful stats.\n1: Player Min Damage in a combat x mobShouldDieWithinThisAttacks. This makes the player have ugliest stats.\n2: (Player Max plus Min Damage in a combat)/2  x mobShouldDieWithinThisAttacks. This makes the player have fairest stats.").defineInRange("playerDamageCalcMethod", 0, 0, 2);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int playerDamageCalcMethod;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        playerDamageCalcMethod = PLAYER_DAMAGE_CALC_METHOD.get();
    }
}
