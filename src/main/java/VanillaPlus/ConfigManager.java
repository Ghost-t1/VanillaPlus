package VanillaPlus;

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

    public void ReloadConfig() {
        LoadConfig();
    }

    public void LoadConfig() {
        PluginInstance.saveDefaultConfig();
        PluginInstance.reloadConfig();
        Config = PluginInstance.getConfig();

        FrogFeederEnabled = Config.getBoolean("FrogFeeder.Enabled", true);
        ToolStatsEnabled = Config.getBoolean("ToolStats.Enabled", true);
        WoodStonecutterEnabled = Config.getBoolean("WoodStonecutter.Enabled", true);
    }

    public boolean IsFrogFeederEnabled() {
        return FrogFeederEnabled;
    }

    public int GetFrogCooldown() {
        return Config.getInt("FrogFeeder.Cooldown", 1);
    }

    public double GetFrogAngryChance() {
        return Config.getDouble("FrogFeeder.AngryChance", 0.2);
    }

    public int GetFrogParticleMin() {
        return Config.getInt("FrogFeeder.ParticleMin", 10);
    }

    public int GetFrogParticleMax() {
        return Config.getInt("FrogFeeder.ParticleMax", 25);
    }

    public boolean IsAprilFoolsEnabled() {
        return Config.getBoolean("FrogFeeder.AprilFools", false);
    }

    public double GetAnvilDamageChance() {
        return Config.getDouble("ArmorReseter.AnvilDamageChance", 0.12);
    }

    public double GetArmorBreakChance() {
        return Config.getDouble("ArmorReseter.ArmorBreakChance", 0.5);
    }

    public int GetMinDurabilityLoss() {
        return Config.getInt("ArmorReseter.MinDurabilityLoss", 3);
    }

    public int GetMaxDurabilityLoss() {
        return Config.getInt("ArmorReseter.MaxDurabilityLoss", 14);
    }

    public boolean IsToolStatsEnabled() {
        return ToolStatsEnabled;
    }

    public boolean IsWoodStonecutterEnabled() {
        return WoodStonecutterEnabled;
    }

    public boolean IsToolInfoEnabled(String ToolType) {
        String PascalToolType = ToolType.substring(0, 1).toUpperCase() + ToolType.substring(1);
        return Config.getBoolean("ToolStats.Info" + PascalToolType + ".Enabled", false);
    }

    public java.util.List<String> GetToolInfoLines(String ToolType) {
        String PascalToolType = ToolType.substring(0, 1).toUpperCase() + ToolType.substring(1);
        return Config.getStringList("ToolStats.Info" + PascalToolType + ".Lines");
    }

    public int GetLoreUpdateInterval() {
        return Config.getInt("ToolStats.UpdateInterval", 2);
    }

    public boolean IsFastLeafDecayEnabled() {
        return Config.getBoolean("FastLeafDecay.Enabled", true);
    }

    public int GetFastLeafDecayMaxLeaves() {
        return Config.getInt("FastLeafDecay.MaxLeaves", 20);
    }

    public int GetFastLeafDecayMinDelay() {
        return Config.getInt("FastLeafDecay.MinDelaySeconds", 1);
    }

    public int GetFastLeafDecayMaxDelay() {
        return Config.getInt("FastLeafDecay.MaxDelaySeconds", 3);
    }

    public boolean IsConcreteHardenerEnabled() {
        return Config.getBoolean("ConcreteHardener.Enabled", true);
    }

    public int GetConcreteHardenerAmountPerLevel() {
        return Config.getInt("ConcreteHardener.AmountPerLevel", 8);
    }

    public int GetConcreteHardenerCooldown() {
        return Config.getInt("ConcreteHardener.CooldownSeconds", 1);
    }

    public boolean IsOxidizeCopperEnabled() {
        return Config.getBoolean("OxidizeCopper.Enabled", true);
    }

    public int GetOxidizeCopperMultiplier() {
        return Config.getInt("OxidizeCopper.BottleAccelerationMultiplier", 20);
    }
}
