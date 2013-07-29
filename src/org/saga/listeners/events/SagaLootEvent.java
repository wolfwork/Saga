package org.saga.listeners.events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.saga.Saga;
import org.saga.buildings.production.SagaPricedItem;
import org.saga.config.EconomyConfiguration;
import org.saga.config.ExperienceConfiguration;
import org.saga.config.GeneralConfiguration;
import org.saga.messages.GeneralMessages;
import org.saga.metadata.UnnaturalTag;
import org.saga.player.SagaLiving;
import org.saga.player.SagaPlayer;
import org.saga.settlements.SagaChunk;
import org.saga.statistics.StatisticsManager;
import org.saga.utility.TwoPointFunction;

public class SagaLootEvent {


	/**
	 * target block.
	 */
	public final Block block;

	/**
	 * Tool material.
	 */
	public final Material tool;
	
	/**
	 * Saga living.
	 */
	public final SagaLiving sagaLiving;
	
	/**
	 * Location saga chunk.
	 */
	public final SagaChunk blockSagaChunk;
	

	/**
	 * Minecraft event.
	 */
	private BlockBreakEvent event;
	
	/**
	 * Extra drop chance.
	 */
	private Double extra = 0.0;

	/**
	 * Tool sloppiness multiplier.
	 */
	private double sloppiness = 1.0;
	
	/**
	 * True if the block is considered player placed.
	 */
	private Boolean isNatural = true;
	
	
	
	// Initialise:
	/**
	 * Sets event and player.
	 * 
	 * @param event event
	 * @param sagaPlayer saga player
	 * @param sagaChunk saga chunk
	 */
	public SagaLootEvent(BlockBreakEvent event, SagaLiving sagaPlayer, SagaChunk sagaChunk) {

		
		this.event = event;
		this.tool = event.getPlayer().getItemInHand().getType();
		this.sagaLiving = sagaPlayer;
		this.block = event.getBlock();
    	this.blockSagaChunk = sagaChunk;
    	
    	// Set player placed:
    	if(block.hasMetadata(UnnaturalTag.METADATA_KEY)){
    		
    		isNatural = false;
    		
	    	// Compensate for growth:
	    	Byte newData = GeneralConfiguration.config().getNewBlockData(block);
	    	
	    	if(!newData.equals((byte)-1) && !newData.equals(block.getData())){
	    		isNatural = true;
	    	}
    		
		}
		

	}
	
	
	
	// Modify:
	/**
	 * Modifies extra drop chance.
	 * 
	 * @param amount amount to add
	 */
	public void modifyExtraDrop(Double amount) {
		extra += amount;
	}

	/**
	 * Modifies tool handling.
	 * 
	 * @param amount amount to modify
	 */
	public void modifyToolHandling(double amount) {
		sloppiness-= amount;
	}
	
	

	// Conclude:
	/**
	 * Applies the event.
	 * 
	 */
	public void apply() {
		
		
		ItemStack item = event.getPlayer().getItemInHand();
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>(event.getBlock().getDrops(item));

		// Reduce tool damage:
		final int undurability = item.getDurability();
		
		// Natural break:
		if(isNatural){

			// Award:
			if(sagaLiving != null && sagaLiving instanceof SagaPlayer){
				
				SagaPlayer rsagaPlayer = (SagaPlayer) sagaLiving;
				
				// Award exp:
				Double exp = ExperienceConfiguration.config().getExp(block);
				((SagaPlayer)sagaLiving).awardExp(exp);
				
				SagaPricedItem blockCoins = EconomyConfiguration.config().getBlocCoinsItem(block);
				if(blockCoins != null){
				
					// Award coins:
					double coins = blockCoins.getPrice();
					rsagaPlayer.handleModCoins(coins);
					
					// Statistics:
					StatisticsManager.manager().addBlockCoins(rsagaPlayer, block.getType(), coins);
					
				}
				
				// Statistics:
				StatisticsManager.manager().addExp("block", GeneralMessages.material(block.getType()), exp);
				
			}
			
			// Select and drop:
			if(Saga.RANDOM.nextDouble() < extra && drops.size() > 0){
				Location location = block.getLocation();
				ItemStack drop = drops.get(Saga.RANDOM.nextInt(drops.size()));
				location.getWorld().dropItemNaturally(location, drop);
			}
			
		}
		
		// Schedule for next tick:
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Saga.plugin(), new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				
				// Tool damage reduction:
				Player player = event.getPlayer();
				ItemStack item = player.getItemInHand();
				int damage = item.getDurability() - undurability;
				damage = TwoPointFunction.randomRound(sloppiness * damage).shortValue();
				int pundurability = item.getDurability();
				item.setDurability((short) (undurability + damage));
				if(item.getDurability() != pundurability) player.updateInventory();
				
			}
		}, 1);
		
		
		
		
	}

	/**
	 * Cancels the event.
	 * 
	 */
	public void cancel() {

		event.setCancelled(true);

	}

	
	
	// Event information:
	/**
	 * Gets the block.
	 * 
	 * @return the block
	 */
	public Block getBlock() {
	
	
		return block;
	}

	/**
	 * Gets the Saga living entity.
	 * 
	 * @return the Saga living entity
	 */
	public SagaLiving getSagaPlayer() {
		return sagaLiving;
	}

	/**
	 * Gets the blockSagaChunk.
	 * 
	 * @return the blockSagaChunk
	 */
	public SagaChunk getBlockSagaChunk() {
		return blockSagaChunk;
	}

	/**
	 * Gets the isPlayerPlaced.
	 * 
	 * @return the isPlayerPlaced
	 */
	public Boolean getIsPlayerPlaced() {
		return !isNatural;
	}
	
	
	
	
}
