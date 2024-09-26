package com.gmail.uprial.railnet.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigReaderMaterial {
    public static Material getMaterial(FileConfiguration config, String key, String title) throws InvalidConfigException {
        String strMaterial = config.getString(key);
        if (strMaterial == null) {
            throw new InvalidConfigException(String.format("Empty %s", title));
        } else {
            Material tmpMaterial = Material.getMaterial(strMaterial);
            if (tmpMaterial == null) {
                throw new InvalidConfigException(String.format("Unknown %s '%s'", title, strMaterial));
            } else {
                return tmpMaterial;
            }
        }
    }
}
