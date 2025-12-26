package VanillaPlus.Modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.view.AnvilView;

public class AnvilMechanics implements Listener {

    @EventHandler
    public void OnPrepareAnvil(PrepareAnvilEvent Event) {
        if (Event.getView() instanceof AnvilView View) {
            View.setMaximumRepairCost(Integer.MAX_VALUE);
            
            if (View.getRepairCost() > 39) {
                View.setRepairCost(39);
            }
        }
    }
}
