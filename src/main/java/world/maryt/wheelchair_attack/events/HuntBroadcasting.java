package world.maryt.wheelchair_attack.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import world.maryt.wheelchair.Config;

import static world.maryt.wheelchair_attack.util.TagUtils.getFloatNonNull;

// TODO: Server-wide stat
public class HuntBroadcasting {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!Config.shouldNotifyPlayers) return;
        if (!(event.getEntity() instanceof Player)) {
            LivingEntity prey = event.getEntity();
            if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof Player player) {
                huntBroadCast(player, prey);
            }
        }
    }

    private static void huntBroadCast(Player player, LivingEntity prey) {
        // Current prey's properties
        float currentPreyHealth = prey.getMaxHealth();
        float currentHunterDamage = getFloatNonNull(prey.getPersistentData(),"maxDamage", 0);

        // Player's own record
        float recordPreyHealth = getRecord(player, "maxPreyHealth", prey, currentPreyHealth).getFloat("value");
        float recordHunterDamage = getRecord(player, "maxHunterDamage", prey, currentHunterDamage).getFloat("value");

        // Update record
        boolean healthFlag = false;
        boolean damageFlag = false;

        if (currentPreyHealth > recordPreyHealth) {
            setRecord(player, "maxPreyHealth", prey, currentPreyHealth);
            healthFlag = true;
        }

        if (currentHunterDamage > recordHunterDamage) {
            setRecord(player, "maxHunterDamage", prey, currentHunterDamage);
            damageFlag = true;
        }

        // Broadcast
        if (healthFlag && damageFlag) {
            broadCast(player, prey.getDisplayName(), currentPreyHealth, boast(player, currentHunterDamage));
        }
    }

    private static void broadCast(Player player, Component preyName, float preyHealth, double hunterDamage) {
        MinecraftServer serverLevel = player.getServer();
        if (serverLevel == null) {
            player.sendSystemMessage(Component.translatable( "broadcast.for_single_player.text", fixDigit(preyHealth), preyName ,fixDigit(hunterDamage)));
        }
        else {
            PlayerList playerList = serverLevel.getPlayerList();
            if (playerList.getPlayerCount() > 1) {
                broadCast(player, playerList, preyName, preyHealth, hunterDamage);
            } else player.sendSystemMessage(Component.translatable( "broadcast.for_single_player.text", fixDigit(preyHealth), preyName ,fixDigit(hunterDamage)));
        }
    }

    private static void broadCast(Player hunter, PlayerList playerList, Component preyName, float preyHealth, double hunterDamage) {
        for (ServerPlayer player: playerList.getPlayers()) {
            player.sendSystemMessage(Component.translatable( "broadcast.for_multiplayer.text", hunter.getName(), fixDigit(preyHealth), preyName ,fixDigit(hunterDamage)));
        }
    }

    private static CompoundTag getRecord(Player player, String key, LivingEntity prey, float value) {
        CompoundTag playerTag = player.getPersistentData();
        if (!(playerTag.contains(key))) {
            setRecord(player, key, prey, value);
        }
        return playerTag.getCompound(key);
    }

    private static void setRecord(Player player, String key, LivingEntity prey, float value) {
        CompoundTag defaultTag = new CompoundTag();
        defaultTag.putString("entityId", getPreyId(prey));
        defaultTag.putFloat("value", value);
        player.getPersistentData().put(key, defaultTag);
    }

    private static String getPreyId(LivingEntity prey) {
        return prey.getEncodeId() == null ? prey.getType().getDescriptionId() : prey.getEncodeId();
    }

    // Boast the hunter damage.
    private static double boast(Player player, float rawHunterDamage) {
        return rawHunterDamage * (1 +
                Math.abs(
                        (player.getRandom().nextGaussian() + 0.1) * 0.4
                )
        );
    }

    private static double fixDigit(float raw) {
        return fixDigit(1.0D * raw);
    }

    private static double fixDigit(double raw) {
        return Math.round(10 * raw)/10.0;
    }
}
