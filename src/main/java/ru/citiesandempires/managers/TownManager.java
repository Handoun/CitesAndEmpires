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

    // ========== СОЗДАНИЕ ГОРОДА ==========
    public boolean createTown(Player player, String name) {
        if (!hasCreationItems(player)) {
            player.sendMessage("§cНе хватает ресурсов: 128 булыжника, 128 дуб. досок, 8 угля.");
            return false;
        }
        removeCreationItems(player);
        String uuid = player.getUniqueId().toString();
        if (getTownIdByMember(uuid) != 0) {
            player.sendMessage("§cВы уже состоите в городе. Покиньте его перед созданием нового.");
            return false;
        }
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO towns (name, mayor_uuid) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, uuid);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int townId = rs.getInt(1);
                addMember(townId, uuid, "Мэр");
                player.sendMessage("§aГород " + name + " создан! Вы стали мэром.");
                return true;
            } else {
                player.sendMessage("§cНе удалось получить ID города.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cОшибка базы данных при создании города.");
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
                     "INSERT OR REPLACE INTO town_members (town_id, uuid, rank) VALUES (?, ?, ?)")) {
            ps.setInt(1, townId);
            ps.setString(2, uuid);
            ps.setString(3, rank);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean removeMember(int townId, String uuid) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM town_members WHERE town_id=? AND uuid=?")) {
            ps.setInt(1, townId);
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== ЗАХВАТ ЧАНКОВ ==========
    public boolean claimChunk(Player player, int radius) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) {
            player.sendMessage("§cВы не состоите в городе.");
            return false;
        }
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
        } catch (SQLException e) {
            return false;
        }
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
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean unclaimChunk(Player player) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) {
            player.sendMessage("§cВы не состоите в городе.");
            return false;
        }
        Chunk chunk = player.getLocation().getChunk();
        return unclaimSingleChunk(townId, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public boolean unclaimSingleChunk(int townId, String world, int x, int z) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM town_chunks WHERE town_id=? AND world=? AND x=? AND z=?")) {
            ps.setInt(1, townId);
            ps.setString(2, world);
            ps.setInt(3, x);
            ps.setInt(4, z);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean unclaimAll(int townId) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM town_chunks WHERE town_id=?")) {
            ps.setInt(1, townId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ========== ИНФОРМАЦИЯ ==========
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

    public String getRank(Player player) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return null;
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT rank FROM town_members WHERE town_id=? AND uuid=?")) {
            ps.setInt(1, townId);
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("rank");
        } catch (SQLException e) { }
        return "Житель";
    }

    public boolean isMayor(Player player) {
        return "Мэр".equals(getRank(player));
    }

    public boolean canBuild(Player player, Chunk chunk) {
        if (player.hasPermission("cities.bypass")) return true;
        String world = chunk.getWorld().getName();
        int x = chunk.getX(), z = chunk.getZ();
        if (!isClaimed(world, x, z)) return true;
        int chunkTownId = getTownIdAt(world, x, z);
        if (chunkTownId == 0) return true;
        int playerTownId = getTownIdByMember(player.getUniqueId().toString());
        return playerTownId == chunkTownId;
    }

    // НОВОЕ: получение названия города по координатам (для ActionBar)
    public String getTownNameAt(String world, int x, int z) {
        int townId = getTownIdAt(world, x, z);
        if (townId == 0) return null;
        return getTownName(townId);
    }

    private int getTownIdAt(String world, int x, int z) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT town_id FROM town_chunks WHERE world=? AND x=? AND z=?")) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, z);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("town_id");
        } catch (SQLException e) { }
        return 0;
    }

    // ========== УХОД / КИК / ПРИГЛАШЕНИЕ / ВСТУПЛЕНИЕ ==========
    public boolean leaveTown(Player player) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return false;
        String rank = getRank(player);
        if ("Мэр".equals(rank)) {
            player.sendMessage("§cМэр не может покинуть город. Передайте должность или удалите город.");
            return false;
        }
        removeMember(townId, player.getUniqueId().toString());
        player.sendMessage("§aВы покинули город.");
        return true;
    }

    public boolean kickPlayer(Player kicker, String targetName) {
        int townId = getTownIdByMember(kicker.getUniqueId().toString());
        if (townId == 0) return false;
        String kickerRank = getRank(kicker);
        if (!"Мэр".equals(kickerRank) && !"Советник".equals(kickerRank)) {
            kicker.sendMessage("§cНедостаточно прав для кика.");
            return false;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            kicker.sendMessage("§cИгрок не найден.");
            return false;
        }
        if (getTownIdByMember(target.getUniqueId().toString()) != townId) {
            kicker.sendMessage("§cИгрок не состоит в вашем городе.");
            return false;
        }
        if ("Мэр".equals(getRank(target))) {
            kicker.sendMessage("§cНельзя кикнуть мэра.");
            return false;
        }
        removeMember(townId, target.getUniqueId().toString());
        kicker.sendMessage("§aИгрок " + targetName + " изгнан из города.");
        target.sendMessage("§cВас исключили из города.");
        return true;
    }

    public boolean addPlayer(Player inviter, String targetName) {
        int townId = getTownIdByMember(inviter.getUniqueId().toString());
        if (townId == 0) return false;
        String rank = getRank(inviter);
        if (!"Мэр".equals(rank) && !"Советник".equals(rank) && !"Рекрутер".equals(rank)) {
            inviter.sendMessage("§cУ вас нет прав приглашать игроков.");
            return false;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            inviter.sendMessage("§cИгрок не в сети.");
            return false;
        }
        if (getTownIdByMember(target.getUniqueId().toString()) != 0) {
            inviter.sendMessage("§cИгрок уже состоит в другом городе.");
            return false;
        }
        addMember(townId, target.getUniqueId().toString(), "Житель");
        inviter.sendMessage("§aИгрок " + targetName + " добавлен в город.");
        target.sendMessage("§aВас добавили в город " + getTownName(townId) + ".");
        return true;
    }

    public boolean askForInvite(Player player) {
        for (Player mayor : plugin.getServer().getOnlinePlayers()) {
            if (isMayor(mayor)) {
                int tId = getTownIdByMember(mayor.getUniqueId().toString());
                if (isTownOpen(tId)) {
                    mayor.sendMessage("§eИгрок " + player.getName() + " ищет город для вступления.");
                }
            }
        }
        player.sendMessage("§aЗапрос отправлен всем городам с открытым набором.");
        return true;
    }

    public boolean joinTown(Player player, String townName) {
        if (getTownIdByMember(player.getUniqueId().toString()) != 0) {
            player.sendMessage("§cВы уже в городе.");
            return false;
        }
        int townId = getTownIdByName(townName);
        if (townId == 0) {
            player.sendMessage("§cГород не найден.");
            return false;
        }
        if (!isTownOpen(townId)) {
            player.sendMessage("§cГород закрыт для свободного вступления.");
            return false;
        }
        addMember(townId, player.getUniqueId().toString(), "Житель");
        player.sendMessage("§aВы вступили в город " + townName + ".");
        return true;
    }

    private int getTownIdByName(String name) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM towns WHERE name=?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { }
        return 0;
    }

    private boolean isTownOpen(int townId) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT open_entry FROM towns WHERE id=?")) {
            ps.setInt(1, townId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("open_entry") == 1;
        } catch (SQLException e) { }
        return false;
    }

    public boolean toggleOpen(Player player) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return false;
        if (!"Мэр".equals(getRank(player)) && !"Советник".equals(getRank(player))) {
            player.sendMessage("§cНедостаточно прав.");
            return false;
        }
        boolean current = isTownOpen(townId);
        setTownOpen(townId, !current);
        player.sendMessage(current ? "§aГород закрыт для свободного входа." : "§aГород теперь открыт для всех!");
        return true;
    }

    private void setTownOpen(int townId, boolean open) {
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE towns SET open_entry=? WHERE id=?")) {
            ps.setInt(1, open ? 1 : 0);
            ps.setInt(2, townId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ========== РАНГИ ==========
    public boolean setRank(Player granter, String targetName, String rank) {
        int townId = getTownIdByMember(granter.getUniqueId().toString());
        if (townId == 0) return false;
        String granterRank = getRank(granter);
        if (!"Мэр".equals(granterRank) && !"Советник".equals(granterRank)) {
            granter.sendMessage("§cНедостаточно прав для назначения рангов.");
            return false;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            granter.sendMessage("§cИгрок не в сети.");
            return false;
        }
        if (getTownIdByMember(target.getUniqueId().toString()) != townId) {
            granter.sendMessage("§cИгрок не в вашем городе.");
            return false;
        }
        if ("Мэр".equals(rank)) {
            granter.sendMessage("§cНельзя назначить мэра через эту команду.");
            return false;
        }
        if ("Советник".equals(granterRank) && ("Советник".equals(rank) || "Мэр".equals(rank))) {
            granter.sendMessage("§cВы не можете назначить этот ранг.");
            return false;
        }
        addMember(townId, target.getUniqueId().toString(), rank);
        granter.sendMessage("§aИгрок " + targetName + " теперь " + rank + ".");
        target.sendMessage("§aВаш ранг изменён на " + rank + ".");
        return true;
    }

    // ========== SPAWN ==========
    public boolean spawnTown(Player player) {
        int townId = getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return false;
        String worldName = player.getWorld().getName();
        int cx = 0, cz = 0;
        try (Connection conn = plugin.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT world, x, z FROM town_chunks WHERE town_id=? LIMIT 1")) {
            ps.setInt(1, townId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                worldName = rs.getString("world");
                cx = rs.getInt("x");
                cz = rs.getInt("z");
            }
        } catch (SQLException e) { }
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) world = player.getWorld();
        Location loc = new Location(world, cx * 16 + 8, world.getHighestBlockYAt(cx * 16 + 8, cz * 16 + 8) + 1, cz * 16 + 8);
        player.teleport(loc);
        player.sendMessage("§aВы телепортированы на спавн города.");
        return true;
    }

    public boolean isMemberOfTown(Player player, int townId) {
        return getTownIdByMember(player.getUniqueId().toString()) == townId;
    }
}