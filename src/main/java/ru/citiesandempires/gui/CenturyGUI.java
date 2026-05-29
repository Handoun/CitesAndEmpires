package ru.citiesandempires.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.citiesandempires.CitiesAndEmpires;
import java.util.*;

public class CenturyGUI {
    private final CitiesAndEmpires plugin;
    private final int townId;

    // Иконки для каждого века (индексы 1..8)
    private static final Material[] ICONS = {
        Material.STONE,          // 1 - Примитивный
        Material.COBBLESTONE,    // 2 - Каменный
        Material.IRON_INGOT,     // 3 - Железный
        Material.GOLD_INGOT,     // 4 - Золотой
        Material.DIAMOND,        // 5 - Алмазный
        Material.EMERALD,        // 6 - Изумрудный
        Material.NETHERITE_INGOT,// 7 - Незеритовый
        Material.BEACON          // 8 - Энергетический
    };

    private static final String[] NAMES = {
        "Примитивный век",
        "Каменный век",
        "Железный век",
        "Золотой век",
        "Алмазный век",
        "Изумрудный век",
        "Незеритовый век",
        "Энергетический век"
    };

    // Требования ресурсов для перехода на следующий век (ключ: номер следующего века)
    private static final Map<Integer, Map<Material, Integer>> COSTS = new HashMap<>();
    static {
        Map<Material, Integer> to2 = new HashMap<>();
        to2.put(Material.COBBLESTONE, 64);
        to2.put(Material.OAK_PLANKS, 32);
        COSTS.put(2, to2);

        Map<Material, Integer> to3 = new HashMap<>();
        to3.put(Material.IRON_INGOT, 32);
        to3.put(Material.COAL, 16);
        COSTS.put(3, to3);

        Map<Material, Integer> to4 = new HashMap<>();
        to4.put(Material.GOLD_INGOT, 32);
        to4.put(Material.REDSTONE, 16);
        COSTS.put(4, to4);

        Map<Material, Integer> to5 = new HashMap<>();
        to5.put(Material.DIAMOND, 16);
        to5.put(Material.OBSIDIAN, 8);
        COSTS.put(5, to5);

        Map<Material, Integer> to6 = new HashMap<>();
        to6.put(Material.EMERALD, 16);
        to6.put(Material.LAPIS_LAZULI, 16);
        COSTS.put(6, to6);

        Map<Material, Integer> to7 = new HashMap<>();
        to7.put(Material.NETHERITE_INGOT, 4);
        to7.put(Material.ANCIENT_DEBRIS, 8);
        COSTS.put(7, to7);

        Map<Material, Integer> to8 = new HashMap<>();
        to8.put(Material.BEACON, 1);
        to8.put(Material.NETHER_STAR, 1);
        COSTS.put(8, to8);
    }

    // Минимальный век для крафта предмета (для CraftListener)
    public static int getRequiredCenturyForCraft(Material mat) {
        switch (mat) {
            case IRON_PICKAXE: case IRON_AXE: case IRON_SWORD: case IRON_HOE: case IRON_SHOVEL:
            case IRON_HELMET: case IRON_CHESTPLATE: case IRON_LEGGINGS: case IRON_BOOTS:
            case IRON_INGOT: case IRON_BLOCK:
                return 3;  // Железный век
            case GOLDEN_PICKAXE: case GOLDEN_AXE: case GOLDEN_SWORD: case GOLDEN_HOE: case GOLDEN_SHOVEL:
            case GOLDEN_HELMET: case GOLDEN_CHESTPLATE: case GOLDEN_LEGGINGS: case GOLDEN_BOOTS:
            case GOLD_INGOT: case GOLD_BLOCK:
                return 4;  // Золотой век
            case DIAMOND_PICKAXE: case DIAMOND_AXE: case DIAMOND_SWORD: case DIAMOND_HOE: case DIAMOND_SHOVEL:
            case DIAMOND_HELMET: case DIAMOND_CHESTPLATE: case DIAMOND_LEGGINGS: case DIAMOND_BOOTS:
            case DIAMOND: case DIAMOND_BLOCK:
                return 5;  // Алмазный век
            case NETHERITE_PICKAXE: case NETHERITE_AXE: case NETHERITE_SWORD: case NETHERITE_HOE: case NETHERITE_SHOVEL:
            case NETHERITE_HELMET: case NETHERITE_CHESTPLATE: case NETHERITE_LEGGINGS: case NETHERITE_BOOTS:
            case NETHERITE_INGOT: case NETHERITE_BLOCK:
                return 7;  // Незеритовый век
            default:
                return 1;  // Остальное доступно с примитивного века
        }
    }

