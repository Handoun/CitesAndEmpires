package ru.citiesandempires.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.managers.TownManager;

public class TownProtect implements Listener {
    private final CitiesAndEmpires plugin;
    private final TownManager townManager;

    public TownProtect(CitiesAndEmpires plugin) {
        this.plugin = plugin;
        this.townManager = plugin.getTownManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!canBuild(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cВы не можете строить на чужой территории.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!canBuild(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cВы не можете строить на чужой территории.");
        }
    }

    private boolean canBuild(Player player, Chunk chunk) {
        if (player.hasPermission("cities.bypass")) return true;
        String world = chunk.getWorld().getName();
        int x = chunk.getX(), z = chunk.getZ();
        if (!townManager.isClaimed(world, x, z)) return true;
        // здесь будет полная проверка прав
        return false;
    }
}
