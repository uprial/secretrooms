package com.gmail.uprial.secretrooms.populator.dungeon;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.common.BlockSeed;
import com.gmail.uprial.secretrooms.populator.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gmail.uprial.secretrooms.common.Formatter.format;
import static com.gmail.uprial.secretrooms.common.Utils.seconds2ticks;

public class DungeonPopulator extends AbstractSeedSpecificPopulator {
    private final CustomLogger customLogger;

    VirtualChunk vc;

    // (1,000 / 16) ^ 2 = 3,906
    private final static int PROBABILITY = 3_906;

    private final static int ROOM_SIZE = 5;

    public DungeonPopulator(final CustomLogger customLogger) {
        super(PROBABILITY);

        this.customLogger = customLogger;
    }

    public String getName() {
        return "Dungeon";
    }

    // DungeonChestLootTable
    private static class D {
        private final List<Material> materials;
        private final Integer count;
        private final ItemConfig itemConfig;

        D(final Material material, final Integer count) {
            this(material, count, null);
        }

        D(final Material material, final Integer count, final ItemConfig itemConfig) {
            this.materials = Collections.singletonList(material);
            this.count = count;
            this.itemConfig = itemConfig;
        }

        D(final Set<Material> materials, final Integer count) {
            this.materials = Lists.newArrayList(materials);
            this.count = count;
            this.itemConfig = null;
        }

        Material getMaterial(final ContentSeed cs) {
            return cs.oneOf(materials);
        }

        Integer getCount() {
            return count;
        }

        void applyItemConfig(final ContentSeed cs, final ItemStack itemStack) {
            if(itemConfig != null) {
                itemConfig.apply(cs, itemStack);
            }
        }
    }

    /*
        Ideated from:
            https://minecraft.wiki/w/Non-renewable_resource

        The idea is to give items with only enchantments
        unavailable in Survival mode, with potential for extension.
     */
    private final int STACK = 64;

    private final List<List<D>> chestLootTable
            = ImmutableList.<List<D>>builder()

