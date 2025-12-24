package GhostyPlugin;

import GhostyPlugin.Modules.ArmorReseter;
import GhostyPlugin.Modules.DirtUpdater;
import GhostyPlugin.Modules.FrogFeeder;
import GhostyPlugin.Modules.ToolStats;
import GhostyPlugin.Modules.WoodStonecutter;
import org.bukkit.plugin.java.JavaPlugin;

public class MainLoader extends JavaPlugin {

    private ConfigManager Config;

    @Override
    public void onEnable() {
        Config = new ConfigManager(this);

        getServer().getPluginManager().registerEvents(new ArmorReseter(Config), this);
        getServer().getPluginManager().registerEvents(new DirtUpdater(), this);

        if (Config.IsFrogFeederEnabled()) {
            getServer().getPluginManager().registerEvents(new FrogFeeder(this, Config), this);
        }

        if (Config.IsToolStatsEnabled()) {
            getServer().getPluginManager().registerEvents(new ToolStats(this, Config), this);
        }

        if (Config.IsWoodStonecutterEnabled()) {
            WoodStonecutter.RegisterRecipes(this, Config);
        }

        getCommand("vanillaplus").setExecutor(new GhostyPlugin.Commands.VanillaPlusCommand(Config));
        getCommand("togglestats").setExecutor(new GhostyPlugin.Commands.ToggleStatsCommand(this));
        getCommand("clearstats").setExecutor(new GhostyPlugin.Commands.ClearStatsCommand(this));
    }

    @Override
    public void onDisable() {
    }
}