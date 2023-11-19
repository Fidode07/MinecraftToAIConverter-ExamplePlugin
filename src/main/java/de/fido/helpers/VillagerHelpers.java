package de.fido.helpers;

import de.fido.main.Main;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VillagerHelpers {
    public static void Start() {
        Runnable scannerRunnable = () -> {
            for (Entity villager : Main.villagers) {
                List<Player> nearbyPlayers = getNearbyPlayers(villager.getLocation());
                if (nearbyPlayers.size() != 1) {
                    villager.setGlowing(false);
                    villager.setCustomNameVisible(false);
                    continue;
                }
                Player p = nearbyPlayers.get(0);
                List<Entity> nearbyVillagersFromPlayer = getNearbyVillagers(p.getLocation());

                if (nearbyVillagersFromPlayer.size() != 1) {
                    villager.setGlowing(false);
                    villager.setCustomNameVisible(false);
                    continue;
                }

                if (villager.isGlowing() && villager.isCustomNameVisible()) continue;
                villager.setCustomNameVisible(true);
                villager.setGlowing(true);
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(scannerRunnable, 0, Main.ScannerInterval, TimeUnit.MICROSECONDS);
    }

    public static List<Entity> getNearbyVillagers(Location location) {
        List<Entity> nearbyEntities = (List<Entity>) location.getWorld().getNearbyEntities(location,
                Main.VillagerThreshold,
                Main.VillagerThreshold,
                Main.VillagerThreshold);
        List<Entity> nearbyVillagers = new ArrayList<>();

        for (Entity curEntity : nearbyEntities) {
            if (!(curEntity.getType() == EntityType.VILLAGER)) continue;
            nearbyVillagers.add(curEntity);
        }
        return nearbyVillagers;
    }

    public static List<Player> getNearbyPlayers(Location location) {
        List<Entity> nearbyEntities = (List<Entity>) location.getWorld().getNearbyEntities(location, Main.VillagerThreshold,
                Main.VillagerThreshold,
                Main.VillagerThreshold);
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Entity entity : nearbyEntities) {
            if (!(entity.getType() == EntityType.PLAYER)) continue;
            nearbyPlayers.add((Player) entity);
        }
        return nearbyPlayers;
    }
}
