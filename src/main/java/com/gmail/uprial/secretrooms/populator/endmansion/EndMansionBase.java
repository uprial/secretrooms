package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import com.gmail.uprial.secretrooms.populator.ContentSeed;
import com.gmail.uprial.secretrooms.populator.ItemConfig;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.List;
import java.util.Map;

public class EndMansionBase extends EndMansionChunk {
    VirtualChunk vc;

    EndMansionBase(final BlockFace blockFace) {
        super(blockFace);
    }

    private final Map<Material, ItemConfig> chestLootTable
            = ImmutableMap.<Material, ItemConfig>builder()
            .put(Material.DIAMOND_CHESTPLATE, new ItemConfig()
//diamond_chestplate[glider={},rarity=epic,lore=[[{"text":"It lets you fly like an Elytra","italic":false}]],minecraft:trim={material:"minecraft:resin",pattern:"minecraft:silence"}]
                    .glider(true)
                    .rarity(ItemRarity.EPIC)
                    .lore(List.of("It lets you fly like an Elytra"))
                    .trim(TrimMaterial.RESIN, TrimPattern.SILENCE))
            .put(Material.BOW, new ItemConfig()
//bow[minecraft:enchantments={"minecraft:power":10,"minecraft:infinity":1,"minecraft:mending":1}]
                    /*
                        A potential UNBREAKING(3), PUNCH(2) and FLAME(1)
                        upgrade would cost 18 levels.
                     */
                    // Survival maximum level is 5, here it's 10.
                    .ench(Enchantment.POWER, 10, 10)
                    // Not compatible with INFINITY in survival.
                    .ench(Enchantment.MENDING)
                    // Not compatible with MENDING in survival.
                    .ench(Enchantment.INFINITY))
            .build();

    @Override
    void populate(final VirtualChunk vc) {
        this.vc = vc;

        {
            int y = vc.getMinHeight();

            circle(y, 0, 1, Material.OBSIDIAN);

            for (int i = 1; i <= 3; i++) {
                y++;
                circle(y, i * 2, i * 2 + 1, Material.OBSIDIAN);
            }

            for (int i = 0; i < 3; i ++) {
                y++;
                circle(y, 7, 7, Material.AIR);
            }

            for (int i = 7; i >= 0; i--) {
                y++;
                circle(y, i, i, Material.OBSIDIAN);
            }
        }

        {
            int y = vc.getMinHeight() + 3;
            circle(y, 0, 1, Material.OBSIDIAN);
        }

        {
            int y = vc.getMinHeight() + 7;

            spawner(3, y, 12, EntityType.BLAZE);
            spawner(12, y, 3, EntityType.SHULKER);
            spawner(3, y, 3, EntityType.CREEPER);
            spawner(12, y, 12, EntityType.CREEPER);
        }

        {
            int y = vc.getMinHeight() + 1;

            final Block chest = vc.set(7, y, 7, Material.CHEST);

            final Material material = BlockSeed.valueOf(chest).oneOf(chestLootTable.keySet());
            final ItemStack itemStack = new ItemStack(material);
            chestLootTable.get(material).apply(ContentSeed.valueOf(chest), itemStack);

            ((Chest) chest.getState()).getInventory().setItem(0, itemStack);
        }
    }

    private void circle(final int y, final int r1, final int r2, final Material material) {
        final int nr1 = Math.min(r1, r2);
        final int nr2 = Math.max(r1, r2);

        for(int r = nr1; r <= nr2; r ++) {
            for (int x = 7 - r; x <= 8 + r; x++) {
                vc.set(x, y, 7 - r, material);
                vc.set(x, y, 8 + r, material);
            }
            for (int z = 7 - r + 1; z <= 8 + r - 1; z++) {
                vc.set(7 - r, y, z, material);
                vc.set(8 + r, y, z, material);
            }
        }

        for(int x = 7 - nr1 + 1; x <= 8 + nr1 - 1; x ++) {
            for (int z = 7 - nr1 + 1; z <= 8 + nr1 - 1; z++) {
                vc.set(x, y, z, Material.AIR);
            }
        }
    }

    private void spawner(final int x, final int y, final int z, final EntityType entityType) {
        for (int i = -1; i <= +1; i += 2) {
            // Vertical defence
            vc.set(x, y + i, z, Material.OBSIDIAN);
            // Horizontal defence
            vc.set(x + i, y, z, Material.OBSIDIAN);
            vc.set(x, y, z + i, Material.OBSIDIAN);
        }
        final CreatureSpawner spawner
                = (CreatureSpawner) vc.set(x, y, z, Material.SPAWNER).getState();

        // Spawn fewer entities
        spawner.setMaxNearbyEntities(8); // Default: 16
        spawner.setSpawnCount(2); // Default: 4

        spawner.setSpawnedType(entityType);

        spawner.update();
    }

    @Override
    public String toString() {
        return "Base";
    }
}