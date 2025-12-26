package VanillaPlus.Modules;

import VanillaPlus.Database.SQLite;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Type;
import java.util.List;

public class BlockPersistence implements Listener {

    private final SQLite Database;
    private final Gson GsonSerializer = new Gson();

    public BlockPersistence(JavaPlugin Plugin, SQLite Database) {
        this.Database = Database;
        Plugin.getServer().getPluginManager().registerEvents(this, Plugin);
    }

    private boolean IsTargetBlock(Material Mat) {
        return Mat == Material.CARVED_PUMPKIN ||
                Mat.name().endsWith("_HEAD") ||
                Mat.name().endsWith("_SKULL");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void OnBlockPlace(BlockPlaceEvent Event) {
        ItemStack Item = Event.getItemInHand();
        if (!IsTargetBlock(Item.getType()))
            return;

        if (!Item.hasItemMeta())
            return;
        ItemMeta Meta = Item.getItemMeta();

        boolean HasName = Meta.hasDisplayName();
        boolean HasLore = Meta.hasLore();

        if (HasName || HasLore) {
            String CustomName = HasName ? GsonComponentSerializer.gson().serialize(Meta.displayName()) : null;
            String LoreJson = null;

            if (HasLore && Meta.lore() != null) {
                List<String> SerializedLore = Meta.lore().stream()
                        .map(GsonComponentSerializer.gson()::serialize)
                        .toList();
                LoreJson = GsonSerializer.toJson(SerializedLore);
            }

            Database.SaveBlockData(Event.getBlock().getLocation(), CustomName, LoreJson);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnBlockBreak(BlockBreakEvent Event) {
        Block TargetBlock = Event.getBlock();
        Location Loc = TargetBlock.getLocation();

        SQLite.BlockInfo Info = Database.GetBlockData(Loc);
        if (Info != null) {
            Event.setDropItems(false);
            SpawnCustomDrop(TargetBlock.getType(), Loc, Info);
            Database.RemoveBlockData(Loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent Event) {
        if (Event.getEntityType() == org.bukkit.entity.EntityType.ENDERMAN) {
            Block B = Event.getBlock();
            if (Database.GetBlockData(B.getLocation()) != null && Event.getTo() == Material.AIR) {
                Database.RemoveBlockData(B.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnEntityExplode(EntityExplodeEvent Event) {
        HandleExplosion(Event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnBlockExplode(BlockExplodeEvent Event) {
        HandleExplosion(Event.blockList());
    }

    private void HandleExplosion(List<Block> Blocks) {
        Blocks.removeIf(B -> {
            SQLite.BlockInfo Info = Database.GetBlockData(B.getLocation());
            if (Info != null) {
                Material Type = B.getType();
                B.setType(Material.AIR);
                SpawnCustomDrop(Type, B.getLocation(), Info);
                Database.RemoveBlockData(B.getLocation());
                return true;
            }
            return false;
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnPistonExtend(BlockPistonExtendEvent Event) {
        for (Block B : Event.getBlocks()) {
            if (Database.GetBlockData(B.getLocation()) != null) {
                Event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OnPistonRetract(BlockPistonRetractEvent Event) {
        for (Block B : Event.getBlocks()) {
            if (Database.GetBlockData(B.getLocation()) != null) {
                Event.setCancelled(true);
                return;
            }
        }
    }

    private void SpawnCustomDrop(Material Type, Location Loc, SQLite.BlockInfo Info) {
        ItemStack Drop = new ItemStack(Type);
        ItemMeta Meta = Drop.getItemMeta();

        if (Info.CustomName != null) {
            Meta.displayName(GsonComponentSerializer.gson().deserialize(Info.CustomName));
        }

        if (Info.LoreJson != null) {
            Type ListType = new TypeToken<List<String>>() {
            }.getType();
            List<String> SerializedLore = GsonSerializer.fromJson(Info.LoreJson, ListType);
            List<Component> Lore = SerializedLore.stream()
                    .map(GsonComponentSerializer.gson()::deserialize)
                    .toList();
            Meta.lore(Lore);
        }

        Drop.setItemMeta(Meta);
        Loc.getWorld().dropItemNaturally(Loc, Drop);
    }

}
