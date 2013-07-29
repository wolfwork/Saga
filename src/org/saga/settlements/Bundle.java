package org.saga.settlements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
import org.bukkit.entity.Enderman;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.saga.Saga;
import org.saga.SagaLogger;
import org.saga.buildings.Building;
import org.saga.config.BuildingConfiguration;
import org.saga.config.GeneralConfiguration;
import org.saga.config.SettlementConfiguration;
import org.saga.exceptions.InvalidBuildingException;
import org.saga.exceptions.NonExistantSagaPlayerException;
import org.saga.listeners.events.SagaBuildEvent;
import org.saga.listeners.events.SagaBuildEvent.BuildOverride;
import org.saga.listeners.events.SagaDamageEvent;
import org.saga.listeners.events.SagaDamageEvent.PvPOverride;
import org.saga.messages.GeneralMessages;
import org.saga.messages.SettlementMessages;
import org.saga.messages.colours.Colour;
import org.saga.player.SagaPlayer;
import org.saga.saveload.Directory;
import org.saga.saveload.SagaCustomSerialization;
import org.saga.saveload.WriterReader;
import org.saga.settlements.Settlement.SettlementPermission;
import org.saga.statistics.StatisticsManager;

public class Bundle extends SagaCustomSerialization{

	
	/**
	 * Group name ID. -1 if none.
	 */
	private Integer id;
	
	/**
	 * Group name.
	 */
	private String name;
	
	/**
	 * Players associated with the group.
	 */
	private ArrayList<String> players;

	/**
	 * Group chunks.
	 */
	private ArrayList<SagaChunk> groupChunks;


	/**
	 * Chunk group owners.
	 */
	private String owner;

	
	// Optimisation:
	/**
	 * Buildings list.
	 */
	transient private ArrayList<Building> buildings = null;
	
	
	// Control:
	/**
	 * If true then saving is enabled.
	 */
	transient private Boolean isSavingEnabled;
	
	/**
	 * True if enabled.
	 */
	transient private boolean enabled = false;
	
	
	
	// Options:
	/**
	 * Toggle options.
	 */
	private HashSet<BundleToggleable> toggleOptions;
	
	

	// Properties:
	/**
	 * True if fire spread is enabled.
	 */
	private Boolean fireSpread;
	
	/**
	 * True if lava spread is enabled.
	 */
	private Boolean lavaSpread;
	
	
	
	// Initialisation:
	/**
	 * Sets name and ID.
	 * 
	 * @param id ID
	 * @param name name
	 */
	public Bundle(String name){
		
		
		this.name = name;
		this.id = BundleManager.manager().getUnusedId();
		this.players = new ArrayList<String>();
		this.groupChunks = new ArrayList<SagaChunk>();
		this.isSavingEnabled = true;
		this.owner = "";
		this.fireSpread = false;
		this.lavaSpread = false;
		this.toggleOptions = new HashSet<BundleToggleable>();
		
		
	}
	
	/**
	 * Fixes problematic fields.
	 * 
	 */
	public void complete() {

		
		if(name == null){
			SagaLogger.nullField(this, "name");
			name = "unnamed";
		}
		if(id == null){
			SagaLogger.nullField(this, "id");
			id = -1;
		}
		if(players == null){
			SagaLogger.nullField(this, "players");
			players = new ArrayList<String>();
		}
		for (int i = 0; i < players.size(); i++) {
			if(players.get(i) == null){
				SagaLogger.nullField(this, "players element");
				players.remove(i);
				i--;
			}
		}

		if(owner == null){
			SagaLogger.nullField(this, "owners");
			owner = "";
		}
		
		if(groupChunks == null){
			SagaLogger.nullField(this, "groupChunks");
			groupChunks = new ArrayList<SagaChunk>();
		}
		for (int i = 0; i < groupChunks.size(); i++) {
			
			SagaChunk sagaChunk = groupChunks.get(i);
			
			if(sagaChunk == null){
				SagaLogger.nullField(this, "groupChunks element");
				groupChunks.remove(i);
				i--;
				continue;
			}
			sagaChunk.complete(this);
			
			// Building:
			if(sagaChunk.getBuilding() != null){
				
				try {
					sagaChunk.getBuilding().complete();
				} catch (InvalidBuildingException e) {
					SagaLogger.severe(this,"failed to complete " + sagaChunk.getBuilding().getName() + " building: "+ e.getClass().getSimpleName() + ":" + e.getMessage());
					disableSaving();
					sagaChunk.clearBuilding();
				}
				
			}
			
		}
		
		// Properties:
		if(fireSpread == null){
			SagaLogger.nullField(this, "fireSpread");
			fireSpread = false;
		}
		if(lavaSpread == null){
			SagaLogger.nullField(this, "lavaSpread");
			lavaSpread = false;
		}
		
		if(toggleOptions == null){
			SagaLogger.nullField(this, "toggleOptions");
			toggleOptions = new HashSet<BundleToggleable>();
		}
		if(toggleOptions.remove(null)){
			SagaLogger.nullField(this, "toggleOptions element");
		}

		// Control:
		isSavingEnabled = true;
	
		
		// Statistics:
		StatisticsManager.manager().setBuildings(this);
		
		
	}

