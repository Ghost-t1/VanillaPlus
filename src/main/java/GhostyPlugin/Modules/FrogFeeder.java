package GhostyPlugin.Modules;

import GhostyPlugin.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FrogFeeder implements Listener {

    private final JavaPlugin PluginInstance;
    private final ConfigManager Config;
    private final NamespacedKey AngryKey;
    private final NamespacedKey NextParticleTimeKey;
    private final Map<UUID, Long> Cooldowns = new HashMap<>();
    private final Random RandomGenerator = new Random();

    public FrogFeeder(JavaPlugin Plugin, ConfigManager Config) {
        this.PluginInstance = Plugin;
        this.Config = Config;
        this.AngryKey = new NamespacedKey(Plugin, "is_angry");
        this.NextParticleTimeKey = new NamespacedKey(Plugin, "next_particle_time");
        StartParticleTask();
    }

    private void StartParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long CurrentTime = System.currentTimeMillis();

                for (org.bukkit.World World : Bukkit.getWorlds()) {
                    for (Frog FrogEntity : World.getEntitiesByClass(Frog.class)) {
                        PersistentDataContainer Data = FrogEntity.getPersistentDataContainer();
                        Integer AngryState = Data.get(AngryKey, PersistentDataType.INTEGER);

                        if (AngryState == null) {
                            continue;
                        }

                        Long NextParticleTime = Data.get(NextParticleTimeKey, PersistentDataType.LONG);
                        if (NextParticleTime == null || CurrentTime >= NextParticleTime) {
                            Location Loc = FrogEntity.getLocation().add(0, 0.5, 0);

                            int MinSeconds;
                            int MaxSeconds;

                            if (AngryState == 1) {
                                Loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, Loc, 1, 0.2, 0.2, 0.2, 0.0);
                                MinSeconds = Config.GetFrogAngryMin();
                                MaxSeconds = Config.GetFrogAngryMax();
                            } else {
                                Loc.getWorld().spawnParticle(Particle.HEART, Loc, 1, 0.2, 0.2, 0.2, 0.0);
                                MinSeconds = Config.GetFrogFriendlyMin();
                                MaxSeconds = Config.GetFrogFriendlyMax();
                            }

                            int RandomSeconds = MinSeconds + RandomGenerator.nextInt(MaxSeconds - MinSeconds + 1);
                            long NextTime = CurrentTime + (RandomSeconds * 1000L);
                            Data.set(NextParticleTimeKey, PersistentDataType.LONG, NextTime);
                        }
                    }
                }
            }
        }.runTaskTimer(PluginInstance, 20L, 20L);
    }

    @EventHandler
    public void OnPlayerInteractEntity(PlayerInteractEntityEvent Event) {
        if (Event.getRightClicked().getType() != EntityType.FROG) {
            return;
        }

        Player PlayerEntity = Event.getPlayer();
        ItemStack Item = PlayerEntity.getInventory().getItemInMainHand();

        if (Item.getType() != Material.MAGMA_CREAM) {
            return;
        }

        Frog FrogEntity = (Frog) Event.getRightClicked();

        PersistentDataContainer Data = FrogEntity.getPersistentDataContainer();
        Integer AngryState = Data.get(AngryKey, PersistentDataType.INTEGER);

        if (AngryState == null) {
            boolean IsAngry = Math.random() < Config.GetFrogAngryChance();
            AngryState = IsAngry ? 1 : 0;
            Data.set(AngryKey, PersistentDataType.INTEGER, AngryState);

            int MinSeconds;
            int MaxSeconds;

            if (AngryState == 1) {
                MinSeconds = Config.GetFrogAngryMin();
                MaxSeconds = Config.GetFrogAngryMax();
            } else {
                MinSeconds = Config.GetFrogFriendlyMin();
                MaxSeconds = Config.GetFrogFriendlyMax();
            }

            int RandomSeconds = MinSeconds + RandomGenerator.nextInt(MaxSeconds - MinSeconds + 1);
            long NextTime = System.currentTimeMillis() + (RandomSeconds * 1000L);
            Data.set(NextParticleTimeKey, PersistentDataType.LONG, NextTime);
        }

        if (AngryState == 1) {
            Location Loc = FrogEntity.getLocation();
            Loc.getWorld().playSound(Loc, Sound.ENTITY_FROG_DEATH, 1.0f, 0.5f);
            Loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, Loc.add(0, 0.5, 0), 5, 0.3, 0.3, 0.3, 0.0);
            Event.setCancelled(true);
            return;
        }

        int CooldownSeconds = Config.GetFrogCooldown();
        if (CooldownSeconds > 0) {
            long Now = System.currentTimeMillis();
            long LastUsed = Cooldowns.getOrDefault(PlayerEntity.getUniqueId(), 0L);
            if (Now - LastUsed < CooldownSeconds * 1000L) {
                return;
            }
            Cooldowns.put(PlayerEntity.getUniqueId(), Now);
        }

        Material FrogLightType;
        Frog.Variant Variant = FrogEntity.getVariant();

        if (Variant == Frog.Variant.WARM) {
            FrogLightType = Material.PEARLESCENT_FROGLIGHT;
        } else if (Variant == Frog.Variant.COLD) {
            FrogLightType = Material.VERDANT_FROGLIGHT;
        } else {
            FrogLightType = Material.OCHRE_FROGLIGHT;
        }

        Location Loc = FrogEntity.getLocation();
        Loc.getWorld().dropItemNaturally(Loc, new ItemStack(FrogLightType));

        Loc.getWorld().playSound(Loc, Sound.ENTITY_FROG_EAT, 1.0f, 1.0f);
        Loc.getWorld().spawnParticle(Particle.HEART, Loc.add(0, 0.5, 0), 5, 0.3, 0.3, 0.3, 0.0);

        if (PlayerEntity.getGameMode() != GameMode.CREATIVE) {
            Item.setAmount(Item.getAmount() - 1);
        }

        Event.setCancelled(true);
    }
}
