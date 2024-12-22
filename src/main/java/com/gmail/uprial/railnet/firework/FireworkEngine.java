package com.gmail.uprial.railnet.firework;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Nuke;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class FireworkEngine {
    public final static int MAGIC_TYPE_EXPLOSIVE = 0;

    private final static Map<Integer, EntityType> MAGIC_TYPE_2_ENTITY_TYPE = ImmutableMap.<Integer,EntityType>builder()
            .put(1, EntityType.ARMADILLO)
            .put(2, EntityType.ALLAY)
            .put(3, EntityType.AXOLOTL)
            .put(4, EntityType.BAT)
            .put(5, EntityType.BEE)
            .put(6, EntityType.BLAZE)
            .put(7, EntityType.BOGGED)
            .put(8, EntityType.BREEZE)
            .put(9, EntityType.CAT)
            .put(10, EntityType.CAMEL)
            .put(11, EntityType.CAVE_SPIDER)
            .put(12, EntityType.CHICKEN)
            .put(13, EntityType.COD)
            .put(14, EntityType.COW)
            .put(15, EntityType.CREEPER)
            .put(16, EntityType.DOLPHIN)
            .put(17, EntityType.DONKEY)
            .put(18, EntityType.DROWNED)
            .put(19, EntityType.ELDER_GUARDIAN)
            .put(20, EntityType.ENDER_DRAGON)
            .put(21, EntityType.ENDERMAN)
            .put(22, EntityType.ENDERMITE)
            .put(23, EntityType.EVOKER)
            .put(24, EntityType.FOX)
            .put(25, EntityType.FROG)
            .put(26, EntityType.GHAST)
            .put(27, EntityType.GLOW_SQUID)
            .put(28, EntityType.GOAT)
            .put(29, EntityType.GUARDIAN)
            .put(30, EntityType.HOGLIN)
            .put(31, EntityType.HORSE)
            .put(32, EntityType.HUSK)
            .put(33, EntityType.IRON_GOLEM)
            .put(34, EntityType.LLAMA)
            .put(35, EntityType.MAGMA_CUBE)
            .put(36, EntityType.MOOSHROOM)
            .put(37, EntityType.MULE)
            .put(38, EntityType.OCELOT)
            .put(39, EntityType.PANDA)
            .put(40, EntityType.PARROT)
            .put(41, EntityType.PHANTOM)
            .put(42, EntityType.PIG)
            .put(43, EntityType.PIGLIN)
            .put(44, EntityType.PIGLIN_BRUTE)
            .put(45, EntityType.PILLAGER)
            .put(46, EntityType.POLAR_BEAR)
            .put(47, EntityType.PUFFERFISH)
            .put(48, EntityType.RABBIT)
            .put(49, EntityType.RAVAGER)
            .put(50, EntityType.SALMON)
            .put(51, EntityType.SHEEP)
            .put(52, EntityType.SHULKER)
            .put(53, EntityType.SILVERFISH)
            .put(54, EntityType.SKELETON)
            .put(55, EntityType.SKELETON_HORSE)
            .put(56, EntityType.SLIME)
            .put(57, EntityType.SNIFFER)
            .put(58, EntityType.SNOW_GOLEM)
            .put(59, EntityType.SPIDER)
            .put(60, EntityType.SQUID)
            .put(61, EntityType.STRAY)
            .put(62, EntityType.STRIDER)
            .put(63, EntityType.TADPOLE)
            .put(64, EntityType.TRADER_LLAMA)
            .put(65, EntityType.TROPICAL_FISH)
            .put(66, EntityType.TURTLE)
            .put(67, EntityType.VEX)
            .put(68, EntityType.VILLAGER)
            .put(69, EntityType.VINDICATOR)
            .put(70, EntityType.WANDERING_TRADER)
            .put(71, EntityType.WARDEN)
            .put(72, EntityType.WITCH)
            .put(73, EntityType.WITHER)
            .put(74, EntityType.WITHER_SKELETON)
            .put(75, EntityType.WOLF)
            .put(76, EntityType.ZOGLIN)
            .put(77, EntityType.ZOMBIE)
            .put(78, EntityType.ZOMBIE_HORSE)
            .put(79, EntityType.ZOMBIE_VILLAGER)
            .put(80, EntityType.ZOMBIFIED_PIGLIN)
            .build();

    private final static Map<EntityType, Integer> ENTITY_TYPE_2_MAGIC_TYPE = invert(MAGIC_TYPE_2_ENTITY_TYPE);

    private final RailNet plugin;
    private final CustomLogger customLogger;

    public FireworkEngine(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    public static void apply(
            final ItemStack itemStack,
            final FireworkEffect.Type type,
            final int power,
            final EntityType entityType,
            final int entityAmount) {

        apply(itemStack, type, power,
                new FireworkMagic(ENTITY_TYPE_2_MAGIC_TYPE.get(entityType), entityAmount),
                String.format("Spawns: %d x %s", entityAmount, entityType));
    }

    public static void apply(
            final ItemStack itemStack,
            final FireworkEffect.Type type,
            final int power,
            final int explosionPower) {

        apply(itemStack, type, power,
                new FireworkMagic(MAGIC_TYPE_EXPLOSIVE, explosionPower),
                String.format("Explosion power: %d", explosionPower));
    }

    private static void apply(
            final ItemStack itemStack,
            final FireworkEffect.Type type,
            final int power,
            final FireworkMagic magic,
            final String description) {

        final FireworkMeta fireworkMeta = (FireworkMeta) itemStack.getItemMeta();

        fireworkMeta.addEffect(FireworkEffect.builder()
                .with(type)
                .withFlicker()
                .withTrail()
                .withColor(FireworkMagicColor.encode(magic))
                .build());
        fireworkMeta.setPower(power);

        fireworkMeta.setLore(Collections.singletonList(description));

        itemStack.setItemMeta(fireworkMeta);

        itemStack.addUnsafeEnchantment(Enchantment.FLAME,  1);
    }

    public void onExplode(final Firework firework) {
        for(FireworkEffect effect : firework.getFireworkMeta().getEffects()) {
            if(effect.hasFlicker() && effect.hasTrail()) {
                for (Color color : effect.getColors()) {
                    final FireworkMagic magic = FireworkMagicColor.decode(color);
                    if (magic != null) {
                        trigger(firework, magic);
                    }
                }
            }
        }
    }

    private void trigger(final Firework firework, final FireworkMagic magic) {
        if(magic.getType() == MAGIC_TYPE_EXPLOSIVE) {
            final int explosionPower = magic.getAmount();

            if (explosionPower <= Nuke.MAX_ENGINE_POWER) {
                firework.getWorld().createExplosion(firework.getLocation(), explosionPower, true, true);
                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("Firework exploded at %s with power %d",
                            format(firework.getLocation()), explosionPower));
                }
            } else {
                Nuke.explode(plugin, firework.getLocation(), explosionPower, 1, () -> 2);
                customLogger.info(String.format("Firework exploded at %s with power %d",
                        format(firework.getLocation()), explosionPower));
            }
        } else if (MAGIC_TYPE_2_ENTITY_TYPE.containsKey(magic.getType())) {
            final EntityType entityType = MAGIC_TYPE_2_ENTITY_TYPE.get(magic.getType());
            final int entityAmount = magic.getAmount();

            for(int i = 0; i < entityAmount; i++) {
                firework.getWorld().spawnEntity(firework.getLocation(), entityType);
            }

            if (customLogger.isDebugMode()) {
                customLogger.debug(String.format("Firework exploded at %s with %d x %s",
                        format(firework.getLocation()), entityAmount, entityType));
            }
        } else {
            customLogger.warning(String.format("Firework at %s has unrecognized magic %d",
                    format(firework.getLocation()), magic.getType()));
        }
    }

    private static <K,V> Map<V,K> invert(final Map<K,V> map) {
        final Map<V,K> result = new HashMap<>();
        for(final Map.Entry<K,V> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }

        return result;
    }
}
