package ru.citiesandempires.data;

import org.bukkit.configuration.file.FileConfiguration;
import ru.citiesandempires.CitiesAndEmpires;

public class Config {
    private final CitiesAndEmpires plugin;
    private FileConfiguration config;

    public Config(CitiesAndEmpires plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getCreationCost(String material) {
        return config.getInt("town.creation-cost." + material.toLowerCase().replace('_','-'), 0);
    }

    public double getDailyTaxPerChunk() {
        return config.getDouble("town.daily-tax-per-chunk", 10.0);
    }

    public int getMaxBuildingLevel(String building) {
        return config.getInt("buildings.levels." + building, 3);
    }
}
