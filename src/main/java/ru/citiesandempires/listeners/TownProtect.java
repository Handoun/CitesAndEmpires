package ru.citiesandempires.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import ru.citiesandempires.CitiesAndEmpires;

public class TownProtect implements Listener {
    private final CitiesAndEmpires plugin;

    public TownProtect(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getTownManager().canBuild(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cЭто чужая территория!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getTownManager().canBuild(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cЭто чужая территория!");
        }
    }
}