package org.saga.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDeathEvent;
import org.saga.Clock;
import org.saga.Clock.HourTicker;
import org.saga.Saga;
import org.saga.config.BalanceConfiguration;
import org.saga.player.SagaPlayer;
import org.saga.utility.WriterReader;

import com.google.gson.JsonParseException;

public class StatisticsManager implements HourTicker{


	/**
	 * Instance of the configuration.
	 */
	transient private static StatisticsManager instance;
	
	/**
	 * Gets the instance.
	 * 
	 * @return instance
	 */
	public static StatisticsManager manager() {
		return instance;
	}
	
	
	/**
	 * Class kills.
	 */
	private Hashtable<String, Hashtable<String, Integer>> classKills;

	/**
	 * Skill upgrades.
	 */
	private Hashtable<String, Integer> skillUpgrades;

	/**
	 * Levels used for upgrades.
	 */
	private Hashtable<String, Integer> skillUpgradeLevels;

	/**
	 * Coins used for upgrades.
	 */
	private Hashtable<String, Double> skillUpgradeCoins;

	/**
	 * Guardian stone breaks.
	 */
	private Integer guardRuneRestores;

	/**
	 * Guardian stone fixes.
	 */
	private Integer guardRuneRecharges;

	/**
	 * Ability usage.
	 */
	private Hashtable<String, Integer> abilityUsage;

	/**
	 * Ability awarded experience.
	 */
	private Hashtable<String, Integer> abilityExp;

	/**
	 * Ore mining.
	 */
	private Hashtable<String, Hashtable<Material, Integer>> xrayStatistics;

	/**
	 * Block data changes.
	 */
	private Integer blockDataChanges; 
	
	/**
	 * Experience awarded by skills for breaking blocks.
	 */
	private Hashtable<String, Integer> blockSkillExp; 
	
	/**
	 * Players that were awarded with skill block experience.
	 */
	private Hashtable<String, HashSet<String>> blockSkillExpPlayers; 
	
	
	/**
	 * Last date.
	 */
	private Long startDate = null;
	
	
	// Initialization:
	/**
	 * Initializes.
	 * 
	 * @param str nothing
	 */
	public StatisticsManager(String str) {
		
		reset();
		
	}
	
	/**
	 * Goes trough all the fields and makes sure everything has been set after gson load.
	 * If not, it fills the field with defaults.
	 * 
	 * @return true if everything was correct.
	 */
	public boolean complete() {
		
		
		boolean integrity = true;
		
		if(classKills == null){
			Saga.severe(getClass(), "classKills field failed to initialize", "setting default");
			classKills = new Hashtable<String, Hashtable<String,Integer>>();
			integrity=false;
		}
		
		if(skillUpgrades == null){
			Saga.severe(getClass(), "skillUpgrades field failed to initialize", "setting default");
			skillUpgrades = new Hashtable<String, Integer>();
			integrity=false;
		}
		
		if(skillUpgradeLevels == null){
			Saga.severe(getClass(), "skillUpgradeLevels field failed to initialize", "setting default");
			skillUpgradeLevels = new Hashtable<String, Integer>();
			integrity=false;
		}
		
		if(skillUpgradeCoins == null){
			Saga.severe(getClass(), "skillUpgradeCoins field failed to initialize", "setting default");
			skillUpgradeCoins = new Hashtable<String, Double>();
			integrity=false;
		}
		
		if(startDate == null){
			Saga.severe(getClass(), "startDate field failed to initialize", "setting default");
			startDate = System.currentTimeMillis();
			integrity=false;
		}
		
		if(guardRuneRestores == null){
			Saga.severe(getClass(), "guardRuneRestores field failed to initialize", "setting default");
			guardRuneRestores = 0;
			integrity=false;
		}
		
		if(guardRuneRecharges == null){
			Saga.severe(getClass(), "guardRuneRecharges field failed to initialize", "setting default");
			guardRuneRecharges = 0;
			integrity=false;
		}
		
		if(abilityUsage == null){
			Saga.severe(getClass(), "abilityUsage field failed to initialize", "setting default");
			abilityUsage = new Hashtable<String, Integer>();
			integrity=false;
		}
		
		if(abilityExp == null){
			Saga.severe(getClass(), "abilityExp field failed to initialize", "setting default");
			abilityExp = new Hashtable<String, Integer>();
			integrity=false;
		}
		
		if(xrayStatistics == null){
			Saga.severe(getClass(), "oreMining field failed to initialize", "setting default");
			xrayStatistics = new Hashtable<String, Hashtable<Material,Integer>>();
			integrity=false;
		}
		
		if(blockDataChanges == null){
			Saga.severe(getClass(), "blockDataChanges field failed to initialize", "setting default");
			blockDataChanges = 0;
			integrity=false;
		}
		
		if(blockSkillExp == null){
			Saga.severe(getClass(), "blockSkillExp field failed to initialize", "setting default");
			blockSkillExp = new Hashtable<String, Integer>();
			integrity=false;
		}
		
		if(blockSkillExpPlayers == null){
			Saga.severe(getClass(), "blockSkillExpPlayers field failed to initialize", "setting default");
			blockSkillExpPlayers = new Hashtable<String, HashSet<String>>();
			integrity=false;
		}
		
		return integrity;
		
		
	}
	