            .add(ImmutableList.<D>builder()
                    /*
                        According to https://minecraft.wiki/w/Log,
                        there are 9 types of logs and 2 types of stems.
                     */
                    .add(new D(ImmutableSet.<Material>builder()
                            .add(Material.OAK_LOG)
                            .add(Material.SPRUCE_LOG)
                            .add(Material.BIRCH_LOG)
                            .add(Material.JUNGLE_LOG)
                            .add(Material.ACACIA_LOG)
                            .add(Material.DARK_OAK_LOG)
                            .add(Material.MANGROVE_LOG)
                            .add(Material.CHERRY_LOG)
                            .add(Material.PALE_OAK_LOG)
                            .add(Material.CRIMSON_STEM)
                            .add(Material.WARPED_STEM)
                            .build(), STACK * 27))
                    .build())
            .add(ImmutableList.<D>builder()
                    /*
                        According to https://www.youtube.com/watch?v=ZntF03lSY4E,
                        the top-1 of best-looking blocks.
                     */
                    .add(new D(Material.SEA_LANTERN, STACK * 3))
                    .build())
            /*
                            Regular*1   Deepslate*1 Regular*2   Deepslate*2 Max mining
                Hardness    1.5         3.0         -           -           efficiency*3

                Coal        642         40          158,964     1,999       642/1.5=428
                Redstone    91          512         1,801       36,115      512/3*4.5=768
                Lapis       114         133         17,137      20,579      114/1.5*6.5=494
                Gold        82          173         7,548       27,462      173/3=58
                Diamond     14          226         569         15,443      226/3=75
                Obsidian    -           -           40,499      -

                *1 According to https://minecraft.wiki/w/Ore

                *2 According to "loaded-stats" command with view-distance 32

                *3 According to https://minecraft.wiki/w/Ore,
                Redstone ore and its deepslate variant drop 4–5 redstone dust.
                Lapis lazuli ore and its deepslate variant drop 4–9 lapis lazuli.

             */
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.REDSTONE_BLOCK, STACK * 4))
                    .add(new D(Material.LAPIS_BLOCK, STACK * 4))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.GOLD_BLOCK, STACK))
                    .add(new D(Material.DIAMOND_BLOCK, STACK))
                    .build())
            .add(ImmutableList.<D>builder()
                    /*
                        According to https://minecraft.wiki/w/Wither,
                        Reinforced Deepslate is the most vanilla block
                        that the Wither cannot break.

                        One stack is enough to make a Wither stuck.
                     */
                    .add(new D(Material.REINFORCED_DEEPSLATE, STACK))
                    .build())
            /*
                https://minecraft.wiki/w/Dye

                This is the only joke: some useless but fun loot.
             */
            .add(ImmutableList.<D>builder()
                    .add(new D(ImmutableSet.<Material>builder()
                            .add(Material.WHITE_DYE)
                            .add(Material.GRAY_DYE)
                            .add(Material.BROWN_DYE)
                            .add(Material.ORANGE_DYE)
                            .add(Material.LIME_DYE)
                            .add(Material.CYAN_DYE)
                            .add(Material.BLUE_DYE)
                            .add(Material.MAGENTA_DYE)
                            .add(Material.LIGHT_GRAY_DYE)
                            .add(Material.BLACK_DYE)
                            .add(Material.RED_DYE)
                            .add(Material.YELLOW_DYE)
                            .add(Material.GREEN_DYE)
                            .add(Material.LIGHT_BLUE_DYE)
                            .add(Material.PURPLE_DYE)
                            .add(Material.PINK_DYE)
                            .build(), STACK))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.DIAMOND_PICKAXE, 1, new ItemConfig()
                            /*
                                Survival maximum level is 3, here it's 5.

                                diamond_pickaxe[minecraft:enchantments={"minecraft:fortune":5}]

                                A potential EFFICIENCY(5), UNBREAKING(3) and MENDING(1)
                                upgrade would cost 15 levels.
                             */
                            .ench(Enchantment.FORTUNE, 5, 5)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.SPLASH_POTION, 3, new ItemConfig()
                            // The highest potion amplifier that has a name
                            .effect(PotionEffectType.INSTANT_DAMAGE, 0, 5)))
                    .add(new D(Material.SPLASH_POTION, 3, new ItemConfig()
                            // The highest potion amplifier that has a name
                            .effect(PotionEffectType.INSTANT_HEALTH, 0, 5)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.SPLASH_POTION, 2, new ItemConfig()
                            // The highest potion amplifier that protects less than for 100%
                            .effect(PotionEffectType.RESISTANCE, seconds2ticks(300), 3)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.TRIDENT, 1, new ItemConfig()
                            /*
                                Survival maximum level is 3, here it's 5.

                                trident[minecraft:enchantments={"minecraft:loyalty":5}]

                                A potential UNBREAKING(3), CHANNELING(1) and MENDING(1)
                                upgrade would cost 18 levels.

                                IMO, impaling is just useless, and isn't a potential upgrade.

                                In fact, riptide isn't compatible ith loyalty.
                             */
                            .ench(Enchantment.LOYALTY, 5, 5)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.CROSSBOW, 1, new ItemConfig()
                            /*
                                Survival maximum levels are ZERO.

                                crossbow[minecraft:enchantments={"minecraft:power":10,"minecraft:infinity":1,"minecraft:flame":1,"minecraft:punch":2}]

                                A potential UNBREAKING(3), QUICK_CHARGE(3) and MENDING(1)
                                upgrade would cost 15 levels.

                                With PIERCING(4) - 19.

                                With MULTISHOT(1) - 19.
                             */
                            .ench(Enchantment.POWER, 10, 10)
                            .ench(Enchantment.PUNCH, 2, 2)
                            .ench(Enchantment.FLAME)
                            .ench(Enchantment.INFINITY)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.CROSSBOW, 1, new ItemConfig()
                            /*
                                Survival maximum level is 1, here it's 3.

                                crossbow[minecraft:enchantments={"minecraft:multishot":3}]

                                A potential UNBREAKING(3), QUICK_CHARGE(3) and MENDING(1)
                                upgrade would cost 16 levels.
                             */
                            .ench(Enchantment.MULTISHOT, 3, 3)))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.DIAMOND_SWORD, 1, new ItemConfig()
                            /*
                                Survival maximum level is 3, here it's 5.

                                diamond_sword[minecraft:enchantments={"minecraft:looting":5}]

                                A potential UNBREAKING(3), SHARPNESS(5), FIRE_ASPECT(2), SWEEPING_EDGE(3) and MENDING(1)
                                upgrade would cost 35 levels.

                                IMO, knockback prevents swords from dealing a lot of melee damage per second,
                                and isn't a potential upgrade.
                             */
                            .ench(Enchantment.LOOTING, 5, 5)))
                    .build())
            .build();

    @Override
    protected void populateAppropriateChunk(final Chunk chunk) {
        vc = new VirtualChunk(getName(), chunk, BlockFace.NORTH);
        /*
            Structure is between -1 and ROOM_SIZE + 1,
            so the move must be between (0 - -1) and (15 - ROOM_SIZE - 1).
         */
        vc.move(1, 0, 1);

        final int entranceY;
        {
            // Find the lowest possible entranceY
            int minX = -1;
            int minY = vc.getMaxHeight();
            int minZ = -1;
            for (int x = 0; x <= 15 - ROOM_SIZE - 1; x++) {
                for (int z = 0; z <= 15 - ROOM_SIZE - 1; z++) {
                    int y = getEntranceY(x, z);

                    if(y == vc.getMinHeight()) {
                        if(customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s[%s] can't be populated: void",
                                    getName(), format(chunk)));
                        }

                        return ;
                    }

                    if (y < minY) {
                        minX = x;
                        minY = y;
                        minZ = z;
                    }
                }
            }
            vc.move(minX, 0, minZ);
            entranceY = minY;
        }

        final int roofY, floorY;

        {
            int y = entranceY;

            while ((y > vc.getMinHeight()) && (getPassableShare(0, y, 0) > 0.01D)) {
                y--;
            }
            // Y is a first non-passable layer
            roofY = y;

            while ((y > vc.getMinHeight()) && (getPassableShare(0, y, 0) < 0.01D)) {
                y--;
            }
            // Y is a first passable layer, but we need the non-passable one
            floorY = y + 1;
        }

        // Two rooms with a height of 2 and a floor of 1
        if(floorY + 6 > roofY - 1) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s[%s] can't be populated: %d floor and %d roof",
                        getName(), format(chunk), floorY, roofY));
            }

            return ;
        }

        final Material ladderBase;
        {
            final Map<Material, AtomicInteger> popularity = new HashMap<>();
            for (int y = floorY; y <= entranceY; y++) {
                final AtomicInteger counter
                        = popularity.computeIfAbsent(vc.get(0, y, 0).getType(),
                        (k) -> new AtomicInteger(0));
                counter.incrementAndGet();
            }

            Material topMaterial = null;
            int topCounter = -1;
            for (Map.Entry<Material, AtomicInteger> entry : popularity.entrySet()) {
                if ((topCounter < entry.getValue().intValue()) && (entry.getKey().isOccluding())) {
                    topMaterial = entry.getKey();
                    topCounter = entry.getValue().intValue();
                }
            }

            if(topMaterial == null) {
                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s[%s] can't be populated: %s",
                            getName(), format(chunk), popularity));
                }

                return ;
            }


            ladderBase = topMaterial;
        }

        // We need to start from the layer above the floor and finish before the roof
        vc.box(Material.AIR,
                0, floorY + 1, 0,
                ROOM_SIZE - 1, floorY + 2, ROOM_SIZE - 1);

        vc.box(Material.AIR,
                1, floorY + 5, 1,
                ROOM_SIZE - 1, floorY + 6, ROOM_SIZE - 1);

        vc.box(Material.AIR,
                0, floorY + 5, 2,
                0, floorY + 6, ROOM_SIZE - 1);

        vc.box(Material.AIR,
                2, floorY + 5, 0,
                ROOM_SIZE - 1, floorY + 6, 0);

        /*
            We need to start from the layer above the floor
            and finish one block below the entrance for secrecy.
         */
        for(int y = floorY + 1; y <= entranceY - 1; y++) {
            // Restore the ladder base
            if(vc.get(0, y, -1).isPassable()) {
                vc.set(0, y, -1, ladderBase);
            }
            vc.set(0, y, 0, Material.LADDER, BlockFace.SOUTH);
        }
        // Draw a highly visible sign
        vc.set(0, entranceY, 0, Material.SOUL_WALL_TORCH, BlockFace.SOUTH);

        // A ladder to the sub-room
        for(int y = floorY + 1; y <= floorY + 4; y++) {
            vc.set(ROOM_SIZE - 1, y, ROOM_SIZE - 1, Material.LADDER, BlockFace.NORTH);
        }

        {
            final int sx = ROOM_SIZE / 2;
            final int sy = floorY + 4;
            final int sz = ROOM_SIZE / 2;

            // Defend the creeper spawner from the sides
            for(int s = -1; s <= +1; s += 2) {
                vc.set(sx + s, sy, sz, Material.REINFORCED_DEEPSLATE);
                vc.set(sx, sy, sz + s, Material.REINFORCED_DEEPSLATE);
            }
            vc.set(sx, sy - 1, sz, Material.REINFORCED_DEEPSLATE);
            // Hide the creeper spawner from the top
            vc.set(sx, sy + 1, sz, Material.BLACK_CARPET);

            new SpawnerHelper().setQuick().set(vc.get(sx, sy, sz), EntityType.CREEPER);
        }

        {
            /*
                WARNING: for ROOM_SIZE less than 4, ROOM_SIZE / 2 gives 1 for both X/Z,
                and we'll need to pick another Y or X/Z
                to avoid the Spawner and the Chest overlapping.

                The bedrock prevents the chest from opening from a distance:
                the player must beat the enemies around and only then open the chest.
             */
            // Horizontal box
            vc.set(0, floorY + 5, 1, Material.BEDROCK);
            vc.set(1, floorY + 5, 0, Material.BEDROCK);
            // Vertical box
            vc.set(1, floorY + 6, 1, Material.BEDROCK);
            vc.set(1, floorY + 4, 1, Material.BEDROCK);

            final Block chest = vc.set(1, floorY + 5, 1, Material.CHEST);
            final Inventory inventory = ((Chest) chest.getState()).getInventory();
            final BlockSeed bs = BlockSeed.valueOf(chest);
            final ContentSeed cs = ContentSeed.valueOf(chest);

            int i = 0;
            for(D d : bs.oneOf(chestLootTable)) {
                int count = d.getCount();
                while(count > 0) {
                    final Material material = d.getMaterial(cs);
                    final int amount = Math.min(material.getMaxStackSize(), count);
                    final ItemStack itemStack = new ItemStack(material, amount);

                    d.applyItemConfig(cs, itemStack);

                    inventory.setItem(i, itemStack);

                    if (customLogger.isDebugMode()) {
                        // WARNING: ConsistencyReference#1
                        customLogger.debug(String.format("%s item #%d %s set to %d",
                                format(chest), i, format(itemStack), amount));
                    }

                    count -= amount;
                    i++;
                }
            }
        }

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s[%s] populated", getName(), format(chunk)));
        }
    }

    private int getEntranceY(final int x, final int z) {
        int y = vc.getMaxHeight() - 1;

        // Search a terrain
        while ((y > vc.getMinHeight()) && (isEnterable(vc.get(x, y, z)))) {
            y--;
        }

        // Nether
        if(vc.get(x, y, z).getType().equals(Material.BEDROCK)) {
            // Go to a big air area
            while ((y > vc.getMinHeight()) && (getPassableShare(x, y, z) < 0.99D)) {
                y--;
            }

            // Search a terrain one more time
            while ((y > vc.getMinHeight()) && (isEnterable(vc.get(x, y, z)))) {
                y--;
            }
        }

        // Y is a first non-passable block
        return y;
    }

    private final Set<Material> nonEnterable = ImmutableSet.<Material>builder()
            .add(Material.LAVA)
            .build();

    private boolean isEnterable(final Block block) {
        return block.isPassable() && !nonEnterable.contains(block.getType());
    }

    private double getPassableShare(final int x, final int y, final int z) {
        double passable = 0.0D;

        for (int dx = -1; dx < ROOM_SIZE + 1; dx++) {
            for (int dz = -1; dz < ROOM_SIZE + 1; dz++) {
                if(vc.get(x + dx, y, z + dz).isPassable()) {
                    passable += 1.0D;
                }
            }
        }

        return passable / Math.pow(ROOM_SIZE + 2, 2.0D);
    }
}
