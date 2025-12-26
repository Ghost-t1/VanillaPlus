package VanillaPlus.Modules;

import VanillaPlus.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class OxidizeCopper implements Listener {

    private final ConfigManager Configuration;
    private final org.bukkit.plugin.java.JavaPlugin Plugin;
    private final java.util.Set<Block> ActiveOxidations = new java.util.HashSet<>();

    public OxidizeCopper(ConfigManager Configuration, org.bukkit.plugin.java.JavaPlugin Plugin) {
        this.Configuration = Configuration;
        this.Plugin = Plugin;
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent Event) {
        if (!Configuration.IsOxidizeCopperEnabled()) return;
        if (Event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Block ClickedBlock = Event.getClickedBlock();
        if (ClickedBlock == null) return;

        Player PlayerEntity = Event.getPlayer();
        ItemStack Item = PlayerEntity.getInventory().getItemInMainHand();

        if (Item.getType() != Material.POTION && Item.getType() != Material.SPLASH_POTION && Item.getType() != Material.LINGERING_POTION) {
             if (Item.getType() != Material.POTION) return;
        }

        if (!(Item.getItemMeta() instanceof PotionMeta Meta)) return;
        if (Meta.getBasePotionType() != PotionType.WATER) return;

        Material NextStage = GetNextOxidationStage(ClickedBlock.getType());
        if (NextStage != null) {
            Event.setCancelled(true);

            if (TryOxidize(ClickedBlock, NextStage)) {
                PlayerEntity.getWorld().playSound(ClickedBlock.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.0f);
                PlayerEntity.getWorld().playSound(ClickedBlock.getLocation(), Sound.BLOCK_POINTED_DRIPSTONE_DRIP_WATER, 1.0f, 1.0f);
                
                if (PlayerEntity.getGameMode() != GameMode.CREATIVE) {
                    Item.setAmount(Item.getAmount() - 1);
                    PlayerEntity.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                }
            }
        }
    }

    @EventHandler
    public void OnProjectileHit(ProjectileHitEvent Event) {
        if (!Configuration.IsOxidizeCopperEnabled()) return;

        if (!(Event.getEntity() instanceof ThrownPotion Potion)) return;

        if (!(Potion.getItem().getItemMeta() instanceof PotionMeta Meta)) return;
        if (Meta.getBasePotionType() != PotionType.WATER) return;

        Location HitLocation = Potion.getLocation();
        if (Event.getHitBlock() != null) {
            HitLocation = Event.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
        }

        int Radius = 2;
        Block CenterBlock = HitLocation.getBlock();
        
        for (int X = -Radius; X <= Radius; X++) {
            for (int Y = -Radius; Y <= Radius; Y++) {
                for (int Z = -Radius; Z <= Radius; Z++) {
                    if (X * X + Y * Y + Z * Z > Radius * Radius) continue;
                    
                    Block TargetBlock = CenterBlock.getRelative(X, Y, Z);
                    Material NextStage = GetNextOxidationStage(TargetBlock.getType());
                    if (NextStage != null) {
                        TryOxidize(TargetBlock, NextStage);
                    }
                }
            }
        }
    }

    private static final BlockFace[] CARDINAL_FACES = {
        BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent Event) {
        if (!Configuration.IsOxidizeCopperEnabled()) return;
        Block Placed = Event.getBlock();
        Material NextStage = GetNextOxidationStage(Placed.getType());
        
        if (NextStage != null && IsTouchingWater(Placed)) {
            TryOxidize(Placed, NextStage);
        }
    }

    @EventHandler
    public void OnPlayerBucketEmpty(PlayerBucketEmptyEvent Event) {
        if (!Configuration.IsOxidizeCopperEnabled()) return;
        if (Event.getBucket() != Material.WATER_BUCKET) return;
        
        Block WaterBlock = Event.getBlockClicked().getRelative(Event.getBlockFace());
        CheckAndOxidizeNeighbors(WaterBlock);
    }

    @EventHandler
    public void OnBlockFromTo(BlockFromToEvent Event) {
        if (!Configuration.IsOxidizeCopperEnabled()) return;
        if (Event.getBlock().getType() != Material.WATER) return;
        
        CheckAndOxidizeNeighbors(Event.getToBlock());
    }

    private boolean IsTouchingWater(Block BlockToCheck) {
        if (BlockToCheck.getBlockData() instanceof Waterlogged WaterloggedData) {
            if (WaterloggedData.isWaterlogged()) return true;
        }
        
        for (BlockFace Face : CARDINAL_FACES) {
            if (BlockToCheck.getRelative(Face).getType() == Material.WATER) return true;
        }
        return false;
    }

    private void CheckAndOxidizeNeighbors(Block Center) {
        for (BlockFace Face : CARDINAL_FACES) {
            Block Neighbor = Center.getRelative(Face);
            Material NextStage = GetNextOxidationStage(Neighbor.getType());
            if (NextStage != null) {
                TryOxidize(Neighbor, NextStage);
            }
        }
    }

    private boolean TryOxidize(Block TargetBlock, Material NextType) {
        if (NextType != null) {
            org.bukkit.block.data.BlockData OldData = TargetBlock.getBlockData();
            TargetBlock.setType(NextType);
            org.bukkit.block.data.BlockData NewData = TargetBlock.getBlockData();

            try {
                NewData.merge(OldData);
                TargetBlock.setBlockData(NewData);
            } catch (Exception e) {
                if (OldData instanceof Waterlogged && NewData instanceof Waterlogged) {
                    ((Waterlogged) NewData).setWaterlogged(((Waterlogged) OldData).isWaterlogged());
                }
                if (OldData instanceof Orientable && NewData instanceof Orientable) {
                    ((Orientable) NewData).setAxis(((Orientable) OldData).getAxis());
                }
                if (OldData instanceof Rotatable && NewData instanceof Rotatable) {
                    ((Rotatable) NewData).setRotation(((Rotatable) OldData).getRotation());
                }
                if (OldData instanceof Directional && NewData instanceof Directional) {
                    ((Directional) NewData).setFacing(((Directional) OldData).getFacing());
                }
                if (OldData instanceof Bisected && NewData instanceof Bisected) {
                    ((Bisected) NewData).setHalf(((Bisected) OldData).getHalf());
                }
                if (OldData instanceof Stairs && NewData instanceof Stairs) {
                    ((Stairs) NewData).setShape(((Stairs) OldData).getShape());
                }
                if (OldData instanceof Slab && NewData instanceof Slab) {
                    ((Slab) NewData).setType(((Slab) OldData).getType());
                }
                if (OldData instanceof Lightable && NewData instanceof Lightable) {
                    ((Lightable) NewData).setLit(((Lightable) OldData).isLit());
                }
                if (OldData instanceof Powerable && NewData instanceof Powerable) {
                    ((Powerable) NewData).setPowered(((Powerable) OldData).isPowered());
                }
                if (OldData instanceof Openable && NewData instanceof Openable) {
                    ((Openable) NewData).setOpen(((Openable) OldData).isOpen());
                }
                if (OldData instanceof Door && NewData instanceof Door) {
                    ((Door) NewData).setHinge(((Door) OldData).getHinge());
                }
                
                TargetBlock.setBlockData(NewData);
            }

            TargetBlock.getWorld().spawnParticle(Particle.BUBBLE_POP, TargetBlock.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2);
            ScheduleNextOxidation(TargetBlock);
            return true;
        }
        return false;
    }

    private void ScheduleNextOxidation(Block TargetBlock) {
        if (ActiveOxidations.contains(TargetBlock)) return;
        ActiveOxidations.add(TargetBlock);

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                ActiveOxidations.remove(TargetBlock);
                if (!TargetBlock.getChunk().isLoaded()) return;

                Material NextStage = GetNextOxidationStage(TargetBlock.getType());
                if (NextStage != null && IsTouchingWater(TargetBlock)) {
                    TryOxidize(TargetBlock, NextStage);
                }
            }
        }.runTaskLater(Plugin, 60L);
    }

    private Material GetNextOxidationStage(Material Current) {
        String Name = Current.name();

        if (Name.contains("WAXED") || Name.contains("OXIDIZED")) {
            return null;
        }

        if (!Name.contains("COPPER")) {
            return null;
        }

        String NextName;

        if (Name.equals("COPPER_BLOCK")) {
            NextName = "EXPOSED_COPPER";
        } else if (Name.startsWith("WEATHERED_")) {
            NextName = Name.replace("WEATHERED_", "OXIDIZED_");
        } else if (Name.startsWith("EXPOSED_")) {
            NextName = Name.replace("EXPOSED_", "WEATHERED_");
        } else {
            NextName = "EXPOSED_" + Name;
        }

        return Material.getMaterial(NextName);
    }
}
