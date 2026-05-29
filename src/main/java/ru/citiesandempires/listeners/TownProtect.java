package ru.citiesandempires.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        if (!canBreakOrPlace(e.getPlayer(), e.getBlock().getChunk())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cЭто чужая территория!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!canBreakOrPlace(e.getPlayer(), e.getBlock().getChunk())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cЭто чужая территория!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Chunk from = e.getFrom().getChunk();
        Chunk to = e.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        Player p = e.getPlayer();
        String townName = townManager.getTownNameAt(to.getWorld().getName(), to.getX(), to.getZ());
        String message;
        if (townName != null) {
            message = "§aГородская земля §7(§f" + townName + "§7)";
        } else {
            message = "§7Дикая земля";
        }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    /**
     * Возвращает true, если игрок может ломать/ставить блоки в этом чанке.
     * Разрешает всегда на диких землях.
     */
    private boolean canBreakOrPlace(Player player, Chunk chunk) {
        // Дикие земли – можно всем
        if (!townManager.isClaimed(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())) {
            return true;
        }
        // Городская земля – только своим
        return townManager.canBuild(player, chunk);
    }
}