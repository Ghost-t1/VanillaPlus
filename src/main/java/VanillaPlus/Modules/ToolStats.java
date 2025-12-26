package VanillaPlus.Modules;

import VanillaPlus.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("all")
public class ToolStats implements Listener {

    private final NamespacedKey BlocksMinedKey;
    private final NamespacedKey MobsKilledKey;
    private final NamespacedKey MaxDamageKey;
    private final NamespacedKey AllDamageKey;
    private final NamespacedKey ShotsKey;
    private final NamespacedKey OneshotsKey;
    private final NamespacedKey DistanceKey;
    private final NamespacedKey StatsDisabledKey;
    private final NamespacedKey HitsReceivedKey;
    private final NamespacedKey DamageBlockedKey;
    private final NamespacedKey DamageAbsorbedKey;
    private final NamespacedKey DamageReflectedKey;
    private final NamespacedKey BlockedHitsKey;
    private final NamespacedKey BlockedDamageKey;
    private final NamespacedKey XpSpentKey;
    private final NamespacedKey FullRepairsKey;
    private final NamespacedKey LastLoreUpdateKey;
    private final NamespacedKey UpdateSpamCountKey;
    private final NamespacedKey PlayerStatsDisabledKey;
    private final ConfigManager Config;
    private final JavaPlugin Plugin;
    private final Map<UUID, Location> LastElytraLocations = new HashMap<>();

    public ToolStats(JavaPlugin Plugin, ConfigManager Config) {
        this.Plugin = Plugin;
        this.Config = Config;
        this.BlocksMinedKey = new NamespacedKey(Plugin, "blocks_mined");
        this.MobsKilledKey = new NamespacedKey(Plugin, "mobs_killed");
        this.MaxDamageKey = new NamespacedKey(Plugin, "max_damage");
        this.AllDamageKey = new NamespacedKey(Plugin, "all_damage");
        this.ShotsKey = new NamespacedKey(Plugin, "shots");
        this.OneshotsKey = new NamespacedKey(Plugin, "oneshots");
        this.DistanceKey = new NamespacedKey(Plugin, "distance");
        this.StatsDisabledKey = new NamespacedKey(Plugin, "stats_disabled");
        this.HitsReceivedKey = new NamespacedKey(Plugin, "hits_received");
        this.DamageBlockedKey = new NamespacedKey(Plugin, "damage_blocked");
        this.DamageAbsorbedKey = new NamespacedKey(Plugin, "damage_absorbed");
        this.DamageReflectedKey = new NamespacedKey(Plugin, "damage_reflected");
        this.BlockedHitsKey = new NamespacedKey(Plugin, "blocked_hits");
        this.BlockedDamageKey = new NamespacedKey(Plugin, "blocked_damage");
        this.XpSpentKey = new NamespacedKey(Plugin, "xp_spent");
        this.FullRepairsKey = new NamespacedKey(Plugin, "full_repairs");
        this.LastLoreUpdateKey = new NamespacedKey(Plugin, "last_lore_update");
        this.UpdateSpamCountKey = new NamespacedKey(Plugin, "update_spam_count");
        this.PlayerStatsDisabledKey = new NamespacedKey(Plugin, "player_stats_disabled");
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent Event) {
        Player PlayerEntity = Event.getPlayer();
        ItemStack Tool = PlayerEntity.getInventory().getItemInMainHand();
        String ToolType = GetToolTypeForItem(Tool.getType());

        if (ToolType == null || !Config.IsToolInfoEnabled(ToolType))
            return;

        if (IsStatsDisabled(Tool))
            return;

        if (IsPlayerStatsDisabled(PlayerEntity))
            return;

        IncrementStat(Tool, BlocksMinedKey, ToolType);
    }

    @EventHandler
    public void OnEntityDeath(EntityDeathEvent Event) {
        Player Killer = Event.getEntity().getKiller();
        if (Killer == null)
            return;

        ItemStack Weapon = Killer.getInventory().getItemInMainHand();
        String ToolType = GetToolTypeForItem(Weapon.getType());

        if (ToolType == null || !Config.IsToolInfoEnabled(ToolType))
            return;

        if (IsStatsDisabled(Weapon))
            return;

        if (IsPlayerStatsDisabled(Killer))
            return;

        IncrementStat(Weapon, MobsKilledKey, ToolType);
    }

