package com.gmail.uprial.railnet.populator.railway.schema;

import org.bukkit.generator.structure.Structure;

// Currently, this enum is needed for debugging and testing purposes only.
public enum StructureEnum {
    PILLAGER_OUTPOST(Structure.PILLAGER_OUTPOST),
    MINESHAFT(Structure.MINESHAFT),
    MINESHAFT_MESA(Structure.MINESHAFT_MESA),
    MANSION(Structure.MANSION),
    JUNGLE_PYRAMID(Structure.JUNGLE_PYRAMID),
    DESERT_PYRAMID(Structure.DESERT_PYRAMID),
    IGLOO(Structure.IGLOO),
    SHIPWRECK(Structure.SHIPWRECK),
    SHIPWRECK_BEACHED(Structure.SHIPWRECK_BEACHED),
    SWAMP_HUT(Structure.SWAMP_HUT),
    STRONGHOLD(Structure.STRONGHOLD),
    MONUMENT(Structure.MONUMENT),
    OCEAN_RUIN_COLD(Structure.OCEAN_RUIN_COLD),
    OCEAN_RUIN_WARM(Structure.OCEAN_RUIN_WARM),
    FORTRESS(Structure.FORTRESS),
    NETHER_FOSSIL(Structure.NETHER_FOSSIL),
    END_CITY(Structure.END_CITY),
    BURIED_TREASURE(Structure.BURIED_TREASURE),
    BASTION_REMNANT(Structure.BASTION_REMNANT),
    VILLAGE_PLAINS(Structure.VILLAGE_PLAINS),
    VILLAGE_DESERT(Structure.VILLAGE_DESERT),
    VILLAGE_SAVANNA(Structure.VILLAGE_SAVANNA),
    VILLAGE_SNOWY(Structure.VILLAGE_SNOWY),
    VILLAGE_TAIGA(Structure.VILLAGE_TAIGA),
    RUINED_PORTAL(Structure.RUINED_PORTAL),
    RUINED_PORTAL_DESERT(Structure.RUINED_PORTAL_DESERT),
    RUINED_PORTAL_JUNGLE(Structure.RUINED_PORTAL_JUNGLE),
    RUINED_PORTAL_SWAMP(Structure.RUINED_PORTAL_SWAMP),
    RUINED_PORTAL_MOUNTAIN(Structure.RUINED_PORTAL_MOUNTAIN),
    RUINED_PORTAL_OCEAN(Structure.RUINED_PORTAL_OCEAN),
    RUINED_PORTAL_NETHER(Structure.RUINED_PORTAL_NETHER),
    ANCIENT_CITY(Structure.ANCIENT_CITY),
    TRAIL_RUINS(Structure.TRAIL_RUINS),
    TRIAL_CHAMBERS(Structure.TRIAL_CHAMBERS);

    private final Structure structure;

    StructureEnum(final Structure structure) {
        this.structure = structure;
    }

    public Structure getStructure() {
        return structure;
    }
}
