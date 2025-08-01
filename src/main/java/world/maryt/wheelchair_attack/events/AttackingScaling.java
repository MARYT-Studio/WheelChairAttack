package world.maryt.wheelchair_attack.events;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import world.maryt.wheelchair.Config;
import world.maryt.wheelchair_attack.WheelchairAttack;

import static world.maryt.wheelchair_attack.util.TagUtils.*;
import static world.maryt.wheelchair_attack.Config.attackMergeTicks;

import java.util.ArrayList;

public class AttackingScaling {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamaged(@NotNull LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        // Only apply for non-players
        if (target instanceof Player || source.getEntity() == null || !(source.getEntity() instanceof Player)) return;

        Player player = (Player)(source.getEntity());

        // Backup the damage
        float rawDamageAmount = event.getAmount();
        // If no affective damage, do nothing
        if (rawDamageAmount <= 0) return;

        // Update the hit counter
        CompoundTag tag = target.getPersistentData();
        int hitByPlayerCounter = getIntNonNull(tag, "hitByPlayerCounter", Config.mobShouldDieWithinThisAttacks);

        // Scaling mob's health and player's damage

        // Store the damage for comparing

        float maxDamage = getFloatNonNull(tag,"maxDamage", 0);
        float minDamage = getFloatNonNull(tag,"minDamage", Float.MAX_VALUE);

        if (rawDamageAmount > maxDamage) {
            tag.putFloat("maxDamage", rawDamageAmount);

            // Scale the mob's health proportional to the damage
            float scaledHealth = rawDamageAmount * Config.mobShouldDieWithinThisAttacks;
            if (scaledHealth > target.getMaxHealth()) scaleMobHealth(scaledHealth, target);
        }
        if (rawDamageAmount < minDamage) {
            tag.putFloat("minDamage", rawDamageAmount);
        }

        // Cancel the raw damage
        event.setAmount(0);
        target.setHealth(target.getMaxHealth() * ((float) hitByPlayerCounter / Config.mobShouldDieWithinThisAttacks));

        // Attack merge
        if (attackMergeTicks <= 0 || target.tickCount - target.getLastHurtByMobTimestamp() <= attackMergeTicks) {
            debugInfo(player, tag, target.tickCount, target.getLastHurtByMobTimestamp());
        } else {
            tag.putInt("hitByPlayerCounter", tag.getInt("hitByPlayerCounter") - 1);
            debugInfo(player, rawDamageAmount, tag);
        }

        if (tag.getInt("hitByPlayerCounter") == 0) {
            // Mob must be killed at this time
            event.setAmount(Float.MAX_VALUE);
            debugInfo(player);
        }
    }

    private static void scaleMobHealth(float scaledHealth, @NotNull LivingEntity target) {
        Multimap<Attribute, AttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
        map.put(Attributes.MAX_HEALTH, new AttributeModifier("generic.max_health", scaledHealth - target.getMaxHealth(), AttributeModifier.Operation.ADDITION));
        target.getAttributes().addTransientAttributeModifiers(map);
    }

    // Force kill
    private static void debugInfo(@NotNull Player player) {
        if (WheelchairAttack.DEBUG) player.sendSystemMessage(Component.nullToEmpty("Enemy has been forced to be killed."));
    }

    private static void debugInfo(@NotNull Player player, float rawDamageAmount, @NotNull CompoundTag tag) {
        if (WheelchairAttack.DEBUG) {
            player.sendSystemMessage(Component.nullToEmpty("Raw Damage: " + rawDamageAmount));
            player.sendSystemMessage(Component.nullToEmpty("Have hit this mob for: " + tag.getInt("hitByPlayerCounter") + "/" + Config.mobShouldDieWithinThisAttacks));
        }
    }

    private static void debugInfo(@NotNull Player player, @NotNull CompoundTag tag, int tickCount, int lastHurtByMobTimestamp) {
        // Debug info when merged
        if (WheelchairAttack.DEBUG) player.sendSystemMessage(Component.nullToEmpty(tickCount + "-" + lastHurtByMobTimestamp + "<=" + attackMergeTicks + ", Attack is merged. Have hit this mob for: " + tag.getInt("hitByPlayerCounter") + "/" + Config.mobShouldDieWithinThisAttacks));
    }
}
