package org.saga.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
import org.bukkit.entity.Creature;
import org.saga.SagaLogger;
import org.saga.abilities.Ability;
import org.saga.player.SagaLiving;
import org.saga.saveload.Directory;
import org.saga.saveload.WriterReader;
import org.saga.utility.TwoPointFunction;

public class ExperienceConfiguration {

	
	/**
	 * Instance of the configuration.
	 */
	transient private static ExperienceConfiguration instance;
	
	/**
	 * Gets the instance.
	 * 
	 * @return instance
	 */
	public static ExperienceConfiguration config() {
		return instance;
	}
	
	
	// Attributes and abilities:
	/**
	 * Attribute point cost.
	 */
	private Double attributePointCost;

	/**
	 * Ability point cost.
	 */
	private Double abilityPointCost;
	
	
	// Experience gain:
	/**
	 * Maximum experience.
	 */
	private Double maxExp;
	
	/**
	 * Experience gain multiplier for given exp value.
	 */
	private TwoPointFunction expGainMultiplier;
	
	
	/**
	 * Block break experience.
	 */
	private Hashtable<Material, Hashtable<Byte, Double>> blockExp;
	
	/**
	 * Player kill experience.
	 */
	private TwoPointFunction playerExp;
	
	/**
	 * Creature kill experience.
	 */
	private Hashtable<String, Double> creatureExp;
	
	/**
	 * Ability experience.
	 */
	private Hashtable<String, TwoPointFunction> abilityExp;
	
	
	// Spawners:
	/**
	 * Spawner enchant points multiplier.
	 */
	public Double spawnerEncPointMult;
	
	/**
	 * Spawner exp multiplier.
	 */
	public Double spawnerExpMult;
	
	
	
	
	// Initialisation:
	/**
	 * Used by gson.
	 * 
	 */
	public ExperienceConfiguration() {
	}
	
	/**
	 * Completes.
	 * 
	 * @return integrity check
	 */
	public boolean complete() {
		

		boolean integrity = true;
		
		// Set instance:
		instance = this;
		
		// Attributes and abilities:
		if(attributePointCost == null){
			SagaLogger.nullField(getClass(), "attributePointCost");
			attributePointCost = 500.0;
		}
		
		if(abilityPointCost == null){
			SagaLogger.nullField(getClass(), "abilityPointCost");
			abilityPointCost = 500.0;
		}

		// Experience gain:
		if(maxExp == null){
			maxExp = 10000000000.0;
			SagaLogger.nullField(getClass(), "maxExp");
			integrity = false;
		}
		
		if(expGainMultiplier == null){
			expGainMultiplier = new TwoPointFunction(1.0);
			SagaLogger.nullField(getClass(), "expGainMultiplier");
			integrity = false;
		}
		
		if(blockExp == null){
			blockExp = new Hashtable<Material, Hashtable<Byte,Double>>();
			SagaLogger.nullField(getClass(), "blockExp");
			integrity = false;
		}
		
		if(playerExp == null){
			playerExp = new TwoPointFunction(0.0);
			SagaLogger.nullField(getClass(), "playerExp");
			integrity = false;
		}
		
		if(creatureExp == null){
			creatureExp = new Hashtable<String, Double>();
			SagaLogger.nullField(getClass(), "creatureExp");
			integrity = false;
		}
		
		if(abilityExp == null){
			abilityExp = new Hashtable<String, TwoPointFunction>();
			SagaLogger.nullField(getClass(), "abilityExp");
			integrity = false;
		}
		Collection<TwoPointFunction> abExpVals = abilityExp.values();
		for (TwoPointFunction abExpVal : abExpVals) {
			abExpVal.complete();
		}
		
		if(spawnerEncPointMult == null){
			spawnerEncPointMult = 1.0;
			SagaLogger.nullField(this, "spawnerEncPointMult");
			integrity = false;
		}
		
		if(spawnerExpMult == null){
			spawnerExpMult = 1.0;
			SagaLogger.nullField(this, "spawnerExpMult");
			integrity = false;
		}
		
		return integrity;
		
		
	}

	
	
	// Attributes and abilities:
	/**
	 * Gets the cost of a single attribute point.
	 * 
	 * @return attribute point cost
	 */
	public Double getAttributePointCost() {
		return attributePointCost;
	}
	
	/**
	 * Gets the amount of attribute points available.
	 * 
	 * @param exp player exp
	 * @return attribute points
	 */
	public Integer getAttributePoints(Double exp) {
		return (int) (exp / attributePointCost);
	}
	
