package VanillaPlus;

import VanillaPlus.Modules.ArmorReseter;
import VanillaPlus.Modules.DirtUpdater;
import VanillaPlus.Modules.FrogFeeder;
import VanillaPlus.Modules.ToolStats;
import VanillaPlus.Modules.WoodStonecutter;
import org.bukkit.plugin.java.JavaPlugin;

public class MainLoader extends JavaPlugin {

    private ConfigManager Config;
    private VanillaPlus.Database.SQLite Database;

    @Override
    public void onEnable() {
        Config = new ConfigManager(this);
        Database = new VanillaPlus.Database.SQLite(this);

        getServer().getPluginManager().registerEvents(new ArmorReseter(Config), this);
        getServer().getPluginManager().registerEvents(new DirtUpdater(), this);
        new VanillaPlus.Modules.BlockPersistence(this, Database);

        if (Config.IsFrogFeederEnabled()) {
            getServer().getPluginManager().registerEvents(new FrogFeeder(this, Config), this);
        }

        if (Config.IsToolStatsEnabled()) {
            getServer().getPluginManager().registerEvents(new ToolStats(this, Config), this);
        }

        if (Config.IsWoodStonecutterEnabled()) {
            WoodStonecutter.RegisterRecipes(this, Config);
        }

        if (Config.IsFastLeafDecayEnabled()) {
            getServer().getPluginManager().registerEvents(new VanillaPlus.Modules.FastLeafDecay(this, Config), this);
        }

        if (Config.IsConcreteHardenerEnabled()) {
            getServer().getPluginManager().registerEvents(new VanillaPlus.Modules.ConcreteHardener(Config),
                    this);
        }

        if (Config.IsOxidizeCopperEnabled()) {
            getServer().getPluginManager().registerEvents(new VanillaPlus.Modules.OxidizeCopper(Config, this), this);
        }

        getServer().getPluginManager().registerEvents(new VanillaPlus.Modules.AnvilMechanics(), this);
        getServer().getPluginManager().registerEvents(new VanillaPlus.Modules.LecternLock(this, Database), this);

        VanillaPlus.Commands.ToggleStatsCommand ToggleStatsCmd = new VanillaPlus.Commands.ToggleStatsCommand(this);
        VanillaPlus.Commands.ClearStatsCommand ClearStatsCmd = new VanillaPlus.Commands.ClearStatsCommand(this);

        getCommand("togglestats").setExecutor(ToggleStatsCmd);
        getCommand("clearstats").setExecutor(ClearStatsCmd);
        getCommand("vp").setExecutor(new VanillaPlus.Commands.VanillaPlusCommand(ToggleStatsCmd, ClearStatsCmd, Config));
    }

    @Override
    public void onDisable() {
        if (Database != null) {
            Database.Close();
        }
    }
}