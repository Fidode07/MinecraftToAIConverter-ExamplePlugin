package de.fido.commands;

import com.google.gson.*;
import de.fido.helpers.VillagerHelpers;
import de.fido.main.Main;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class AskCMD implements CommandExecutor {
    private final Gson gson = new Gson();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String labels, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.prefix + "Sorry, you must be a player to use that command!");
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("minecrafttoaiconverter.ask_command")) {
            p.sendMessage(Main.prefix + ChatColor.RED + "Sorry, you don't have the permissions to use that command!");
            return true;
        }

        List<Entity> nearbyVillagers = VillagerHelpers.getNearbyVillagers(p.getLocation());

        if (nearbyVillagers.isEmpty()) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You are not nearby Villagers!");
            return true;
        }

        if (nearbyVillagers.size() > 1) {
            p.sendMessage(Main.prefix + ChatColor.RED + "There are too many villagers!");
            return true;
        }

        String msg = String.join(" ", args);

        // get server response
        String serverResponse = getServerResponse(msg);
        if (serverResponse.equalsIgnoreCase("")) {
            p.sendMessage(Main.prefix + ChatColor.RED + "Sorry, unable to get server response!");
            return true;
        }

        JsonObject parsedServerResponse = parseServerResponse(serverResponse);
        if (parsedServerResponse.get("status").getAsString().equals("error")) {
            p.sendMessage(Main.prefix + ChatColor.RED + "Sorry, unable to get server response! Please try it again.");
            return true;
        }

        Entity askedVillager = nearbyVillagers.get(0);
        Villager villager = (Villager) askedVillager;

        switch (parsedServerResponse.get("tag").getAsString()) {
            case "stopword":
                // this happens, if the user wrote something that is not in the dataset
                askedVillager.setCustomName("huh?");
                return true;
            case "sell-diamond-swords":
                // user asked for diamond swords so add a trade for that
                MerchantRecipe diamondSwordTrade = new MerchantRecipe(
                        new ItemStack(Material.DIAMOND_SWORD),
                        3
                );

                // add ingredient
                diamondSwordTrade.addIngredient(new ItemStack(Material.EMERALD, 16));

                // add trade to villager
                List<MerchantRecipe> villagerRecipes = new ArrayList<>();
                villagerRecipes.add(diamondSwordTrade);
                villagerRecipes.addAll(villager.getRecipes());
                villager.setRecipes(villagerRecipes);
                break;
        }
        showResponseInVillager(askedVillager, parsedServerResponse);
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, SoundCategory.VOICE, 8f, 8f);
        p.sendMessage(buildPlayerPrefix(p.getName()) + ChatColor.RED + " -> " + buildVillagerPrefix(villager) + " " + ChatColor.WHITE + msg);
        return true;
    }

    private String buildVillagerPrefix(Villager villager) {
        return ChatColor.GOLD + "[" + ChatColor.WHITE + "(Villager) " + villager.getCareer().name() + ChatColor.GOLD + "]";
    }

    private String buildPlayerPrefix(String name) {
        return ChatColor.GOLD + "[" + ChatColor.WHITE + name + ChatColor.GOLD + "]";
    }

    private void showResponseInVillager(Entity villager, JsonObject serverResponse) {
        ArrayList<String> allResponses = parseResponses(serverResponse);
        String responseMsg = getRandomStringOfList(allResponses);

        typeMessageIntoVillager(villager, responseMsg);
        villager.setCustomNameVisible(true);
    }

    private void typeMessageIntoVillager(Entity villager, String response) {
        new BukkitRunnable() {
            private int currentIndex = 0;

            @Override
            public void run() {
                // Check if the villager is still valid (not removed or killed)
                if (!villager.isValid() || villager.getType() != EntityType.VILLAGER) {
                    this.cancel();
                    return;
                }

                // Get the substring up to the current character index
                String partialMessage = response.substring(0, currentIndex + 1);

                // Set the partial message as the custom name
                villager.setCustomName(partialMessage);

                currentIndex++;

                // Check if the entire message has been displayed
                if (currentIndex >= response.length()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    private String getRandomStringOfList(ArrayList<String> responses) {
        return responses.get(new Random().nextInt(responses.size()));
    }

    private ArrayList<String> parseResponses(JsonObject obj) {
        ArrayList<String> responses = new ArrayList<>();

        for (JsonElement jsonElement : obj.getAsJsonArray("responses")) {
            responses.add(jsonElement.getAsString());
        }
        return responses;
    }

    private String buildJsonString(String userMsg) {
        Map<String, String> inputData = new HashMap<>();
        inputData.put("sentence", userMsg);
        return gson.toJson(inputData);
    }

    private JsonObject parseServerResponse(String serverResponse) {
        return new JsonParser().parse(serverResponse).getAsJsonObject();
    }

    private String getServerResponse(String inputString) {
        String response;
        try (Socket s = new Socket("localhost", 3030)) {
            // init in- and output
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader rd = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw.println(buildJsonString(inputString));

            response = rd.readLine();

            pw.close();
            rd.close();
        } catch (IOException e) {
            Main.console.sendMessage(Main.prefix + ChatColor.RED + "Something went wrong while connecting to backend!");
            return "";
        }
        return response;
    }
}