	/**
	 * Gets the cost of a single ability point.
	 * 
	 * @return ability point cost
	 */
	public Double getAbilityPointCost() {
		return abilityPointCost;
	}
	
	/**
	 * Gets the amount of ability points available.
	 * 
	 * @param exp player exp
	 * @return ability points
	 */
	public Integer getAbilityPoints(Double exp) {
		return (int) (exp / abilityPointCost);
	}
	
	
	
	
	// Experience gain:
	/**
	 * Gets max experience.
	 * 
	 * @return max experience
	 */
	public Double getMaxExp() {
		return maxExp;
	}

	/**
	 * Calculates experience points needed for given attribute points.
	 * 
	 * @param attrPoints attribute points
	 * @return experience needed
	 */
	public Double calcAttributeExp(Integer attrPoints) {
		return attributePointCost * attrPoints;
	}
	
	/**
	 * Calculates experience points needed for given ability points.
	 * 
	 * @param abilPoints ability points
	 * @return experience needed
	 */
	public Double calcAbilityExp(Integer abilPoints) {
		return abilityPointCost * abilPoints;
	}
	
	
	/**
	 * Gets the experience gain multiplier.
	 * 
	 * @param exp exp
	 * @return experience gain multiplier
	 */
	public Double getExpGainMultiplier(Double exp) {
		return expGainMultiplier.value(exp);
	}

	
	/**
	 * Gets the experience for a block break.
	 * 
	 * @param block block
	 * @return experience
	 */
	public Double getExp(Block block) {

		
		Hashtable<Byte, Double> datas = blockExp.get(block.getType());
		if(datas == null) return 0.0;
		
		Double exp = datas.get(new Byte(block.getData()));
		if(exp == null) return 0.0;
			
		return exp;
		
		
	}

	/**
	 * gets the experience for a player kill.
	 * 
	 * @param sagaDefender saga player
	 * @return experience
	 */
	public Double getExp(SagaLiving sagaDefender) {
		
		Double exp = playerExp.value(sagaDefender.getUsedAttributePoints());
		if(exp == null) return 0.0;
		
		return exp;
		
	}
	
	/**
	 * Gets the experience for a creature kill.
	 * 
	 * @param creature creature
	 * @return experience
	 */
	public Double getExp(Creature creature) {
		
		Double exp = creatureExp.get(creature.getClass().getSimpleName().toLowerCase().replace("craft", ""));
		
		if(exp == null) exp = creatureExp.get("default");
		
		if(exp == null) return 0.0;
		
		return exp;
		
	}
	
	/**
	 * Gets the experience for ability usage.
	 * 
	 * @param ability ability
	 * @param value ability specific value
	 * @return
	 */
	public Double getExp(Ability ability, Integer value) {

		
		Double exp = null;
		
		TwoPointFunction expFun = abilityExp.get(ability.getName());
		if(expFun != null) exp = expFun.value(value);
		
		if(exp == null) return 0.0;
		
		return exp;
		
		
	}
	
	
	
	// Load unload:
	/**
	 * Loads configuration.
	 * 
	 * @return configuration
	 */
	public static ExperienceConfiguration load(){


		// Create config:
		if(!WriterReader.checkExists(Directory.EXPERIENCE_CONFIG)){

			try {
				WriterReader.unpackConfig(Directory.EXPERIENCE_CONFIG);
			}
			catch (IOException e) {
				SagaLogger.severe(ExperienceConfiguration.class, "failed to create default configuration: " + e.getClass().getSimpleName());
			}
			
		}
		
		// Read config:
		ExperienceConfiguration config;
		try {
			
			config = WriterReader.readConfig(Directory.EXPERIENCE_CONFIG, ExperienceConfiguration.class);
			
		} catch (IOException e) {
			
			SagaLogger.severe(ExperienceConfiguration.class, "failed to read configuration: " + e.getClass().getSimpleName());
			config = new ExperienceConfiguration();
			
		} catch (JsonParseException e) {

			SagaLogger.severe(ExperienceConfiguration.class, "failed to parse configuration: " + e.getClass().getSimpleName());
			SagaLogger.info("message: " + e.getMessage());
			config = new ExperienceConfiguration();
			
		}
		
		// Set instance:
		instance = config;
		
		config.complete();
		
		return config;
		
		
	}
	
	/**
	 * Unloads configuration.
	 * 
	 */
	public static void unload(){
		instance = null;
	}
	
	
}
