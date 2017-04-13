package com.wasteofplastic.greenhouses.greenhouse;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.wasteofplastic.greenhouses.Greenhouses;

public class Walls {
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    private int floor;
    private boolean useRoofMaxX;
    private boolean useRoofMinX;
    private boolean useRoofMaxZ;
    private boolean useRoofMinZ;
    private Location roofBlock;
    Location roofHopperLoc = null;
    final static List<Material> wallBlocks = Arrays.asList(new Material[]{Material.HOPPER, Material.GLASS, Material.THIN_GLASS, Material.GLOWSTONE, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK,Material.STAINED_GLASS,Material.STAINED_GLASS_PANE});
    /**
     * 
     */
    public Walls(Greenhouses plugin, final Player player, Roof roof) {
	// The player is under the roof
	// Assume the player is inside the greenhouse they are trying to create
	Location loc = player.getLocation();
	World world = player.getWorld();
	// Find the floor - defined as the last y under the roof where there are no wall blocks
	int wallBlockCount = 0;
	int y = roof.getHeight();
	do {
	    wallBlockCount = 0;
	    for (int x = roof.getMinX(); x <= roof.getMaxX(); x++) {
		for (int z = roof.getMinZ(); z <= roof.getMaxZ(); z++) {
		    if (wallBlocks.contains(world.getBlockAt(x, y, z).getType())) {
			wallBlockCount++;
		    } 
		}
	    }

	} while( y-- > 0 && wallBlockCount > 0);
	floor = y + 1;	
	plugin.logger(3,"#1 Floor found at " + floor);
	// Now start with the player's x and z location
	int radiusMinX = 0;
	int radiusMaxX = 0;
	int radiusMinZ = 0;
	int radiusMaxZ = 0;
	boolean stopMinX = false;
	boolean stopMaxX = false;
	boolean stopMinZ = false;
	boolean stopMaxZ = false;
	minX = loc.getBlockX();
	maxX = loc.getBlockX();
	minZ = loc.getBlockZ();
	maxZ = loc.getBlockZ();
	plugin.logger(3,"Starting point = " + loc.getBlockX() + "," + loc.getBlockZ());
	plugin.logger(3,"roof minX = " + roof.getMinX());
	plugin.logger(3,"roof maxX = " + roof.getMaxX());
	plugin.logger(3,"roof minZ = " + roof.getMinZ());
	plugin.logger(3,"roof maxZ = " + roof.getMaxZ());
	do {
	    plugin.logger(3,"wall radiusminX = " + radiusMinX);
	    plugin.logger(3,"wall radius maxX = " + radiusMaxX);
	    plugin.logger(3,"wall radius minZ = " + radiusMinZ);
	    plugin.logger(3,"wall radius maxZ = " + radiusMaxZ);

	    // Look around player in an ever expanding cube
	    minX = loc.getBlockX() - radiusMinX;
	    maxX = loc.getBlockX() + radiusMaxX;
	    minZ = loc.getBlockZ() - radiusMinZ;
	    maxZ = loc.getBlockZ() + radiusMaxZ;
	    y = roof.getHeight() - 1;
	    for (y = roof.getHeight() - 1; y > floor; y--) {
		for (int x = minX; x <= maxX; x++) {
		    for (int z = minZ; z <= maxZ; z++) {
			// Only look around outside edge
			if (!((x > minX && x < maxX) && (z > minZ && z < maxZ))) {
			    plugin.logger(3,"Checking block " + x + " " + y + " " + z);
			    // Look at block faces
			    for (BlockFace bf: BlockFace.values()) {
				switch (bf) {
				case EAST:
				    // positive x
				    if (wallBlocks.contains(world.getBlockAt(x, y, z).getRelative(bf).getType())) {
					stopMaxX = true;
					plugin.logger(3,"Wall found, stopping MaxX");
				    }
				    break;
				case WEST:
				    // negative x
				    if (wallBlocks.contains(world.getBlockAt(x, y, z).getRelative(bf).getType())) {
					stopMinX = true;
					plugin.logger(3,"Wall found, stopping minX");
				    }
				    break;
				case NORTH:
				    // negative Z
				    if (wallBlocks.contains(world.getBlockAt(x, y, z).getRelative(bf).getType())) {
					stopMinZ = true;
					plugin.logger(3,"Wall found, stopping minZ");
				    }
				    break;
				case SOUTH:
				    // positive Z
				    if (wallBlocks.contains(world.getBlockAt(x, y, z).getRelative(bf).getType())) {
					stopMaxZ = true;
					plugin.logger(3,"Wall found, stopping maxZ");
				    }
				    break;
				default:
				    break;
				}
			    }
			}
		    }
		}
	    }
	    if (minX < roof.getMinX()) {
		plugin.logger(3,"minx is less that the roof minX");
		stopMinX = true;
	    }
	    if (maxX > roof.getMaxX()) {
		plugin.logger(3,"maxx is > that the roof minX");
		stopMaxX = true;
	    }
	    if (minZ < roof.getMinZ()) {
		plugin.logger(3,"minz is less that the roof minz");
		stopMinZ = true;
	    }
	    if (maxZ > roof.getMaxZ()) {
		plugin.logger(3,"maxZ is >t the roof maxZ");
		stopMaxZ = true;
	    }
	    // Expand the edges
	    if (!stopMinX) {
		radiusMinX++;
	    }
	    if (!stopMaxX) {
		radiusMaxX++;
	    }
	    if (!stopMinZ) {
		radiusMinZ++;
	    }
	    if (!stopMaxZ) {
		radiusMaxZ++;
	    }
	} while (!stopMinX || !stopMaxX || !stopMinZ || !stopMaxZ);
	// We should have the largest cube we can make now
	minX--;
	maxX++;
	minZ--;
	maxZ++;
	plugin.logger(3,"wall minX = " + minX);
	plugin.logger(3,"wall maxX = " + maxX);
	plugin.logger(3,"wall minZ = " + minZ);
	plugin.logger(3,"wall maxZ = " + maxZ);
	
	// Find the floor again, only looking within the walls
	y = roof.getHeight();
	do {
	    wallBlockCount = 0;
	    for (int x = minX; x <= maxX; x++) {
		for (int z = minZ; z <= maxZ; z++) {
		    if (wallBlocks.contains(world.getBlockAt(x, y, z).getType())) {
			wallBlockCount++;
		    } 
		}
	    }

	} while( y-- > 0 && wallBlockCount > 0);
	floor = y + 1;	
	plugin.logger(3,"#2 floor = " + floor);	

    }

