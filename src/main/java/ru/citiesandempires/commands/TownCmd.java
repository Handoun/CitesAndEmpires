package ru.citiesandempires.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.gui.BuildsGUI;
import ru.citiesandempires.gui.CenturyGUI;
import java.util.*;

public class TownCmd implements CommandExecutor, TabCompleter {
    private final CitiesAndEmpires plugin;

    public TownCmd(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }
        Player player = (Player) sender;
        int townId = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString());
        boolean inTown = townId != 0;

        if (args.length == 0) {
            showHelp(player, inTown, townId);
            return true;
        }

        String sub = args[0].toLowerCase();

        // Команды, доступные ВНЕ города
        switch (sub) {
            case "new":
                if (args.length < 2) { player.sendMessage("§c/town new <название>"); return true; }
                plugin.getTownManager().createTown(player, args[1]);
                return true;
            case "ask":
                if (inTown) { player.sendMessage("§cВы уже состоите в городе."); return true; }
                plugin.getTownManager().askForInvite(player);
                return true;
            case "join":
                if (args.length < 2) { player.sendMessage("§c/town join <название>"); return true; }
                plugin.getTownManager().joinTown(player, args[1]);
                return true;
        }

        // Остальные команды требуют членства
        if (!inTown) {
            player.sendMessage("§cВы не состоите в городе.");
            return true;
        }

        switch (sub) {
            case "claim":
                int radius = 1;
                if (args.length > 1) {
                    try { radius = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
                }
                plugin.getTownManager().claimChunk(player, radius);
                break;
            case "unclaim":
                if (args.length > 1 && "all".equalsIgnoreCase(args[1])) {
                    if (plugin.getTownManager().isMayor(player)) {
                        plugin.getTownManager().unclaimAll(townId);
                        player.sendMessage("§aВся территория города удалена.");
                    } else {
                        player.sendMessage("§cТолько мэр может снять всю территорию.");
                    }
                } else {
                    if (plugin.getTownManager().unclaimChunk(player))
                        player.sendMessage("§aЧанк больше не принадлежит городу.");
                    else
                        player.sendMessage("§cНе удалось снять чанк.");
                }
                break;
            case "builds":
                new BuildsGUI(plugin, townId).open(player);
                break;
            case "inventory":
            case "inv":
                player.openInventory(plugin.getTownManager().getTownStorage(townId));
                break;
            case "century":
                new CenturyGUI(plugin, townId).open(player);
                break;
            case "deposit":
                if (args.length < 2) { player.sendMessage("§c/town deposit <сумма>"); return true; }
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (plugin.getEconomyManager().depositTown(townId, player, amount))
                        player.sendMessage("§aВнесено в казну: " + amount);
                    else
                        player.sendMessage("§cНе удалось внести (проверьте баланс).");
                } catch (NumberFormatException e) { player.sendMessage("§cНекорректная сумма."); }
                break;
            case "withdraw":
                if (!plugin.getTownManager().isMayor(player)) {
                    player.sendMessage("§cТолько мэр может снимать из казны.");
                    return true;
                }
                if (args.length < 2) { player.sendMessage("§c/town withdraw <сумма>"); return true; }
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (plugin.getEconomyManager().withdrawTown(townId, player, amount))
                        player.sendMessage("§aСнято из казны: " + amount);
                    else
                        player.sendMessage("§cНедостаточно средств в казне.");
                } catch (NumberFormatException e) { player.sendMessage("§cНекорректная сумма."); }
                break;
            case "leave":
                plugin.getTownManager().leaveTown(player);
                break;
            case "kick":
                if (args.length < 2) { player.sendMessage("§c/town kick <ник>"); return true; }
                plugin.getTownManager().kickPlayer(player, args[1]);
                break;
            case "add":
                if (args.length < 2) { player.sendMessage("§c/town add <ник>"); return true; }
                plugin.getTownManager().addPlayer(player, args[1]);
                break;
            case "rank":
                if (args.length < 4 || !"add".equalsIgnoreCase(args[1])) {
                    player.sendMessage("§c/town rank add <ник> <должность>");
                    return true;
                }
                plugin.getTownManager().setRank(player, args[2], args[3]);
                break;
            case "toggle":
                if (args.length < 2) { player.sendMessage("§c/town toggle open"); return true; }
                if ("open".equalsIgnoreCase(args[1])) {
                    plugin.getTownManager().toggleOpen(player);
                }
                break;
            case "spawn":
                plugin.getTownManager().spawnTown(player);
                break;
            default:
                showHelp(player, inTown, townId);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        boolean inTown = plugin.getTownManager().getTownIdByMember(player.getUniqueId().toString()) != 0;

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (!inTown) {
                subs.add("new");
                subs.add("ask");
                subs.add("join");
            } else {
                subs.add("claim");
                subs.add("unclaim");
                subs.add("builds");
                subs.add("inventory");
                subs.add("inv");
                subs.add("century");
                subs.add("deposit");
                subs.add("withdraw");
                subs.add("leave");
                subs.add("kick");
                subs.add("add");
                subs.add("rank");
                subs.add("toggle");
                subs.add("spawn");
            }
            return filterByInput(subs, args[0]);
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "join":
                    return null; // имена игроков
                case "kick":
                case "add":
                case "rank":
                    return null; // имена игроков
                case "toggle":
                    return filterByInput(Arrays.asList("open"), args[1]);
                case "unclaim":
                    return filterByInput(Arrays.asList("all"), args[1]);
                default:
                    return null;
            }
        } else if (args.length == 3 && "rank".equalsIgnoreCase(args[0]) && "add".equalsIgnoreCase(args[1])) {
            return null; // имена игроков
        } else if (args.length == 4 && "rank".equalsIgnoreCase(args[0]) && "add".equalsIgnoreCase(args[1])) {
            return filterByInput(Arrays.asList("Советник", "Депутат", "Рекрутер", "Житель"), args[3]);
        }
        return null;
    }

    private List<String> filterByInput(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }

    private void showHelp(Player player, boolean inTown, int townId) {
        player.sendMessage("§6===== Городские команды =====");
        if (!inTown) {
            player.sendMessage("§e/town new <название> §7- создать город");
            player.sendMessage("§e/town ask §7- запрос во все открытые города");
            player.sendMessage("§e/town join <название> §7- вступить в открытый город");
        } else {
            String townName = plugin.getTownManager().getTownName(townId);
            String rank = plugin.getTownManager().getRank(player);
            player.sendMessage("§aГород: §f" + townName + " §7(ранг: " + rank + ")");
            player.sendMessage("§e/town claim [радиус] §7- захватить чанки");
            player.sendMessage("§e/town unclaim [all] §7- снять чанк или всю территорию");
            player.sendMessage("§e/town builds §7- меню построек");
            player.sendMessage("§e/town inventory (inv) §7- открыть склад города");
            player.sendMessage("§e/town century §7- эпохи города");
            player.sendMessage("§e/town deposit <сумма> §7- пополнить казну");
            player.sendMessage("§e/town withdraw <сумма> §7- снять из казны (мэр)");
            player.sendMessage("§e/town add <ник> §7- пригласить игрока");
            player.sendMessage("§e/town kick <ник> §7- выгнать игрока");
            player.sendMessage("§e/town rank add <ник> <ранг> §7- назначить ранг");
            player.sendMessage("§e/town leave §7- покинуть город");
            player.sendMessage("§e/town toggle open §7- открыть/закрыть вход");
            player.sendMessage("§e/town spawn §7- телепорт на спавн города");
        }
    }
}