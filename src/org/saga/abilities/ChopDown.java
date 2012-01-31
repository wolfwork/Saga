package org.saga.abilities;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.saga.Saga;
import org.saga.player.PlayerMessages;
import org.saga.statistics.StatisticsManager;
import org.saga.utility.TextUtil;

public class ChopDown extends Ability{

	
	/**
	 * Amount logs a tree can have.
	 */
	private static Integer LOGS_LIMIT = 150;
	
	/**
	 * Minimum leaves to logs ratio.
	 */
	private static Integer MINIMUM_LEAVES_LOGS_RATIO = 1;

	
	// Initialization:
	/**
	 * Initializes using definition.
	 * 
	 * @param definition ability definition
	 */
	public ChopDown(AbilityDefinition definition) {
		
        super(definition);
	
	}

	
	// Ability usage:
	@Override
	public boolean instant(PlayerInteractEvent event) {

		
		// Check preuse:
		if(!handlePreUse()){
			return false;
		}
		
		// Pointing a log:
		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock == null || !clickedBlock.getType().equals(Material.LOG)){
			return false;
		}
		
		// Get the tree:
		ArrayList<Block> logs = new ArrayList<Block>();
		logs.add(clickedBlock);
		ArrayList<Block> leaves = new ArrayList<Block>();
		getTree(clickedBlock, logs, leaves);
		
		// Check ratio:
		if(logs.size() == 0) return false;
		double ratio = new Double(leaves.size()) / new Double(logs.size());
		
		if(ratio < MINIMUM_LEAVES_LOGS_RATIO){
			
			getSagaPlayer().message(notTree());
			
			return false;
		}
		
		// Tree to big:
		Integer skillLevel = getSkillLevel();
		Integer chopSize = getDefinition().getPrimaryFunction().calculateValue(skillLevel).intValue();
		if(chopSize < logs.size()){
			getSagaPlayer().message(notStroungEnough(logs.size(), chopSize));
			return false;
		}
		
		// Get player:
		Player player = getSagaPlayer().getPlayer();
		if(player == null){
			Saga.severe(this, "failed to retrieve player", "ignoring request");
			return false;
		}
		
		// Call events:
		for (Block log : logs) {
			
			BlockBreakEvent bbEvent = new BlockBreakEvent(log, player);
			Saga.plugin().getServer().getPluginManager().callEvent(bbEvent);
			
			if(bbEvent.isCancelled()){
				return false;
			}
			
		}
		
		// Chop down:
		for (Block log : logs) {
			
			byte data = log.getData();
			int type = log.getTypeId();
			
			// Remove:
			log.setType(Material.AIR);
			Location dropLocation = log.getLocation();
			
			// Drop:
			dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(type, 1, (short)0,data));
			
		}
		
		// Play effect:
		player.playEffect(clickedBlock.getLocation(), Effect.STEP_SOUND, Material.LOG.getId());
		
		// Award exp:
		Integer expval = logs.size();
		Integer awardedExp = awardExperience(expval);
		
		// Statistics:
		StatisticsManager.manager().onAbilityUse(getName(), awardedExp);
		
		return true;

		
	}
	
	/**
	 * Adds a tree to the given lists.
	 * 
	 * @param anchor anchor block
	 * @param logs logs
	 * @param leaves loeaves
	 */
	private static void getTree(Block anchor, ArrayList<Block> logs, ArrayList<Block> leaves){
		

		// Limits:
		if(logs.size() > LOGS_LIMIT) return;
		
		Block nextAnchor = null;
		
		// North:
		nextAnchor = anchor.getRelative(BlockFace.NORTH);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		// North-east:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		// East:
		nextAnchor = anchor.getRelative(BlockFace.EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South-east:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South-west:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// West:
		nextAnchor = anchor.getRelative(BlockFace.WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// North-west:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Shift anchor one up:
		anchor = anchor.getRelative(BlockFace.UP);

		// Up-north:
		nextAnchor = anchor.getRelative(BlockFace.NORTH);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		// Up-north-east:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		// Up-east:
		nextAnchor = anchor.getRelative(BlockFace.EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south-east:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_EAST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south-west:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-west:
		nextAnchor = anchor.getRelative(BlockFace.WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-north-west:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		// Up:
		nextAnchor = anchor.getRelative(BlockFace.SELF);
		if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
		
		
	}
	
	
	// Messages:
	public String notTree() {
		return PlayerMessages.negative + TextUtil.capitalize(getName()) + " ability can only be used on trees.";
	}
	
	public String notStroungEnough(int blocks, int maximum) {
		return PlayerMessages.negative + "Not strong enough to " + getName() + " a tree larger than " + maximum + " blocks.";
	}
	
	
}