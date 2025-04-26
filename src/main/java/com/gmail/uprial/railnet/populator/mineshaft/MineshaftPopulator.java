package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.RailNetCron;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.common.RandomUtils;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.CLT;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.ItemConfig;
import com.gmail.uprial.railnet.populator.PopulationHistory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.Utils.seconds2ticks;

public class MineshaftPopulator implements ChunkPopulator {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    public MineshaftPopulator(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @Override
    public boolean populate(final Chunk readonlyChunk, final PopulationHistory history) {
        /*
            Fix population in structures with post-generation,
            known examples: Desert Pyramid, Outpost
            - chests there seems to be populated somehow after their chunk load.
         */
        final UUID readonlyWorldUID = readonlyChunk.getWorld().getUID();
        final int chunkX = readonlyChunk.getX();
        final int chunkZ = readonlyChunk.getZ();

        RailNetCron.defer(() -> populateDeferred(readonlyWorldUID, chunkX, chunkZ));

        return true;
    }

    @Override
    public String getName() {
        return "Mineshaft";
    }

    private void populateDeferred(final UUID worldUID, final int chunkX, final int chunkZ) {
        final World world = plugin.getServer().getWorld(worldUID);
        if(world == null) {
            customLogger.warning(String.format("World %s not found", worldUID));
            return;
        }

        final Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        final int minY = chunk.getWorld().getMinHeight();
        final int maxY = chunk.getWorld().getMaxHeight();
        // Takes 3-10ms per chunk
        for(int y = minY; y < maxY; y++) {
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    final Block block = chunk.getBlock(x, y, z);
                    maybePopulateBlock(block);
                    maybeMutateBlock(block);
                }
            }
        }

