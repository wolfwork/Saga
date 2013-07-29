package org.saga.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.saga.config.GeneralConfiguration;
import org.saga.settlements.BundleManager;
import org.saga.settlements.SagaChunk;

public class WorldListener implements Listener{

	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoadEvent(ChunkLoadEvent event) {

		if(GeneralConfiguration.isDisabled(event.getWorld())) return;
    	
		
		SagaChunk sagaChunk = BundleManager.manager().getSagaChunk(event.getChunk());

		// Forward event:
		if(sagaChunk != null) sagaChunk.onChunkLoad();
		
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnloadEvent(ChunkUnloadEvent event) {
		
	}
	
	
}
