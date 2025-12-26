package VanillaPlus.Commands;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ToggleStatsCommand implements CommandExecutor {

    private final NamespacedKey StatsDisabledKey;

    public ToggleStatsCommand(JavaPlugin Plugin) {
        this.StatsDisabledKey = new NamespacedKey(Plugin, "stats_disabled");
        this.PlayerStatsDisabledKey = new NamespacedKey(Plugin, "player_stats_disabled");
    }

    private final NamespacedKey PlayerStatsDisabledKey;

    @Override
    public boolean onCommand(CommandSender Sender, Command Command, String Label, String[] Args) {
        if (!(Sender instanceof Player PlayerEntity)) {
            return true;
        }

        if (Args.length > 0 && Args[0].equalsIgnoreCase("item")) {
            ToggleItemStats(PlayerEntity);
        } else {
            ToggleGlobalStats(PlayerEntity);
        }

        return true;
    }

    private void ToggleGlobalStats(Player PlayerEntity) {
        PersistentDataContainer Data = PlayerEntity.getPersistentDataContainer();
        boolean CurrentlyDisabled = Data.has(PlayerStatsDisabledKey, PersistentDataType.BYTE);

        if (CurrentlyDisabled) {
            Data.remove(PlayerStatsDisabledKey);
            PlayerEntity.sendMessage("§aГлобальный сбор статистики для вас §2включен");
        } else {
            Data.set(PlayerStatsDisabledKey, PersistentDataType.BYTE, (byte) 1);
            PlayerEntity.sendMessage("§cГлобальный сбор статистики для вас §4отключен");
        }
    }

    private void ToggleItemStats(Player PlayerEntity) {
        ItemStack Item = PlayerEntity.getInventory().getItemInMainHand();

        if (Item == null || Item.getType() == Material.AIR) {
            PlayerEntity.sendMessage("§cВозьмите предмет в руку!");
            return;
        }

        if (!Item.hasItemMeta()) {
            PlayerEntity.sendMessage("§cЭтот предмет не поддерживает статистику!");
            return;
        }

        PersistentDataContainer Data = Item.getItemMeta().getPersistentDataContainer();
        boolean CurrentlyDisabled = Data.has(StatsDisabledKey, PersistentDataType.BYTE);

        if (CurrentlyDisabled) {
            Data.remove(StatsDisabledKey);
            PlayerEntity.sendMessage("§aСбор статистики §2включен§a для этого предмета");
        } else {
            Data.set(StatsDisabledKey, PersistentDataType.BYTE, (byte) 1);
            PlayerEntity.sendMessage("§cСбор статистики §4отключен§c для этого предмета");
        }

        Item.setItemMeta(Item.getItemMeta());
    }
}
