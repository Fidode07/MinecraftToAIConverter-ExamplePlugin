package de.fido.listeners;

import de.fido.main.Main;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class VillagerHandlerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onVillagerSpawn(EntitySpawnEvent e) {
        if (!(e.getEntityType() == EntityType.VILLAGER)) return;
        Main.villagers.add(e.getEntity());
    }

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent e) {
        if (!(e.getEntityType() == EntityType.VILLAGER)) return;
        Main.villagers.remove(e.getEntity());
    }
}
