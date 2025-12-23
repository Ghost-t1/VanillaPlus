package GhostyPlugin.Modules;

import GhostyPlugin.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class WoodStonecutter {

    private static final String[] WoodTypes = {
            "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA",
            "DARK_OAK", "MANGROVE", "CHERRY", "BAMBOO", "CRIMSON", "WARPED"
    };

    public static void RegisterRecipes(JavaPlugin Plugin, ConfigManager Config) {
        for (String WoodType : WoodTypes) {
            RegisterWoodRecipes(Plugin, Config, WoodType);
        }
    }

    private static void RegisterWoodRecipes(JavaPlugin Plugin, ConfigManager Config, String WoodType) {
        String PlanksName = WoodType + "_PLANKS";
        String LogName = WoodType + "_LOG";
        String StrippedLogName = "STRIPPED_" + LogName;
        String WoodName = WoodType + "_WOOD";
        String StrippedWoodName = "STRIPPED_" + WoodName;

        Material Planks = Material.getMaterial(PlanksName);
        Material Log = Material.getMaterial(LogName);
        Material StrippedLog = Material.getMaterial(StrippedLogName);
        Material Wood = Material.getMaterial(WoodName);
        Material StrippedWood = Material.getMaterial(StrippedWoodName);

        if (Planks == null) {
            return;
        }

        RegisterIfExists(Plugin, Config, WoodType, "STAIRS", Planks, "planks-to-stairs");
        RegisterIfExists(Plugin, Config, WoodType, "SLAB", Planks, "planks-to-slabs");
        RegisterIfExists(Plugin, Config, WoodType, "FENCE", Planks, "planks-to-fence");
        RegisterIfExists(Plugin, Config, WoodType, "FENCE_GATE", Planks, "planks-to-fence-gate");
        RegisterIfExists(Plugin, Config, WoodType, "DOOR", Planks, "planks-to-door");
        RegisterIfExists(Plugin, Config, WoodType, "TRAPDOOR", Planks, "planks-to-trapdoor");
        RegisterIfExists(Plugin, Config, WoodType, "BUTTON", Planks, "planks-to-button");
        RegisterIfExists(Plugin, Config, WoodType, "PRESSURE_PLATE", Planks, "planks-to-pressure-plate");

        if (Log != null) {
            ConfigManager.RecipeConfig RecipeConf = Config.GetRecipeConfig("log-to-planks");
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_planks_from_log",
                    new ItemStack(Planks, RecipeConf.GetOutputCount()),
                    Log);
        }

        if (StrippedLog != null) {
            ConfigManager.RecipeConfig RecipeConf = Config.GetRecipeConfig("log-to-planks");
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_planks_from_stripped_log",
                    new ItemStack(Planks, RecipeConf.GetOutputCount()),
                    StrippedLog);
        }

        if (Log != null && StrippedLog != null) {
            ConfigManager.RecipeConfig RecipeConf = Config.GetRecipeConfig("log-to-stripped");
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_stripped_log",
                    new ItemStack(StrippedLog, RecipeConf.GetOutputCount()),
                    Log);
        }

        if (Wood != null && StrippedWood != null) {
            ConfigManager.RecipeConfig RecipeConf = Config.GetRecipeConfig("wood-to-stripped");
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_stripped_wood",
                    new ItemStack(StrippedWood, RecipeConf.GetOutputCount()),
                    Wood);
        }
    }

    private static void RegisterIfExists(JavaPlugin Plugin, ConfigManager Config, String WoodType, String Suffix,
            Material Input, String ConfigKey) {
        String MaterialName = WoodType + "_" + Suffix;
        Material ResultMaterial = Material.getMaterial(MaterialName);

        if (ResultMaterial != null) {
            ConfigManager.RecipeConfig RecipeConf = Config.GetRecipeConfig(ConfigKey);
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_" + Suffix.toLowerCase(),
                    new ItemStack(ResultMaterial, RecipeConf.GetOutputCount()),
                    Input);
        }
    }

    private static void RegisterStonecuttingRecipe(JavaPlugin Plugin, String RecipeKey, ItemStack Result,
            Material Input) {
        NamespacedKey Key = new NamespacedKey(Plugin, RecipeKey);

        if (Bukkit.getRecipe(Key) != null) {
            return;
        }

        StonecuttingRecipe Recipe = new StonecuttingRecipe(
                Key,
                Result,
                new RecipeChoice.MaterialChoice(Input));

        Bukkit.addRecipe(Recipe);
    }
}
