package org.saga.commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.saga.Saga;
import org.saga.SagaLogger;
import org.saga.buildings.Arena;
import org.saga.buildings.Building;
import org.saga.buildings.CrumbleArena;
import org.saga.buildings.Home;
import org.saga.buildings.TownSquare;
import org.saga.buildings.storage.StorageArea;
import org.saga.config.BuildingConfiguration;
import org.saga.exceptions.InvalidBuildingException;
import org.saga.factions.Faction;
import org.saga.factions.SiegeManager;
import org.saga.messages.BuildingMessages;
import org.saga.messages.GeneralMessages;
import org.saga.messages.SettlementMessages;
import org.saga.messages.StatsMessages;
import org.saga.messages.WarMessages;
import org.saga.messages.effects.SettlementEffectHandler;
import org.saga.player.SagaPlayer;
import org.saga.settlements.Bundle;
import org.saga.settlements.BundleManager;
import org.saga.settlements.SagaChunk;
import org.saga.settlements.Settlement;
import org.saga.settlements.Settlement.SettlementPermission;
import org.saga.statistics.StatisticsManager;
import org.saga.utility.SagaLocation;
import org.sk89q.Command;
import org.sk89q.CommandContext;
import org.sk89q.CommandPermissions;


public class BuildingCommands {

	
	// Buildings general:
	@Command(
		aliases = {"bset"},
		usage = "<building name>",
		flags = "",
		desc = "Set a building on the chunk of land.",
		min = 1
	)
	@CommandPermissions({"saga.user.building.set"})
	public static void set(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String buildingName = null;
		Bundle selBundle = null;

		// Arguments:
		buildingName = GeneralMessages.nameFromArg(args.getJoinedStrings(0));
			
		// Selected chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
			
		// Selected chunk bundle:
		selBundle = selChunk.getBundle();

		// Valid building:
		if(BuildingConfiguration.config().getBuildingDefinition(buildingName) == null){
			sagaPlayer.message(BuildingMessages.invalidBuilding(buildingName));
			return;
		}
		   	
		// Building:
		Building selBuilding;
		try {
			selBuilding = BuildingConfiguration.config().createBuilding(buildingName);
		} catch (InvalidBuildingException e) {
			SagaLogger.severe(SettlementCommands.class, sagaPlayer + " tried to set a building with missing definition");
			sagaPlayer.error("definition missing for " + buildingName + " building");
			return;
		}
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.invalidBuilding(buildingName));
			return;
		}
		
		// Permission:
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.SET_BUILDING)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
		
	   	// Build points:
	   	Settlement selSettlement = null;
	   	if(selBundle instanceof Settlement) selSettlement = (Settlement) selBundle;
	   	if(selSettlement == null || !selSettlement.isBuildPointsAvailable(selBuilding)){
	   		sagaPlayer.message(SettlementMessages.notEnoughBuildingPoints(selBuilding));
			return;
	   	}

		// Existing building:
		if(selChunk.getBuilding() != null){
			sagaPlayer.message(BuildingMessages.oneBuilding(selBundle));
			return;
		}

		// Building available:
		if(!selBundle.isBuildingAvailable(buildingName)){
			sagaPlayer.message(BuildingMessages.unavailable(selBuilding));
			return;
		}
			
		// Set building:
		selChunk.setBuilding(selBuilding);

		// Inform:
		if(sagaPlayer.getBundle() == selBundle){
			sagaPlayer.message(SettlementMessages.setBuilding(selBuilding));
		}else{
			sagaPlayer.message(SettlementMessages.setBuilding(selBuilding, selBundle));
		}
			
		// Play effect:
		SettlementEffectHandler.playBuildingSet(sagaPlayer, selBuilding);

		// Statistics:
		StatisticsManager.manager().setBuildings(selBundle);
			

	}
		
	@Command(
		aliases = {"bremove"},
		usage = "",
		flags = "",
		desc = "Remove a building from the chunk of land.",
		min = 0,
		max = 0
	)
	@CommandPermissions({"saga.user.building.remove"})
	public static void remove(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		SagaChunk sagaChunk = sagaPlayer.getSagaChunk();
		
		// Bundle:
		Bundle selBundle = null;
		if(sagaChunk != null) selBundle = sagaChunk.getBundle();
		
		if(selBundle == null){
			sagaPlayer.message(SettlementMessages.notMember());
			return;
		}
		
		// Selected chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
	   	if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Existing building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(SettlementMessages.noBuilding());
			return;
		}
		
		// Permission:
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.REMOVE_BUILDING)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}

		// Inform:
		if(sagaPlayer.getBundle() == selBundle){
			sagaPlayer.message(SettlementMessages.removedBuilding(selBuilding));
		}else{
			sagaPlayer.message(SettlementMessages.removedBuilding(selBuilding, selBundle));
		}

		// Play effect:
		SettlementEffectHandler.playBuildingRemove(sagaPlayer, selBuilding);
		
		// Remove building:
		selBuilding.remove();
		selChunk.removeBuilding();

		// Statistics:
		StatisticsManager.manager().setBuildings(selBundle);
			
			
	}
	
	@Command(
		aliases = {"bstats"},
		usage = "",
		flags = "",
		desc = "Show building stats.",
		min = 0,
		max = 0
	)
	@CommandPermissions({"saga.user.building.stats"})
	public static void stats(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Selected chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
	   	if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Existing building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(SettlementMessages.noBuilding());
			return;
		}
		
		// Inform:
		sagaPlayer.message(StatsMessages.stats(selBuilding));
		
			
	}
	

	
	// General building storage:
	@Command(
			aliases = {"baddstorage","baddstore"},
			usage = "",
			flags = "",
			desc = "Add a storage area to the building.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.storage.add"})
	public static void addStorage(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	

		// Retrieve Saga chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Retrieve building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.noBuildingSet());
			return;
		}

		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.STORAGE_AREA_ADD)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
	
		// Remaining storage areas:
		if(selBuilding.getRemainingStorageAreas() < 1){
			sagaPlayer.message(BuildingMessages.storeAreasUnavailable(selBuilding));
			return;
		}
		
		// Create storage:
		StorageArea newStoreArea = new StorageArea(sagaPlayer);
	
		// Check overlap:
		if(selBuilding.checkOverlap(newStoreArea)){
			sagaPlayer.message(BuildingMessages.storeAreaOverlap());
			return;
		}
		
		// Multiple chunks:
		ArrayList<SagaChunk> sagaChunks = newStoreArea.getSagaChunks();
		if(sagaChunks.size() > 1 || !sagaChunks.get(0).equals(selChunk)){
			sagaPlayer.message(BuildingMessages.storeAreaSingleChunk());
			return;
		}
		
		// Add:
		selBuilding.addStorageArea(newStoreArea);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.storeAreaAdded(selBuilding));
		
		// Effect:
		SettlementEffectHandler.playStoreAreaCreate(sagaPlayer, newStoreArea);
		
		
	}
	
	@Command(
			aliases = {"bremovestorage","bremovestore"},
			usage = "",
			flags = "",
			desc = "Remove a storage area from the building.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.storage.remove"})
	public static void removeStorage(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	

		// Retrieve Saga chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Retrieve building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.noBuildingSet());
			return;
		}

		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.STORAGE_AREA_REMOVE)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
	
		// Retrieve storage:
		StorageArea storageArea = selBuilding.getStorageArea(sagaPlayer.getLocation());
		
		// No storage area:
		if(storageArea == null){
			sagaPlayer.message(BuildingMessages.storeAreaNotFound(selBuilding));
			return;
		}
		
		// Remove:
		selBuilding.removeStorageArea(storageArea);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.storeAreaRemoved(selBuilding));
		
		// Effect:
		SettlementEffectHandler.playStoreAreaRemove(sagaPlayer, storageArea);
		
		
	}
	
	@Command(
			aliases = {"bborder","bflash"},
			usage = "",
			flags = "",
			desc = "Show storage area border outline.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.storage.showborder"})
	public static void storageBorder(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	

		// Retrieve Saga chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Retrieve building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.noBuildingSet());
			return;
		}

		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.STORAGE_AREA_FLASH)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
	
		// Retrieve storages:
		ArrayList<StorageArea> storages = selBuilding.getStorageAreas();

		// Effect:
		for (StorageArea storageArea : storages) {
			SettlementEffectHandler.playStoreAreaFashBorder(sagaPlayer, storageArea);
		}
		
		
	}
	
	
	
	// Arena:
	@Command(
			aliases = {"bpvptop"},
			usage = "[to_display]",
			flags = "",
			desc = "Show pvp arena top players.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.arena.top"})
	public static void pvpTop(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Integer count = null;
		
		// Retrieve building:
		Arena selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Arena.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		if (args.argsLength() == 1) {
		
			try {
				count = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(0)));
				return;
			}
			
		}else{
			
			count = 10;
			
		}
		
		// Inform:
		sagaPlayer.message(BuildingMessages.arenaTop(selBuilding, count));
		
	
	}

	

	// Crumble arena:
	@Command(
			aliases = {"bsetarenay","bsetheight","bsety"},
			usage = "",
			flags = "",
			desc = "Set arena height.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.crumblearena.sety"})
	public static void setHeight(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		CrumbleArena selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, CrumbleArena.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.CRUMBLE_ARENA_SETUP)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
		
		// Set y:
		Integer y = (int)sagaPlayer.getLocation().getY();
		selBuilding.setY(y);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.crumbleHeightSet(selBuilding));
		
	
	}

	@Command(
			aliases = {"bsetkickloc","bsetkick"},
			usage = "",
			flags = "",
			desc = "Set crumble arena kick location.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.crumblearena.setkick"})
	public static void setKickLocation(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		CrumbleArena selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, CrumbleArena.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.CRUMBLE_ARENA_SETUP)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
		
		Block target = sagaPlayer.getPlayer().getTargetBlock(null, 16);
		
		// Move up if not air:
		if(target.getType() != Material.AIR && target.getRelative(BlockFace.UP).getType() == Material.AIR) target = target.getRelative(BlockFace.UP);
		
		// Location on chunk:
		if(BundleManager.manager().getSagaChunk(target.getChunk()) == selBuilding.getSagaChunk()){
			sagaPlayer.message(BuildingMessages.crumbleKickMustBeOutside(selBuilding));
			return;
		}
		
		// Set kick location:
		selBuilding.setKickLocation(new SagaLocation(target.getLocation().add(0.5, 0, 0.5)));
		
		// Inform:
		sagaPlayer.message(BuildingMessages.crumbleKickLocationSet(selBuilding));
		
	
	}


	// Arena:
	@Command(
			aliases = {"bcrumbletop"},
			usage = "[to_display]",
			flags = "",
			desc = "Show crumble arena top players.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.crumblearena.top"})
	public static void top(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Integer count = null;
		
		// Retrieve building:
		CrumbleArena selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, CrumbleArena.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		if (args.argsLength() == 1) {
		
			try {
				count = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(0)));
				return;
			}
			
		}else{
			
			count = 10;
			
		}
		
		// Inform:
		sagaPlayer.message(BuildingMessages.arenaTop(selBuilding, count));
		
	
	}

	

	// Home:
	@Command(
			aliases = {"baddresident"},
			usage = "<name>",
			flags = "",
			desc = "Add a resident to a home.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.home.addresident"})
	public static void addResident(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		String targetName = null;
		
		// Retrieve building:
		Home selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Home.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		targetName = args.getString(0);

		// Permission:
		Bundle bundle = selBuilding.getChunkBundle();
		if(!bundle.hasPermission(sagaPlayer, SettlementPermission.ADD_RESIDENT)){
			sagaPlayer.message(GeneralMessages.noPermission(bundle));
			return;
		}
		
		// Member:
		
		if(!SagaPlayer.checkExists(targetName)){
			sagaPlayer.message(GeneralMessages.invalidPlayer(targetName));
			return;
		}
		
		// Already a resident:
		if(selBuilding.isResident(targetName)){
			sagaPlayer.message(BuildingMessages.alreadyResident(targetName));
			return;
		}
		
		// Add:
		selBuilding.addResident(targetName);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.addedResident(targetName));
		
	
	}
	
	@Command(
			aliases = {"bremoveresident"},
			usage = "<name>",
			flags = "",
			desc = "Remove a resident from a home.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.home.removeresident"})
	public static void removeResident(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		String targetName = null;
		
		// Retrieve building:
		Home selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Home.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		targetName = args.getString(0);
		
		// Permission:
		Bundle bundle = selBuilding.getChunkBundle();
		if(!bundle.hasPermission(sagaPlayer, SettlementPermission.REMOVE_RESIDENT)){
			sagaPlayer.message(GeneralMessages.noPermission(bundle));
			return;
		}
		
		// Already a resident:
		if(!selBuilding.isResident(targetName)){
			sagaPlayer.message(BuildingMessages.notResident(targetName));
			return;
		}
		
		// Remove:
		selBuilding.removeResident(targetName);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.removedResident(targetName));
		
	
	}
	
	
	
	// Town square:
	@Command(
		aliases = {"sspawn"},
		usage = "[settlement_name]",
		flags = "",
		desc = "Spawn in a town square.",
		min = 0,
		max = 1)
	@CommandPermissions({"saga.user.building.townsquare.spawn"})
	public static void spawn(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Bundle selBundle = null;
		
		String strBundle = null;
		
		// Arguments:
		switch (args.argsLength()) {
			
			case 1:

				// Bundle:
				strBundle = GeneralMessages.nameFromArg(args.getString(0));
				selBundle = BundleManager.manager().matchBundle(strBundle);
				
				if(selBundle == null){
					sagaPlayer.message(GeneralMessages.invalidSettlement(strBundle));
					return;
				}
				
				break;

			default:
				
				// Bundle:
				selBundle = sagaPlayer.getBundle();
				
				if(selBundle == null){
					sagaPlayer.message(SettlementMessages.notMember());
					return;
				}
				
				break;
				
		}
		
		// Handle spawn:
		handleSpawn(sagaPlayer, selBundle);
		
		
	}
	
	@Command(
		aliases = {"bsetspawn"},
		usage = "",
		flags = "",
		desc = "Set spawn area for town square.",
		min = 0,
		max = 0)
	@CommandPermissions({"saga.user.building.townsquare.setspawn"})
	public static void setSpawn(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Retrieve building:
		TownSquare selBuilding = null;
		try {
			selBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TownSquare.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Permission:
		Bundle selBundle = selBuilding.getChunkBundle();
		if(!selBundle.hasPermission(sagaPlayer, SettlementPermission.TOWN_SQUARE_SET_SPAWN)){
			sagaPlayer.message(GeneralMessages.noPermission(selBundle));
			return;
		}
		
		// Set location:
		selBuilding.setSpawn(new SagaLocation(sagaPlayer.getLocation()));
		
		// Inform:
		sagaPlayer.message(BuildingMessages.townSquareSpawnAreaSet());
			
	}
	
	
	public static void handleSpawn(SagaPlayer sagaPlayer, Bundle bundle){
		

		// Faction spawn:
		Faction playerFaction = sagaPlayer.getFaction();
		Faction owningFaction = SiegeManager.manager().getOwningFaction(bundle.getId());
		if(playerFaction != null && playerFaction == owningFaction){
			// Do nothing;
		}
		
		// Permission:
		else if(!bundle.hasPermission(sagaPlayer, SettlementPermission.SPAWN)){
			sagaPlayer.message(GeneralMessages.noPermission());
			return;
		}
		
		// Sieged by a faction:
		if(SiegeManager.manager().isSieged(bundle.getId())){
			
			Faction defendingFaction = owningFaction;
			Faction attackingFaction = SiegeManager.manager().getAttackingFaction(bundle.getId());
			
			if(playerFaction != null){
				
				if(playerFaction == defendingFaction){
					sagaPlayer.message(WarMessages.siegeSpawnDeny(defendingFaction));
					return;
				}
				
				else if(playerFaction == attackingFaction){
					sagaPlayer.message(WarMessages.siegeSpawnDeny(attackingFaction));
					return;
				}
				
			}
			
		}
		
		ArrayList<TownSquare> selBuildings = bundle.getBuildings(TownSquare.class);
		
		if(selBuildings.size() == 0){
			sagaPlayer.message(BuildingMessages.noTownSquare(bundle));
			return;
		}
		
		TownSquare selBuilding = null;
		
		for (TownSquare townSquare : selBuildings) {
			
			selBuilding = townSquare;
			break;
			
		}

		// Update spawning protection:
		selBuilding.updateSpawningProtect(sagaPlayer);
		
		// Prepare chunk:
		selBuilding.getSagaChunk().loadChunk();
		
		// Location:
		Location spawnLocation = selBuilding.findSpawnLocation();
		if(spawnLocation == null){
			
			SagaLogger.severe(selBuilding, sagaPlayer + " player failed to respawn at " + selBuilding.getDisplayName());
			sagaPlayer.error("failed to respawn");
			return;
			
		}
		
		// Direction:
		Location playerLocation = sagaPlayer.getLocation();
		spawnLocation.setPitch(playerLocation.getPitch());
		spawnLocation.setYaw(playerLocation.getYaw());
		
		// Teleport:
		sagaPlayer.teleport(spawnLocation);
		
		
	}
	
	
}
