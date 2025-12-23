package GhostyPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin PluginInstance;
    private FileConfiguration Config;

    private boolean FrogFeederEnabled;
    private boolean ToolStatsEnabled;
    private boolean WoodStonecutterEnabled;

    private final Map<String, RecipeConfig> WoodRecipes = new HashMap<>();

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

        LoadWoodRecipes();
    }

    private void LoadWoodRecipes() {
        WoodRecipes.clear();

        String BasePath = "wood-stonecutter.recipes.";
        String[] RecipeKeys = {
                "planks-to-stairs", "planks-to-slabs", "planks-to-fence",
                "planks-to-fence-gate", "planks-to-door", "planks-to-trapdoor",
                "planks-to-button", "planks-to-pressure-plate", "log-to-planks",
                "log-to-stripped", "wood-to-stripped"
        };

        for (String Key : RecipeKeys) {
            int InputCount = Config.getInt(BasePath + Key + ".input-count", 1);
            int OutputCount = Config.getInt(BasePath + Key + ".output-count", 1);
            WoodRecipes.put(Key, new RecipeConfig(InputCount, OutputCount));
        }
    }

    public boolean IsFrogFeederEnabled() {
        return FrogFeederEnabled;
    }

    public int GetFrogCooldown() {
        return Config.getInt("frog-feeder.cooldown", 0);
    }

    public double GetFrogGrumpyChance() {
        return Config.getDouble("frog-feeder.grumpy-chance", 0.2);
    }

    // ArmorReseter

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

    public RecipeConfig GetRecipeConfig(String RecipeKey) {
        return WoodRecipes.getOrDefault(RecipeKey, new RecipeConfig(1, 1));
    }

    public String GetToolLabel(String Key) {
        return Config.getString("tool-stats.labels." + Key, "Stat");
    }

    public boolean ShouldTrack(String ToolType, String Metric) {
        return Config.getBoolean("tool-stats.tracking." + ToolType + "." + Metric, false);
    }

    public static class RecipeConfig {
        private final int InputCount;
        private final int OutputCount;

        public RecipeConfig(int InputCount, int OutputCount) {
            this.InputCount = InputCount;
            this.OutputCount = OutputCount;
        }

        public int GetInputCount() {
            return InputCount;
        }

        public int GetOutputCount() {
            return OutputCount;
        }
    }
}
