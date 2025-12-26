package VanillaPlus.Modules;

import VanillaPlus.ConfigManager;
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
            RegisterWoodRecipes(Plugin, WoodType);
        }
    }

    private static void RegisterWoodRecipes(JavaPlugin Plugin, String WoodType) {
        String PlanksName = WoodType + "_PLANKS";
        String LogName;
        String StrippedLogName;
        String WoodName;
        String StrippedWoodName;

        if (WoodType.equals("CRIMSON") || WoodType.equals("WARPED")) {
            LogName = WoodType + "_STEM";
            StrippedLogName = "STRIPPED_" + WoodType + "_STEM";
            WoodName = WoodType + "_HYPHAE";
            StrippedWoodName = "STRIPPED_" + WoodType + "_HYPHAE";
        } else if (WoodType.equals("BAMBOO")) {
            LogName = "BAMBOO_BLOCK";
            StrippedLogName = "STRIPPED_BAMBOO_BLOCK";
            WoodName = null;
            StrippedWoodName = null;
        } else {
            LogName = WoodType + "_LOG";
            StrippedLogName = "STRIPPED_" + LogName;
            WoodName = WoodType + "_WOOD";
            StrippedWoodName = "STRIPPED_" + WoodName;
        }

        Material Planks = Material.getMaterial(PlanksName);
        Material Log = Material.getMaterial(LogName);
        Material StrippedLog = Material.getMaterial(StrippedLogName);
        Material Wood = WoodName != null ? Material.getMaterial(WoodName) : null;
        Material StrippedWood = StrippedWoodName != null ? Material.getMaterial(StrippedWoodName) : null;

        if (Planks == null) {
            return;
        }

        RegisterIfExists(Plugin, WoodType, "BUTTON", Planks, 1);
        RegisterIfExists(Plugin, WoodType, "PRESSURE_PLATE", Planks, 1);

        if (Log != null) {
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_planks_from_log",
                    new ItemStack(Planks, 4),
                    Log);
        }

        if (StrippedLog != null) {
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_planks_from_stripped_log",
                    new ItemStack(Planks, 4),
                    StrippedLog);
        }

        if (Log != null && StrippedLog != null) {
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_stripped_log",
                    new ItemStack(StrippedLog, 1),
                    Log);
        }

        if (Wood != null && StrippedWood != null) {
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_stripped_wood",
                    new ItemStack(StrippedWood, 1),
                    Wood);
        }
    }

    private static void RegisterIfExists(JavaPlugin Plugin, String WoodType, String Suffix,
            Material Input, int OutputCount) {
        String MaterialName = WoodType + "_" + Suffix;
        Material ResultMaterial = Material.getMaterial(MaterialName);

        if (ResultMaterial != null) {
            RegisterStonecuttingRecipe(
                    Plugin,
                    "stonecutter_" + WoodType.toLowerCase() + "_" + Suffix.toLowerCase(),
                    new ItemStack(ResultMaterial, OutputCount),
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