	/**
	 * Enables the building.
	 * 
	 */
	public void enable() {
		
		ArrayList<Building> buildings = getBuildings();
		
		for (Building building : buildings) {
			building.enable();
		}
		
		enabled = true;
		
	}
	
	/**
	 * Disables the building.
	 * 
	 */
	public void disable() {
		
		ArrayList<Building> buildings = getBuildings();
		
		for (Building building : buildings) {
			building.disable();
		}
		
		enabled = false;
		
	}
	
	/**
	 * Checks if the bundle is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	
	
	// Naming:
	/**
	 * Gets chunk group ID.
	 * 
	 * @return ID
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the ID.
	 * 
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	// Saga chunk management:
	/**
	 * Adds a new chunk group.
	 * 
	 * @param bundle chunk group.
	 */
	public final static void create(Bundle bundle){

		
		// Log:
		SagaLogger.info("Creating " + bundle + " chunk group.");

		// Update chunk group manager:
		BundleManager.manager().addBundle(bundle);
		
		// Do the first save:
		bundle.save();
		
		// Refresh:
		for (SagaChunk sagaChunk : bundle.groupChunks) {
			sagaChunk.refresh();
		}
		
		// Enable:
		bundle.enable();
		
		
	}
	
	/**
	 * Adds a new chunk group.
	 * 
	 * @param bundle chunk group
	 * @param owner owner
	 */
	public static void create(Bundle bundle, SagaPlayer owner){
		

		// Add player:
		bundle.addMember(owner);

		// Set owner:
		bundle.setOwner(owner.getName());
		
		// Forward:
		create(bundle);

		
	}
	
	/**
	 * Deletes a chunk group
	 * 
	 * @param groupName group name
	 */
	public void delete() {


		// Log:
		SagaLogger.info("Deleting " + this + " chunk group.");
		
		// Disable:
		disable();
		
		// Remove all members:
		ArrayList<String> members = getMembers();
		for (String member : members) {
			
			try {
				
				SagaPlayer sagaPlayer = Saga.plugin().forceSagaPlayer(member);
				removeMember(sagaPlayer);
				sagaPlayer.indicateRelease();
				
			} catch (NonExistantSagaPlayerException e) {
				
				SagaLogger.severe(this, "failed to remove " + member + " player");
				members.remove(member);
				
			}
			
		}
		
		// Remove all saga chunks:
		ArrayList<SagaChunk> schunks = new ArrayList<SagaChunk>(groupChunks);
		for (SagaChunk sagaChunk : schunks) {
			removeChunk(sagaChunk);
		}
		
		// Save one last time:
		save();
		
		// Remove from disc:
		WriterReader.delete(Directory.SETTLEMENT_DATA, getId().toString());
		
		// Update chunk group manager:
		BundleManager.manager().removeBundle(this);
		
		
	}
	
	/**
	 * Checks if the bundle can be deleted.
	 * 
	 * @return true if can be deleted
	 */
	public boolean checkDetele() {
		
		// Too big:
		if(groupChunks.size() >= SettlementConfiguration.config().getNoDeleteSize()) return false;
		
		// No delete option:
		if(toggleOptions.contains(BundleToggleable.NO_DELETE)) return false;
		
		return true;
		
	}


	/**
	 * Gets all Saga chunks from the bundle.
	 * 
	 * @return all Saga chunks
	 */
	public ArrayList<SagaChunk> getSagaChunks() {
		return new ArrayList<SagaChunk>(groupChunks);
	}
	
	/**
	 * Returns settlement size in chunks.
	 * 
	 * @return settlement size
	 */
	public int getSize() {
		return groupChunks.size();
	}
	
