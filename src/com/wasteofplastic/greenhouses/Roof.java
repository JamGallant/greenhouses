package com.wasteofplastic.greenhouses;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Contains the parameters of a greenhouse roof
 * @author tastybento
 *
 */
public class Roof {
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    private int height;
    private boolean roofFound;
    final List<Material> roofBlocks = Arrays.asList(new Material[]{Material.GLASS, Material.STAINED_GLASS, Material.HOPPER, Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.GLOWSTONE});


    /**
     * Finds a roof from a starting location under the roof and characterizes it
     * @param loc
     */
    public Roof(Location loc) {
	World world = loc.getWorld();
	// This section tries to find a roof block
	// Try just going up - this covers every case except if the player is standing under a hole
	roofFound = false;
	/*
	int roofY = loc.getBlockY();
	for (; roofY < world.getMaxHeight(); roofY++) {
	    if (roofBlocks.contains(loc.getBlock().getType())) {
		roofFound = true;
		Greenhouses.logger(3,"roof block found " + roofY + " of type " + loc.getBlock().getType().toString());
		break;
	    }
	    loc.add(new Vector(0,1,0));
	}*/
	// This does a ever-growing check around the player to find a roof block. It is possible for the player
	// to be outside the greenhouse in this situation, so a check is done later to mkae sure the player is inside
	//if (!roofFound) {
	int roofY = loc.getBlockY();
	// If the roof was not found start going around in circles until something is found
	// Expand in ever increasing squares around location until a wall block is found
	for (int radius = 0; radius < 100; radius++) {
	    for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
		for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
		    if (!((x > loc.getBlockX() - radius && x < loc.getBlockX() + radius) 
			    && (z > loc.getBlockZ() - radius && z < loc.getBlockZ() + radius))) {
			//player.sendBlockChange(new Location(world,x,roofY,z), Material.GLASS, (byte)(radius % 14));
			Block b = world.getBlockAt(x,roofY,z);
			Greenhouses.logger(3,"Checking column " + x + " " + z );					
			if (!Walls.isWallBlock(b.getType())) {
			    // Look up
			    for (int y = roofY; y < world.getMaxHeight(); y++) {
				if (roofBlocks.contains(world.getBlockAt(x,y,z).getType())) {
				    roofFound = true;
				    loc = new Location(world,x,y,z);
				    Greenhouses.logger(3,"Roof block found at " + x + " " + y + " " + z + " of type " + loc.getBlock().getType().toString());
				    break;
				}
			    }
			}
		    }
		    if (roofFound) {
			break;
		    }
		}
		if (roofFound) {
		    break;
		}
	    }
	    if (roofFound) {
		break;
	    }
	}
	//}
	// Record the height
	this.height = loc.getBlockY();
	// Now we have a roof block, find how far we can go NSWE
	minX = loc.getBlockX();
	maxX = loc.getBlockX();
	minZ = loc.getBlockZ();
	maxZ = loc.getBlockZ();
	expandCoords(loc);
	int minx = minX;
	int maxx = maxX;
	int minz = minZ;
	int maxz = maxZ;
	// Now we have some idea of the mins and maxes, check each block and see if it goes further
	do {
	    Greenhouses.logger(3, "Roof minx=" + minx);
	    Greenhouses.logger(3, "Roof maxx=" + maxx);
	    Greenhouses.logger(3, "Roof minz=" + minz);
	    Greenhouses.logger(3, "Roof maxz=" + maxz);
	    minx = minX;
	    maxx = maxX;
	    minz = minZ;
	    maxz = maxZ;
	    for (int x = minx; x <= maxx; x++) {
		for (int z = minz; z <= maxz; z++) {
		    // This will push out the coords if possible
		    expandCoords(new Location(world, x, loc.getBlockY(), z));
		}
	    }
	    // Repeat until nothing changes
	} while (minx != minX || maxx != maxX || minz != minZ || maxz != maxZ);
	// That's as much as we can do!
    }


    /**
     * This takes any location and tries to go as far as possible in NWSE directions finding contiguous roof blocks
     * up to 100 in any direction
     * @param height
     */
    private void expandCoords(Location height) {
	Location maxx = height.clone();
	Location minx = height.clone();
	Location maxz = height.clone();
	Location minz = height.clone();
	int limit = 0;
	while (roofBlocks.contains(maxx.getBlock().getType()) && limit < 100) {
	    limit++;
	    maxx.add(new Vector(1,0,0));
	}
	if (maxx.getBlockX()-1 > maxX) {
	    maxX = maxx.getBlockX()-1;
	}

	while (roofBlocks.contains(minx.getBlock().getType()) && limit < 200) {
	    limit++;
	    minx.subtract(new Vector(1,0,0));
	}
	if (minx.getBlockX() + 1 < minX) {
	    minX = minx.getBlockX() + 1;
	}

	while (roofBlocks.contains(maxz.getBlock().getType()) && limit < 300) {
	    limit++;
	    maxz.add(new Vector(0,0,1));
	} 
	if (maxz.getBlockZ() - 1 > maxZ) {
	    maxZ = maxz.getBlockZ() - 1;
	}

	while (roofBlocks.contains(minz.getBlock().getType()) && limit < 400) {
	    limit++;
	    minz.subtract(new Vector(0,0,1));
	}
	if (minz.getBlockZ() + 1 < minZ) {
	    minZ = minz.getBlockZ() + 1;
	}
    }
    /**
     * @return the minX
     */
    public int getMinX() {
	return minX;
    }
    /**
     * @param minX the minX to set
     */
    public void setMinX(int minX) {
	this.minX = minX;
    }
    /**
     * @return the maxX
     */
    public int getMaxX() {
	return maxX;
    }
    /**
     * @param maxX the maxX to set
     */
    public void setMaxX(int maxX) {
	this.maxX = maxX;
    }
    /**
     * @return the minZ
     */
    public int getMinZ() {
	return minZ;
    }
    /**
     * @param minZ the minZ to set
     */
    public void setMinZ(int minZ) {
	this.minZ = minZ;
    }
    /**
     * @return the maxZ
     */
    public int getMaxZ() {
	return maxZ;
    }
    /**
     * @param maxZ the maxZ to set
     */
    public void setMaxZ(int maxZ) {
	this.maxZ = maxZ;
    }

    /**
     * @return the area
     */
    public int getArea() {
	return (maxX - minX) * (maxZ - minZ);
    }
    /**
     * @return the roofFound
     */
    public boolean isRoofFound() {
	return roofFound;
    }


    /**
     * @return the height
     */
    public int getHeight() {
	return height;
    }

    /**
     * Check if roof block
     * @param blockType
     * @return true if this is a roof block
     */
    public boolean isRoofBlock(Material blockType) {
	if (roofBlocks.contains(blockType)) {
	    return true;
	}
	return false;
    }
}
