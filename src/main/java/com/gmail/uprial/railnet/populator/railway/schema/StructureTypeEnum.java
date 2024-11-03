package com.gmail.uprial.railnet.populator.railway.schema;

import org.bukkit.generator.structure.StructureType;

// Currently, this enum is needed for debugging and testing purposes only.
public enum StructureTypeEnum {

    BURIED_TREASURE(StructureType.BURIED_TREASURE),
    DESERT_PYRAMID(StructureType.DESERT_PYRAMID),
    END_CITY(StructureType.END_CITY),
    FORTRESS(StructureType.FORTRESS),
    IGLOO(StructureType.IGLOO),
    JIGSAW(StructureType.JIGSAW),
    JUNGLE_TEMPLE(StructureType.JUNGLE_TEMPLE),
    MINESHAFT(StructureType.MINESHAFT),
    NETHER_FOSSIL(StructureType.NETHER_FOSSIL),
    OCEAN_MONUMENT(StructureType.OCEAN_MONUMENT),
    OCEAN_RUIN(StructureType.OCEAN_RUIN),
    RUINED_PORTAL(StructureType.RUINED_PORTAL),
    SHIPWRECK(StructureType.SHIPWRECK),
    STRONGHOLD(StructureType.STRONGHOLD),
    SWAMP_HUT(StructureType.SWAMP_HUT),
    WOODLAND_MANSION(StructureType.WOODLAND_MANSION);

    private final StructureType structureType;

    StructureTypeEnum(final StructureType structureType) {
        this.structureType = structureType;
    }

    public StructureType getStructureType() {
        return structureType;
    }
}