package ru.citiesandempires.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.citiesandempires.CitiesAndEmpires;

public class EconomyManager {
    private final CitiesAndEmpires plugin;
    private Economy economy;

    public EconomyManager(CitiesAndEmpires plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) economy = rsp.getProvider();
    }

    public boolean hasEconomy() { return economy != null; }

    public boolean depositTown(int townId, Player player, double amount) {
        if (!hasEconomy()) return false;
        if (economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            updateTownBank(townId, amount);
            return true;
        }
        return false;
    }

    public boolean withdrawTown(int townId, Player player, double amount) {
        double bank = getTownBank(townId);
        if (bank >= amount) {
            updateTownBank(townId, -amount);
            economy.depositPlayer(player, amount);
            return true;
        }
        return false;
    }

    private void updateTownBank(int townId, double delta) {
        try (java.sql.Connection conn = plugin.getDatabase().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE towns SET bank = bank + ? WHERE id = ?")) {
            ps.setDouble(1, delta);
            ps.setInt(2, townId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    private double getTownBank(int townId) {
        try (java.sql.Connection conn = plugin.getDatabase().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT bank FROM towns WHERE id = ?")) {
            ps.setInt(1, townId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("bank");
        } catch (java.sql.SQLException e) { }
        return 0;
    }

    public void collectDailyTaxes() {
        // TODO: ежедневное списание налогов
    }
}