	/**
	 * Adds a chunk.
	 * Needs to be done by chunk group manager, to update chunk shortcuts.
	 * 
	 * @param sagaChunk saga chunk
	 */
	public void addChunk(SagaChunk sagaChunk) {

		
		// Check if already on the list:
		if(groupChunks.contains(sagaChunk)){
			SagaLogger.severe(this, "tried to add an already existing " + sagaChunk + "chunk");
			return;
		}
		
		// Set chunk chunk group:
		sagaChunk.complete(this);
		
		// Add:
		groupChunks.add(sagaChunk);
		
		// Update chunk group manager:
		BundleManager.manager().addSagaChunk(sagaChunk);
		
		// Refresh:
		sagaChunk.refresh();
		
		
	}
	
	/**
	 * Removes a chunk.
	 * Needs to be done by chunk group manager, to update chunk shortcuts.
	 * 
	 * @param sagaChunk saga chunk
	 */
	public void removeChunk(SagaChunk sagaChunk) {

		
		// Check if not in this group:
		if(!groupChunks.contains(sagaChunk)){
			SagaLogger.severe(this, "tried to remove a non-existing " + sagaChunk + "chunk");
			return;
		}
		
		// Remove member:
		groupChunks.remove(sagaChunk);

		// Update chunk group manager:
		BundleManager.manager().removeSagaChunk(sagaChunk);

		// Refresh:
		sagaChunk.refresh();
		
		
	}

	/**
	 * Checks if the given bukkit chunk is adjacent to the chunk group.
	 * 
	 * @param bukkitChunk bukkit chunk
	 * @return true if adjacent
	 */
	public boolean isAdjacent(Chunk bukkitChunk) {

		String bWorld = bukkitChunk.getWorld().getName();
		int bX = bukkitChunk.getX();
		int bZ = bukkitChunk.getZ();
		
		for (int i = 0; i < groupChunks.size(); i++) {
			SagaChunk sChunk = groupChunks.get(i);
			// World:
			if(!sChunk.getWorldName().equals(bWorld)){
				continue;
			}
			// North from saga chunk:
			if( (sChunk.getX() == bX + 1) && (sChunk.getZ() == bZ) ){
				return true;
			}
			// East from saga chunk:
			if( (sChunk.getX() == bX) && (sChunk.getZ() == bZ + 1) ){
				return true;
			}
			// South from saga chunk:
			if( (sChunk.getX() == bX - 1) && (sChunk.getZ() == bZ) ){
				return true;
			}
			// West from saga chunk:
			if( (sChunk.getX() == bX) && (sChunk.getZ() == bZ - 1) ){
				return true;
			}
		}
		return false;
		
		
	}
	
	
	
	// Buildings:
	/**
	 * Gets all settlement buildings.
	 * 
	 * @return all settlement buildings
	 */
	public ArrayList<Building> getBuildings() {
		
		if(buildings == null){
			
			buildings = new ArrayList<Building>();
		
			for (int i = 0; i < groupChunks.size(); i++) {
				Building building = groupChunks.get(i).getBuilding();
				if(building != null) buildings.add(building);
			}
			
			Collections.sort(buildings, BuildingConfiguration.config().getComparator());
		
		}
		
		return buildings;
		
	}
	
	/**
	 * Notifies that buildings have changed.
	 * 
	 */
	public void notifyBuildingChange() {
		buildings = null;
	}
	
	
	/**
	 * Gets the total amount of buildings with the given name. 
	 * 
	 * @param buildingName building name
	 * @return total amount
	 */
	public Integer getTotalBuildings(String buildingName) {


		// Total buildings:
		Integer total = 0;
		for (SagaChunk sagaChunk : groupChunks) {
			
			Building building = sagaChunk.getBuilding();
			if(building == null) continue;
			if(!building.getName().equals(buildingName)) continue;
			
			total ++;
			
		}
		
		return total;
		
		
	}
	
	/**
	 * Gets the amount of available buildings. 
	 * 
	 * @param buildingName building name
	 * @return amount available
	 */
	public Integer getAvailableBuildings(String buildingName) {
		
		return 0;
		
	}
	
	/**
	 * Gets the amount of remaining buildings. 
	 * 
	 * @param buildingName building name
	 * @return amount remaining
	 */
	public Integer getRemainingBuildings(String buildingName) {

		return getAvailableBuildings(buildingName) - getTotalBuildings(buildingName);
		
	}
	
