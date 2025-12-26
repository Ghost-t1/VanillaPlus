package VanillaPlus.Modules;

import VanillaPlus.ConfigManager;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FastLeafDecay implements Listener {

    private final JavaPlugin Plugin;
    private final ConfigManager Config;
    private final Set<Block> ScheduledLeaves = new HashSet<>();

    public FastLeafDecay(JavaPlugin Plugin, ConfigManager Config) {
        this.Plugin = Plugin;
        this.Config = Config;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void OnBlockBreak(BlockBreakEvent Event) {
        if (!Config.IsFastLeafDecayEnabled())
            return;

        Block BrokenBlock = Event.getBlock();

        if (Tag.LOGS.isTagged(BrokenBlock.getType())) {
            if (Event.getPlayer() != null && Event.getPlayer().isSneaking()) {
                return;
            }
            StartDecay(BrokenBlock);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void OnLeavesDecay(LeavesDecayEvent Event) {
        if (!Config.IsFastLeafDecayEnabled())
            return;
        
        Block Leaf = Event.getBlock();
        if (Tag.LEAVES.isTagged(Leaf.getType())) {
            Leaves LeafData = (Leaves) Leaf.getBlockData();
            if (LeafData.isPersistent()) return;
        }

        StartDecay(Leaf);
    }

    private void StartDecay(Block Center) {
        Set<Block> LeavesToDecay = FindConnectedLeaves(Center);

        int MinDelay = Config.GetFastLeafDecayMinDelay();
        int MaxDelay = Config.GetFastLeafDecayMaxDelay();

        for (Block Leaf : LeavesToDecay) {
            if (ScheduledLeaves.contains(Leaf)) continue;
            ScheduledLeaves.add(Leaf);

            long DelaySeconds = ThreadLocalRandom.current().nextInt(MinDelay, MaxDelay + 1);
            long DelayTicks = DelaySeconds * 20L;
            DelayTicks += ThreadLocalRandom.current().nextInt(0, 20);

            new BukkitRunnable() {
                @Override
                public void run() {
                    ScheduledLeaves.remove(Leaf);
                    if (!Tag.LEAVES.isTagged(Leaf.getType())) return;
                    
                    Leaves LeafData = (Leaves) Leaf.getBlockData();
                    if (LeafData.isPersistent()) return;

                    if (IsConnectedToLog(Leaf)) return;

                    Leaf.breakNaturally();
                    PlayEffect(Leaf);
                }
            }.runTaskLater(Plugin, DelayTicks);
        }
    }

    private Set<Block> FindConnectedLeaves(Block Start) {
        Set<Block> FoundLeaves = new HashSet<>();
        Queue<Block> Queue = new LinkedList<>();
        Set<Block> Visited = new HashSet<>();

        AddNeighbors(Start, Queue, Visited);

        int MaxLeaves = Config.GetFastLeafDecayMaxLeaves() * 5;
        int Count = 0;

        while (!Queue.isEmpty() && Count < MaxLeaves) {
            Block Current = Queue.poll();

            if (Tag.LEAVES.isTagged(Current.getType())) {
                Leaves LeafData = (Leaves) Current.getBlockData();
                if (!LeafData.isPersistent() && !IsConnectedToLog(Current)) {
                    FoundLeaves.add(Current);
                    Count++;
                    AddNeighbors(Current, Queue, Visited);
                }
            }
        }
        return FoundLeaves;
    }

    private void AddNeighbors(Block Center, Queue<Block> Queue, Set<Block> Visited) {
        for (int X = -1; X <= 1; X++) {
            for (int Y = -1; Y <= 1; Y++) {
                for (int Z = -1; Z <= 1; Z++) {
                    if (X == 0 && Y == 0 && Z == 0) continue;

                    Block Rel = Center.getRelative(X, Y, Z);
                    if (Visited.contains(Rel)) continue;
                    Visited.add(Rel);

                    if (Tag.LEAVES.isTagged(Rel.getType())) {
                        Queue.add(Rel);
                    }
                }
            }
        }
    }

    private boolean IsConnectedToLog(Block Leaf) {
        Leaves LeafData = (Leaves) Leaf.getBlockData();
        if (LeafData.getDistance() == 7) return false;

        return CheckLogConnection(Leaf);
    }

    private boolean CheckLogConnection(Block Leaf) {
        Set<Block> Visited = new HashSet<>();
        Queue<Block> Queue = new LinkedList<>();
        Queue.add(Leaf);
        Visited.add(Leaf);
        
        Map<Block, Integer> Dist = new HashMap<>();
        Dist.put(Leaf, 0);

        while (!Queue.isEmpty()) {
            Block Current = Queue.poll();
            int D = Dist.get(Current);

            if (D >= 4) continue;

            for (int X = -1; X <= 1; X++) {
                for (int Y = -1; Y <= 1; Y++) {
                    for (int Z = -1; Z <= 1; Z++) {
                        if (X == 0 && Y == 0 && Z == 0) continue;
                        
                        Block Rel = Current.getRelative(X, Y, Z);
                        if (Visited.contains(Rel)) continue;
                        
                        if (Tag.LOGS.isTagged(Rel.getType())) return true;
                        
                        if (Tag.LEAVES.isTagged(Rel.getType())) {
                            Leaves L = (Leaves) Rel.getBlockData();
                            if (!L.isPersistent()) {
                                Visited.add(Rel);
                                Dist.put(Rel, D + 1);
                                Queue.add(Rel);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void PlayEffect(Block Leaf) {
        Leaf.getWorld().spawnParticle(org.bukkit.Particle.COMPOSTER, Leaf.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0);
        Leaf.getWorld().playSound(Leaf.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 0.5f, 1.2f);
    }
}
