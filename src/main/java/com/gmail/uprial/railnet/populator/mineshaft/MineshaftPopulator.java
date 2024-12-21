package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.RailNetCron;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.common.RandomUtils;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.CLT;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.ItemConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.Utils.seconds2ticks;

public class MineshaftPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;

    public MineshaftPopulator(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    @Override
    public void populate(final Chunk chunk) {
        /*
            Fix population in structures with post-generation,
            known examples: Desert Pyramid, Outpost
            - chests there seems to be populated somehow after their chunk load.
         */
        RailNetCron.defer(() -> {
            final int minY = chunk.getWorld().getMinHeight();
            final int maxY = chunk.getWorld().getMaxHeight();
            for(int y = minY; y < maxY; y++) {
                for(int x = 0; x < 16; x++) {
                    for(int z = 0; z < 16; z++) {
                        populateBlock(chunk.getBlock(x, y, z));
                    }
                }
            }

            for(final Entity entity : chunk.getEntities()) {
                if(entity instanceof StorageMinecart) {
                    populateStorageMinecart((StorageMinecart)entity);
                }
            }
        });
    }

    private interface BlockPopulator {
        void populate(final Block block);
    }

    private final Map<Material, BlockPopulator> blockPopulators = ImmutableMap.<Material, BlockPopulator>builder()
            .put(Material.CHEST, this::populateChest)
            .put(Material.TRAPPED_CHEST, this::populateChest)
            .put(Material.FURNACE, this::populateFurnace)
            .put(Material.BLAST_FURNACE, this::populateFurnace)
            .put(Material.BARREL, this::populateBarrel)
            .build();

    private void populateBlock(final Block block) {
        final BlockPopulator blockPopulator = blockPopulators.get(block.getType());
        if(blockPopulator != null) {
            blockPopulator.populate(block);
        }
    }

    // Increase density in some worlds
    private final Map<String,Integer> worldDensities = ImmutableMap.<String,Integer>builder()
            .put(WorldName.NETHER, 1)
            .put(WorldName.END, 2)
            .build();

    /*
        Increase density above some blocks.

        Specifically, structure research:
        $ grep "world:<X-prefix>.*:<Z-prefix>.*populated" logs/latest.log
            | cut -d' ' -f12 | cut -d'[' -f1 | sort | uniq

        gives...
     */
        private final Map<Material,Integer> materialDensities = ImmutableMap.<Material,Integer>builder()
            // Woodland mansion, inherits 0 from worldDensities
            .put(Material.DARK_OAK_PLANKS, 4)
            .put(Material.DARK_OAK_SLAB, 4)
            .put(Material.DARK_OAK_STAIRS, 4)
            // Bastion, inherits 1 from worldDensities
            .put(Material.BLACKSTONE, 2)
            .put(Material.GILDED_BLACKSTONE, 2)
            .put(Material.POLISHED_BLACKSTONE_BRICKS, 2)
            .put(Material.POLISHED_BLACKSTONE_SLAB, 2)
            .build();

    private int getDensity(final Block basement) {
        final int worldDensity = worldDensities.getOrDefault(WorldName.normalize(basement.getWorld().getName()), 0);
        final int materialDensity = materialDensities.getOrDefault(basement.getType(), 0);

        return worldDensity + materialDensity;
    }

    /*
        According to https://minecraft.wiki/w/Food,
        food with good saturation, which can't be found in chests.
     */
    //private final Material chestIdempotencyMarker = Material.COOKED_MUTTON;

    private final ItemConfig netheriteClothConfig =  new ItemConfig()
            // Survival maximum level is 4, here it's 5
            .ench(Enchantment.PROTECTION, 0, 5)
            .ench(Enchantment.VANISHING_CURSE)
            .trim(TrimMaterial.NETHERITE, TrimPattern.RIB);

    private final ItemConfig goldenClothConfig =  new ItemConfig()
            // Survival maximum level is 4, here it's 5
            .ench(Enchantment.PROTECTION, 3, 5)
            .ench(Enchantment.THORNS, 0, 3)
            .ench(Enchantment.MENDING)
            .trim(TrimMaterial.GOLD, TrimPattern.RIB);

    private final ItemConfig netheriteToolConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.EFFICIENCY, 0, 10)
            .ench(Enchantment.VANISHING_CURSE);

    private final ItemConfig goldenToolConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.EFFICIENCY, 5, 10)
            .ench(Enchantment.FORTUNE, 0, 3);

    private final ItemConfig netheriteSwordConfig =  new ItemConfig()
            // Survival maximum level is 5, here it's 10
            .ench(Enchantment.SHARPNESS, 0, 10)
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
            .add(PotionEffect.INFINITE_DURATION)
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

    private final ItemConfig potionConfig = new ItemConfig().effects(potionDurationOptions, potionEffects);
    private final ItemConfig arrowConfig = new ItemConfig().effects(arrowDurationOptions, potionEffects);

    /*
        Ideated from:
            https://minecraft.wiki/w/Rarity
            https://minecraft.wiki/w/Spawn_Egg

        Removed ideas:
            Ender pearls motivate player to fight endermans.
            .put(Material.ENDER_PEARL,

            End crystals require a block of obsidian beneath,
            which removes all potential fun of placing them.
            .put(Material.END_CRYSTAL, new CLT(7.5D, 1))

            Bedrock has no real usage, but may bring potential damage to the world.
            .put(Material.BEDROCK,

            Golden carrots and apples are funny for 1st time,
            but they are neither food with good saturation for everyday life
            nor provide enough regeneration in a fight.
            .put(Material.GOLDEN_APPLE, new CLT(7.5D, 1))
            .put(Material.GOLDEN_CARROT, new CLT(7.5D, 1))

     */
    private final Map<Material, CLT> chestLootTable = ImmutableMap.<Material, CLT>builder()
            //.put(chestIdempotencyMarker, new CLT(MAX_PERCENT))

            /*
                Obtaining these resources isn't worth its time,
                but as a gift it's a lot of fun.
             */
            .put(Material.TNT, new CLT(5.0D, 2))
            .put(Material.OBSIDIAN, new CLT(5.0D, 2))

            // 1.5 + 0.5 + 0.5 + 1.5 = 4
            .put(Material.POTION, new CLT(1.5D, potionConfig))
            .put(Material.SPLASH_POTION, new CLT(0.5D, potionConfig))
            .put(Material.LINGERING_POTION, new CLT(0.5D, potionConfig))
            .put(Material.TIPPED_ARROW, new CLT(1.5D, arrowConfig, CLT.MAX_POWER))

            .put(Material.DIAMOND, new CLT(3.0D))

            // Please, keep consistent with FireworkCraftBook
            .put(Material.FIREWORK_ROCKET, new CLT(3.0D, 2)
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BURST, 3, 5))
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BALL, 5, 12))
                    .addItemConfigOption(new ItemConfig().firework(FireworkEffect.Type.BALL_LARGE, 10, 20))
            )

            .put(Material.ENCHANTED_GOLDEN_APPLE, new CLT(2.5D))
            .put(Material.TOTEM_OF_UNDYING, new CLT(2.5D))
            .put(Material.SPAWNER, new CLT(2.5D))

            .put(Material.GOLDEN_HELMET, new CLT(2.0D, goldenClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.GOLDEN_CHESTPLATE, new CLT(2.0D, goldenClothConfig))
            .put(Material.GOLDEN_LEGGINGS, new CLT(2.0D, goldenClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.GOLDEN_BOOTS, new CLT(2.0D, goldenClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            .put(Material.NETHERITE_HELMET, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.RESPIRATION, 0, 3)
                    .ench(Enchantment.AQUA_AFFINITY, 0, 1)))
            .put(Material.NETHERITE_CHESTPLATE, new CLT(2.0D, netheriteClothConfig))
            .put(Material.NETHERITE_LEGGINGS, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.SWIFT_SNEAK, 0, 3)))
            .put(Material.NETHERITE_BOOTS, new CLT(2.0D, netheriteClothConfig
                    .ench(Enchantment.FEATHER_FALLING, 0, 4)
                    .ench(Enchantment.DEPTH_STRIDER, 0, 3)))

            .put(Material.GOLDEN_PICKAXE, new CLT(1.5D, goldenToolConfig))
            .put(Material.GOLDEN_SWORD, new CLT(1.5D, goldenSwordConfig))

            .put(Material.NETHERITE_PICKAXE, new CLT(1.5D, netheriteToolConfig))
            .put(Material.NETHERITE_SWORD, new CLT(1.5D, netheriteSwordConfig))

            .put(Material.CREEPER_SPAWN_EGG, new CLT(1.0D))
            .put(Material.ZOMBIE_SPAWN_EGG, new CLT(1.0D))
            .put(Material.SKELETON_SPAWN_EGG, new CLT(1.0D))
            .put(Material.SPIDER_SPAWN_EGG, new CLT(1.0D))

            .put(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, new CLT(1.0D))
            .put(Material.OMINOUS_BOTTLE, new CLT(1.0D, new ItemConfig().amplify(4)))

            .put(Material.SLIME_SPAWN_EGG, new CLT(0.5D))
            .put(Material.MOOSHROOM_SPAWN_EGG, new CLT(0.5D))
            .put(Material.BLAZE_SPAWN_EGG, new CLT(0.5D).onlyInWorld(WorldName.NETHER))

            .put(Material.EVOKER_SPAWN_EGG, new CLT(0.25D))
            .put(Material.WITHER_SKELETON_SPAWN_EGG, new CLT(0.25D).onlyInWorld(WorldName.NETHER))
            .put(Material.GHAST_SPAWN_EGG, new CLT(0.25D).onlyInWorld(WorldName.NETHER))
            .put(Material.SHULKER_SPAWN_EGG, new CLT(0.25D).onlyInWorld(WorldName.END))

            // Just for fun
            .put(Material.SKELETON_SKULL, new CLT(0.25D))
            .put(Material.CREEPER_HEAD, new CLT(0.25D))
            .put(Material.PIGLIN_HEAD, new CLT(0.25D))
            .put(Material.PLAYER_HEAD, new CLT(0.25D))
            .put(Material.ZOMBIE_HEAD, new CLT(0.25D))

            .put(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))
            .put(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, new CLT(0.2D))

            // Something insane
            .put(Material.WARDEN_SPAWN_EGG, new CLT(0.1D))
            .put(Material.WITHER_SPAWN_EGG, new CLT(0.1D).onlyInWorld(WorldName.NETHER))
            .put(Material.ENDER_DRAGON_SPAWN_EGG, new CLT(0.1D).onlyInWorld(WorldName.END))

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
                - whirlpools (up to 5)
                - woodland mansions (4)
                - bastions (3)
                - the end (2)
                - the nether (1)

                and there is nothing specific to multiply.
             */
            if((itemStack != null) && (itemStack.getMaxStackSize() > 1) && (Probability.PASS(MULTIPLY_PROBABILITY, 0))) {
                setAmount(String.format("%s item #%d", title, i),
                        itemStack.getAmount(), itemStack, 1, CLT.MAX_POWER);
            }
        }

        for(Map.Entry<Material, CLT> entry : chestLootTable.entrySet()) {
            if(Probability.PASS(entry.getValue().getProbability(), density)
                    && entry.getValue().isAppropriateWorld(worldName)) {
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

                if(entry.getValue().hasItemConfig()) {
                    // The fresh getItem() is needed to properly update the amount
                    entry.getValue().getItemConfig().apply(inventory.getItem(i));
                }

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

    private void populateChest(final Block block) {
        populateChest(block, getDensity(getBasement(block)));
    }

    public void populateChest(final Block block, final int density) {
        populateInventory(format(block), block.getWorld().getName(), ((Chest)block.getState()).getBlockInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d and %s under",
                    format(block), density, format(getBasement(block))));
        }
    }

    private void populateStorageMinecart(final StorageMinecart storageMinecart) {
        final int density = getDensity(getBasement(storageMinecart));

        populateInventory(format(storageMinecart), storageMinecart.getWorld().getName(), storageMinecart.getInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d and %s under",
                    format(storageMinecart), density, format(getBasement(storageMinecart))));
        }
    }

    private void populateBarrel(final Block block) {
        final int density = getDensity(getBasement(block));

        populateInventory(format(block), block.getWorld().getName(), ((Barrel)block.getState()).getInventory(), density);

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated with density %d and %s under",
                    format(block), density, format(getBasement(block))));
        }
    }

    public void populatePlayer(final Player player, final int density) {
        populateInventory(format(player), player.getWorld().getName(), player.getInventory(), density);
    }

    // Ideated from https://minecraft.wiki/w/Smelting
    // Material -> max power of drop
    private final Map<Material,Integer> furnaceResultTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.GOLD_NUGGET, CLT.MAX_POWER)
            .put(Material.IRON_NUGGET, CLT.MAX_POWER)

            .put(Material.IRON_INGOT, CLT.MAX_POWER - 1)
            .put(Material.GOLD_INGOT, CLT.MAX_POWER - 1)

            .put(Material.REDSTONE, CLT.MAX_POWER - 2)
            .put(Material.LAPIS_LAZULI, CLT.MAX_POWER - 2)

            .put(Material.NETHERITE_SCRAP, 0)
            .put(Material.SPONGE, 0)
            .build();

    private final Map<Material,Integer> furnaceFuelTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.COAL, CLT.MAX_POWER)
            .put(Material.COAL_BLOCK, CLT.MAX_POWER - 2)
            .put(Material.LAVA_BUCKET, 0)
            .build();


    private interface ItemStackGetter {
        ItemStack get();
    }

    private interface ItemStackSetter {
        void set(final ItemStack itemStack);
    }

    private void updateItemStack(final String title,
                                 final ItemStackGetter itemStackGetter,
                                 final ItemStackSetter itemStackSetter,
                                 final Map<Material,Integer> lootTable) {
        ItemStack itemStack = itemStackGetter.get();
        if(itemStack != null) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s already has an idempotency marker", title));
            }
            return;
        }

        final Material material = RandomUtils.getSetItem(lootTable.keySet());
        itemStackSetter.set(new ItemStack(material, 1));
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
}