	/**
	 * Checks if the a building is available.
	 * 
	 * @param buildingName building name
	 * @return true if available
	 */
	public boolean isBuildingAvailable(String buildingName) {

		if(isOptionEnabled(BundleToggleable.UNLIMITED_BUILDINGS)) return true;
		
		return getRemainingBuildings(buildingName) > 0;
		
	}
	
	
	/**
	 * Gets the buildings with the given name.
	 * 
	 * @param bldgName building name
	 * @return buildings with the given name
	 */
	public ArrayList<Building> getBuildings(String bldgName) {

		ArrayList<Building> allBuildings = getBuildings();
		ArrayList<Building> buildings = new ArrayList<Building>();
		
		for (Building building : allBuildings) {
			if(building.getName().equals(bldgName)) buildings.add(building);
		}
		
		return buildings;
		
	}
	
	/**
	 * Gets all buildings instance of the given class.
	 * 
	 * @param bldgClass class
	 * @return buildings that are instances of the given class
	 */
	public <T extends Building> ArrayList<T> getBuildings(Class<T> bldgClass){
		
		ArrayList<Building> allBuildings = getBuildings();
		ArrayList<T> buildings = new ArrayList<T>();
		
		for (Building building : allBuildings) {
			
			if(bldgClass.isInstance(building)){
				try {
					buildings.add(bldgClass.cast(building));
				} catch (Exception e) { }
			}
			
		}
		
		return buildings;
		
	}

	/**
	 * Gets all buildings instances of the given class and name.
	 * 
	 * @param bldClass class
	 * @param bldgName building name
	 * @return buildings that are instances of the given class
	 */
	public <T extends Building> ArrayList<T> getBuildings(Class<T> bldClass, String bldgName){
		
		ArrayList<Building> allBuildings = getBuildings();
		ArrayList<T> buildings = new ArrayList<T>();
		
		for (Building building : allBuildings) {
			
			if(bldClass.isInstance(building) && building.getName().equalsIgnoreCase(bldgName)){
				try {
					buildings.add(bldClass.cast(building));
				} catch (Exception e) { }
			}
			
		}
		
		return buildings;
		
	}
	
	
	/**
	 * Gets the first building with the given name.
	 * 
	 * @param buildingName building name
	 * @return first building with the given name
	 */
	public Building getFirstBuilding(String buildingName) {

		ArrayList<Building> allBuildings = getBuildings();
		
		for (Building building : allBuildings) {
			if(building.getName().equals(buildingName)) return building;
		}
		
		return null;
		
	}
	
	
	
	// Members:
	/**
	 * Gets members associated.
	 * 
	 * @return member names
	 */
	public ArrayList<String> getMembers() {
		return new ArrayList<String>(players);
	}
	
	/**
	 * Checks if the player is a member.
	 * 
	 * @param playerName player name
	 * @return true if member
	 */
	public boolean isMember(String playerName) {

		return players.contains(playerName);

	}
	
	/**
	 * Gets the member count
	 * 
	 * @return member count
	 */
	public int getMemberCount() {
		return players.size();
	}
	

	/**
	 * Adds and registers a player.
	 * 
	 * @param sagaPlayer saga player
	 */
	public void addMember(SagaPlayer sagaPlayer) {


		// Add player:
		players.add(sagaPlayer.getName());
		
		// Set bundle ID:
		sagaPlayer.setBundleId(getId());
		
		
	}

	/**
	 * Removes and unregisters a player.
	 * 
	 * @param playerName saga player
	 */
	public void removeMember(SagaPlayer sagaPlayer) {
		
		
		// Remove member:
		players.remove(sagaPlayer.getName());

		// Remove chunk group ID:
		sagaPlayer.removeBundleId();

		// Remove ownership:
		if(isOwner(sagaPlayer.getName())){
			removeOwner();
		}
		
		
	}
	
	
	/**
	 * Gets the online members.
	 * 
	 * @return online members
	 */
	public Collection<SagaPlayer> getOnlineMembers() {
		
		
		Collection<SagaPlayer> onlinePlayers = Saga.plugin().getLoadedPlayers();
		Collection<SagaPlayer> onlineMembers = new HashSet<SagaPlayer>();
		
		for (SagaPlayer onlinePlayer : onlinePlayers) {
			
			if(isMember(onlinePlayer.getName())) onlineMembers.add(onlinePlayer);
			
		}
		
		return onlineMembers;
		
		
	}
	
