package VanillaPlus.Commands;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClearStatsCommand implements CommandExecutor {

    private final JavaPlugin PluginInstance;
    private final Map<UUID, Long> ConfirmationMap = new HashMap<>();
    private static final long ConfirmationTimeout = 10000;

    public ClearStatsCommand(JavaPlugin Plugin) {
        this.PluginInstance = Plugin;
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
            PlayerEntity.sendMessage("§cЭтот предмет не содержит статистики!");
            return true;
        }

        Long LastConfirmTime = ConfirmationMap.get(PlayerEntity.getUniqueId());
        long CurrentTime = System.currentTimeMillis();

        if (LastConfirmTime != null && (CurrentTime - LastConfirmTime) < ConfirmationTimeout) {
            ClearAllStats(Item);
            ConfirmationMap.remove(PlayerEntity.getUniqueId());
            PlayerEntity.sendMessage("§aВся статистика предмета очищена!");
            return true;
        }

        ConfirmationMap.put(PlayerEntity.getUniqueId(), CurrentTime);
        PlayerEntity.sendMessage("§eПовторите команду в течение 10 секунд для подтверждения");
        PlayerEntity.sendMessage("§7Вся статистика предмета будет безвозвратно удалена!");

        return true;
    }

    private void ClearAllStats(ItemStack Item) {
        ItemMeta Meta = Item.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();

        NamespacedKey[] StatsKeys = {
                new NamespacedKey(PluginInstance, "blocks_mined"),
                new NamespacedKey(PluginInstance, "mobs_killed"),
                new NamespacedKey(PluginInstance, "max_damage"),
                new NamespacedKey(PluginInstance, "all_damage"),
                new NamespacedKey(PluginInstance, "shots"),
                new NamespacedKey(PluginInstance, "oneshots"),
                new NamespacedKey(PluginInstance, "distance"),
                new NamespacedKey(PluginInstance, "hits_received"),
                new NamespacedKey(PluginInstance, "damage_blocked"),
                new NamespacedKey(PluginInstance, "damage_absorbed")
        };

        for (NamespacedKey Key : StatsKeys) {
            if (Data.has(Key, PersistentDataType.INTEGER)) {
                Data.remove(Key);
            }
            if (Data.has(Key, PersistentDataType.DOUBLE)) {
                Data.remove(Key);
            }
        }

        if (Meta.hasLore()) {
            Meta.lore(new java.util.ArrayList<>());
        }

        Item.setItemMeta(Meta);
    }
}
