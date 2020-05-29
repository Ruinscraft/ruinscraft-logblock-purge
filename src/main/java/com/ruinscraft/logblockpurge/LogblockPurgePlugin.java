package com.ruinscraft.logblockpurge;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class LogblockPurgePlugin extends JavaPlugin {

    private BukkitTask task;

    @Override
    public void onEnable() {
        String host = getConfig().getString("");
        int port = getConfig().getInt("");
        String db = getConfig().getString("");
        String user = getConfig().getString("");
        String pass = getConfig().getString("");

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
                connection = DriverManager.getConnection("");
            } catch (SQLException e) {
                e.printStackTrace();
                return; // Could not make mysql connection, just return
            }

            while (true) {
                try {
                    testConnection(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


            }
        }

    }

    private static void testConnection(Connection connection) throws Exception {
        if (connection == null || connection.isClosed()) {
            throw new Exception("jdbc connection failed");
        }
    }

}
