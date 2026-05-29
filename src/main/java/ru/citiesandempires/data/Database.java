package ru.citiesandempires.data;

import ru.citiesandempires.CitiesAndEmpires;
import java.sql.*;

public class Database {
    private final CitiesAndEmpires plugin;
    private Connection connection;

    public Database(CitiesAndEmpires plugin) {
        this.plugin = plugin;
    }

    /**
     * Подключается к базе данных при запуске плагина.
     */
    public void connect() {
        try {
            openConnection();
            plugin.getLogger().info("База данных успешно подключена.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось подключиться к базе данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Закрывает соединение при выключении плагина.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Соединение с базой данных закрыто.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает активное соединение. Если оно закрыто или отсутствует, открывает новое.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при проверке/открытии соединения: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Внутренний метод для открытия соединения с учётом настроек (SQLite/MySQL).
     */
    private void openConnection() throws SQLException {
        String type = plugin.getConfig().getString("database.type", "SQLite");
        if (type.equalsIgnoreCase("MySQL")) {
            String host = plugin.getConfig().getString("database.mysql.host");
            String port = plugin.getConfig().getString("database.mysql.port");
            String db = plugin.getConfig().getString("database.mysql.database");
            String user = plugin.getConfig().getString("database.mysql.username");
            String pass = plugin.getConfig().getString("database.mysql.password");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false", user, pass);
        } else {
            // SQLite – создаст файл, если его нет
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/database.db");
        }
    }

    /**
     * Создаёт все таблицы, если их ещё нет.
     */
    public void createTables() {
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS towns (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "mayor_uuid TEXT NOT NULL, " +
                "bank REAL DEFAULT 0, " +
                "open_entry INTEGER DEFAULT 0, " +
                "nation_id INTEGER, " +
                "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS town_members (" +
                "town_id INTEGER, " +
                "uuid TEXT NOT NULL, " +
                "rank TEXT DEFAULT 'Житель', " +
                "PRIMARY KEY (town_id, uuid))",
            "CREATE TABLE IF NOT EXISTS town_chunks (" +
                "town_id INTEGER, " +
                "world TEXT NOT NULL, " +
                "x INTEGER NOT NULL, " +
                "z INTEGER NOT NULL, " +
                "PRIMARY KEY (world, x, z))",
            "CREATE TABLE IF NOT EXISTS plots (" +
                "town_id INTEGER, " +
                "chunk_x INTEGER NOT NULL, " +
                "chunk_z INTEGER NOT NULL, " +
                "world TEXT NOT NULL, " +
                "owner_uuid TEXT, " +
                "type TEXT DEFAULT 'default', " +
                "forsale REAL DEFAULT 0, " +
                "permissions TEXT DEFAULT '{}')",
            "CREATE TABLE IF NOT EXISTS nations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "capital_id INTEGER)",
            "CREATE TABLE IF NOT EXISTS nation_relations (" +
                "nation_id INTEGER, " +
                "target_id INTEGER, " +
                "relation TEXT DEFAULT 'neutral')",
            "CREATE TABLE IF NOT EXISTS town_buildings (" +
                "town_id INTEGER, " +
                "building_name TEXT, " +
                "level INTEGER DEFAULT 0, " +
                "PRIMARY KEY (town_id, building_name))"
        };
        try (Statement stmt = getConnection().createStatement()) {
            for (String q : queries) {
                stmt.executeUpdate(q);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка создания таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }
}