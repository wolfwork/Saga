package org.saga.settlements;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import org.saga.Clock;
import org.saga.Clock.MinuteTicker;
import org.saga.Saga;
import org.saga.buildings.Building;
import org.saga.chunkGroups.ChunkGroup;
import org.saga.chunkGroups.ChunkGroupMessages;
import org.saga.chunkGroups.SagaChunk;
import org.saga.config.ChunkGroupConfiguration;
import org.saga.config.ProficiencyConfiguration;
import org.saga.config.ProficiencyConfiguration.InvalidProficiencyException;
import org.saga.player.Proficiency;
import org.saga.player.SagaPlayer;

/**
 * @author andf
 *
 */
public class Settlement extends ChunkGroup implements MinuteTicker{

	
	/**
	 * Settlement level.
	 */
	private Short level;
	
	/**
	 * Player roles.
	 */
	private Hashtable<String, Proficiency> playerRoles;
	
	/**
	 * Level progress.
	 */
	private Double levelProgress;
	
	/**
	 * Experience requirement.
	 */
	transient private Double experienceRequirement; 
	
	/**
	 * True if the minute tick is registered.
	 */
	transient boolean isTicking = false;
	
	/**
	 * Settlement definition.
	 */
	transient private SettlementDefinition definition;
	
	
	// Initialization:
	/**
	 * Sets name.
	 * 
	 * @param name name
	 */
	public Settlement(String name) {
		super(name);
		level = 0;
		playerRoles = new Hashtable<String, Proficiency>();
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#completeExtended()
	 */
	@Override
	protected boolean completeExtended() {
		

		boolean integrity=true;
		
		if(level == null){
			Saga.info("ChunkGroup "+ this +" level not initialized. Setting default.");
			level = 0;
			integrity = false;
		}
		
		if(levelProgress == null){
			Saga.info("ChunkGroup "+ this +" levelProgress not initialized. Setting default.");
			levelProgress = 0.0;
			integrity = false;
		}
		
		if(playerRoles == null){
			Saga.info("ChunkGroup "+ this +" playerRoles not initialized. Setting default.");
			playerRoles = new Hashtable<String, Proficiency>();
			integrity = false;
		}
		
		Enumeration<String> playerNames = playerRoles.keys();
		while(playerNames.hasMoreElements()){
			String playerName = null;
			Proficiency proficiency = null;
			playerName = playerNames.nextElement();
			try {
				proficiency = playerRoles.get(playerName);
				proficiency.complete();
			} catch (InvalidProficiencyException e) {
				Saga.severe(this, "tried to add an invalid " + proficiency + " role:" + e.getMessage(), "removing proficiency");
				playerRoles.remove(playerName);
			}
			catch (NullPointerException e) {
				Saga.severe("Tried to add an null proficiency to " +  this + " settlement. Removing proficiency.");
				playerRoles.remove(playerName);
			}
		}
		
		// Calculated fields:
		experienceRequirement = ChunkGroupConfiguration.config().calculateLevelExperience(getLevel());
		
		// Definition:
		definition = ChunkGroupConfiguration.config().getSettlementDefinition();
		
		return integrity;
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#delete()
	 */
	@Override
	public void delete() {
		
		super.delete();
		
		// Clear roles:
		playerRoles.clear();
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#enable()
	 */
	@Override
	public void enable() {
		
		super.enable();
		
		Clock.clock().registerMinuteTick(this);
		
	}
	
	@Override
	public void disable() {
		
		super.disable();
		
		Clock.clock().unregisterMinuteTick(this);
		
	}
	
	// Chunk group management:
	/**
	 * Adds a new settlement.
	 * 
	 * @param settlement settlement
	 * @param owner owner
	 */
	public static void create(Settlement settlement, SagaPlayer owner){

		
		// Forward:
		ChunkGroup.create(settlement, owner);

		// Set owners role:
		try {
			settlement.setRole(owner, ChunkGroupConfiguration.config().settlementOwnerRole);
		} catch (InvalidProficiencyException e) {
			Saga.severe(settlement, "failed to set " + ChunkGroupConfiguration.config().settlementDefaultRole + " role, because the role name is invalid", "ignoring request");
		}
		
		
	}
	
	
	// Player and faction management:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#addPlayer3(org.saga.SagaPlayer)
	 */
	@Override
	public void addPlayer(SagaPlayer sagaPlayer) {


		super.addPlayer(sagaPlayer);

		// Set default role:
		try {
			setRole(sagaPlayer, ChunkGroupConfiguration.config().settlementDefaultRole);
		} catch (InvalidProficiencyException e) {
			Saga.severe(this, "failed to set " + ChunkGroupConfiguration.config().settlementDefaultRole + " role, because the role name is invalid", "ignoring request");
		}
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#registerPlayer3(org.saga.SagaPlayer)
	 */
	@Override
	public void registerPlayer(SagaPlayer sagaPlayer) {

		// Register role:
		Proficiency role = getRole(sagaPlayer.getName());
		if(role != null) sagaPlayer.registerRole(role);
		
		super.registerPlayer(sagaPlayer);
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#unregisterPlayer3(org.saga.SagaPlayer)
	 */
	@Override
	public void unregisterPlayer(SagaPlayer sagaPlayer) {
	
		// Unregister role:
		Proficiency role = getRole(sagaPlayer.getName());
		if(role != null) sagaPlayer.unregisterRole();

		super.unregisterPlayer(sagaPlayer);
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#removePlayer3(org.saga.SagaPlayer)
	 */
	@Override
	public void removePlayer(SagaPlayer sagaPlayer) {
		
		
		// Clear role:
		clearRole(sagaPlayer);
		
		// Forward:
		super.removePlayer(sagaPlayer);
		
		
	}
	
	
	// Leveling:
	/**
	 * Gets settlement level.
	 * 
	 */
	public Short getLevel() {
		return level;
	}
	
	/**
	 * Sets the level of the settlement.
	 * 
	 * @param level level
	 */
	public void setLevel(Short level) {
		
		
		// Set related fields:
		this.level = level;
		this.levelProgress = 0.0;
		experienceRequirement = ChunkGroupConfiguration.config().calculateLevelExperience(level);
		
		
	}
	
	/**
	 * Levels up the settlement.
	 * 
	 */
	public void levelUp() {
		setLevel((short) (getLevel()+1));
	}
	
	
	// Roles:
	/**
	 * Adds a role to the player.
	 * 
	 * @param sagaPlayer saga player
	 * @param roleName role name
	 * @throws InvalidProficiencyException thrown if the role name is invalid
	 */
	public void setRole(SagaPlayer sagaPlayer, String roleName) throws InvalidProficiencyException {

		
		// Void role:
		if(roleName.equals(ChunkGroupConfiguration.VOID_PROFICIENCY)){
			return;
		}
		
		// Clear previous role:
		if( playerRoles.get(sagaPlayer.getName()) != null ){
			clearRole(sagaPlayer);
		}
		
		// Create role:
		Proficiency role = ProficiencyConfiguration.config().createProficiency(roleName);

		// Add to settlement:
		playerRoles.put(sagaPlayer.getName(), role);
		
		// Register:
		sagaPlayer.setRole(role);
		
		
	}
	
	/**
	 * Clears a role from the player.
	 * 
	 * @param sagaPlayer sga player
	 */
	public void clearRole(SagaPlayer sagaPlayer) {
	

		// Check role:
		Proficiency role = playerRoles.get( sagaPlayer.getName() );
		if( role == null ){
			return;
		}

		// Remove from settlement:
		playerRoles.remove(sagaPlayer.getName());
	
		// Unregister:
		sagaPlayer.clearRole();
		
			
	}

	/**
	 * Checks if the given role is available.
	 * 
	 * @param roleName role name
	 * @return true if available
	 */
	public boolean isRoleAvailable(String roleName) {
		
		if(roleName.equals(ChunkGroupConfiguration.config().settlementDefaultRole)){
			return true;
		}
		
		return getAvailableRoles(roleName) > 0;
		
	}
	
	/**
	 * Gets a player role.
	 * 
	 * @param playerName player name
	 * @return player role. null if none
	 */
	public Proficiency getRole(String playerName) {
		
		return playerRoles.get(playerName);
		
	}
	
	/**
	 * Gets the total assigned proficiencies with the given name.
	 * 
	 * @param proficiencyName proficiency name
	 * @return amount of proficiencies assigned with the given name
	 */
	public Integer getTotalAssignedProficiencies(String proficiencyName) {


		int total = 0;
		
		Proficiency proficiency = null;
		Enumeration<Proficiency> proficiencies = playerRoles.elements();
		while(proficiencies.hasMoreElements()){
			proficiency = proficiencies.nextElement();
			if(proficiency.getName().equals(proficiencyName)){
				total++;
			}
		}
		
		return total;
		
		
	}
	
	/**
	 * Gets the total amount of roles.
	 * 
	 * @param roleName role name
	 * @return total amount roles
	 */
	public Integer getTotalRoles(String roleName) {
		
		
		if(roleName.equals(ChunkGroupConfiguration.config().settlementDefaultRole)){
			return getPlayerCount() - getInactivePlayerCount();
		}
		
		Integer total = 0;
		
		// Settlement roles:
		total += getDefinition().getTotalRoles(roleName, getLevel());
		
		// Buildings roles:
		ArrayList<SagaChunk> groupChunks = getGroupChunks();
		for (SagaChunk sagaChunk : groupChunks) {
			
			Building building = sagaChunk.getBuilding();
			if(building == null) continue;
			total += building.getDefinition().getTotalRoles(roleName, building.getLevel());
			
		}
		
		return total;
		
	}
	
	/**
	 * Gets the amount of used roles.
	 * 
	 * @param roleName role name
	 * @return amount of used roles
	 */
	public Integer getUsedRoles(String roleName) {
		
		Integer total = 0;
		Enumeration<String> players = playerRoles.keys();
		while (players.hasMoreElements()) {
			
			String player = players.nextElement();
			Proficiency role = playerRoles.get(player);
			
			if(roleName.equals(role.getName()))total ++;
			
		}
 		
		return total;
		
	}
	
	/**
	 * Gets the amount of used roles.
	 * 
	 * @param roleName role name
	 * @return amount of available roles
	 */
	public Integer getAvailableRoles(String roleName) {
		
		return getTotalRoles(roleName) - getUsedRoles(roleName);
		
	}
	
	/**
	 * Gets the available roles.
	 * 
	 * @return all available roles
	 */
	public HashSet<String> getRoles() {
		
		HashSet<String> roles = new HashSet<String>();
		
		// Add default role:
		roles.add(ChunkGroupConfiguration.config().settlementDefaultRole);
		
		// Add settlement roles:
		roles.addAll(getDefinition().getRoles());
		
		// Add building roles:
		ArrayList<SagaChunk> groupChunks = getGroupChunks();
		for (SagaChunk sagaChunk : groupChunks) {
			
			Building building = sagaChunk.getBuilding();
			if(building == null) continue;
			
			ArrayList<String> promotions = building.getDefinition().getRoles();
			roles.addAll(promotions);
			
		}
		
		return roles;
		
	}

	
	// Claiming:
	/**
	 * Gets owned chunks count.
	 * 
	 * @return owned chunks
	 */
	public Short getTotalClaimed() {
		return new Integer(getGroupChunks().size()).shortValue();
	}
	
	/**
	 * Gets the claims the faction has in total.
	 * 
	 * @return claims in total.
	 */
	public Short getTotalClaims() {
		return ChunkGroupConfiguration.config().calculateSettlementClaims(getLevel()).shortValue();
	}
	
	/**
	 * Gets remaining claims.
	 * 
	 * @return remaining claims
	 */
	public Short getRemainingClaims() {
		return ( new Integer(getTotalClaims() - getTotalClaimed()) ).shortValue();
	}

	/**
	 * Check if there is a claim available.
	 * 
	 * @return true if available
	 */
	public boolean isClaimAvailable() {
		return getRemainingClaims() > 0 || hasUnlimitedClaimBonus();
	}
	
	// Buildings:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#getTotalBuildingPoints()
	 */
	@Override
	public Integer getTotalBuildingPoints() {
		return definition.getBuildingPoints(getLevel());
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#getTotalBuildings(java.lang.String)
	 */
	@Override
	public Integer getTotalBuildings(String buildingName) {
		return getDefinition().getTotalBuildings(buildingName, getLevel()) + super.getTotalBuildings(buildingName);
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#getEnabledBuildings()
	 */
	@Override
	public HashSet<String> getEnabledBuildings() {
		
		HashSet<String> enabledBuildings = getDefinition().getAllBuildings(getLevel());
		enabledBuildings.addAll(super.getEnabledBuildings());
		return enabledBuildings;
		
	}
	
	// Permissions:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canClaim(org.saga.SagaPlayer)
	 */
	public boolean canClaim(SagaPlayer sagaPlayer) {
		
		
		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.CLAIM);
		
		
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canAbandon(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canAbandon(SagaPlayer sagaPlayer) {

		
		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;

		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.ABANDON);
			
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canDelete(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canDisolve(SagaPlayer sagaPlayer) {
		

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;

		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.DISOLVE);
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canBuild(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canBuild(SagaPlayer sagaPlayer) {
		

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.BUILD);
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canInvite(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canInvite(SagaPlayer sagaPlayer) {
		

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.INVITE);
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canClaimChunkGroup(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canClaimChunkGroup(SagaPlayer sagaPlayer) {
		return getPlayerCount() == 0;
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canKick(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canKick(SagaPlayer sagaPlayer) {


		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.KICK);
		
		
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canSetBuilding(org.saga.SagaPlayer, org.saga.buildings.Building)
	 */
	@Override
	public boolean canSetBuilding(SagaPlayer sagaPlayer, Building building) {

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.SET_BUILDING);
		
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canSetBuilding(org.saga.SagaPlayer, org.saga.buildings.Building)
	 */
	@Override
	public boolean canRemoveBuilding(SagaPlayer sagaPlayer, Building building) {

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.REMOVE_BUILDING);
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canDeclareOwner(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canDeclareOwner(SagaPlayer sagaPlayer) {
		
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		return !hasOwner() && isMember(sagaPlayer);
		
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canSetBuilding(org.saga.SagaPlayer, org.saga.buildings.Building)
	 */
	public boolean canSetRole(SagaPlayer sagaPlayer) {

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.SET_ROLE);
		
	}
	
	@Override
	public boolean canRename(SagaPlayer sagaPlayer) {

		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check role:
		Proficiency role = playerRoles.get(sagaPlayer.getName());
		if(role == null){
			return false;
		}
		
		// Check permission:
		return role.hasSettlementPermission(SettlementPermission.RENAME);
		
	}


	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canKick(org.saga.SagaPlayer)
	 */
	@Override
	public boolean canSpawn(SagaPlayer sagaPlayer) {


		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check membership:
		return isMember(sagaPlayer);
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canHurtAnimals(org.saga.player.SagaPlayer)
	 */
	@Override
	public boolean canHurtAnimals(SagaPlayer sagaPlayer) {


		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check membership:
		return isMember(sagaPlayer);
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#canHurtAnimals(org.saga.player.SagaPlayer)
	 */
	@Override
	public boolean canTrample(SagaPlayer sagaPlayer) {


		// Owner:
		if(isOwner(sagaPlayer.getName()) || sagaPlayer.isAdminMode()) return true;
		
		// Check membership:
		return isMember(sagaPlayer);
		
		
	}

	@Override
	public boolean canUseCommand(SagaPlayer sagaPlayer, String command) {
		
		if(super.canUseCommand(sagaPlayer, command)) return true;

		return isMember(sagaPlayer);
	
	}
	
	
	// Leveling:
	/**
	 * Gets the levelProgress.
	 * 
	 * @return the levelProgress
	 */
	public Double getLevelProgress() {
		return levelProgress;
	}

	/**
	 * Gets the experienceRequirement.
	 * 
	 * @return the experienceRequirement
	 */
	public Double getExperienceRequirement() {
		return experienceRequirement;
	}
	
	/**
	 * Gets the experience amount the settlement is getting.
	 * 
	 * @return experience amount
	 */
	public Double getExperienceAmount() {
		return ChunkGroupConfiguration.config().calculateExperiencePerPlayer(getRegisteredPlayerCount());
	}

	/**
	 * Gets the definition for the settlement.
	 * 
	 * @return definition
	 */
	public SettlementDefinition getDefinition() {
		return definition;
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.Clock.MinuteTicker#clockMinuteTick()
	 */
	@Override
	public void clockMinuteTick() {


		// Level progress:
		if(level < ChunkGroupConfiguration.config().settlementMaximumLevel && definition.canLevelUp(this)){
			levelProgress += getExperienceAmount();
			if(levelProgress >= experienceRequirement){
				
				levelUp();

				// Inform:
				Saga.broadcast(ChunkGroupMessages.settlementLevel(this));
				
			}
		}
		
		
	}

	
	// Other:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.chunkGroups.ChunkGroup#toString()
	 */
	@Override
	public String toString() {
		return getId() + " (" + getName() + ")";
	}
	
	
	/**
	 * Settlement permissions.
	 * 
	 * @author andf
	 *
	 */
	public enum SettlementPermission{

		
		BUILD,
		CLAIM,
		ABANDON,
		DISOLVE,
		INVITE,
		KICK,
		SET_ROLE,
		SET_BUILDING,
		REMOVE_BUILDING,
		RENAME;
		
		
	}
	
	
	
}