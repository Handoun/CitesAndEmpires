package ru.citiesandempires.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.citiesandempires.CitiesAndEmpires;
import java.sql.*;
import java.util.*;

public class TownManager {
    private final CitiesAndEmpires plugin;

    public TownManager(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    public boolean createTown(Player player, String name) {
        if (!hasCreationItems(player)) {
            player.sendMessage("§cНе хватает ресурсов: 128 булыжника, 128 дуб. досок, 8 угля.");
            return false;
        }
        removeCreationItems(player);

        String uuid = player.getUniqueId().toString();
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO towns (name, mayor_uuid) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, uuid);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int townId = rs.getInt(1);
                addMember(townId, uuid, "Мэр");
                player.sendMessage("§aГород " + name + " создан!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cОшибка создания города.");
        }
        return false;
    }

    private boolean hasCreationItems(Player p) {
        return p.getInventory().contains(Material.COBBLESTONE, 128) &&
               p.getInventory().contains(Material.OAK_PLANKS, 128) &&
               p.getInventory().contains(Material.COAL, 8);
    }

    private void removeCreationItems(Player p) {
        removeItem(p, Material.COBBLESTONE, 128);
        removeItem(p, Material.OAK_PLANKS, 128);
        removeItem(p, Material.COAL, 8);
    }

    private void removeItem(Player p, Material mat, int amount) {
        p.getInventory().removeItem(new ItemStack(mat, amount));
    }

    private void addMember(int townId, String uuid, String rank) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR IGNORE INTO town_members (town_id, uuid, rank) VALUES (?, ?, ?)")) {
            ps.setInt(1, townId);
            ps.setString(2, uuid);
            ps.setString(3, rank);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean claimChunk(Player player, int radius) {
        String uuid = player.getUniqueId().toString();
        int townId = getTownIdByMember(uuid);
        if (townId == 0) { player.sendMessage("§cВы не состоите в городе."); return false; }
        World world = player.getWorld();
        Chunk center = player.getLocation().getChunk();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk c = world.getChunkAt(center.getX() + dx, center.getZ() + dz);
                if (!isClaimed(world.getName(), c.getX(), c.getZ())) {
                    claimSingleChunk(townId, world.getName(), c.getX(), c.getZ());
                }
            }
        }
        player.sendMessage("§aТерритория захвачена.");
        return true;
    }

    public boolean claimSingleChunk(int townId, String world, int x, int z) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO town_chunks (town_id, world, x, z) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, townId);
            ps.setString(2, world);
            ps.setInt(3, x);
            ps.setInt(4, z);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public boolean isClaimed(String world, int x, int z) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM town_chunks WHERE world=? AND x=? AND z=?")) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, z);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public int getTownIdByMember(String uuid) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT town_id FROM town_members WHERE uuid=?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("town_id");
        } catch (SQLException e) { }
        return 0;
    }

    public String getTownName(int townId) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM towns WHERE id=?")) {
            ps.setInt(1, townId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) { }
        return null;
    }
}
