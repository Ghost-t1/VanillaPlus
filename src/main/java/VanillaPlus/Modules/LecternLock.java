package VanillaPlus.Modules;

import VanillaPlus.Database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("all")
public class LecternLock implements Listener {

    private final JavaPlugin Plugin;
    private final SQLite Database;
    private final Map<Location, String> LockCache = new HashMap<>();

    public LecternLock(JavaPlugin Plugin, SQLite Database) {
        this.Plugin = Plugin;
        this.Database = Database;
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent Event) {
        if (Event.getClickedBlock() == null || Event.getClickedBlock().getType() != Material.LECTERN)
            return;

        Block LecternBlock = Event.getClickedBlock();
        Location Loc = LecternBlock.getLocation();
        Player Player = Event.getPlayer();
        UUID PlayerUUID = Player.getUniqueId();

        if (Event.getAction() == Action.RIGHT_CLICK_BLOCK && Event.getItem() != null
                && Event.getItem().getType() == Material.GLASS_PANE) {
            Lectern LecternData = (Lectern) LecternBlock.getState();
            Inventory Inv = LecternData.getInventory();

            if (Inv.isEmpty())
                return;
            
            String OwnerUUID = GetLockOwner(Loc);
            
            if (OwnerUUID != null) {
                String OwnerName = GetPlayerName(OwnerUUID);
                Player.sendMessage("§cЭта кафедра уже заблокирована игроком " + OwnerName);
                return; 
            }

            Inv.removeItem(new ItemStack(Material.GLASS_PANE, 1));
            
            SetLock(Loc, PlayerUUID.toString());
            String OwnerName = Player.getName();
            
            Player.sendMessage("§cКафедра заблокирована игроком " + OwnerName);
            Player.playSound(Loc, Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.0f);

            Event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void OnLecternBookTake(PlayerTakeLecternBookEvent Event) {
        Block LecternBlock = Event.getLectern().getBlock();
        Location Loc = LecternBlock.getLocation();
        String OwnerUUID = GetLockOwner(Loc);
        Player Player = Event.getPlayer();

        if (OwnerUUID != null) {
            if (!OwnerUUID.equals(Player.getUniqueId().toString())) {
                String OwnerName = GetPlayerName(OwnerUUID);
                Player.sendMessage("§cЭта книга заблокирована игроком " + OwnerName);
                Event.setCancelled(true);
            } else {
                RemoveLock(Loc);
                Player.sendMessage("§cКафедра разблокирована.");
            }
        }
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent Event) {
        if (Event.getBlock().getType() != Material.LECTERN)
            return;

        Location Loc = Event.getBlock().getLocation();
        String OwnerUUID = GetLockOwner(Loc);

        if (OwnerUUID != null) {
            if (!OwnerUUID.equals(Event.getPlayer().getUniqueId().toString())) {
                String OwnerName = GetPlayerName(OwnerUUID);
                Event.getPlayer().sendMessage("§cВы не можете сломать кафедру, заблокированную игроком " + OwnerName);
                Event.setCancelled(true);
            } else {
                RemoveLock(Loc);
            }
        }
    }

    private String GetLockOwner(Location Loc) {
        if (LockCache.containsKey(Loc)) {
            return LockCache.get(Loc);
        }
        String Owner = Database.GetLecternLock(Loc);
        LockCache.put(Loc, Owner);
        return Owner;
    }

    private void SetLock(Location Loc, String OwnerUUID) {
        Database.SaveLecternLock(Loc, OwnerUUID);
        LockCache.put(Loc, OwnerUUID);
    }

    private void RemoveLock(Location Loc) {
        Database.RemoveLecternLock(Loc);
        LockCache.remove(Loc);
        LockCache.put(Loc, null);
    }
    
    private String GetPlayerName(String UUIDStr) {
        try {
            UUID Id = UUID.fromString(UUIDStr);
            org.bukkit.OfflinePlayer OffPlayer = Bukkit.getOfflinePlayer(Id);
            String Name = OffPlayer.getName();
            return (Name != null) ? Name : "Призрак~ - ~";
        } catch (Exception e) {
            return "Призрак";
        }
    }
}
