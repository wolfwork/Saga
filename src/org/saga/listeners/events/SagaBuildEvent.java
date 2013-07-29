package org.saga.listeners.events;

import java.util.PriorityQueue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.saga.player.SagaPlayer;
import org.saga.settlements.SagaChunk;

public class SagaBuildEvent {

	
	/**
	 * Event.
	 */
	private Cancellable event;
	
	/**
	 * Saga player.
	 */
	private SagaPlayer sagaPlayer;
	
	/**
	 * Origin Saga chunk.
	 */
	private SagaChunk sagaChunk = null;
	
	/**
	 * Block.
	 */
	private Block block;

	/**
	 * Build override.
	 */
	private PriorityQueue<BuildOverride> buildOverride = new PriorityQueue<SagaBuildEvent.BuildOverride>();
	
	
	
	// Initialise:
	/**
	 * Sets event.
	 * 
	 * @param event event
	 */
	public SagaBuildEvent(BlockBreakEvent event, SagaPlayer sagaPlayer, SagaChunk sagaChunk) {
		
		this.event = event;
		this.sagaPlayer = sagaPlayer;
		this.sagaChunk = sagaChunk;
		this.block = event.getBlock();
		
	}
	
	/**
	 * Sets event.
	 * 
	 * @param event event
	 */
	public SagaBuildEvent(BlockPlaceEvent event, SagaPlayer sagaPlayer, SagaChunk sagaChunk) {
		
		this.event = event;
		this.sagaPlayer = sagaPlayer;
		this.sagaChunk = sagaChunk;
		this.block = event.getBlock();
		
	}
	
	/**
	 * Sets event.
	 * 
	 * @param event event
	 */
	public SagaBuildEvent(PlayerInteractEvent event, SagaPlayer sagaPlayer, SagaChunk sagaChunk) {
		
		this.event = event;
		this.sagaPlayer = sagaPlayer;
		this.sagaChunk = sagaChunk;
		this.block = event.getClickedBlock();
		
	}
	
	/**
	 * Sets event.
	 * 
	 * @param event event
	 */
	public SagaBuildEvent(SignChangeEvent event, SagaPlayer sagaPlayer, SagaChunk sagaChunk) {
		
		this.event = event;
		this.sagaPlayer = sagaPlayer;
		this.sagaChunk = sagaChunk;
		this.block = event.getBlock();
		
	}
	
	
	
	// Modify:
	/**
	 * Adds a build override.
	 * 
	 * @param override build override
	 */
	public void addBuildOverride(BuildOverride override) {

		buildOverride.add(override);
		
	}
	
	
	
	// Conclude:
	/**
	 * Cancel event.
	 * 
	 */
	public void cancel() {

		event.setCancelled(true);
		
		if(event instanceof BlockPlaceEvent){
			((BlockPlaceEvent) event).setBuild(false);
		}
		
		else if(event instanceof PlayerInteractEvent){
			((PlayerInteractEvent) event).setUseInteractedBlock(Result.DENY);
			((PlayerInteractEvent) event).setUseItemInHand(Result.DENY);
		}

	}


	
	// Event information:
	/**
	 * Gets the sagaPlayer.
	 * 
	 * @return the sagaPlayer
	 */
	public SagaPlayer getSagaPlayer() {
		return sagaPlayer;
	}
	
	/**
	 * Gets the sagaChunk.
	 * 
	 * @return the sagaChunk, null if none
	 */
	public SagaChunk getSagaChunk() {
		return sagaChunk;
	}
	
	/**
	 * Gets the block.
	 * 
	 * @return block, null if none
	 */
	public Block getBlock() {
		return block;
	}
	
	/**
	 * Gets the top override.
	 * 
	 * @return top override, NONE if none
	 */
	public BuildOverride getbuildOverride() {

		if(buildOverride.size() == 0) return BuildOverride.NONE;
		
		return buildOverride.peek();

	}
	
	/**
	 * Checks if the event is cancelled.
	 * 
	 * @return true if cancelled
	 */
	public boolean isCancelled() {
		return event.isCancelled();
	}

	/**
	 * Gets the wrapped event.
	 * 
	 * @return wrapped event
	 */
	public Cancellable getWrappedEvent() {
		return event;
	}
	
	
	// Other:
	/**
	 * Check if the interact event is a build event.
	 * 
	 * @param event interact event
	 * @return build event
	 */
	public static boolean isBuildEvent(PlayerInteractEvent event) {
		
		
		ItemStack item = event.getPlayer().getItemInHand();
		Block block = event.getClickedBlock();
		
		if(block == null) return false;
		
		switch (item.getType()) {
			
			case LAVA_BUCKET:
				
				return true;
			
			case FLINT_AND_STEEL:
				
				return true;
				
			case FIREBALL:
				
				return true;

			case WATER_BUCKET:
				
				return true;
				
			case BUCKET:

				return true;
				
			case INK_SACK:
				
				if(item.getData().getData() != 15) break;
				return true;
				
			case PAINTING:
	
				return true;

			default:
				break;
			
		}
		
		// Extinguish fire:
		if(block.getRelative(BlockFace.UP) != null && block.getRelative(BlockFace.UP).getType() == Material.FIRE){
			return true;
		}
		
		// Trample:
		if(event.getAction() == Action.PHYSICAL && block.getType() == Material.SOIL){
			return true;
		}
		
		return false;
		
		
	}
	

	// Types:
	/**
	 * Build overrides.
	 * 
	 * @author andf
	 *
	 */
	public enum BuildOverride{
		
		
		ADMIN_ALLOW(true),
		ADMIN_DENY(false),
		
		CRUMBLE_ARENA_DENY(false),
		
		OPEN_CLAIMED_STORAGE_AREA_ALLOW(true),
		OPEN_STORAGE_AREA_ALLOW(true),
		STORAGE_AREA_DENY(false),
		
		CHUNK_GROUP_DENY(false),
		
		SETTLEMENT_OWNER_ALLOW(true),
		
		HOME_RESIDENT_ALLOW(true),
		HOME_DENY(false),
		
		BUILDING_DENY(false),
		SETTLEMENT_DENY(false),
		WILDERNESS_SPECIFIC_BLOCK_ALLOW(true),
		WILDERNESS_DENY(false),
		
		NONE(true);
		
		
		/**
		 * If true, then build will be allowed.
		 */
		private boolean allow;
		
		/**
		 * Sets if build override enables build.
		 * 
		 * @param true if allows build, false if denies build
		 */
		private BuildOverride(boolean allow) {
			this.allow = allow;
		}
		
		/**
		 * If true, then build will be allowed. Denied if false.
		 * 
		 * @return true if allowed, false if denied
		 */
		public boolean isAllow() {
			return allow;
		}		
		
	}
	
	
}
