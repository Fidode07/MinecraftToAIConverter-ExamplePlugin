package de.fido.main;

import de.fido.commands.AskCMD;
import de.fido.helpers.VillagerHelpers;
import de.fido.listeners.PlayerHandlerListener;
import de.fido.listeners.VillagerHandlerListener;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin {
    public final static String prefix = ChatColor.GOLD + "[" + ChatColor.LIGHT_PURPLE + "MinecraftToAIConverter" +
            ChatColor.GOLD + "] ";
    public static final ArrayList<Player> players = new ArrayList<>();
    public static final ArrayList<Entity> villagers = new ArrayList<>();

    public static ConsoleCommandSender console;
    public final static double VillagerThreshold = 1.75; // in blocks
    public final static int ScannerInterval = 1500; // in milliseconds
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        this.getCommand("ask").setExecutor(new AskCMD());
        getServer().getPluginManager().registerEvents(new PlayerHandlerListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerHandlerListener(), this);

        console = getServer().getConsoleSender();
        players.addAll(getServer().getOnlinePlayers());
        for (World w : getServer().getWorlds()) {
            List<Entity> entities = w.getEntities();
            for (Entity e : entities) {
                if (!(e.getType() == EntityType.VILLAGER)) continue;
                villagers.add(e);
            }
        }
        VillagerHelpers.Start();
        console.sendMessage(prefix + "Boot was successfully!");
    }

    @Override
    public void onDisable() {
    }

    public static Main getPlugin() {
        return instance;
    }
}