    @EventHandler
    public void OnEntityDamageByEntity(EntityDamageByEntityEvent Event) {
        if (Event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.THORNS) {
            if (Event.getDamager() instanceof Player PlayerEntity) {
                double Damage = Event.getFinalDamage();
                ItemStack[] ArmorPieces = PlayerEntity.getInventory().getArmorContents();

                for (ItemStack ArmorPiece : ArmorPieces) {
                    if (ArmorPiece == null || !ArmorPiece.hasItemMeta())
                        continue;

                    if (ArmorPiece.getItemMeta().hasEnchant(org.bukkit.enchantments.Enchantment.THORNS)) {
                        String ArmorType = GetArmorType(ArmorPiece.getType());
                        if (ArmorType != null && Config.IsToolInfoEnabled(ArmorType)
                                && !IsStatsDisabled(ArmorPiece) && !IsPlayerStatsDisabled(PlayerEntity)) {
                            IncrementArmorStat(ArmorPiece, DamageReflectedKey, Damage, ArmorType);
                        }
                    }
                }
            }
            return;
        }

        if (!(Event.getDamager() instanceof Player PlayerEntity))
            return;

        if (!(Event.getEntity() instanceof LivingEntity Target))
            return;

        ItemStack Weapon = PlayerEntity.getInventory().getItemInMainHand();
        String ToolType = GetToolTypeForItem(Weapon.getType());

        if (ToolType == null || !Config.IsToolInfoEnabled(ToolType))
            return;

        if (IsStatsDisabled(Weapon))
            return;

        if (IsPlayerStatsDisabled(PlayerEntity))
            return;

        double Damage = Event.getFinalDamage();

        UpdateMaxDamage(Weapon, Damage, ToolType);
        IncrementAllDamage(Weapon, Damage, ToolType);

        if (Target.getHealth() <= Damage) {
            IncrementStat(Weapon, OneshotsKey, ToolType);
        }
    }

    @EventHandler
    public void OnEntityDamage(org.bukkit.event.entity.EntityDamageEvent Event) {
        if (!(Event.getEntity() instanceof Player PlayerEntity))
            return;

        double DamageBeforeArmor = Event.getDamage();
        double FinalDamage = Event.getFinalDamage();
        double AbsorbedDamage = Math.max(0, DamageBeforeArmor - FinalDamage);

        ItemStack[] ArmorPieces = PlayerEntity.getInventory().getArmorContents();

        for (ItemStack ArmorPiece : ArmorPieces) {
            if (ArmorPiece == null || ArmorPiece.getType() == Material.AIR)
                continue;

            String ArmorType = GetArmorType(ArmorPiece.getType());
            if (ArmorType == null || !Config.IsToolInfoEnabled(ArmorType))
                continue;

            if (IsStatsDisabled(ArmorPiece))
                continue;

            if (IsPlayerStatsDisabled(PlayerEntity))
                continue;

            IncrementStat(ArmorPiece, HitsReceivedKey, ArmorType);

            if (AbsorbedDamage > 0) {
                IncrementArmorStat(ArmorPiece, DamageAbsorbedKey, AbsorbedDamage / 4.0, ArmorType);
            }
        }

        if (PlayerEntity.isBlocking()) {
            ItemStack Shield = PlayerEntity.getInventory().getItemInOffHand();
            if (Shield.getType() == Material.SHIELD) {
                String ShieldType = "shield";
                if (Config.IsToolInfoEnabled(ShieldType) && !IsStatsDisabled(Shield)
                        && !IsPlayerStatsDisabled(PlayerEntity)) {
                    IncrementStat(Shield, BlockedHitsKey, ShieldType);
                    IncrementArmorStat(Shield, BlockedDamageKey, FinalDamage, ShieldType);
                }
            }
        }
    }

