package VanillaPlus.Modules;

import VanillaPlus.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ConcreteHardener implements Listener {

    private final ConfigManager Config;

    public ConcreteHardener(ConfigManager Config) {
        this.Config = Config;
    }

    @EventHandler(ignoreCancelled = true)
    public void OnPlayerInteract(org.bukkit.event.player.PlayerInteractEvent Event) {
        if (!Config.IsConcreteHardenerEnabled())
            return;
        if (Event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        Block ClickedBlock = Event.getClickedBlock();
        if (ClickedBlock == null || ClickedBlock.getType() != Material.WATER_CAULDRON)
            return;

        Block BlockBelow = ClickedBlock.getRelative(0, -1, 0);
        Material BelowType = BlockBelow.getType();
        if (BelowType == Material.MAGMA_BLOCK || BelowType == Material.CAMPFIRE ||
            BelowType == Material.SOUL_CAMPFIRE || BelowType == Material.FIRE ||
            BelowType == Material.SOUL_FIRE || BelowType == Material.LAVA) {
            return;
        }

        ItemStack HandItem = Event.getItem();
        if (HandItem == null || !IsConcretePowder(HandItem.getType()))
            return;

        if (Event.getPlayer().isSneaking())
            return;

        Levelled CauldronData = (Levelled) ClickedBlock.getBlockData();
        int Levels = CauldronData.getLevel();
        if (Levels == 0)
            return;

        Event.setCancelled(true);

        Event.getPlayer().updateInventory();

        int ToConvert = Math.min(HandItem.getAmount(), 8);
        Material PowderType = HandItem.getType();

        HandItem.setAmount(HandItem.getAmount() - ToConvert);
        Event.getPlayer().getInventory().setItemInMainHand(HandItem);

        Material ConcreteType = GetConcrete(PowderType);
        ItemStack Result = new ItemStack(ConcreteType, ToConvert);
        HashMap<Integer, ItemStack> Leftover = Event.getPlayer().getInventory().addItem(Result);
        if (!Leftover.isEmpty()) {
            for (ItemStack Drop : Leftover.values()) {
                Event.getPlayer().getWorld().dropItemNaturally(Event.getPlayer().getLocation(), Drop);
            }
        }

        Location Center = ClickedBlock.getLocation().add(0.5, 0.8, 0.5);
        Center.getWorld().spawnParticle(Particle.BUBBLE_POP, Center, 15, 0.3, 0.1, 0.3, 0.05);
        Center.getWorld().playSound(Center, Sound.ENTITY_GENERIC_SPLASH, 0.5f, 1.5f);

        int NewLevel = Levels - 1;

        if (NewLevel == 0) {
            ClickedBlock.setType(Material.CAULDRON);
        } else {
            CauldronData.setLevel(NewLevel);
            ClickedBlock.setBlockData(CauldronData);
        }

        Event.getPlayer().updateInventory();
    }

    private boolean IsConcretePowder(Material Mat) {
        return Mat.name().endsWith("_CONCRETE_POWDER");
    }

    private Material GetConcrete(Material Powder) {
        String Name = Powder.name().replace("_POWDER", "");
        return Material.getMaterial(Name);
    }
}