    /**
     * @return the minXX
     */
    public int getMinX() {
	return minX;
    }
    /**
     * @return the maxXX
     */
    public int getMaxX() {
	return maxX;
    }
    /**
     * @return the minZZ
     */
    public int getMinZ() {
	return minZ;
    }
    /**
     * @return the maxZZ
     */
    public int getMaxZ() {
	return maxZ;
    }
    /**
     * @return the useRoofMaxX
     */
    public boolean useRoofMaxX() {
	return useRoofMaxX;
    }
    /**
     * @return the useRoofMinX
     */
    public boolean useRoofMinX() {
	return useRoofMinX;
    }
    /**
     * @return the useRoofMaxZ
     */
    public boolean useRoofMaxZ() {
	return useRoofMaxZ;
    }
    /**
     * @return the useRoofMinZ
     */
    public boolean useRoofMinZ() {
	return useRoofMinZ;
    }
    /**
     * @return the wallBlocks
     */
    public List<Material> getWallBlocks() {
	return wallBlocks;
    }

    public int getArea() {
	// Get interior area
	return (maxX - minX) * (maxZ - minZ);
    }

    /**
     * @return the roofBlock
     */
    public Location getRoofBlock() {
	return roofBlock;
    }

    /**
     * @return the roofHopperLoc
     */
    public Location getRoofHopperLoc() {
	return roofHopperLoc;
    }

    /**
     * @return the floor
     */
    public int getFloor() {
	return floor;
    }

    public static boolean isWallBlock(Material blockType) {
	return wallBlocks.contains(blockType);
    }
}