	/**
	 * Resets the statistics.
	 * 
	 */
	public void reset() {

		
		classKills = new Hashtable<String, Hashtable<String,Integer>>();
		skillUpgrades = new Hashtable<String, Integer>();
		skillUpgradeLevels = new Hashtable<String, Integer>();
		skillUpgradeCoins = new Hashtable<String, Double>();
		guardRuneRestores = 0;
		guardRuneRecharges = 0;
		startDate = System.currentTimeMillis();
		abilityExp = new Hashtable<String, Integer>();
		abilityUsage = new Hashtable<String, Integer>();
		xrayStatistics = new Hashtable<String, Hashtable<Material,Integer>>();
		blockDataChanges = 0;
		blockSkillExp = new Hashtable<String, Integer>();
		blockSkillExpPlayers = new Hashtable<String, HashSet<String>>();
		
		
	}

	
	// Clock:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.Clock.HourTicker#clockHourTick()
	 */
	@Override
	public void clockHourTick() {
		
		
		// Check if a day has passed:
		Integer ageDays = new Double(calcStatisticsAge() / (60.0 * 60.0 * 1000.0)).intValue();
		if(ageDays < BalanceConfiguration.config().statisticsUpdateAge) return;
		
		Saga.info("Resetting statistics.");
		
		archive();
		
		reset();
		
		
	}
	
	
	// Kills:
	/**
	 * Adds a class kill.
	 * 
	 * @param attacker attacker
	 * @param defender defender
	 */
	private void addClassKill(String attacker, String defender) {

		Hashtable<String, Integer> allKills = classKills.get(attacker);
		if(allKills == null) allKills = new Hashtable<String, Integer>();
		classKills.put(attacker, allKills);
		
		Integer kills = allKills.get(defender);
		if(kills == null) kills = 0;
		
		kills++;
		
		allKills.put(defender, kills);
		
	}
	
	/**
	 * Gets class kills.
	 * 
	 * @return class kills
	 */
	public Hashtable<String, Hashtable<String, Integer>> getClassKills() {
		return classKills;
	}
	
	/**
	 * Gets the kills by the class.
	 * 
	 * @param attacker attacker class
	 * @param defender defender class
	 * @return kills
	 */
	public Integer getClazzKills(String attacker, String defender) {

		Hashtable<String, Integer> allKills = classKills.get(attacker);
		if(allKills == null) return 0;
		
		Integer kills = allKills.get(defender);
		if(kills == null) return 0;
		
		return kills;
		
	}

	
	// Skills:
	/**
	 * Gets skill upgrades.
	 * 
	 * @param skillName skill name
	 * @return upgrades
	 */
	public Integer getSkillUpgrades(String skillName) {
		
		Integer value = skillUpgrades.get(skillName);
		if(value == null) value = 0;
		
		return value;
		
	}
	
	/**
	 * Gets skill upgrade level costs.
	 * 
	 * @param skillName skill name
	 * @return upgrade level costs
	 */
	public Integer getSkillUpgradeLevels(String skillName) {
		
		Integer value = skillUpgradeLevels.get(skillName);
		if(value == null) value = 0;
		
		return value;
		
	}
	
	/**
	 * Gets skill upgrade coin costs.
	 * 
	 * @param skillName skill name
	 * @return upgrade coin costs
	 */
	public Double getSkillUpgradeCoins(String skillName) {
		
		Double value = skillUpgradeCoins.get(skillName);
		if(value == null) value = 0.0;
		
		return value;
		
	}
	
	
	// Abilities:
	/**
	 * Gets ability uses.
	 * 
	 * @param abilityName ability name
	 * @return uses
	 */
	public Integer getAbilityUses(String abilityName) {

		Integer value = abilityUsage.get(abilityName);
		if(value == null) value = 0;
		
		return value;
		
	}

	/**
	 * Gets ability rewarded experience.
	 * 
	 * @param abilityName ability name
	 * @return experience
	 */
	public Integer getAbilityExp(String abilityName) {

		Integer value = abilityExp.get(abilityName);
		if(value == null) value = 0;
		
		return value;
		
	}
	
	
	// Guardian stones:
	/**
	 * Gets the guardStoneBreaks.
	 * 
	 * @return the guardStoneBreaks
	 */
	public Integer getGuardStoneBreaks() {
		return guardRuneRestores;
	}
	
