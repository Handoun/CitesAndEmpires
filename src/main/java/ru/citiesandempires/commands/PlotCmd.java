package ru.citiesandempires.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;

public class PlotCmd implements CommandExecutor {
    private final CitiesAndEmpires plugin;

    public PlotCmd(CitiesAndEmpires plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { return true; }
        Player player = (Player) sender;
        player.sendMessage("§eСистема участков в разработке.");
        return true;
    }
}
