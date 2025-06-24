package de.xlb.minebase;

import de.xlb.minebase.utils.Test;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Provides Database Classes
 */
public final class Minebase extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        //new Test(getLogger(), getDataFolder());
    }

    @Override
    public void onDisable() {

    }
}