	/**
	 * Gets the guardStoneFixes.
	 * 
	 * @return the guardStoneFixes
	 */
	public Integer getGuardStoneFixes() {
		return guardRuneRecharges;
	}
	
	
	// Xray:
	/**
	 * Adds ore mined.
	 * 
	 * @param name player name
	 * @param material material
	 * @param amount amount
	 */
	public void addOreMined(String name, Material material, Integer amount) {

		Hashtable<Material, Integer> blocks = xrayStatistics.get(name);
		if(blocks == null) blocks = new Hashtable<Material, Integer>();
	
		Integer oldAmount = blocks.get(material);
		if(oldAmount == null) oldAmount = 0;
		
		blocks.put(material, oldAmount + amount);
		xrayStatistics.put(name, blocks);
		
	}
	
	/**
	 * Gets ore mined.
	 * 
	 * @param name player name
	 * @param material material
	 * @return amount mined
	 */
	public Integer getOreMined(String name, Material material) {

		Hashtable<Material, Integer> blocks = xrayStatistics.get(name);
		if(blocks == null) return 0;
	
		Integer amount = blocks.get(material);
		if(amount == null) return 0;
		
		return amount;
		
	}
	
	// Age:
	/**
	 * Calculates the milliseconds the statistics are active.
	 * 
	 * @return age in milliseconds
	 */
	public long calcStatisticsAge() {

		return System.currentTimeMillis() - startDate;
		
	}
	

	// Events:
	/**
	 * Called when a player kills a player.
	 * 
	 * @param attacker attacker
	 * @param defender defender
	 */
	public void onPlayerKillPlayer(SagaPlayer attacker, SagaPlayer defender, EntityDeathEvent event) {

		
		// Classes:
		String attackerClass = "none";
		String defenderClass = "none";

		if(attacker.getClazz() != null){
			attackerClass = attacker.getClazz().getName();
		}

		if(defender.getClazz() != null){
			defenderClass = defender.getClazz().getName();
		}
		
		addClassKill(attackerClass, defenderClass);
		
		
	}
	
	/**
	 * Called when a player upgrades his skill.
	 * 
	 * @param skillName skill name
	 * @param levelCost level cost
	 * @param coinCost coin cost
	 */
	public void onSkillUpgrade(String skillName, Integer levelCost, Double coinCost) {

		
		// Upgrades:
		Integer upgrades = skillUpgrades.get(skillName);
		if(upgrades == null) upgrades = 0;
		skillUpgrades.put(skillName, upgrades + 1);
		
		// Upgrade level costs:
		Integer upgradesLevels = skillUpgradeLevels.get(skillName);
		if(upgradesLevels == null) upgradesLevels = 0;
		skillUpgradeLevels.put(skillName, upgradesLevels + levelCost);
		
		// Upgrade coin costs:
		Double upgradeCoins = skillUpgradeCoins.get(skillName);
		if(upgradeCoins == null) upgradeCoins = 0.0;
		skillUpgradeCoins.put(skillName, upgradeCoins + coinCost);
		
		
	}
	

	/**
	 * Called when a guardian stone restored items.
	 * 
	 */
	public void onGuardanRuneRestore() {

		guardRuneRestores++;
		
	}
	
	/**
	 * Called when a guardian stone is recharged.
	 * 
	 */
	public void onGuardanRuneRecharge() {

		guardRuneRecharges++;
		
	}

	/**
	 * Called when a player uses his ability.
	 * 
	 * @param abilityName ability name
	 * @param rewardedExp exp rewarded
	 */
	public void onAbilityUse(String abilityName, Integer rewardedExp) {

		
		// Upgrades:
		Integer uses = abilityUsage.get(abilityName);
		if(uses == null) uses = 0;
		abilityUsage.put(abilityName, uses + 1);
		
		// Upgrade level costs:
		Integer expReward = abilityExp.get(abilityName);
		if(expReward == null) expReward = 0;
		abilityExp.put(abilityName, expReward + rewardedExp);

		
	}
	
	/**
	 * Called when xray statistics are updated.
	 * 
	 * @param name player name
	 * @param material material
	 * @param amount amount
	 */
	public void onXrayStatisticsUpdate(String name, Material material, Integer amount) {

		addOreMined(name, material, amount);
		
	}

	/**
	 * Called when a block data is changed.
	 * 
	 */
	public void onBlockDataChange() {

		blockDataChanges++;
		
	}

