package ru.citiesandempires.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
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
        Player p = e.getPlayer();
        if (!canBreakOrPlace(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cЭто чужая территория!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!canBreakOrPlace(p, e.getBlock().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cЭто чужая территория!");
        }
    }

    // Запрет выливания жидкости (ведро воды, лавы, рыбы)
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        if (!canBreakOrPlace(p, e.getBlockClicked().getRelative(e.getBlockFace()).getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cНельзя разливать жидкости на чужой территории!");
        }
    }

    // Запрет забора жидкости (зачерпывание) – на всякий случай
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        if (!canBreakOrPlace(p, e.getBlockClicked().getChunk())) {
            e.setCancelled(true);
            p.sendMessage("§cНельзя забирать жидкости на чужой территории!");
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

    private boolean canBreakOrPlace(Player player, Chunk chunk) {
        // Если чанк не захвачен – дикая земля, можно всем
        if (!townManager.isClaimed(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())) {
            return true;
        }
        // Иначе чанк городской – проверяем членство
        return townManager.canBuild(player, chunk);
    }
}