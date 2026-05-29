package ru.citiesandempires.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;
import ru.citiesandempires.gui.BuildsGUI;

public class TownCmd implements CommandExecutor {
    private final CitiesAndEmpires plugin;

    public TownCmd(CitiesAndEmpires plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) { player.sendMessage("/town new <название> — создать город"); return true; }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "new":
                if (args.length < 2) { player.sendMessage("§cУкажите название города."); return true; }
                String name = args[1];
                plugin.getTownManager().createTown(player, name);
                break;
            case "claim":
                int radius = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                plugin.getTownManager().claimChunk(player, radius);
                break;
            case "builds":
                new BuildsGUI(plugin).open(player);
                break;
            // остальные подкоманды добавляются здесь
        }
        return true;
    }
}
