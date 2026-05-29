package ru.citiesandempires.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.citiesandempires.CitiesAndEmpires;

public class BuildingsListener implements Listener {
    private final CitiesAndEmpires plugin;

    public BuildingsListener(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§6Городские постройки")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        int townId = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString());
        if (townId == 0) {
            player.closeInventory();
            return;
        }

        Material clicked = e.getCurrentItem().getType();
        String buildingKey = null;
        switch (clicked) {
            case IRON_SWORD: buildingKey = "security"; break;
            case GOLDEN_APPLE: buildingKey = "hospital"; break;
            case BOOK: buildingKey = "school"; break;
            case ANVIL: buildingKey = "labor"; break;
        }
        if (buildingKey == null) return;

        int currentLevel = plugin.getBuildingManager().getBuildingLevel(townId, buildingKey);
        int maxLevel = plugin.getPluginConfig().getMaxBuildingLevel(buildingKey);

        if (currentLevel >= maxLevel) {
            player.sendMessage("§cЗдание уже максимального уровня.");
            return;
        }

        // Стоимость прокачки (можно вынести в конфиг)
        int stoneCost = 64 * (currentLevel + 1);
        int woodCost = 64 * (currentLevel + 1);
        player.sendMessage("§6Для прокачки нужно: §f" + stoneCost + " булыжника, " + woodCost + " досок.");
        // Сама прокачка будет вызываться позже (требует ресурсы из инвентаря)
    }
}