package GhostyPlugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin PluginInstance;
    private FileConfiguration Config;

    private boolean FrogFeederEnabled;
    private boolean ToolStatsEnabled;
    private boolean WoodStonecutterEnabled;

    public ConfigManager(JavaPlugin Plugin) {
        this.PluginInstance = Plugin;
        LoadConfig();
    }

    public void LoadConfig() {
        PluginInstance.saveDefaultConfig();
        PluginInstance.reloadConfig();
        Config = PluginInstance.getConfig();

        FrogFeederEnabled = Config.getBoolean("frog-feeder.enabled", true);
        ToolStatsEnabled = Config.getBoolean("tool-stats.enabled", true);
        WoodStonecutterEnabled = Config.getBoolean("wood-stonecutter.enabled", true);
    }

    public void SetToolStatsEnabled(boolean Enabled) {
        ToolStatsEnabled = Enabled;
        Config.set("tool-stats.enabled", Enabled);
        PluginInstance.saveConfig();
    }

    public boolean IsFrogFeederEnabled() {
        return FrogFeederEnabled;
    }

    public int GetFrogCooldown() {
        return Config.getInt("frog-feeder.cooldown", 0);
    }

    public double GetFrogAngryChance() {
        return Config.getDouble("frog-feeder.angry-chance", 0.2);
    }

    public int GetFrogAngryMin() {
        return Config.getInt("frog-feeder.angry-min", 10);
    }

    public int GetFrogAngryMax() {
        return Config.getInt("frog-feeder.angry-max", 25);
    }

    public int GetFrogFriendlyMin() {
        return Config.getInt("frog-feeder.friendly-min", 15);
    }

    public int GetFrogFriendlyMax() {
        return Config.getInt("frog-feeder.friendly-max", 30);
    }

    public double GetAnvilDamageChance() {
        return Config.getDouble("armor-reseter.anvil-damage-chance", 0.12);
    }

    public double GetArmorBreakChance() {
        return Config.getDouble("armor-reseter.armor-break-chance", 0.5);
    }

    public int GetMinDurabilityLoss() {
        return Config.getInt("armor-reseter.min-durability-loss", 3);
    }

    public int GetMaxDurabilityLoss() {
        return Config.getInt("armor-reseter.max-durability-loss", 14);
    }

    public boolean IsToolStatsEnabled() {
        return ToolStatsEnabled;
    }

    public boolean IsWoodStonecutterEnabled() {
        return WoodStonecutterEnabled;
    }

    public boolean IsToolInfoEnabled(String ToolType) {
        return Config.getBoolean("tool-stats.info-" + ToolType + ".enabled", false);
    }

    public java.util.List<String> GetToolInfoLines(String ToolType) {
        return Config.getStringList("tool-stats.info-" + ToolType + ".lines");
    }
}
