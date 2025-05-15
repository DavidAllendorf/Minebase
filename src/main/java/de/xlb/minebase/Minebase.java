package de.xlb.minebase;

import de.xlb.minebase.api.MineDb;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Minebase extends JavaPlugin implements Listener {

    private MineDb api;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        connectDB();
    }

    @Override
    public void onDisable() {
        try {
            api.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interface to get Database Class
     * Example: xDatabase db = getServer().getPluginManager().getPlugin("Minebase").getApi();
     * @return xDatabase
     */
    public MineDb getApi() {
        return api;
    }

    /**
     * Creates and Connects to Database
     * Databasetype: sqlite
     */
    private void connectDB(){
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File dbFile = new File(getDataFolder(), "data.db");
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            this.api = new MineDb(connection, getLogger(), getDataFolder());
            getLogger().info("Database connectet");
        } catch (Exception e) {
            getLogger().severe("Error connection to Database " + e.getMessage());
        }
    }
}
