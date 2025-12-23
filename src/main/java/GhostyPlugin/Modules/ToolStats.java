package GhostyPlugin.Modules;

import GhostyPlugin.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToolStats implements Listener {

    private final NamespacedKey BlocksMinedKey;
    private final NamespacedKey MobsKilledKey;
    private final ConfigManager Config;

    public ToolStats(JavaPlugin Plugin, ConfigManager Config) {
        this.Config = Config;
        this.BlocksMinedKey = new NamespacedKey(Plugin, "blocks_mined");
        this.MobsKilledKey = new NamespacedKey(Plugin, "mobs_killed");
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent Event) {
        Player PlayerEntity = Event.getPlayer();
        ItemStack Tool = PlayerEntity.getInventory().getItemInMainHand();
        String ToolType = GetToolType(Tool.getType());

        if (ToolType == null)
            return;
        if (!Config.ShouldTrack(ToolType, "blocks"))
            return;

        IncrementStat(Tool, BlocksMinedKey, true, ToolType);
    }

    @EventHandler
    public void OnEntityDeath(EntityDeathEvent Event) {
        Player Killer = Event.getEntity().getKiller();
        if (Killer == null)
            return;

        ItemStack Weapon = Killer.getInventory().getItemInMainHand();
        String ToolType = GetToolType(Weapon.getType());

        if (ToolType == null)
            return;
        if (!Config.ShouldTrack(ToolType, "kills"))
            return;

        IncrementStat(Weapon, MobsKilledKey, false, ToolType);
    }

    private String GetToolType(Material MaterialType) {
        String Name = MaterialType.name();
        if (Name.endsWith("_PICKAXE"))
            return "pickaxe";
        if (Name.endsWith("_AXE"))
            return "axe";
        if (Name.endsWith("_SHOVEL"))
            return "shovel";
        if (Name.endsWith("_HOE"))
            return "hoe";
        if (Name.endsWith("_SWORD"))
            return "sword";

        return Name.toLowerCase();
    }

    private void IncrementStat(ItemStack Tool, NamespacedKey Key, boolean IsBlockStat, String ToolType) {
        ItemMeta Meta = Tool.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        int CurrentCount = Data.getOrDefault(Key, PersistentDataType.INTEGER, 0);
        CurrentCount++;
        Data.set(Key, PersistentDataType.INTEGER, CurrentCount);

        UpdateLore(Meta, CurrentCount, IsBlockStat, ToolType);
        Tool.setItemMeta(Meta);
    }

    private void UpdateLore(ItemMeta Meta, int Count, boolean IsBlockStat, String ToolType) {
        List<Component> Lore = Meta.lore();
        if (Lore == null) {
            Lore = new ArrayList<>();
        }

        Component NewLine = CreateStatLine(Count, IsBlockStat, ToolType);
        String SearchEmoji = IsBlockStat ? "⛏" : "⚔";

        boolean Found = false;
        for (int I = 0; I < Lore.size(); I++) {
            Component Line = Lore.get(I);
            String PlainText = PlainTextComponentSerializer.plainText().serialize(Line);

            if (PlainText.contains("[ " + SearchEmoji + " ]")) {
                Lore.set(I, NewLine);
                Found = true;
                break;
            }
        }

        if (!Found) {
            Lore.add(NewLine);
        }

        Meta.lore(Lore);
    }

    private Component CreateStatLine(int Count, boolean IsBlockStat, String ToolType) {
        NumberFormat Formatter = NumberFormat.getInstance(Locale.US);
        String FormattedCount = Formatter.format(Count);

        String Emoji = IsBlockStat ? "⛏" : "⚔";

        String SpecificKey = IsBlockStat ? "mined-" + ToolType : "killed-" + ToolType;
        String Label = Config.GetToolLabel(SpecificKey);

        if (Label.equals("Stat")) {
            String GeneralKey = IsBlockStat ? "mined" : "killed";
            Label = Config.GetToolLabel(GeneralKey);
        }

        String FullText = "[ " + Emoji + " ] " + Label + ": " + FormattedCount;

        return ApplyGradient(FullText);
    }

    private Component ApplyGradient(String Text) {
        int StartR = 255, StartG = 140, StartB = 0;
        int EndR = 255, EndG = 184, EndB = 77;

        Component Result = Component.empty();
        int Length = Text.length();

        for (int I = 0; I < Length; I++) {
            float Ratio = (float) I / (Length - 1);
            int R = (int) (StartR + (EndR - StartR) * Ratio);
            int G = (int) (StartG + (EndG - StartG) * Ratio);
            int B = (int) (StartB + (EndB - StartB) * Ratio);

            TextColor Color = TextColor.color(R, G, B);
            Result = Result.append(Component.text(String.valueOf(Text.charAt(I))).color(Color));
        }

        return Result;
    }
}
