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
    private final Inventory inv;

    public BuildsGUI(CitiesAndEmpires plugin) {
        this.plugin = plugin;
        inv = Bukkit.createInventory(null, 27, "§6Городские постройки");
        addBuildingItem("security", Material.IRON_SWORD, "Институт безопасности");
        addBuildingItem("hospital", Material.GOLDEN_APPLE, "Военный госпиталь");
        addBuildingItem("school", Material.BOOK, "Школа");
        addBuildingItem("labor", Material.ANVIL, "Биржа труда");
    }

    private void addBuildingItem(String key, Material mat, String display) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(display);
        meta.setLore(List.of("§7Уровень: §e0", "§7Клик для прокачки"));
        item.setItemMeta(meta);
        inv.addItem(item);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }
}
