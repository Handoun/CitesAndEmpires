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

    @EventHandler(priority = EventPriority.HIGH) // после привата
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        int townId = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return; // не в городе – без ограничений

        Material block = e.getBlock().getType();
        int requiredCentury = CenturyGUI.getRequiredCenturyForMining(block);
        if (requiredCentury <= 1) return; // всегда доступно

        int currentCentury = plugin.getTownManager().getCentury(townId);
        if (currentCentury < requiredCentury) {
            e.setCancelled(true);
            player.sendMessage("§cВаш город не достиг " + CenturyGUI.getCenturyName(requiredCentury) + ". Добыча этого ресурса запрещена.");
        }
    }
}