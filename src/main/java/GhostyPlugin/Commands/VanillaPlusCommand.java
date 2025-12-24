package GhostyPlugin.Commands;

import GhostyPlugin.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanillaPlusCommand implements CommandExecutor {

    private final ConfigManager Config;

    public VanillaPlusCommand(ConfigManager Config) {
        this.Config = Config;
    }

    @Override
    public boolean onCommand(CommandSender Sender, Command Command, String Label, String[] Args) {
        if (!(Sender instanceof Player PlayerEntity)) {
            Sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        if (Args.length == 0) {
            SendHelp(PlayerEntity);
            return true;
        }

        String SubCommand = Args[0].toLowerCase();

        if (SubCommand.equals("toolstats")) {
            if (Args.length < 2) {
                PlayerEntity.sendMessage("§eИспользование: /vanillaplus toolstats <on|off>");
                return true;
            }

            String Action = Args[1].toLowerCase();

            if (Action.equals("on")) {
                Config.SetToolStatsEnabled(true);
                PlayerEntity.sendMessage("§a✓ Сбор статистики инструментов §2включен");
            } else if (Action.equals("off")) {
                Config.SetToolStatsEnabled(false);
                PlayerEntity.sendMessage("§c✗ Сбор статистики инструментов §4отключен");
            } else {
                PlayerEntity.sendMessage("§cНеверный аргумент. Используйте: on или off");
            }

            return true;
        }

        SendHelp(PlayerEntity);
        return true;
    }

    private void SendHelp(Player PlayerEntity) {
        PlayerEntity.sendMessage("§6=== VanillaPlus Команды ===");
        PlayerEntity.sendMessage("§e/vp toolstats <on|off> §7- Вкл/выкл сбор статистики");
    }
}