	/**
	 * Checks if the member is registered.
	 * 
	 * @param playerName player name
	 * @return true if member is registered
	 */
	public boolean isMemberOnline(String playerName) {
		
		return isMember(playerName) && Saga.plugin().isSagaPlayerLoaded(playerName);

	}
	
	
	/**
	 * Matches a name to a members name.
	 * 
	 * @param name name
	 * @return matched name, same as given if not found
	 */
	public String matchName(String name) {
		
		ArrayList<String> members = getMembers();
		for (String memberName : members) {
			
			if(memberName.equalsIgnoreCase(name)) return memberName;
			
		}
		return name;

	}
	
	
	
	// Owners:
	/**
	 * Checks if the player counts as the owner of the settlement.
	 * 
	 * @param playerName player name
	 * @return true if owner
	 */
	public boolean isOwner(String playerName) {

		return owner.equalsIgnoreCase(playerName);
		
	}
	
	/**
	 * Sets an owner.
	 * 
	 * @param playerName player name
	 */
	public void setOwner(String playerName) {
		owner = playerName;
	}

	/**
	 * Removes an owner.
	 * 
	 * @param playerName player name
	 */
	public void removeOwner() {
		owner = "";
	}
	
	/**
	 * Gets the owner.
	 * 
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Returns the owner count.
	 * 
	 * @return owner count
	 */
	public boolean hasOwner() {
		return !owner.equals("");
	}
	
	
	
	// Permissions:
	/**
	 * Checks if the player has permission.
	 * 
	 * @param sagaPlayer saga player
	 * @param permission permission
	 * @return true if has permission
	 */
	public boolean hasPermission(SagaPlayer sagaPlayer, SettlementPermission permission) {

		return false;
	}

	
	
	// Options:
	/**
	 * Checks if the option is enabled.
	 * 
	 * @param option toggle option
	 * @return true if enabled
	 */
	public boolean isOptionEnabled(BundleToggleable option) {

		return toggleOptions.contains(option);

	}
	
	/**
	 * Enables option.
	 * 
	 * @param option toggle option
	 */
	public void enableOption(BundleToggleable option) {

		toggleOptions.add(option);

	}
	
	/**
	 * Disables option.
	 * 
	 * @param option toggle option
	 */
	public void disableOption(BundleToggleable option) {

		toggleOptions.remove(option);

	}
	
	
	
	// Messages:
	/**
	 * Sends a chat message to all members.
	 * 
	 * @param sagaPlayer sender Saga player
	 * @param message message
	 */
	public void chat(SagaPlayer sagaPlayer, String message){
		
		message = Colour.normal2 + "[" + Colour.normal1 + sagaPlayer.getName() + "] ";
		
		Collection<SagaPlayer> onlineMembers = getOnlineMembers();
		
		for (SagaPlayer onlineMember : onlineMembers) {
			onlineMember.message(message);
		}
		
	}

	
	/**
	 * Sends an information message to a member.
	 * 
	 * @param message message
	 * @param member faction member
	 */
	public void information(String message, SagaPlayer member) {
		
		message = Colour.normal2 + "(" + Colour.normal1 + "info" + Colour.normal2 + ") " + message;

		member.message(message);
		
	}
	
	/**
	 * Sends a information message.
	 * 
	 * @param message message
	 */
	public void information(String message) {

		Collection<SagaPlayer> onlineMembers = getOnlineMembers();
		
		for (SagaPlayer onlineMember : onlineMembers) {
			information(message, onlineMember);
		}
		
	}

	
	/**
	 * Sends a information message.
	 * 
	 * @param building building
	 * @param message message
	 */
	public void information(Building building, String message) {

		Collection<SagaPlayer> onlineMembers = getOnlineMembers();
		
		for (SagaPlayer onlineMember : onlineMembers) {
			information(building, message, onlineMember);
		}
		
	}

	/**
	 * Sends an information message to a member.
	 * 
	 * @param building building
	 * @param message message
	 * @param member faction member
	 */
	public void information(Building building, String message, SagaPlayer member) {
		
		message = Colour.normal2 + "(" + Colour.normal1 + building.getName() + Colour.normal2 + ") " + message;

		member.message(message);
		
	}
	
	
	
