package com.wasteofplastic.greenhouses.greenhouse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.wasteofplastic.greenhouses.Greenhouses;
import com.wasteofplastic.greenhouses.Settings;

/**
 * Monitors the greenhouses and grows things, adds weather etc.
 * @author ben
 *
 */
public class Ecosystem implements Listener {
    private final Greenhouses plugin;
    private final static List<Biome> SNOWBIOMES = new ArrayList<Biome>();
    static {
        for (Biome biome : Biome.values()) {
            if (biome.name().contains("COLD") || biome.name().contains("ICE") || biome.name().contains("FROZEN")) {
                SNOWBIOMES.add(biome);
            }
        }
    }
    private BukkitTask snow = null;
    private List<Greenhouse> snowGlobes = new ArrayList<Greenhouse>();

    public Ecosystem(final Greenhouses plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChangeEvent(final WeatherChangeEvent e) {
        if (!Settings.worldName.contains(e.getWorld().getName())) {
            return;
        }
        if (e.toWeatherState()) {
            // It's raining
            plugin.logger(3,"It's raining!");
            startSnow();
        } else {
            // It's stopped raining!
            plugin.logger(3,"Stopped raining!");
            if (snow != null)
                snow.cancel();
        }
    }

    private void startSnow() {
        if (snow != null) {
            // Cancel the old snow task
            snow.cancel();
        }

        // Spin off scheduler
        snowGlobes.clear();
        snow = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {
                // Run through each greenhouse - only bother with snow biomes
                plugin.logger(3,"started scheduler");
                // Check all the greenhouses and their hoppers and build a list of snow greenhouses that exist now
                List<Greenhouse> toBeRemoved = new ArrayList<Greenhouse>();
                for (Greenhouse g : plugin.getGreenhouses()) {
                    plugin.logger(3,"Testing greenhouse biome : " + g.getBiome().toString());
                    if (SNOWBIOMES.contains(g.getBiome())) {
                        plugin.logger(3,"Snow biome found!");
                        // If it is already on the list, just snow, otherwise check if the hopper has water
                        if (!snowGlobes.contains(g)) {
                            Location hopper = g.getRoofHopperLocation();
                            if (hopper != null) {
                                plugin.logger(3,"Hopper location:" + hopper.toString());
                                Block b = hopper.getBlock();
                                // Check the hopper is still there
                                if (b.getType().equals(Material.HOPPER)) {
                                    Hopper h = (Hopper)b.getState();
                                    plugin.logger(3,"Hopper found!");
                                    // Check what is in the hopper
                                    if (h.getInventory().contains(Material.WATER_BUCKET)) {
                                        // Remove the water in the bucket
                                        // We cannot remove an itemstack the easy way because on acid island the bucket is changed to acid
                                        for (ItemStack item : h.getInventory().getContents()) {
                                            if (item != null && item.getType().equals(Material.WATER_BUCKET)) {
                                                // Remove one from this item stack
                                                // Water buckets in theory do no stack...
                                                ItemStack i = item.clone();
                                                i.setAmount(1);
                                                h.getInventory().removeItem(i);
                                                h.getInventory().addItem(new ItemStack(Material.BUCKET));
                                                break;
                                            }
                                        }
                                        // Add to list
                                        snowGlobes.add(g);
                                    }
                                } else {
                                    // Greenhouse is broken or no longer has a hopper when it should
                                    plugin.getLogger().warning("Hopper missing from greenhouse at " + g.getRoofHopperLocation().getBlockX() + " "
                                            + g.getRoofHopperLocation().getBlockY() + " " + g.getRoofHopperLocation().getBlockZ());
                                    plugin.getLogger().warning("Removing greenhouse");
                                    toBeRemoved.add(g);
                                }
                            }
                        }
                    }
                }
                if (!snowGlobes.isEmpty()) {
                    snowOn(snowGlobes);
                }
                // Remove any greenhouses that need it
                for (Greenhouse g : toBeRemoved) {
                    //UUID owner = g.getOwner();
                    plugin.removeGreenhouse(g);
                    //plugin.players.save(owner);
                }
                plugin.saveGreenhouses();
            }
        }, 0L, (Settings.snowSpeed * 20L)); // Every 30 seconds

    }

    protected void snowOn(List<Greenhouse> snowGlobes) {
        for (Greenhouse g : snowGlobes) {
            plugin.logger(3,"snowing in a greenhouse");
            // Chance of snow
            if (Math.random()>Settings.snowChanceGlobal)
                return;
            g.snow();
        }
    }

    /**
     * Removes any greenhouses that are currently in the eco system
     * @param g
     */
    public void remove(Greenhouse g) {
        if (snowGlobes.contains(g))
            snowGlobes.remove(g);	
    }

}