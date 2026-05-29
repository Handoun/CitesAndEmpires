package ru.citiesandempires.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.gui.CenturyGUI;

public class MiningListener implements Listener {
    private final CitiesAndEmpires plugin;

    public MiningListener(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        int townId = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString());
        int currentCentury = townId == 0 ? 1 : plugin.getTownManager().getCentury(townId);

        Material block = e.getBlock().getType();
        int required = CenturyGUI.getRequiredCenturyForMining(block);
        if (required <= 1) return; // примитивный век – всё доступно

        if (currentCentury < required) {
            e.setCancelled(true);
            player.sendMessage("§cВаш век не позволяет добывать это. Требуется: " + CenturyGUI.getCenturyName(required));
        }
    }
}