package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import com.gmail.uprial.secretrooms.populator.ContentSeed;
import com.gmail.uprial.secretrooms.populator.ItemConfig;
import com.gmail.uprial.secretrooms.populator.SpawnerHelper;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.List;
import java.util.Map;

import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_BLOCK;
import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_SPACE;

public class EndMansionBase extends EndMansionChunk {
    VirtualChunk vc;

    EndMansionBase(final BlockFace blockFace) {
        super(blockFace);
    }

    private final Map<Material, ItemConfig> chestLootTable
            = ImmutableMap.<Material, ItemConfig>builder()
            .put(Material.DIAMOND_CHESTPLATE, new ItemConfig()
//diamond_chestplate[glider={},rarity=epic,lore=[[{"text":"Lets you fly like an Elytra","italic":false}]],minecraft:trim={material:"minecraft:resin",pattern:"minecraft:silence"}]
                    .rarity(ItemRarity.EPIC)
                    .lore(List.of("Lets you fly like an Elytra"))
                    .glider(true)
                    .trim(TrimMaterial.RESIN, TrimPattern.SILENCE))
            .put(Material.BOW, new ItemConfig()
//bow[rarity=epic,lore=[[{"text":"Combines two impossibilities","italic":false}]],minecraft:enchantments={"minecraft:power":10,"minecraft:infinity":1,"minecraft:mending":1}]
                    .rarity(ItemRarity.EPIC)
                    .lore(List.of("Combines two impossibilities"))
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
            int y = vc.getMinHeight() - 1;

            for (int i = 0; i <= 3; i++) {
                y++;
                circle(y, i * 2, i * 2 + 1, END_MANSION_BLOCK);
            }

            for (int i = 0; i < 3; i ++) {
                y++;
                circle(y, 7, 7, END_MANSION_SPACE);
            }

            for (int i = 3; i >= 0; i--) {
                y++;
                circle(y, i * 2, i * 2 + 1, END_MANSION_BLOCK);
            }
        }

        {
            int y = vc.getMinHeight() + 3;
            circle(y, 0, 1, END_MANSION_BLOCK);
        }

        {
            int y = vc.getMinHeight() + 7;

            /*
                Spawn all the most annoying mobs in one place, excluding bosses.

                The spawners are not symmetrical to prevent overcrowding.
             */
            spawner(3, y, 12, EntityType.BLAZE);
            spawner(12, y, 3, EntityType.SHULKER);
            spawner(3, y, 3, EntityType.CREEPER);
            spawner(12, y, 12, EntityType.SKELETON);
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
                vc.set(x, y, z, END_MANSION_SPACE);
            }
        }
    }

    private void spawner(final int x, final int y, final int z, final EntityType entityType) {
        for (int i = -1; i <= +1; i += 2) {
            // Vertical
            vc.set(x, y + i, z, END_MANSION_BLOCK);
            // Horizontal
            vc.set(x + i, y, z, END_MANSION_BLOCK);
            vc.set(x, y, z + i, END_MANSION_BLOCK);
        }
        new SpawnerHelper().set(vc.get(x, y, z), entityType);
    }

    @Override
    public String toString() {
        return "Base";
    }
}