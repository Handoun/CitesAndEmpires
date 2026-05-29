package ru.citiesandempires.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.CraftItemEvent;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.gui.CenturyGUI;

public class CraftListener implements Listener {
    private final CitiesAndEmpires plugin;

    public CraftListener(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        int townId = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) return; // не в городе — без ограничений

        int currentCentury = plugin.getTownManager().getCentury(townId);
        Material result = e.getRecipe().getResult().getType();
        int required = CenturyGUI.getRequiredCentury(result);
        if (currentCentury < required) {
            e.setCancelled(true);
            player.sendMessage("§cНеобходим век: " + CenturyGUI.getCenturyName(required));
        }
    }
}