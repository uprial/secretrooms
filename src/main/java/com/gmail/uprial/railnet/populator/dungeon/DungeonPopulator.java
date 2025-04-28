package com.gmail.uprial.railnet.populator.dungeon;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.HashUtils;
import com.gmail.uprial.railnet.populator.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class DungeonPopulator extends AbstractSeedSpecificPopulator implements Tested_On_1_21_5 {
    private final CustomLogger customLogger;
    private final String conflictingPopulatorName;

    VirtualChunk vc;

    private final static String WORLD = null;
    private final static int DENSITY = 500;

    private final static int ROOM_SIZE = 5;

    public DungeonPopulator(final CustomLogger customLogger,
                            final String conflictingPopulatorName) {
        super(WORLD, DENSITY);

        this.customLogger = customLogger;
        this.conflictingPopulatorName = conflictingPopulatorName;
    }

    @Override
    public String getName() {
        return "Dungeon";
    }

    // DungeonChestLootTable
    private static class D {
        private final Material material;
        private final Integer count;
        private final ItemConfig itemConfig;

        D(final Material material, final Integer count) {
            this(material, count, null);
        }

        D(final Material material, final Integer count, final ItemConfig itemConfig) {
            this.material = material;
            this.count = count;
            this.itemConfig = itemConfig;
        }

        Material getMaterial() {
            return material;
        }

        Integer getCount() {
            return count;
        }

        void applyItemConfig(final ItemStack itemStack) {
            if(itemConfig != null) {
                itemConfig.apply(itemStack);
            }
        }
    }

    /*
        Ideated from:
            https://minecraft.wiki/w/Non-renewable_resource
     */
    private final int STACK = 64;
    private final int DYE_COUNT = 16;

    /*
        The idea is to give items with only enchantments
        unavailable in Survival mode, with potential for extension.
     */
    private final ItemConfig diamondToolConfig = new ItemConfig()
            /*
                Survival maximum level is 3, here it's 5.

                diamond_pickaxe[minecraft:enchantments={"minecraft:fortune":5}]

                A potential EFFICIENCY(5) and UNBREAKING(3)
                upgrade would cost 11 levels.
             */
            .ench(Enchantment.FORTUNE, 5, 5);

    private final ItemConfig tridentConfig = new ItemConfig()
            /*
                Survival maximum level is 5, here it's 10.

                trident[minecraft:enchantments={"minecraft:impaling":10}]

                A potential LOYALTY(3), UNBREAKING(3) and CHANNELING(1)
                upgrade would cost 20 levels.

                A potential RIPTIDE(3) and UNBREAKING(3)
                upgrade would cost 18 levels.
             */
            .ench(Enchantment.IMPALING, 10, 10);

    private final ItemConfig crossbowConfig = new ItemConfig()
            /*
                Survival maximum level is 5, here it's 10.

                crossbow[minecraft:enchantments={"minecraft:power":10,"minecraft:infinity":1,"minecraft:flame":1,"minecraft:punch":2}]

                A potential PIERCING(4), UNBREAKING(3) and QUICK_CHARGE(3)
                upgrade would cost 18 levels.

                A potential MULTISHOT(1), UNBREAKING(3) and QUICK_CHARGE(3)
                upgrade would cost 16 levels.
             */
            .ench(Enchantment.POWER, 10, 10)
            .ench(Enchantment.PUNCH, 2, 2)
            .ench(Enchantment.FLAME)
            .ench(Enchantment.INFINITY);

    private final ItemConfig diamondSwordConfig = new ItemConfig()
            /*
                Survival maximum level is 3, here it's 5.

                diamond_sword[minecraft:enchantments={"minecraft:looting":5}]

                A potential SHARPNESS(5), KNOCKBACK(2), FIRE_ASPECT(2) and SWEEPING_EDGE(3)
                upgrade would cost 29 levels.
             */
            .ench(Enchantment.LOOTING, 5, 5);

    private final ItemConfig damageSplashPotionConfig = new ItemConfig()
            // The highest potion amplifier that has a name
            .effect(PotionEffectType.INSTANT_DAMAGE, 0, 5);

    private final ItemConfig healthSplashPotionConfig = new ItemConfig()
            // The highest potion amplifier that has a name
            .effect(PotionEffectType.INSTANT_HEALTH, 0, 5);

    /*
        ==== Test ====

            $ grep "DEBUG.* BAMBOO_BLOCK " logs/1/latest.log | cut -d' ' -f12 | awk '{s+=$1} END {print s}'
            143424
     */
    private final List<List<D>> chestLootTable
            = ImmutableList.<List<D>>builder()

            // # 143,424
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.BAMBOO_BLOCK, STACK * 27))
                    .build())
            // # 88,704
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.SEA_LANTERN, STACK * 18))
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
            // # 12,288 * 2
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.REDSTONE_BLOCK, STACK * 4))
                    .add(new D(Material.LAPIS_BLOCK, STACK * 4))
                    .build())
            // # 4,288 * 2
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.GOLD_BLOCK, STACK))
                    .add(new D(Material.DIAMOND_BLOCK, STACK))
                    .build())
            // https://minecraft.wiki/w/Dye
            // # 1,760 + 1,696 + 1,744 + 1,776 + ...
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.WHITE_DYE, DYE_COUNT))
                    .add(new D(Material.GRAY_DYE, DYE_COUNT))
                    .add(new D(Material.BROWN_DYE, DYE_COUNT))
                    .add(new D(Material.ORANGE_DYE, DYE_COUNT))
                    .add(new D(Material.LIME_DYE, DYE_COUNT))
                    .add(new D(Material.CYAN_DYE, DYE_COUNT))
                    .add(new D(Material.BLUE_DYE, DYE_COUNT))
                    .add(new D(Material.MAGENTA_DYE, DYE_COUNT))
                    .add(new D(Material.LIGHT_GRAY_DYE, DYE_COUNT))
                    .add(new D(Material.BLACK_DYE, DYE_COUNT))
                    .add(new D(Material.RED_DYE, DYE_COUNT))
                    .add(new D(Material.YELLOW_DYE, DYE_COUNT))
                    .add(new D(Material.GREEN_DYE, DYE_COUNT))
                    .add(new D(Material.LIGHT_BLUE_DYE, DYE_COUNT))
                    .add(new D(Material.PURPLE_DYE, DYE_COUNT))
                    .add(new D(Material.PINK_DYE, DYE_COUNT))
                    .build())
            // # 86
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.DIAMOND_PICKAXE, 1, diamondToolConfig))
                    .build())
            // # ??? can't count due to an overlap with Mineshaft
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.SPLASH_POTION, 3, damageSplashPotionConfig))
                    .add(new D(Material.SPLASH_POTION, 3, healthSplashPotionConfig))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.TRIDENT, 1, tridentConfig))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.CROSSBOW, 1, crossbowConfig))
                    .build())
            .add(ImmutableList.<D>builder()
                    .add(new D(Material.DIAMOND_SWORD, 1, diamondSwordConfig))
                    .build())
            .build();

    protected boolean populateAppropriateChunk(final Chunk chunk, final PopulationHistory history) {
        if (history.contains(conflictingPopulatorName)) {
            // Don't overlap with other structures
            return false;
        }

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

                        return false;
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
        if(floorY + 5 > roofY - 1) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s[%s] can't be populated: %d floor and %d roof",
                        getName(), format(chunk), floorY, roofY));
            }

            return false;
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

                return false;
            }


            ladderBase = topMaterial;
        }

        // We need to start from the layer above the floor and finish before the roof
        box(Material.AIR,
                0, floorY + 1, 0,
                ROOM_SIZE - 1, floorY + 2, ROOM_SIZE - 1);

        box(Material.AIR,
                1, floorY + 4, 1,
                ROOM_SIZE - 1, floorY + 5, ROOM_SIZE - 1);

        box(Material.AIR,
                0, floorY + 4, 2,
                0, floorY + 5, ROOM_SIZE - 1);

        box(Material.AIR,
                2, floorY + 4, 0,
                ROOM_SIZE - 1, floorY + 5, 0);

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
        for(int y = floorY + 1; y <= floorY + 3; y++) {
            vc.set(ROOM_SIZE - 1, y, ROOM_SIZE - 1, Material.LADDER, BlockFace.NORTH);
        }

        {
            final CreatureSpawner spawner
                    = (CreatureSpawner) vc.set(ROOM_SIZE / 2, floorY + 4, ROOM_SIZE / 2, Material.SPAWNER).getState();

            // Spawn a lot of Creepers, but only when the player is close.
            spawner.setMaxNearbyEntities(8); // Default: 16
            spawner.setMinSpawnDelay(20); // Default: 200
            spawner.setMaxSpawnDelay(80); // Default: 800
            spawner.setSpawnCount(8); // Default: 4
            // spawner.setDelay(-1);
            spawner.setSpawnedType(EntityType.CREEPER);

            spawner.update();
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
            vc.set(0, floorY + 4, 1, Material.BEDROCK);
            vc.set(1, floorY + 4, 0, Material.BEDROCK);
            // Vertical box
            vc.set(1, floorY + 5, 1, Material.BEDROCK);
            vc.set(1, floorY + 3, 1, Material.BEDROCK);

            final Block chest = vc.set(1, floorY + 4, 1, Material.CHEST);
            final Inventory inventory = ((Chest) chest.getState()).getInventory();

            final long code = chunk.getX() + chunk.getZ();
            final int index = (int)(Math.abs(HashUtils.getHash(code)) % chestLootTable.size());

            int i = 0;
            for(D d : chestLootTable.get(index)) {
                int count = d.getCount();
                while(count > 0) {
                    final int amount = Math.min(d.getMaterial().getMaxStackSize(), count);
                    final ItemStack itemStack = new ItemStack(d.getMaterial(), amount);

                    d.applyItemConfig(itemStack);

                    inventory.setItem(i, itemStack);

                    if (customLogger.isDebugMode()) {
                        // WARNING: ConsistencyReference#1
                        customLogger.debug(String.format("%s item #%d %s set to %d",
                                format(chest), i, d.getMaterial(), amount));
                    }

                    count -= amount;
                    i++;
                }
            }
        }

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s[%s] populated", getName(), format(chunk)));
        }

        return true;
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

    private void box(final Material material,
                     final int x1, final int y1, final int z1,
                     final int x2, final int y2, final int z2) {

        final int rx1 = Math.min(x1, x2);
        final int rx2 = Math.max(x1, x2);
        final int ry1 = Math.min(y1, y2);
        final int ry2 = Math.max(y1, y2);
        final int rz1 = Math.min(z1, z2);
        final int rz2 = Math.max(z1, z2);

        for(int x = rx1; x <= rx2; x++) {
            for(int y = ry1; y <= ry2; y++) {
                for(int z = rz1; z <= rz2; z++) {
                    vc.set(x, y, z, material);
                }
            }
        }
    }
}
