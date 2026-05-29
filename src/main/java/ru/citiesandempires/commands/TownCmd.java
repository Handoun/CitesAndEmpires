package ru.citiesandempires.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.gui.BuildsGUI;

public class TownCmd implements CommandExecutor {
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

        // Если нет аргументов – показываем справку
        if (args.length == 0) {
            showHelp(player, inTown, townId);
            return true;
        }

        String sub = args[0].toLowerCase();

        // ----- Команды, доступные ВНЕ города -----
        switch (sub) {
            case "new":
                if (args.length < 2) {
                    player.sendMessage("§cУкажите название города. §7/town new <название>");
                    return true;
                }
                plugin.getTownManager().createTown(player, args[1]);
                break;

            case "ask":
                if (inTown) {
                    player.sendMessage("§cВы уже состоите в городе.");
                } else {
                    // TODO: отправка запроса во все города с набором
                    player.sendMessage("§eЗапрос на вступление разослан городам с открытым набором.");
                }
                break;

            case "join":
                if (args.length < 2) {
                    player.sendMessage("§cУкажите название города. §7/town join <название>");
                    return true;
                }
                if (inTown) {
                    player.sendMessage("§cВы уже состоите в городе. Покиньте текущий: /town leave");
                } else {
                    // TODO: прямое вступление, если город открыт
                    player.sendMessage("§eЗапрос на вступление в город " + args[1] + " отправлен (заглушка).");
                }
                break;

            // ----- Команды, доступные ВНУТРИ города -----
            case "claim":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                int radius = 1;
                if (args.length > 1) {
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                plugin.getTownManager().claimChunk(player, radius);
                break;

            case "unclaim":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: реализовать снятие привата
                player.sendMessage("§eСнятие привата пока не реализовано.");
                break;

            case "builds":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                new BuildsGUI(plugin).open(player);
                break;

            case "deposit":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                if (args.length < 2) {
                    player.sendMessage("§cУкажите сумму. §7/town deposit <сумма>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    plugin.getEconomyManager().depositTown(townId, player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cНекорректная сумма.");
                }
                break;

            case "withdraw":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                if (args.length < 2) {
                    player.sendMessage("§cУкажите сумму. §7/town withdraw <сумма>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    plugin.getEconomyManager().withdrawTown(townId, player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cНекорректная сумма.");
                }
                break;

            case "leave":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: удалить игрока из town_members, передать мэра если надо
                player.sendMessage("§eВы покинули город (заглушка).");
                break;

            case "kick":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: проверка прав и удаление игрока
                player.sendMessage("§eКик пока не реализован.");
                break;

            case "add":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                if (args.length < 2) {
                    player.sendMessage("§cУкажите ник игрока. §7/town add <ник>");
                    return true;
                }
                // TODO: пригласить игрока (доступно рекрутеру и выше)
                player.sendMessage("§eИгрок " + args[1] + " приглашён (заглушка).");
                break;

            case "rank":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: назначение рангов
                player.sendMessage("§eНазначение рангов пока не реализовано.");
                break;

            case "toggle":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: переключение открытого входа и т.п.
                player.sendMessage("§eПереключение режимов пока не реализовано.");
                break;

            case "spawn":
                if (!inTown) { player.sendMessage("§cВы не состоите в городе."); return true; }
                // TODO: телепорт на спавн города
                player.sendMessage("§eТелепортация на спавн города пока не реализована.");
                break;

            default:
                showHelp(player, inTown, townId);
                break;
        }
        return true;
    }

    /**
     * Выводит справку по командам /town в зависимости от статуса игрока.
     */
    private void showHelp(Player player, boolean inTown, int townId) {
        player.sendMessage("§6===== Городские команды =====");
        if (!inTown) {
            player.sendMessage("§e/town new <название> §7- создать город (128 булыжника, 128 досок, 8 угля)");
            player.sendMessage("§e/town ask §7- отправить запрос во все города с набором");
            player.sendMessage("§e/town join <название> §7- вступить в открытый город");
        } else {
            String townName = plugin.getTownManager().getTownName(townId);
            String rank = plugin.getTownManager().getRank(player);
            player.sendMessage("§aВаш город: §f" + townName + " §7(ранг: " + rank + ")");
            player.sendMessage("§e/town claim [радиус] §7- захватить территорию");
            player.sendMessage("§e/town builds §7- открыть меню городских построек");
            player.sendMessage("§e/town deposit <сумма> §7- пополнить казну");
            player.sendMessage("§e/town withdraw <сумма> §7- снять из казны");
            player.sendMessage("§e/town add <ник> §7- пригласить игрока");
            player.sendMessage("§e/town kick <ник> §7- выгнать игрока");
            player.sendMessage("§e/town rank add <ник> <должность> §7- назначить ранг");
            player.sendMessage("§e/town leave §7- покинуть город");
            player.sendMessage("§e/town spawn §7- телепортироваться на спавн города");
            player.sendMessage("§e/town toggle open §7- включить/выключить свободный вход");
        }
    }
}