	// Entity events:
	/**
	 * Called when an entity explodes on the chunk
	 * 
	 * @param event event
	 */
	void onEntityExplode(EntityExplodeEvent event, SagaChunk locationChunk) {
		
		// Cancel entity explosions:
		event.blockList().clear();
		
	}

	/**
	 * Called when a creature spawns.
	 * 
	 * @param event event
	 * @param locationChunk origin chunk.
	 */
	void onCreatureSpawn(CreatureSpawnEvent event, SagaChunk locationChunk) {
		

		if(event.isCancelled()) return;
		
		// Forward to all buildings:
		for (int i = 0; i < groupChunks.size() && !event.isCancelled(); i++) {
			Building building = groupChunks.get(i).getBuilding();
			if(building != null) building.onCreatureSpawn(event, locationChunk);
		}
		
		
	}
	
	
	// Block events:
	/**
	 * Called when an entity forms blocks.
	 * 
	 * @param event event
	 * @param sagaChunk saga chunk
	 */
	public void onEntityBlockForm(EntityBlockFormEvent event, SagaChunk sagaChunk) {
		
		
		if(event.getEntity() instanceof Enderman){
			event.setCancelled(true);
		}
		
		
	}
	
	/**
	 * Called when a block spreads.
	 * 
	 * @param event event
	 * @param sagaChunk saga chunk
	 */
	public void onBlockSpread(BlockSpreadEvent event, SagaChunk sagaChunk) {
		
		
		// Cancel fire spread:
		if(!fireSpread){
			
			if(event.getNewState().getType().equals(Material.FIRE)){
				event.setCancelled(true);
				return;
			}
			
		}
		
		
		
	}
	
	/**
	 * Called when a block forms.
	 * 
	 * @param event event
	 * @param sagaChunk saga chunk
	 */
	public void onBlockFromTo(BlockFromToEvent event, SagaChunk sagaChunk) {
		
		
		// Cancel lava spread:
		if(!lavaSpread){
			
			if(event.getToBlock().getType().equals(Material.STATIONARY_LAVA) && event.getBlock().getLocation().getY() > 10){
				event.setCancelled(true);
				return;
			}
			if(event.getToBlock().getType().equals(Material.LAVA) && event.getBlock().getLocation().getY() > 10){
				event.setCancelled(true);
				return;
			}
			
		}
		
		
	}

