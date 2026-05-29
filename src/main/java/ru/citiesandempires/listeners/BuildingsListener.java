package ru.citiesandempires.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.citiesandempires.CitiesAndEmpires;

public class BuildingsListener implements Listener {
    private final CitiesAndEmpires plugin;

    public BuildingsListener(CitiesAndEmpires plugin) { this.plugin = plugin; }

    @EventHandler
    public void onGUIClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§6Городские постройки")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            Player player = (Player) e.getWhoClicked();
            player.sendMessage("§eВыберите здание для прокачки (заготовка).");
        }
    }
}
