package com.ruinscraft.logblockpurge;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogblockPurgePlugin extends JavaPlugin {

    private BukkitTask task;

    @Override
    public void onEnable() {
        String host = getConfig().getString("logblock-mysql-storage.host");
        int port = getConfig().getInt("logblock-mysql-storage.port");
        String db = getConfig().getString("logblock-mysql-storage.database");
        String user = getConfig().getString("logblock-mysql-storage.username");
        String pass = getConfig().getString("logblock-mysql-storage.password");

        PurgeRunnable runnable = new PurgeRunnable(host, port, db, user, pass);

        long delay = TimeUnit.MINUTES.toMillis(10);
        long period = TimeUnit.HOURS.toMillis(1);

        task = runnable.runTaskTimerAsynchronously(this, delay, period);
    }

    @Override
    public void onDisable() {
        task.cancel();
    }

    public class PurgeRunnable extends BukkitRunnable {

        private String host;
        private int port;
        private String db;
        private String user;
        private String pass;
        private Connection connection;

        public PurgeRunnable(String host, int port, String db, String user, String pass) {
            this.host = host;
            this.port = port;
            this.db = db;
            this.user = user;
            this.pass = pass;
        }

        @Override
        public void run() {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            } catch (SQLException e) {
                e.printStackTrace();
                return; // could not make mysql connection, just return
            }

            while (true) {
                if (isCancelled()) {
                    try {
                        closeConnection(connection);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return; // bukkittask was canceled
                }

                try {
                    testConnection(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                    return; // connection has been closed
                }

                try {
                    purgeTables(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void testConnection(Connection connection) throws Exception {
        if (connection == null || connection.isClosed()) {
            throw new Exception("jdbc connection failed");
        }
    }

    private static void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private static void purgeTables(Connection connection) throws SQLException {
        List<String> worlds = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());

        for (String world : worlds) {
            try (PreparedStatement deleteBlocks = connection.prepareStatement("DELETE FROM `lb-" + world + "-blocks` WHERE date < limit 1000000;")) {
                deleteBlocks.execute();
            }

            try (PreparedStatement deleteEntities = connection.prepareStatement("DELETE FROM `lb-" + world + "-entities` WHERE date < limit 1000000;")) {
                deleteEntities.execute();
            }
        }
    }

}
