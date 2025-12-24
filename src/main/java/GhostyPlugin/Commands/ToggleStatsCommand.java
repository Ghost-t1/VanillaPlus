package GhostyPlugin.Commands;

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
    }

    @Override
    public boolean onCommand(CommandSender Sender, Command Command, String Label, String[] Args) {
        if (!(Sender instanceof Player PlayerEntity)) {
            Sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        ItemStack Item = PlayerEntity.getInventory().getItemInMainHand();

        if (Item == null || Item.getType() == Material.AIR) {
            PlayerEntity.sendMessage("§cВозьмите предмет в руку!");
            return true;
        }

        if (!Item.hasItemMeta()) {
            PlayerEntity.sendMessage("§cЭтот предмет не поддерживает статистику!");
            return true;
        }

        PersistentDataContainer Data = Item.getItemMeta().getPersistentDataContainer();
        boolean CurrentlyDisabled = Data.has(StatsDisabledKey, PersistentDataType.BYTE);

        if (CurrentlyDisabled) {
            Data.remove(StatsDisabledKey);
            PlayerEntity.sendMessage("§a✓ Сбор статистики §2включен§a для этого предмета");
        } else {
            Data.set(StatsDisabledKey, PersistentDataType.BYTE, (byte) 1);
            PlayerEntity.sendMessage("§c✗ Сбор статистики §4отключен§c для этого предмета");
        }

        Item.setItemMeta(Item.getItemMeta());
        return true;
    }
}
