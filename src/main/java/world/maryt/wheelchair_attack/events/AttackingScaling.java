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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import world.maryt.wheelchair.Config;

import java.util.ArrayList;

public class AttackingScaling {
    @SubscribeEvent
    public static void onLivingDamaged(LivingDamageEvent event) {
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
        if (!tag.contains("hitByPlayerCounter")) tag.putInt("hitByPlayerCounter", Config.mobShouldDieWithinThisAttacks - 1);
        else {
            int hitByPlayerCounter = tag.getInt("hitByPlayerCounter") - 1;
            if (hitByPlayerCounter == 0) {
                // Mob must be killed at this time
                event.setAmount(Float.MAX_VALUE);
            } else {
                // Scaling mob's health and player's damage

                // Store the damage for comparing
                if (!tag.contains("maxDamage")) tag.putFloat("maxDamage", rawDamageAmount);
                if (!tag.contains("minDamage")) tag.putFloat("minDamage", rawDamageAmount);
                float maxDamage = tag.getFloat("maxDamage");
                float minDamage = tag.getFloat("minDamage");

                if (rawDamageAmount > maxDamage) {
                    tag.putFloat("maxDamage", rawDamageAmount);

                    // Scale the mob's health proportional to the damage
                    float scaledHealth = rawDamageAmount * Config.mobShouldDieWithinThisAttacks;
                    if (scaledHealth > target.getMaxHealth()) {
                        Multimap<Attribute, AttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
                        map.put(Attributes.MAX_HEALTH, new AttributeModifier("generic.max_health", scaledHealth - target.getMaxHealth(), AttributeModifier.Operation.ADDITION));
                        target.getAttributes().addTransientAttributeModifiers(map);
                    }
                }
                if (rawDamageAmount < minDamage) {
                    tag.putFloat("minDamage", rawDamageAmount);
                }

                // Cancel the raw damage
                event.setAmount(0);
                target.setHealth(target.getMaxHealth() * ((float) hitByPlayerCounter / Config.mobShouldDieWithinThisAttacks));

                // Debug info
                player.sendSystemMessage(Component.nullToEmpty("Raw Damage: " + rawDamageAmount));
            }
        }

    }
}