    // Минимальный век для добычи руды (для MiningListener)
    public static int getRequiredCenturyForMining(Material ore) {
        switch (ore) {
            case COAL_ORE: case DEEPSLATE_COAL_ORE: return 2;      // Каменный век
            case IRON_ORE: case DEEPSLATE_IRON_ORE: return 3;      // Железный век
            case COPPER_ORE: case DEEPSLATE_COPPER_ORE: return 2;  // Каменный век
            case GOLD_ORE: case DEEPSLATE_GOLD_ORE: return 4;      // Золотой век
            case REDSTONE_ORE: case DEEPSLATE_REDSTONE_ORE: return 4;
            case LAPIS_ORE: case DEEPSLATE_LAPIS_ORE: return 4;
            case DIAMOND_ORE: case DEEPSLATE_DIAMOND_ORE: return 5; // Алмазный век
            case EMERALD_ORE: case DEEPSLATE_EMERALD_ORE: return 6; // Изумрудный век
            case ANCIENT_DEBRIS: return 7;                          // Незеритовый век
            default: return 1; // камень, земля и прочее доступно всегда
        }
    }

    public CenturyGUI(CitiesAndEmpires plugin, int townId) {
        this.plugin = plugin;
        this.townId = townId;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Эпохи города");

        int currentCentury = plugin.getTownManager().getCentury(townId);

        for (int level = 1; level <= 8; level++) {
            boolean unlocked = level <= currentCentury;
            ItemStack item = new ItemStack(ICONS[level-1]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((unlocked ? "§a" : "§c") + NAMES[level-1]);

            List<String> lore = new ArrayList<>();
            if (unlocked) {
                lore.add("§7Статус: §aОткрыт");
            } else {
                lore.add("§7Статус: §cЗакрыт");
                if (level == currentCentury + 1) {
                    lore.add("§eТребования для открытия:");
                    Map<Material, Integer> cost = COSTS.get(level);
                    if (cost != null) {
                        for (Map.Entry<Material, Integer> e : cost.entrySet()) {
                            lore.add("§7- " + e.getKey().toString().toLowerCase() + ": §f" + e.getValue());
                        }
                    }
                    lore.add("§6Кликните, чтобы изучить");
                } else {
                    lore.add("§7Сначала изучите предыдущий век");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(level - 1, item);
        }

        player.openInventory(inv);
    }

    /** Попытаться повысить век до указанного уровня */
    public static boolean tryUpgrade(Player player, int townId, int targetLevel, CitiesAndEmpires plugin) {
        int current = plugin.getTownManager().getCentury(townId);
        if (targetLevel != current + 1) {
            player.sendMessage("§cВы можете открыть только следующий век.");
            return false;
        }
        Map<Material, Integer> cost = COSTS.get(targetLevel);
        if (cost == null) return false;

        // Проверяем склад города
        Inventory storage = plugin.getTownManager().getTownStorage(townId);
        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            if (!storage.contains(entry.getKey(), entry.getValue())) {
                player.sendMessage("§cНедостаточно ресурсов на складе города. Требуется: " +
                        entry.getKey().toString().toLowerCase() + " x" + entry.getValue());
                return false;
            }
        }
        // Списываем ресурсы
        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            storage.removeItem(new ItemStack(entry.getKey(), entry.getValue()));
        }
        plugin.getTownManager().setCentury(townId, targetLevel);
        player.sendMessage("§aВек \"" + NAMES[targetLevel-1] + "\" открыт!");
        return true;
    }

    public static String getCenturyName(int level) {
        if (level < 1 || level > 8) return "Неизвестный век";
        return NAMES[level-1];
    }
}