package ru.citiesandempires.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;
import java.sql.*;
import java.util.*;

public class BuildingManager {
    private final CitiesAndEmpires plugin;
    public static final Map<String, String> BUILDING_NAMES = Map.of(
        "security", "Институт безопасности",
        "hospital", "Военный госпиталь",
        "school", "Школа",
        "labor", "Биржа труда"
    );

    public BuildingManager(CitiesAndEmpires plugin) { this.plugin = plugin; }

    public int getBuildingLevel(int townId, String building) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT level FROM town_buildings WHERE town_id=? AND building_name=?")) {
            ps.setInt(1, townId);
            ps.setString(2, building);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("level");
        } catch (SQLException e) { }
        return 0;
    }

    public boolean upgradeBuilding(Player player, int townId, String building, int costStone, int costPlanks) {
        if (player.getInventory().contains(Material.COBBLESTONE, costStone) &&
            player.getInventory().contains(Material.OAK_PLANKS, costPlanks)) {
            player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(Material.COBBLESTONE, costStone));
            player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(Material.OAK_PLANKS, costPlanks));
            setBuildingLevel(townId, building, getBuildingLevel(townId, building) + 1);
            return true;
        }
        return false;
    }

    private void setBuildingLevel(int townId, String building, int level) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "REPLACE INTO town_buildings (town_id, building_name, level) VALUES (?, ?, ?)")) {
            ps.setInt(1, townId);
            ps.setString(2, building);
            ps.setInt(3, level);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