	/**
	 * Called when a block is broken in the chunk.
	 * 
	 * @param event event
	 * @param sagaPlayer saga player
	 */
	public void onBuild(SagaBuildEvent event) {
		
		// Deny building:
		event.addBuildOverride(BuildOverride.CHUNK_GROUP_DENY);
		
	}
	
	
	// Command events:
    /**
     * Called when a player performs a command.
     * 
     * @param sagaPlayer saga player
     * @param event event
     * @param sagaChunk location chunk
     */
    public void onPlayerCommandPreprocess(SagaPlayer sagaPlayer, PlayerCommandPreprocessEvent event, SagaChunk sagaChunk) {

    	String command = event.getMessage().split(" ")[0].replace("/", "");
    	
    	// Permission:
    	if(SettlementConfiguration.config().checkMemberOnlyCommand(command) && 
    			!hasPermission(sagaPlayer, SettlementPermission.MEMBER_COMMAND)
    	){
    		sagaPlayer.message(GeneralMessages.noCommandPermission(this, command));
    		event.setCancelled(true);
    		return;
    	}

    }
	
	
	// Interact events:
	/**
	 * Called when a player interacts with something on the chunk.
	 * 
	 * @param event event
	 * @param sagaPlayer saga player
	 * @param sagaChunk saga chunk
	 */
    @SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event, SagaPlayer sagaPlayer, SagaChunk sagaChunk) {
    	
		
		ItemStack item = event.getPlayer().getItemInHand();
		
		// Harmful potions:
		if(item != null && item.getType() == Material.POTION){

			Short durability = item.getDurability();
			
			if(GeneralConfiguration.config().getHarmfulSplashPotions().contains(durability)){
				event.setUseItemInHand(Result.DENY);
				sagaPlayer.message(GeneralMessages.noPermission(this));
				event.getPlayer().updateInventory();
				return;
			}
			
		}
		
		
    }

    
    // Damage events:
	/**
	 * Called when a living entity gets damaged.
	 * 
	 * @param event event
	 */
	public void onDamage(SagaDamageEvent event, SagaChunk locationChunk){

		// Deny pvp:
		if(isOptionEnabled(BundleToggleable.PVP_PROTECTION)) event.addPvpOverride(PvPOverride.SAFE_AREA_DENY);
		
	}
	
	/**
	 * Called when a player is killed by another player.
	 * 
	 * @param event event
	 * @param damager damager saga player
	 * @param damaged damaged saga player
	 * @param locationChunk chunk where the pvp occurred
	 */
	public void onPvpKill(SagaPlayer attacker, SagaPlayer defender, SagaChunk locationChunk){
		
	}
	
	
	// Move events:
	/**
	 * Called when a player enters the chunk group.
	 * 
	 * @param sagaPlayer saga player
	 * @param last last chunk group, null if none
	 */
	public void onPlayerEnter(SagaPlayer sagaPlayer, Bundle last) {

		sagaPlayer.message(SettlementMessages.entered(this));

	}
	
	/**
	 * Called when a player enters the chunk group.
	 * 
	 * @param sagaPlayer saga player
	 * @param next next chunk group, null if none
	 */
	public void onPlayerLeave(SagaPlayer sagaPlayer, Bundle next) {

		if(next == null) sagaPlayer.message(SettlementMessages.left(this));


	}
	
	
	// Member events:
	/**
	 * Called when a member joins the game.
	 * 
	 * @param event event
	 * @param sagaPlayer saga player
	 */
	public void onMemberJoin(PlayerJoinEvent event, SagaPlayer sagaPlayer) {



	}
	
	/**
	 * Called when a member quits the game.
	 * 
	 * @param event event
	 * @param sagaPlayer saga player
	 */
	public void onMemberQuit(PlayerQuitEvent event, SagaPlayer sagaPlayer) {



	}

	/**
	 * Called when a member respawns.
	 * 
	 * @param sagaPlayer saga player
	 * @param event event
	 */
	public void onMemberRespawn(SagaPlayer sagaPlayer, PlayerRespawnEvent event) {
		
		// Forward to all buildings:
		for (SagaChunk sagaChunk : groupChunks) {
			
			Building building = sagaChunk.getBuilding();
			if(building != null) building.onMemberRespawn(sagaPlayer, event);
			
		}
		
	}
	
	
	
    // Other:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId() + "(" + getName() + ")";
	}
	

	
	// Control:
	/**
	 * Disables saving.
	 * 
	 */
	private void disableSaving() {

		SagaLogger.warning(this, "disabling saving");
		isSavingEnabled = false;
		
	}
	
	/**
	 * Checks if saving is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isSavingEnabled() {
		return isSavingEnabled;
	}

	
	
	// Load save:
	/**
	 * Loads and a faction from disc.
	 * 
	 * @param chunkGroupId faction ID in String form
	 * @return saga faction
	 */
	public static Bundle load(String id) {

		
		// Load:
		Bundle bundle;
		try {
			
			bundle = WriterReader.read(Directory.SETTLEMENT_DATA, id, Bundle.class);
			
		} catch (FileNotFoundException e) {
			
			SagaLogger.info(Bundle.class, "missing data for " + id + " ID");
			bundle = new Bundle("invalid");
			
		} catch (IOException e) {
			
			SagaLogger.severe(Bundle.class, "failed to read data for " + id + " ID");
			bundle = new Bundle("invalid");
			bundle.disableSaving();
			
		} catch (JsonParseException e) {
			
			SagaLogger.severe(Bundle.class, "failed to parse data for " + id + " ID: " + e.getClass().getSimpleName() + "");
			SagaLogger.info("Parse message: " + e.getMessage());
			bundle = new Bundle("invalid");
			bundle.disableSaving();
			
		}
		
		// Complete:
		bundle.complete();
		
		// Add to manager:
		BundleManager.manager().addBundle(bundle);
		for (SagaChunk sagaChunk : bundle.groupChunks) {
			BundleManager.manager().addSagaChunk(sagaChunk);
		}
		
		// Enable:
		bundle.enable();
		
		return bundle;
		
		
	}

	/**
	 * Saves faction to disc.
	 * 
	 */
	public void save() {

		
		if(!isSavingEnabled){
			SagaLogger.warning(this, "saving disabled");
			return;
		}
		
		try {
			WriterReader.write(Directory.SETTLEMENT_DATA, id.toString(), this);
		} catch (IOException e) {
			SagaLogger.severe(this, "write failed: " + e.getClass().getSimpleName() + ":" + e.getMessage());
		}
		
		
	}


}
