package ru.citiesandempires;

import org.bukkit.plugin.java.JavaPlugin;
import ru.citiesandempires.commands.*;
import ru.citiesandempires.data.*;
import ru.citiesandempires.listeners.*;
import ru.citiesandempires.managers.*;

public final class CitiesAndEmpires extends JavaPlugin {

    private static CitiesAndEmpires instance;
    private Database database;
    private Config config;
    private TownManager townManager;
    private NationManager nationManager;
    private EconomyManager economyManager;
    private BuildingManager buildingManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        config = new Config(this);
        database = new Database(this);
        database.connect();
        database.createTables();

        economyManager = new EconomyManager(this);
        townManager = new TownManager(this);
        nationManager = new NationManager(this);
        buildingManager = new BuildingManager(this);

        getCommand("town").setExecutor(new TownCmd(this));
        getCommand("nation").setExecutor(new NationCmd(this));
        getCommand("plot").setExecutor(new PlotCmd(this));

        getServer().getPluginManager().registerEvents(new TownProtect(this), this);
        getServer().getPluginManager().registerEvents(new BuildingsListener(this), this);
        getServer().getPluginManager().registerEvents(new WarListener(this), this);

        getLogger().info("CitiesAndEmpires успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (database != null) database.disconnect();
        getLogger().info("CitiesAndEmpires выключен.");
    }

    public static CitiesAndEmpires getInstance() { return instance; }
    public Database getDatabase() { return database; }
    public Config getPluginConfig() { return config; }
    public TownManager getTownManager() { return townManager; }
    public NationManager getNationManager() { return nationManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public BuildingManager getBuildingManager() { return buildingManager; }
}