	/**
	 * Called when experience is awarded for skill block breaking.
	 * 
	 * @param skillName ability name
	 * @param exp experience awarded
	 * @param sagaPlayer player
	 */
	public void onSkillBlockExp(String skillName, Integer exp, SagaPlayer sagaPlayer) {

		blockSkillExp.put(skillName, getBlockSkillExperience(skillName) + exp);
		
		HashSet<String> players = getBlockSkillPlayers(skillName);
		players.add(sagaPlayer.getName());
		blockSkillExpPlayers.put(skillName, players);
		
	}

	
	// X-ray:
	/**
	 * Gets all players for who xray statistics exist.
	 * 
	 * @return players with xray statistics
	 */
	public ArrayList<String> getXrayPlayers() {
		
		return new ArrayList<String>(xrayStatistics.keySet());
		
	}
	
	/**
	 * Gets a ratio for the given material.
	 * 
	 * @param name player name
	 * @param material material
	 * @return ratio
	 */
	public Double getOreRatio(String name, Material material) {

		
		Double materialAmount = getOreMined(name, material).doubleValue();
		Double stoneAmount = getOreMined(name, Material.STONE).doubleValue();
		
		// Avoid infinity:
		if(stoneAmount == 0.0) stoneAmount = 0.00000000001;
		
		return materialAmount / stoneAmount;
		
		
	}
	
	
	// Block:
	/**
	 * Gets the times the block data was changed.
	 * 
	 * @return times the block data was changed
	 */
	public Integer getBlockDataChanges() {
		return blockDataChanges;
	}
	
	/**
	 * Gets the experience awarded for block breaks.
	 * 
	 * @param name skill name
	 * @return experience awarded
	 */
	public Integer getBlockSkillExperience(String name) {
		
		Integer exp = blockSkillExp.get(name);
		if(exp == null) exp = 0;
		
		return exp;
		
	}
	
	/**
	 * Gets all skills
	 * 
	 * @return
	 */
	public ArrayList<String> getBlockSkills() {

		return new ArrayList<String>(blockSkillExp.keySet());
		
	}
	
	/**
	 * Gets all players that used a block skill.
	 * 
	 * @return players who used a block skill.
	 */
	public HashSet<String> getBlockSkillPlayers(String skillName) {

		HashSet<String> players = blockSkillExpPlayers.get(skillName);
		if(players == null) players = new HashSet<String>();
		
		return players;
		
	}

	/**
	 * Counts all players that used a block skill.
	 * 
	 * @return count of players who used a block skill.
	 */
	public Integer countBlockSkillPlayers(String skillName) {

		HashSet<String> players = blockSkillExpPlayers.get(skillName);
		if(players == null) return 0;
		
		return players.size();
		
	}
	
	
	// Load unload:
	/**
	 * Loads the manager.
	 * 
	 * @return experience configuration
	 */
	public static StatisticsManager load(){

		
		// Inform:
		Saga.info("Loading statistics.");
		
		boolean integrity = true;
		
		// Load:
		StatisticsManager manager;
		try {
			
			manager = WriterReader.readLastStatistics();
			
		} catch (FileNotFoundException e) {
			
			manager = new StatisticsManager("");
			
		} catch (IOException e) {
			
			Saga.severe(StatisticsManager.class, "failed to load", "loading defaults");
			manager = new StatisticsManager("");
			integrity = false;
			
		} catch (JsonParseException e) {
			
			Saga.severe(StatisticsManager.class, "failed to parse", "loading defaults");
			Saga.info("Parse message :" + e.getMessage());
			manager = new StatisticsManager("");
			integrity = false;
			
		}
		
		// Integrity check and complete:
		integrity = manager.complete() && integrity;
		
		
		// Set instance:
		instance = manager;
		
		// Clock:
		Clock.clock().registerHourTick(instance);
		
		return manager;
		
		
	}
	
	/**
	 * Saves the statistics.
	 * 
	 */
	public static void save(){

		
		// Inform:
		Saga.info("Saving statistics.");
		
		try {
			
			WriterReader.writeLastStatistics(instance);
			
		} catch (IOException e) {
			
			Saga.severe(StatisticsManager.class, "write failed", "ignoring write");
			Saga.info("Write failure cause:" + e.getClass().getSimpleName() + ":" + e.getMessage());
			
		}
		
	}
	
	/**
	 * Archives the statistics.
	 * 
	 */
	public void archive(){

		// Inform:
		Saga.info("Archiving statistics.");
		
		try {
			
			WriterReader.writeStatisticsArchive(instance, GregorianCalendar.getInstance());
			
		} catch (IOException e) {
			
			Saga.severe(StatisticsManager.class, "write failed", "ignoring write");
			Saga.info("Write failure cause:" + e.getClass().getSimpleName() + ":" + e.getMessage());
			
		}
		
	}
	
	/**
	 * Unloads the statistics.
	 * 
	 */
	public static void unload(){

		// Inform:
		Saga.info("Unloading statistics.");
		
		// Clock:
		Clock.clock().registerHourTick(instance);
		
		save();
		
		instance = null;
		
		
	}
	
	
}