        // Ideated from InventoryHolder & Entity
        for(final Entity entity : chunk.getEntities()) {
            if(entity instanceof StorageMinecart) {
                populateInventoryHolder((StorageMinecart)entity);
            /*
                Removed ideas:
                    ChestBoat and HopperMinecart are never generated naturally.
            } else if (entity instanceof ChestBoat){
                populateInventoryHolder((ChestBoat) entity);
            } else if (entity instanceof HopperMinecart){
                populateInventoryHolder((HopperMinecart) entity);
             */
            }
        }
    }

    private final Map<Material, Consumer<Block>> blockPopulators = ImmutableMap.<Material, Consumer<Block>>builder()
            // Ideated from BlockInventoryHolder
            .put(Material.CHEST, this::populateContainer)
            .put(Material.TRAPPED_CHEST, this::populateContainer)
            .put(Material.BARREL, this::populateContainer)
            // Ideated from Furnace
            .put(Material.FURNACE, this::populateFurnace)
            .put(Material.BLAST_FURNACE, this::populateFurnace)
            // No Smoker, because the loot table is ore-based
            //.put(Material.SMOKER, this::populateFurnace)
            .put(Material.BREWING_STAND, this::populateEndShip)
            .build();

    private void maybePopulateBlock(final Block block) {
        final Consumer<Block> blockPopulator = blockPopulators.get(block.getType());
        if(blockPopulator != null) {
            blockPopulator.accept(block);
        }
    }

    private static class Mutator {
        private final Double probability;
        private final Material material;

        Mutator(final Double probability, final Material material) {
            this.probability = probability;
            this.material = material;
        }

        public Double getProbability() {
            return probability;
        }

        public Material getMaterial() {
            return material;
        }
    }

    private static class InfestMutator extends Mutator {
        private final static double INFEST_PROBABILITY = 0.01D;

        InfestMutator(final Material material) {
            super(INFEST_PROBABILITY, material);
        }
    }

    private final Map<Material, Mutator> blockMutators = ImmutableMap.<Material, Mutator>builder()
            .put(Material.CHISELED_STONE_BRICKS, new InfestMutator(Material.INFESTED_CHISELED_STONE_BRICKS))
            .put(Material.COBBLESTONE, new InfestMutator(Material.INFESTED_COBBLESTONE))
            .put(Material.CRACKED_STONE_BRICKS, new InfestMutator(Material.INFESTED_CRACKED_STONE_BRICKS))
            .put(Material.DEEPSLATE, new InfestMutator(Material.INFESTED_DEEPSLATE))
            .put(Material.MOSSY_STONE_BRICKS, new InfestMutator(Material.INFESTED_MOSSY_STONE_BRICKS))
            .put(Material.STONE, new InfestMutator(Material.INFESTED_STONE))
            .put(Material.STONE_BRICKS, new InfestMutator(Material.INFESTED_STONE_BRICKS))
            .build();

    private void maybeMutateBlock(final Block block) {
        final Mutator mutator = blockMutators.get(block.getType());
        if((mutator != null) && (Probability.PASS(mutator.getProbability(), getWorldDensity(block.getWorld().getName())))){
            block.setType(mutator.getMaterial(), false);
            // Commented because too frequent
            /*if (customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s mutated to %s", format(block), mutator.getMaterial()));
            }*/
        }
    }

    // Increase density in some worlds
    private final static Map<String,Integer> WORLD_DENSITIES = ImmutableMap.<String,Integer>builder()
            .put(WorldName.NETHER, 1)
            .put(WorldName.END, 2)
            .build();

    private static int getWorldDensity(final String worldName) {
        return WORLD_DENSITIES.getOrDefault(worldName, 0);
    }

    /*
        Increase density above some blocks.

        Specifically, structure research:
        $ grep "world:<X-prefix>.*:<Z-prefix>.*populated" logs/latest.log
            | cut -d' ' -f12 | cut -d'[' -f1 | sort | uniq

        gives...
     */

    // inherits 0 from WORLD_DENSITIES
    private final static int MANSION_DENSITY = 4 - getWorldDensity(WorldName.WORLD);
    // inherits 1 from WORLD_DENSITIES
    private final static int BASTION_DENSITY = 3 - getWorldDensity(WorldName.NETHER);
    // inherits 0 from WORLD_DENSITIES
    private final static int PYRAMID_DENSITY = 2 - getWorldDensity(WorldName.WORLD);
    // inherits 0 from WORLD_DENSITIES
    private final static int WHIRLPOOL_DENSITY = 1 - getWorldDensity(WorldName.WORLD);

    private final static Map<Material,Integer> MATERIAL_DENSITIES = ImmutableMap.<Material,Integer>builder()
            // Woodland mansion
            .put(Material.DARK_OAK_PLANKS, MANSION_DENSITY)
            .put(Material.DARK_OAK_SLAB, MANSION_DENSITY)
            .put(Material.DARK_OAK_STAIRS, MANSION_DENSITY)

            // Bastion
            .put(Material.BLACKSTONE, BASTION_DENSITY)
            .put(Material.GILDED_BLACKSTONE, BASTION_DENSITY)
            .put(Material.POLISHED_BLACKSTONE_BRICKS, BASTION_DENSITY)
            .put(Material.POLISHED_BLACKSTONE_SLAB, BASTION_DENSITY)

            // Pyramid
            /*
                WARNING: WON'T FIX

                Some trapped chests in Pyramids are generated with delays,
                so they aren't timely populated.

                A known way to fix it is to use PlayerInteractEvent,
                then check and replace blocks under these trapped chests,
                making population idempotent.

                However, these trapped chests have TNT below,
                so the typical behaviour of players is to remove blocks under these trapped chests,
                preventing the solution.

                Considering this a rare case in the probability-based population,
                I decided it's too expensive to fix this bug.
             */
            .put(Material.BLUE_TERRACOTTA, PYRAMID_DENSITY)

            // Whirlpool
            .put(Material.MAGMA_BLOCK, WHIRLPOOL_DENSITY)

            .build();

    private static int getDensity(final Block basement) {
        return getWorldDensity(WorldName.normalize(basement.getWorld().getName()))
                + MATERIAL_DENSITIES.getOrDefault(basement.getType(), 0);
    }

    /*
        According to https://minecraft.wiki/w/Food,
        food with good saturation, which can't be found in chests.
     */
    //private final Material chestIdempotencyMarker = Material.COOKED_MUTTON;

    private final ItemConfig netheriteClothConfig =  new ItemConfig()
            // Survival maximum level is 4
            .ench(Enchantment.PROTECTION, 0, 4)
            .ench(Enchantment.VANISHING_CURSE)
            .trim(TrimMaterial.NETHERITE, TrimPattern.RIB);

    private final ItemConfig goldenClothConfig =  new ItemConfig()
            // Survival maximum level is 4, here it's 5
            .ench(Enchantment.PROTECTION, 3, 5)
            .ench(Enchantment.THORNS, 0, 3)
            .trim(TrimMaterial.GOLD, TrimPattern.RIB);

    private final ItemConfig netheriteToolConfig =  new ItemConfig()
            // Survival maximum level is 5
            .ench(Enchantment.EFFICIENCY, 0, 5)
            .ench(Enchantment.VANISHING_CURSE);

    private final ItemConfig goldenToolConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.EFFICIENCY, 5, 10)
            .ench(Enchantment.FORTUNE, 0, 3);

    private final ItemConfig netheriteSwordConfig =  new ItemConfig()
            // Survival maximum level is 5
            .ench(Enchantment.SHARPNESS, 0, 5)
            .ench(Enchantment.VANISHING_CURSE);

    private final ItemConfig goldenSwordConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.SHARPNESS, 5, 10)
            .ench(Enchantment.LOOTING, 0, 3)
            .ench(Enchantment.KNOCKBACK, 0, 2)
            .ench(Enchantment.FIRE_ASPECT, 0, 2)
            .ench(Enchantment.SWEEPING_EDGE, 0, 3);

    private final Set<Integer> potionDurationOptions = ImmutableSet.<Integer>builder()
            .add(seconds2ticks(60 * 15))
            .add(seconds2ticks(3_600))
            .add(seconds2ticks(3_600 * 4))
            .add(seconds2ticks(3_600 * 24))
            // The PotionEffect.INFINITE_DURATION option is discharged as too powerful
            .add(seconds2ticks(3_600 * 24 * 7))
            .build();

    private final Set<Integer> arrowDurationOptions = ImmutableSet.<Integer>builder()
            .add(seconds2ticks(60))
            .add(seconds2ticks(60 * 5))
            .add(seconds2ticks(60 * 15))
            .add(seconds2ticks(3_600))
            .add(seconds2ticks(3_600 * 4))
            .build();

    /*
        Effect type options: effect -> amplifier.
        Please check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html
        to ensure each option is included or excluded by a clear reason.
     */
    private final Map<PotionEffectType,Integer> potionEffects = ImmutableMap.<PotionEffectType, Integer>builder()
            .put(PotionEffectType.ABSORPTION, 4)
            // BAD_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.BLINDNESS, 0) // negative
            // CONDUIT_POWER: duplicates WATER_BREATHING
            .put(PotionEffectType.DARKNESS, 0) // negative
            // DOLPHINS_GRACE: doesn't work
            .put(PotionEffectType.FIRE_RESISTANCE, 0)
            .put(PotionEffectType.GLOWING, 0)
            .put(PotionEffectType.HASTE, 4)
            .put(PotionEffectType.HEALTH_BOOST, 4)
            .put(PotionEffectType.HERO_OF_THE_VILLAGE, 4)
            .put(PotionEffectType.HUNGER, 2) // negative
            // INFESTED: doesn't work
            // INSTANT_DAMAGE: has no lasting effect
            // INSTANT_HEALTH: has no lasting effect
            .put(PotionEffectType.INVISIBILITY, 0)
            .put(PotionEffectType.JUMP_BOOST, 4)
            .put(PotionEffectType.LEVITATION, 2) // negative
            .put(PotionEffectType.LUCK, 4)
            .put(PotionEffectType.MINING_FATIGUE, 2) // negative
            .put(PotionEffectType.NAUSEA, 0) // negative
            .put(PotionEffectType.NIGHT_VISION, 0)
            // OOZING: has no lasting effect
            .put(PotionEffectType.POISON, 2) // negative
            // RAID_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.REGENERATION, 0)
            .put(PotionEffectType.RESISTANCE, 2)
            .put(PotionEffectType.SATURATION, 0)
            .put(PotionEffectType.SLOW_FALLING, 4)
            .put(PotionEffectType.SLOWNESS, 2) // negative
            .put(PotionEffectType.SPEED, 4)
            .put(PotionEffectType.STRENGTH, 4)
            // TRIAL_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.UNLUCK, 2) // negative
            .put(PotionEffectType.WATER_BREATHING, 0)
            .put(PotionEffectType.WEAKNESS, 2) // negative
            // WEAVING: has no lasting effect
            // WIND_CHARGED: has no lasting effect
            .put(PotionEffectType.WITHER, 2) // negative
            .build();

    private final ItemConfig potionConfig = new ItemConfig()
            .effects(potionDurationOptions, potionEffects);
    private final ItemConfig arrowConfig = new ItemConfig()
            .effects(arrowDurationOptions, potionEffects);

    /*
        Ideated from:
            https://minecraft.wiki/w/Rarity
            https://minecraft.wiki/w/Spawn_Egg

        Removed ideas:
            Ender pearls motivate player to fight endermans.
            .put(Material.ENDER_PEARL, ...)

            End crystals require a block of obsidian beneath,
            which removes all potential fun of placing them.
            .put(Material.END_CRYSTAL, ...)

            Bedrock has no real usage, but may bring potential damage to the world.
            .put(Material.BEDROCK, ...)

            Golden carrots and apples are funny for 1st time,
            but they are neither food with good saturation for everyday life
            nor provide enough regeneration in a fight.
            .put(Material.GOLDEN_APPLE, ...)
            .put(Material.GOLDEN_CARROT, ...)

            Wither and Ender Dragon may destroy the world,
            which is too much for a random discovery
            .put(Material.WITHER_SPAWN_EGG, ...onlyInWorld(WorldName.NETHER))
            .put(Material.ENDER_DRAGON_SPAWN_EGG, ...onlyInWorld(WorldName.END))

            Some heads are good late-game achievements:
            .put(Material.DRAGON_HEAD, ...)
            .put(Material.WITHER_SKELETON_SKULL, ...)

        Test
            version 1.21.3
            seed -1565193744182814265 (Belongings 2025-01-12)
            TerraformGenerator-19.1.0
            WorldBorder 4050 x 4050

            $ grep oceanic- plugins/TerraformGenerator/config.yml
            oceanic-frequency: 0.11
            oceanic-threshold: 8.0
            deep-oceanic-threshold: 27.0

            $ grep "DEBUG.* POTION " logs/latest.log | cut -d' ' -f12 | awk '{s+=$1} END {print s}'
            86

            Got pretty similar numbers also on
            seed 120637665933994616 (0.05 island, 2 outpost, 2.5 mansion, 2.5 ancient)
     */
    private final Map<Material, CLT> chestLootTable = ImmutableMap.<Material, CLT>builder()
            //.put(chestIdempotencyMarker, new CLT(MAX_PERCENT))

            /*
                8% / ? - boring resources.
                Obtaining these resources isn't worth its time,
                but as a gift it's a lot of fun.

                ? - can't check due to an overlap with Dungeon.
             */
            .put(Material.TNT, new CLT(4.00D, 2))
            .put(Material.OBSIDIAN, new CLT(4.00D, 2))

            // 3% / 86 + 34 + 39 + 1675 - potions: 1.0 + 0.5 + 0.5 + 1.0
            .put(Material.POTION, new CLT(1.00D, potionConfig))
            .put(Material.SPLASH_POTION, new CLT(0.50D, potionConfig))
            .put(Material.LINGERING_POTION, new CLT(0.50D, potionConfig))
            .put(Material.TIPPED_ARROW, new CLT(1.00D, arrowConfig, CLT.MAX_POWER))

            /*
                2% / 461 - bazookas
                Please, keep consistent with FireworkCraftBook
             */
            .put(Material.FIREWORK_ROCKET, new CLT(2.00D, 2)
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BURST, 3, 5))
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BALL, 7, 12))
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BALL_LARGE, 10, 20))
            )

            // 4% / 172-179 - bonuses
            .put(Material.ENCHANTED_GOLDEN_APPLE, new CLT(2.00D))
            .put(Material.TOTEM_OF_UNDYING, new CLT(2.00D))

            // 6% / 138-154-154-124 - golden cloths
            .put(Material.GOLDEN_HELMET, new CLT(1.50D, goldenClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.GOLDEN_CHESTPLATE, new CLT(1.50D, goldenClothConfig))
            .put(Material.GOLDEN_LEGGINGS, new CLT(1.50D, goldenClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.GOLDEN_BOOTS, new CLT(1.50D, goldenClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            // 1.5% / 50-82 - golden tools
            .put(Material.GOLDEN_PICKAXE, new CLT(0.75D, goldenToolConfig))
            .put(Material.GOLDEN_SWORD, new CLT(0.75D, goldenSwordConfig))

            // Conclusion: 1% ~= 100 per 4050 x 4050 map in the overworld.

            // 1%
            .put(Material.OMINOUS_BOTTLE, new CLT(1.00D, new ItemConfig().amplify(4)))

            // 2% - netherite cloths
            .put(Material.NETHERITE_HELMET, new CLT(0.50D, netheriteClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.NETHERITE_CHESTPLATE, new CLT(0.50D, netheriteClothConfig))
            .put(Material.NETHERITE_LEGGINGS, new CLT(0.50D, netheriteClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.NETHERITE_BOOTS, new CLT(0.50D, netheriteClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            // 0.5% - netherite tools
            .put(Material.NETHERITE_PICKAXE, new CLT(0.25D, netheriteToolConfig))
            .put(Material.NETHERITE_SWORD, new CLT(0.25D, netheriteSwordConfig))

            // 4.4% - hostile mobs from https://minecraft.fandom.com/wiki/Mob
            .put(Material.BOGGED_SPAWN_EGG, new CLT(0.20D))
            .put(Material.BREEZE_SPAWN_EGG, new CLT(0.20D))
            .put(Material.CREAKING_SPAWN_EGG, new CLT(0.20D))
            .put(Material.DROWNED_SPAWN_EGG, new CLT(0.20D))
            .put(Material.ELDER_GUARDIAN_SPAWN_EGG, new CLT(0.20D))
            .put(Material.ENDERMAN_SPAWN_EGG, new CLT(0.20D))
            .put(Material.ENDERMITE_SPAWN_EGG, new CLT(0.20D))
            .put(Material.GUARDIAN_SPAWN_EGG, new CLT(0.20D))
            .put(Material.HOGLIN_SPAWN_EGG, new CLT(0.20D).onlyInWorld(WorldName.NETHER))
            .put(Material.HUSK_SPAWN_EGG, new CLT(0.20D))
            .put(Material.MOOSHROOM_SPAWN_EGG, new CLT(0.20D))
            .put(Material.PIGLIN_BRUTE_SPAWN_EGG, new CLT(0.20D).onlyInWorld(WorldName.NETHER))
            .put(Material.PIGLIN_SPAWN_EGG, new CLT(0.20D).onlyInWorld(WorldName.NETHER))
            .put(Material.PILLAGER_SPAWN_EGG, new CLT(0.20D))
            .put(Material.SILVERFISH_SPAWN_EGG, new CLT(0.20D))
            .put(Material.SKELETON_SPAWN_EGG, new CLT(0.20D))
            .put(Material.SPIDER_SPAWN_EGG, new CLT(0.20D))
            .put(Material.STRAY_SPAWN_EGG, new CLT(0.20D))
            .put(Material.VEX_SPAWN_EGG, new CLT(0.20D))
            .put(Material.VINDICATOR_SPAWN_EGG, new CLT(0.20D))
            .put(Material.ZOGLIN_SPAWN_EGG, new CLT(0.20D))
            .put(Material.ZOMBIE_SPAWN_EGG, new CLT(0.20D))

            // 1%
            .put(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, new CLT(0.50D))
            .put(Material.SPAWNER, new CLT(0.50D))

            // 1.4% - good loot mobs
            .put(Material.BLAZE_SPAWN_EGG, new CLT(0.20D).onlyInWorld(WorldName.NETHER))
            .put(Material.CREEPER_SPAWN_EGG, new CLT(0.20D))
            .put(Material.MAGMA_CUBE_SPAWN_EGG, new CLT(0.20D).onlyInWorld(WorldName.NETHER))
            .put(Material.PHANTOM_SPAWN_EGG, new CLT(0.20D))
            .put(Material.RAVAGER_SPAWN_EGG, new CLT(0.20D))
            .put(Material.SLIME_SPAWN_EGG, new CLT(0.20D))
            .put(Material.WITCH_SPAWN_EGG, new CLT(0.20D))

            // 0.75% - amazing loot mobs
            .put(Material.EVOKER_SPAWN_EGG, new CLT(0.15D))
            .put(Material.GHAST_SPAWN_EGG, new CLT(0.15D).onlyInWorld(WorldName.NETHER))
            .put(Material.SHULKER_SPAWN_EGG, new CLT(0.15D).onlyInWorld(WorldName.END))
            .put(Material.WITHER_SKELETON_SPAWN_EGG, new CLT(0.15D).onlyInWorld(WorldName.NETHER))
            .put(Material.ZOMBIE_VILLAGER_SPAWN_EGG, new CLT(0.15D))

            // 0.75% - just for fun mobs
            .put(Material.CREEPER_HEAD, new CLT(0.15D))
            .put(Material.PIGLIN_HEAD, new CLT(0.15D))
            .put(Material.PLAYER_HEAD, new CLT(0.15D))
            .put(Material.SKELETON_SKULL, new CLT(0.15D))
            .put(Material.ZOMBIE_HEAD, new CLT(0.15D))

            // 1.95% - uncommon templates from https://minecraft.wiki/w/Rarity,
            .put(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))
            .put(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.15D))

            // 0.4% - rare templates
            .put(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.10D))
            .put(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.10D))
            .put(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.10D))
            .put(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.10D))

            // 0.05% - epic templates
            .put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.05D))

            // 0.05% - semi-bosses
            .put(Material.WARDEN_SPAWN_EGG, new CLT(0.05D))

            .build();

    private void stackInventoryOnce(final String title, final Inventory inventory) {
        final Map<Material,Integer> map = new HashMap<>();

        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            /*
                The current use case of calling stackInventoryOnce()
                when inventory.firstEmpty() didn't give a result
                makes itemStack not null,
                but the check is for further safety.
             */
            if(itemStack != null) {
                if(map.containsKey(itemStack.getType())) {
                    final int existingI = map.get(itemStack.getType());
                    final ItemStack existingItemStack = inventory.getItem(existingI);
                    /*
                        Even assuming itemStack might be null in the future,
                        existingItemStack can't be null
                        because we put existingI in the map after checking for null.
                     */
                    if(existingItemStack.getAmount() < existingItemStack.getMaxStackSize()) {
                        final int diff = Math.min(
                                existingItemStack.getMaxStackSize() - existingItemStack.getAmount(),
                                itemStack.getAmount()
                        );

                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s merged: %d %s from item #%d moved to item #%d",
                                    title, diff, itemStack.getType(), i, existingI));
                        }

                        existingItemStack.setAmount(existingItemStack.getAmount() + diff);
                        if(itemStack.getAmount() > diff) {
                            itemStack.setAmount(itemStack.getAmount() - diff);
                        } else {
                            inventory.setItem(i, null);
                            return;
                        }
                    }
                }

                map.put(itemStack.getType(), i);
            }
        }

        if (customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s doesn't have space", title));
        }
    }

    private final static double MULTIPLY_PROBABILITY = 10.0D;
    private void populateInventory(final String title, final String worldName, final Inventory inventory, final int density) {
        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */

        /*
        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if((itemStack != null) && (itemStack.getType().equals(chestIdempotencyMarker))) {
                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s item #%d already has an idempotency marker", title, i));
                }
                return;
            }
        }
        */

        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            /*
                High levels of density are in
                - woodland mansions (4)
                - bastions (3)
                - the end (2)
                - pyramids (2)
                - whirlpools (1)
                - the nether (1)

                and there is nothing specific to multiply.
             */
            if((itemStack != null) && (itemStack.getMaxStackSize() > 1) && (Probability.PASS(MULTIPLY_PROBABILITY, 0))) {
                setAmount(String.format("%s item #%d", title, i),
                        itemStack.getAmount(), itemStack, 1, CLT.MAX_POWER);
            }
        }

        for(Map.Entry<Material, CLT> entry : chestLootTable.entrySet()) {
            if(entry.getValue().pass(density, worldName)) {
                int i = inventory.firstEmpty();
                if(i == -1) {
                    // There are no empty slots.
                    stackInventoryOnce(title, inventory);
                    i = inventory.firstEmpty();
                    if(i == -1) {
                        /*
                            There are no empty slots even after the stack.
                            Potentially, other random items may be added, but it isn't interesting.
                         */
                        break;
                    }
                }
                inventory.setItem(i, new ItemStack(entry.getKey(), 1));

                // The fresh getItem() is needed to properly update the amount
                entry.getValue().applyItemConfig(inventory.getItem(i));

                setAmount(String.format("%s item #%d", title, i),
                        // The fresh getItem() is needed to properly update the amount
                        0, inventory.getItem(i), 0, entry.getValue().getMaxPower());
            }
        }
    }

    private Block getBasement(final Block block) {
        return block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
    }

    private Block getBasement(final Entity entity) {
        return entity.getWorld().getBlockAt((int)entity.getLocation().getX(), (int)entity.getLocation().getY() - 1, (int)entity.getLocation().getZ());
    }

    private void populateContainer(final Block block) {
        final int density = getDensity(getBasement(block));

        populateInventory(format(block), block.getWorld().getName(), ((Container)block.getState()).getInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d and %s under",
                    format(block), density, format(getBasement(block))));
        }
    }

    private <T extends InventoryHolder & Entity> void populateInventoryHolder(final T inventoryHolder) {
        final int density = getDensity(getBasement(inventoryHolder));

        populateInventory(format(inventoryHolder), inventoryHolder.getWorld().getName(), inventoryHolder.getInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d and %s under",
                    format(inventoryHolder), density, format(getBasement(inventoryHolder))));
        }
    }

    public void populatePlayer(final Player player, final int density) {
        populateInventory(format(player), player.getWorld().getName(), player.getInventory(), density);
    }

    /*
        Ideated from: https://minecraft.wiki/w/Smelting

        Removed ideas:
            Player must kill an Elder guardian, not just find a sponge
            .put(Material.SPONGE, 0)

        Test
            version 1.21.3
            seed -1565193744182814265 (Belongings 2025-01-12)
            TerraformGenerator-19.1.0
            WorldBorder 4050 x 4050

            $ grep oceanic- plugins/TerraformGenerator/config.yml
            oceanic-frequency: 0.11
            oceanic-threshold: 8.0
            deep-oceanic-threshold: 27.0

            $ grep "DEBUG.* GOLD_NUGGET " logs/latest.log | cut -d' ' -f11 | awk '{s+=$1} END {print s}'
            10723
     */
    private final Map<Material,Integer> furnaceResultTable = ImmutableMap.<Material,Integer>builder()
            // 10,723
            .put(Material.GOLD_NUGGET, CLT.MAX_POWER)
            // 9,858
            .put(Material.IRON_NUGGET, CLT.MAX_POWER)

            // 5,981
            .put(Material.IRON_INGOT, CLT.MAX_POWER - 1)
            // 5,028
            .put(Material.GOLD_INGOT, CLT.MAX_POWER - 1)

            // 3,827
            .put(Material.REDSTONE, CLT.MAX_POWER - 2)
            // 3,621
            .put(Material.LAPIS_LAZULI, CLT.MAX_POWER - 2)

            // 905
            .put(Material.DIAMOND, 1)
            // 826
            .put(Material.EMERALD, 1)

            // 570
            .put(Material.NETHERITE_SCRAP, 0)
            .build();

    private final Map<Material,Integer> furnaceFuelTable = ImmutableMap.<Material,Integer>builder()
            // 31,492
            .put(Material.COAL, CLT.MAX_POWER)
            // 10,716
            .put(Material.COAL_BLOCK, CLT.MAX_POWER - 2)
            // 1,697
            .put(Material.LAVA_BUCKET, 0)
            .build();


    private void updateItemStack(final String title,
                                 final Supplier<ItemStack> itemStackGetter,
                                 final Consumer<ItemStack> itemStackSetter,
                                 final Map<Material,Integer> lootTable) {
        ItemStack itemStack = itemStackGetter.get();
        if(itemStack != null) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s already has an idempotency marker", title));
            }
            return;
        }

        final Material material = RandomUtils.getSetItem(lootTable.keySet());
        itemStackSetter.accept(new ItemStack(material, 1));
        // The sequence is needed to properly update the amount
        itemStack = itemStackGetter.get();
        setAmount(title, 0, itemStack, 0, lootTable.get(material));
    }

    private void populateFurnace(final Block block) {
        final Furnace furnace = (Furnace)block.getState();

        final FurnaceInventory inventory = furnace.getInventory();

        updateItemStack(String.format("%s fuel", format(block)),
                inventory::getFuel,
                inventory::setFuel,
                furnaceFuelTable);

        updateItemStack(String.format("%s result", format(block)),
                inventory::getResult,
                inventory::setResult,
                furnaceResultTable);
    }

    private void setAmount(final String title, final int oldAmount, final ItemStack itemStack,
                           final int minPower, final int maxPower) {
        final int newAmount =
                Math.min(
                        itemStack.getMaxStackSize(),
                        itemStack.getAmount() * CLT.getRandomAmount(minPower, maxPower)
                );

        itemStack.setAmount(newAmount);

        if (customLogger.isDebugMode()) {
            if(oldAmount == 0) {
                customLogger.debug(String.format("%s %s set to %d",
                        title, itemStack.getType(), newAmount));
            } else if(newAmount > oldAmount) {
                customLogger.debug(String.format("%s %s updated from %d to %d",
                        title, itemStack.getType(), oldAmount, newAmount));
            } else {
                customLogger.warning(String.format("%s %s kept as %d",
                        title, itemStack.getType(), newAmount));
            }
        }
    }

    private void populateEndShip(final Block block) {
        if(block.getWorld().getName().equalsIgnoreCase(WorldName.END)) {

            final Illusioner illusioner = (Illusioner)block.getWorld().spawnEntity(
                    block.getWorld().getHighestBlockAt(block.getX(), block.getZ()).getLocation()
                            // Above the highest block
                            .add(new Vector(0.0D, 1.01D, 0.0D)),
                    EntityType.ILLUSIONER);
            // In case the CustomCreatures plugin is switched off.
            illusioner.setRemoveWhenFarAway(false);

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s populated", format(block)));
            }
        }
    }
}