    @EventHandler
    public void OnPlayerItemMend(org.bukkit.event.player.PlayerItemMendEvent Event) {
        ItemStack Item = Event.getItem();
        int RepairAmount = Event.getRepairAmount();
        int ExperienceOrb = Event.getExperienceOrb().getExperience();

        String ItemType = GetToolTypeForItem(Item.getType());
        if (ItemType == null) {
            ItemType = GetArmorType(Item.getType());
        }
        if (ItemType == null && Item.getType() == Material.SHIELD) {
            ItemType = "shield";
        }

        if (ItemType == null || !Config.IsToolInfoEnabled(ItemType))
            return;

        if (IsStatsDisabled(Item))
            return;

        if (IsPlayerStatsDisabled(Event.getPlayer()))
            return;

        IncrementArmorStat(Item, XpSpentKey, ExperienceOrb, ItemType);

        if (Item.hasItemMeta() && Item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable Damageable) {
            if (Damageable.getDamage() - RepairAmount <= 0) {
                IncrementStat(Item, FullRepairsKey, ItemType);
            }
        }
    }

    @EventHandler
    public void OnProjectileLaunch(ProjectileLaunchEvent Event) {
        if (!(Event.getEntity().getShooter() instanceof Player PlayerEntity))
            return;

        if (!(Event.getEntity() instanceof Arrow))
            return;

        ItemStack Weapon = PlayerEntity.getInventory().getItemInMainHand();
        Material WeaponType = Weapon.getType();

        if (WeaponType != Material.BOW && WeaponType != Material.CROSSBOW)
            return;

        String ToolType = GetToolTypeForItem(WeaponType);

        if (!Config.IsToolInfoEnabled(ToolType))
            return;

        if (IsStatsDisabled(Weapon))
            return;

        if (IsPlayerStatsDisabled(PlayerEntity))
            return;

        IncrementStat(Weapon, ShotsKey, ToolType);
    }

