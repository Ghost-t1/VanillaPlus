package VanillaPlus.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VanillaPlusCommand implements CommandExecutor, TabCompleter {

    private final ToggleStatsCommand ToggleStats;
    private final ClearStatsCommand ClearStats;
    private final VanillaPlus.ConfigManager Config;

    public VanillaPlusCommand(ToggleStatsCommand ToggleStats, ClearStatsCommand ClearStats, VanillaPlus.ConfigManager Config) {
        this.ToggleStats = ToggleStats;
        this.ClearStats = ClearStats;
        this.Config = Config;
    }

    @Override
    public boolean onCommand(CommandSender Sender, Command Cmd, String Label, String[] Args) {
        if (Args.length == 0) {
            Sender.sendMessage("§cИспользование: /vp <reload|togglestats|clearstats>");
            return true;
        }

        String SubCommand = Args[0].toLowerCase();
        String[] SubArgs = Arrays.copyOfRange(Args, 1, Args.length);

        switch (SubCommand) {
            case "clearstats":
                return ClearStats.onCommand(Sender, Cmd, Label, SubArgs);
            case "togglestats":
                return ToggleStats.onCommand(Sender, Cmd, Label, SubArgs);
            case "reload":
                if (!Sender.isOp()) {
                    Sender.sendMessage("§cТы не имеет права использовать эту команду.");
                    return true;
                }
                Config.ReloadConfig();
                Sender.sendMessage("§aКонфигурация успешно перезагружена!");
                return true;
            default:
                Sender.sendMessage("§cНеизвестная команда: " + SubCommand);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender Sender, Command Cmd, String Alias, String[] Args) {
        List<String> Completions = new ArrayList<>();
        
        if (Args.length == 1) {
            List<String> Commands = Arrays.asList("reload", "togglestats", "clearstats");
            String Input = Args[0].toLowerCase();
            
            for (String Command : Commands) {
                if (Command.startsWith(Input)) {
                    Completions.add(Command);
                }
            }
        }
        
        return Completions;
    }
}
