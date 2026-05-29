package ru.citiesandempires.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.citiesandempires.CitiesAndEmpires;

public class NationCmd implements CommandExecutor {
    private final CitiesAndEmpires plugin;
    public NationCmd(CitiesAndEmpires plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("§eСистема наций в разработке.");
        }
        return true;
    }
}