    @EventHandler
    public void OnPlayerQuit(org.bukkit.event.player.PlayerQuitEvent Event) {
        LastElytraLocations.remove(Event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void OnPlayerMove(PlayerMoveEvent Event) {
        Player PlayerEntity = Event.getPlayer();

        if (!PlayerEntity.isGliding())
            return;

        ItemStack Elytra = PlayerEntity.getInventory().getChestplate();

        if (Elytra == null || Elytra.getType() != Material.ELYTRA)
            return;

        if (!Config.IsToolInfoEnabled("elytra"))
            return;

        if (IsStatsDisabled(Elytra))
            return;

        if (IsPlayerStatsDisabled(PlayerEntity))
            return;

        Location CurrentLocation = PlayerEntity.getLocation();
        Location LastLocation = LastElytraLocations.get(PlayerEntity.getUniqueId());

        if (LastLocation != null && LastLocation.getWorld().equals(CurrentLocation.getWorld())) {
            Vector Movement = CurrentLocation.toVector().subtract(LastLocation.toVector());
            Movement.setY(0);
            double Distance = Movement.length();

            if (Distance > 0.1 && Distance < 50) {
                IncrementDistance(Elytra, Distance, "elytra");
            }
        }

        LastElytraLocations.put(PlayerEntity.getUniqueId(), CurrentLocation);
    }

    private String GetToolType(Material MaterialType) {
        String Name = MaterialType.name();
        if (Name.endsWith("_PICKAXE"))
            return "pickaxe";
        if (Name.endsWith("_AXE"))
            return "axe";
        if (Name.endsWith("_SHOVEL"))
            return "shovel";
        if (Name.endsWith("_HOE"))
            return "hoe";
        if (Name.endsWith("_SWORD"))
            return "sword";
        if (Name.equals("MACE"))
            return "mace";
        if (Name.equals("STICK"))
            return "stick";
        if (Name.equals("BOW"))
            return "bow";
        if (Name.equals("CROSSBOW"))
            return "crossbow";
        if (Name.equals("ELYTRA"))
            return "elytra";
        if (Name.equals("SHIELD"))
            return "shield";

        return null;
    }

    private String GetToolTypeForItem(Material MaterialType) {
        String MaterialName = MaterialType.name().toLowerCase();

        if (Config.IsToolInfoEnabled(MaterialName)) {
            return MaterialName;
        }

        return GetToolType(MaterialType);
    }

    private String GetArmorType(Material MaterialType) {
        String Name = MaterialType.name();
        if (Name.endsWith("_HELMET"))
            return "helmet";
        if (Name.endsWith("_CHESTPLATE"))
            return "chestplate";
        if (Name.endsWith("_LEGGINGS"))
            return "leggings";
        if (Name.endsWith("_BOOTS"))
            return "boots";

        return null;
    }

    private boolean IsStatsDisabled(ItemStack Item) {
        if (Item == null || !Item.hasItemMeta())
            return false;

        PersistentDataContainer Data = Item.getItemMeta().getPersistentDataContainer();
        return Data.has(StatsDisabledKey, PersistentDataType.BYTE);
    }

    private boolean IsPlayerStatsDisabled(Player PlayerEntity) {
        PersistentDataContainer Data = PlayerEntity.getPersistentDataContainer();
        return Data.has(PlayerStatsDisabledKey, PersistentDataType.BYTE);
    }

    private void IncrementStat(ItemStack Tool, NamespacedKey Key, String ToolType) {
        if (Tool == null || Tool.getType() == Material.AIR)
            return;

        ItemMeta Meta = Tool.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        int CurrentCount = Data.getOrDefault(Key, PersistentDataType.INTEGER, 0);
        CurrentCount++;
        Data.set(Key, PersistentDataType.INTEGER, CurrentCount);

        if (ShouldUpdateLore(Data)) {
            UpdateLore(Meta, Data, ToolType);
            Tool.setItemMeta(Meta);
        } else {
            Tool.setItemMeta(Meta);
        }
    }

    private boolean ShouldUpdateLore(PersistentDataContainer Data) {
        long LastUpdate = Data.getOrDefault(LastLoreUpdateKey, PersistentDataType.LONG, 0L);
        int SpamCount = Data.getOrDefault(UpdateSpamCountKey, PersistentDataType.INTEGER, 0);
        long CurrentTime = System.currentTimeMillis();

        if (CurrentTime - LastUpdate > 1000) {
            SpamCount = 0;
            Data.set(UpdateSpamCountKey, PersistentDataType.INTEGER, 0);
        }

        long Cooldown = 0;
        if (SpamCount > 5) {
            Cooldown = 2000;
        }

        long TargetTime = LastUpdate + Cooldown;

        if (CurrentTime >= TargetTime) {
            if (CurrentTime - LastUpdate < 500) {
                SpamCount++;
                Data.set(UpdateSpamCountKey, PersistentDataType.INTEGER, SpamCount);
            }

            Data.set(LastLoreUpdateKey, PersistentDataType.LONG, CurrentTime);
            return true;
        }
        return false;
    }

    private void UpdateMaxDamage(ItemStack Tool, double Damage, String ToolType) {
        if (Tool == null || Tool.getType() == Material.AIR)
            return;

        ItemMeta Meta = Tool.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        double CurrentMax = Data.getOrDefault(MaxDamageKey, PersistentDataType.DOUBLE, 0.0);

        if (Damage > CurrentMax) {
            Data.set(MaxDamageKey, PersistentDataType.DOUBLE, Damage);

            if (ShouldUpdateLore(Data)) {
                UpdateLore(Meta, Data, ToolType);
            }
            Tool.setItemMeta(Meta);
        }
    }

    private void IncrementAllDamage(ItemStack Tool, double Damage, String ToolType) {
        if (Tool == null || Tool.getType() == Material.AIR)
            return;

        ItemMeta Meta = Tool.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        double CurrentTotal = Data.getOrDefault(AllDamageKey, PersistentDataType.DOUBLE, 0.0);
        CurrentTotal += Damage;
        Data.set(AllDamageKey, PersistentDataType.DOUBLE, CurrentTotal);

        if (ShouldUpdateLore(Data)) {
            UpdateLore(Meta, Data, ToolType);
        }
        Tool.setItemMeta(Meta);
    }

    private void IncrementDistance(ItemStack Tool, double Distance, String ToolType) {
        if (Tool == null || Tool.getType() == Material.AIR)
            return;

        ItemMeta Meta = Tool.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        double CurrentDistance = Data.getOrDefault(DistanceKey, PersistentDataType.DOUBLE, 0.0);
        CurrentDistance += Distance;
        Data.set(DistanceKey, PersistentDataType.DOUBLE, CurrentDistance);

        if (ShouldUpdateLore(Data)) {
            UpdateLore(Meta, Data, ToolType);
        }
        Tool.setItemMeta(Meta);
    }

    private void IncrementArmorStat(ItemStack ArmorPiece, NamespacedKey Key, double Value, String ArmorType) {
        if (ArmorPiece == null || ArmorPiece.getType() == Material.AIR)
            return;

        ItemMeta Meta = ArmorPiece.getItemMeta();
        if (Meta == null)
            return;

        PersistentDataContainer Data = Meta.getPersistentDataContainer();
        double CurrentValue = Data.getOrDefault(Key, PersistentDataType.DOUBLE, 0.0);
        CurrentValue += Value;
        Data.set(Key, PersistentDataType.DOUBLE, CurrentValue);

        if (ShouldUpdateLore(Data)) {
            UpdateLore(Meta, Data, ArmorType);
        }
        ArmorPiece.setItemMeta(Meta);
    }

    private void UpdateLore(ItemMeta Meta, PersistentDataContainer Data, String ToolType) {
        List<Component> Lore = Meta.lore();
        if (Lore == null) {
            Lore = new ArrayList<>();
        }

        List<String> ConfigLines = Config.GetToolInfoLines(ToolType);
        if (ConfigLines.isEmpty())
            return;

        Lore.removeIf(Line -> {
            String PlainText = PlainTextComponentSerializer.plainText().serialize(Line);
            return PlainText.startsWith("\u200B");
        });

        for (String ConfigLine : ConfigLines) {
            String ProcessedLine = ReplacePlaceholders(ConfigLine, Data);
            Component NewComponent = ParseText(ProcessedLine)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);

            String RegexPattern = CreateRegexFromConfig(ConfigLine);
            boolean Replaced = false;

            for (int i = 0; i < Lore.size(); i++) {
                String PlainLine = PlainTextComponentSerializer.plainText().serialize(Lore.get(i));

                if (PlainLine.matches(RegexPattern)) {
                    Lore.set(i, NewComponent);
                    Replaced = true;
                    break;
                }
            }

            if (!Replaced) {
                if (Lore.size() > 0 && ConfigLine.equals(ConfigLines.get(0))) {
                   Component LastLine = Lore.get(Lore.size() - 1);
                   String LastLineText = PlainTextComponentSerializer.plainText().serialize(LastLine);
                   if (!LastLineText.trim().isEmpty()) {
                       Lore.add(Component.empty());
                   }
                }
                Lore.add(NewComponent);
            }
        }

        Meta.lore(Lore);
    }

    private String CreateRegexFromConfig(String ConfigLine) {
        String PlainConfig = StripColorCodes(ConfigLine);

        String[] Parts = PlainConfig.split("%[a-z_]+%", -1);
        List<String> Placeholders = new ArrayList<>();
        Matcher Pm = Pattern.compile("%[a-z_]+%").matcher(PlainConfig);
        while (Pm.find()) {
            Placeholders.add(Pm.group());
        }

        StringBuilder Regex = new StringBuilder("^");
        
        for (int i = 0; i < Parts.length; i++) {
            if (!Parts[i].isEmpty()) {
                Regex.append(Pattern.quote(Parts[i]));
            }
            if (i < Placeholders.size()) {
                Regex.append(".*");
            }
        }
        Regex.append("$");
        
        return Regex.toString();
    }
    
    private String StripColorCodes(String Text) {
        Text = Text.replaceAll("<[^>]+>", "");
        Text = Text.replaceAll("&#[0-9a-fA-F]{6}", "");
        return Text.replaceAll("&[0-9a-fk-or]", "");
    }

    private String ReplacePlaceholders(String Line, PersistentDataContainer Data) {
        NumberFormat IntFormatter = NumberFormat.getInstance(Locale.US);

        int Blocks = Data.getOrDefault(BlocksMinedKey, PersistentDataType.INTEGER, 0);
        int Kills = Data.getOrDefault(MobsKilledKey, PersistentDataType.INTEGER, 0);
        int Shots = Data.getOrDefault(ShotsKey, PersistentDataType.INTEGER, 0);
        int Oneshots = Data.getOrDefault(OneshotsKey, PersistentDataType.INTEGER, 0);
        double MaxDamage = Data.getOrDefault(MaxDamageKey, PersistentDataType.DOUBLE, 0.0);
        double AllDamage = Data.getOrDefault(AllDamageKey, PersistentDataType.DOUBLE, 0.0);
        double Distance = Data.getOrDefault(DistanceKey, PersistentDataType.DOUBLE, 0.0);
        int HitsReceived = Data.getOrDefault(HitsReceivedKey, PersistentDataType.INTEGER, 0);
        double DamageBlocked = Data.getOrDefault(DamageBlockedKey, PersistentDataType.DOUBLE, 0.0);
        double DamageAbsorbed = Data.getOrDefault(DamageAbsorbedKey, PersistentDataType.DOUBLE, 0.0);
        double DamageReflected = Data.getOrDefault(DamageReflectedKey, PersistentDataType.DOUBLE, 0.0);
        int BlockedHits = Data.getOrDefault(BlockedHitsKey, PersistentDataType.INTEGER, 0);
        double BlockedDamage = Data.getOrDefault(BlockedDamageKey, PersistentDataType.DOUBLE, 0.0);
        double XpSpent = Data.getOrDefault(XpSpentKey, PersistentDataType.DOUBLE, 0.0);
        int FullRepairs = Data.getOrDefault(FullRepairsKey, PersistentDataType.INTEGER, 0);

        return Line
                .replace("%blocks%", IntFormatter.format(Blocks))
                .replace("%kills%", IntFormatter.format(Kills))
                .replace("%shots%", IntFormatter.format(Shots))
                .replace("%oneshots%", IntFormatter.format(Oneshots))
                .replace("%maxdamage%", IntFormatter.format(Math.round(MaxDamage)))
                .replace("%alldamage%", IntFormatter.format(Math.round(AllDamage)))
                .replace("%distance%", IntFormatter.format(Math.round(Distance)))
                .replace("%hits%", IntFormatter.format(HitsReceived))
                .replace("%blocked%", IntFormatter.format(Math.round(DamageBlocked)))
                .replace("%absorbed%", IntFormatter.format(Math.round(DamageAbsorbed)))
                .replace("%reflected%", IntFormatter.format(Math.round(DamageReflected)))
                .replace("%blocked_hits%", IntFormatter.format(BlockedHits))
                .replace("%blocked_damage%", IntFormatter.format(Math.round(BlockedDamage)))
                .replace("%xp_spent%", IntFormatter.format(Math.round(XpSpent)))
                .replace("%full_repairs%", IntFormatter.format(FullRepairs));
    }

    private Component ParseText(String Text) {
        Matcher HexMatcher = Pattern.compile("&#([0-9a-fA-F]{6})").matcher(Text);
        StringBuffer HexBuffer = new StringBuffer();
        while (HexMatcher.find()) {
            HexMatcher.appendReplacement(HexBuffer, "<#$1>");
        }
        HexMatcher.appendTail(HexBuffer);
        Text = HexBuffer.toString();

        Text = Text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<b>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<u>")
                .replace("&o", "<i>")
                .replace("&r", "<reset>");

        return MiniMessage.miniMessage().deserialize(Text);
    }
}
