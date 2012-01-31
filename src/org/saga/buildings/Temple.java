package org.saga.buildings;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.saga.Clock;
import org.saga.Clock.SecondTicker;
import org.saga.chunkGroups.SagaChunk;
import org.saga.player.SagaPlayer;

public class Temple extends Building implements SecondTicker{

	
	/**
	 * Clock is enabled if true.
	 */
	transient private boolean clockEnabled;
	
	// Initialization:
	/**
	 * Initializes
	 * 
	 * @param pointCost point cost
	 * @param moneyCost money cost
	 * @param proficiencies proficiencies
	 */
	private Temple(String name) {
		
		super("");
		
		clockEnabled = false;
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.Building#completeExtended()
	 */
	@Override
	public boolean completeExtended() {
		

		boolean integrity = true;
		
//		if(signs == null){
//			signs = new ArrayList<BuildingSign>();
//			Saga.severe(this, "failed to initialize signs field", "setting default");
//			integrity = false;
//		}
//		for (int i = 0; i < signs.size(); i++) {
//			
//			try {
//				signs.get(i).complete(this);
//			} catch (SignException e) {
//				Saga.severe(this, "failed to initialize signs field element: " + e.getClass().getSimpleName() + ":" + e.getMessage(), "removing element");
//				signs.remove(i);
//				i--;
//				continue;
//			}
//			
//		}
		
		// Transient:
		clockEnabled = false;
		
		return integrity;
		
		
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.Building#blueprint()
	 */
	@Override
	public Building blueprint() {
		return new Temple("");
	}
	
	@Override
	public void enable() {
		
		
		super.enable();
		
		// Enable clock:
		if(countPlayers() > 0){
			enableClock();
		}
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.Building#disable()
	 */
	public void disable() {
		
		
		super.disable();
		
		// Disable clock if enabled:
		if (isClockEnabled()) {
			disableClock();
		}
		
		
	}
	
	
	// Clock:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.Clock.SecondTicker#clockSecondTick()
	 */
	@Override
	public void clockSecondTick() {

		
		// Disable clock if no players:
		if(countPlayers() == 0) disableClock();
		
		// Level too low:
		if(getDefinition().getLevelFunction().getXRequired() > getLevel()){
			return;
		}
		
		// Get saga players:
		SagaChunk sagaChunk = getOriginChunk();
		if(sagaChunk == null) return;
		
		// Regenerate experience:
		ArrayList<SagaPlayer> sagaPlayers = sagaChunk.getSagaPlayers();
		Integer levelLimit = getDefinition().getLevelFunction().calculateValue(getLevel()).intValue();
		for (SagaPlayer sagaPlayer : sagaPlayers) {
			
			if(sagaPlayer.getLevel() < levelLimit){
				sagaPlayer.regenExp();
			}
			
		}
		
		
	}
	
	/**
	 * Enables the clock.
	 * 
	 */
	private void enableClock() {

		Clock.clock().registerSecondTick(this);
		
		clockEnabled = true;
		
	}
	
	/**
	 * Checks if clock is enabled.
	 * 
	 * @return true if enabled.
	 */
	public boolean isClockEnabled() {
		return clockEnabled;
	}
	
	/**
	 * Disable the clock.
	 * 
	 */
	private void disableClock() {

		Clock.clock().unregisterSecondTick(this);
		
		clockEnabled = false;
		
	}
	
	
	// Experience restore:
	/**
	 * Counts the players.
	 * 
	 * @return player count
	 */
	private int countPlayers() {

		
		SagaChunk sagaChunk = getOriginChunk();
		
		if(sagaChunk == null){
			return 0;
		}
		
		return sagaChunk.countPlayers();
		
		
	}
	
	
	// Events:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.Building#onPlayerSagaChunkChange(org.saga.player.SagaPlayer, org.saga.chunkGroups.SagaChunk, org.saga.chunkGroups.SagaChunk, org.bukkit.Location, org.bukkit.Location, org.bukkit.event.player.PlayerMoveEvent)
	 */
	@Override
	public void onPlayerSagaChunkChange(SagaPlayer sagaPlayer, SagaChunk fromChunk, SagaChunk toChunk, Location fromLocation, Location toLocation, PlayerMoveEvent event) {
	
		
		super.onPlayerSagaChunkChange(sagaPlayer, fromChunk, toChunk, fromLocation, toLocation, event);
		
		// Pick related events:
		if(toChunk != getOriginChunk()){
			return;
		}
		
		// Enable clock:
		if(!isClockEnabled()) enableClock();
	
	
	}
	
	
}