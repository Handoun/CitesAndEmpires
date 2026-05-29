package ru.citiesandempires.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.citiesandempires.CitiesAndEmpires;
import java.util.*;

public class BuildsGUI {
    private final CitiesAndEmpires plugin;
    private final int townId;

    public BuildsGUI(CitiesAndEmpires plugin, int townId) {
        this.plugin = plugin;
        this.townId = townId;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Городские постройки");

        addBuildingItem(inv, "security", Material.IRON_SWORD, "Институт безопасности");
        addBuildingItem(inv, "hospital", Material.GOLDEN_APPLE, "Военный госпиталь");
        addBuildingItem(inv, "school", Material.BOOK, "Школа");
        addBuildingItem(inv, "labor", Material.ANVIL, "Биржа труда");

        player.openInventory(inv);
    }

    private void addBuildingItem(Inventory inv, String buildingKey, Material mat, String name) {
        int level = plugin.getBuildingManager().getBuildingLevel(townId, buildingKey);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("§7Уровень: §e" + level, "§7Клик для прокачки"));
        item.setItemMeta(meta);
        inv.addItem(item);
    }
}