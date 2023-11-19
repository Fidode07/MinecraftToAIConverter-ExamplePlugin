package de.fido.listeners;

import de.fido.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerHandlerListener implements Listener {
    @EventHandler
    public void onUserJoins(PlayerJoinEvent e) {
        Main.players.add(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeaves(PlayerQuitEvent e) {
        Main.players.remove(e.getPlayer());
    }
}
