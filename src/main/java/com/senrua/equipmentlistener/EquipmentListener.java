package com.senrua.equipmentlistener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class EquipmentListener extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("EquipmentListener-1.3 has been Enabled.");
        saveDefaultConfig(); // Save default config.yml if it doesn't exist
    }

    @Override
    public void onDisable() {
        getLogger().info("EquipmentListener-1.3 has been Disabled.");
    }


    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        checkHpAndDealDamage(event.getPlayer());
        checkManaAndAdjust(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();

            // Get the configured delay from the config.yml file
            int delay = getConfig().getInt("delay");

            // Schedule the check with the configured delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkHpAndDealDamage(player);
                    checkManaAndAdjust(player);
                }
            }.runTaskLater(this, delay);
        }
    }


    private double getPlaceholderValue(Player player, String placeholder) throws NumberFormatException {
        if (PlaceholderAPI.containsPlaceholders(placeholder)) {
            String value = PlaceholderAPI.setPlaceholders(player, placeholder);

            if (!value.isEmpty()) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    getLogger().warning("Error parsing placeholder value for " + placeholder + " from player " + player.getName() + ": " + e.getMessage());
                    throw e; // throw exception here
                }
            }
        }
        return 0;
    }

    private void checkManaAndAdjust(Player player) {
        try {
            double mana = getPlaceholderValue(player, "%aureliumskills_mana%");
            double manaMax = getPlaceholderValue(player, "%aureliumskills_mana_max%");

            if (mana > manaMax) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mana set " + player.getName() + " " + Math.min(mana, manaMax));
            }
        } catch (NumberFormatException e) {
            getLogger().warning("Error checking mana for player " + player.getName() + ": " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void checkHpAndDealDamage(Player player) {
        try {
            double hp = getPlaceholderValue(player, "%aureliumskills_hp%");
            double hpMax = getPlaceholderValue(player, "%aureliumskills_hp_max%");

            if (hp > hpMax) {
                player.setHealth(Math.min(hp, hpMax));
            }
        } catch (NumberFormatException e) {
            getLogger().warning("Error checking hp for player " + player.getName() + ": " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
