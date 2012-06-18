package org.saga.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.saga.SagaLogger;
import org.saga.attributes.Attribute;
import org.saga.saveload.Directory;
import org.saga.saveload.WriterReader;
import org.saga.utility.TwoPointFunction;

import com.google.gson.JsonParseException;

public class AttributeConfiguration {

	
	/**
	 * Instance of the configuration.
	 */
	transient private static AttributeConfiguration instance;
	
	/**
	 * Gets the instance.
	 * 
	 * @return instance
	 */
	public static AttributeConfiguration config() {
		return instance;
	}
	
	/**
	 * Maximum attribute score.
	 */
	public Integer maxAttributeScore;
	
	/**
	 * Available attribute points.
	 */
	private TwoPointFunction attributePoints;

	/**
	 * Attributes.
	 */
	private ArrayList<Attribute> attributes; 
	
	
	
	
	// Initialisation:
	/**
	 * Completes construction.
	 * 
	 */
	public void complete() {
		
		
		if(maxAttributeScore == null){
			SagaLogger.nullField(getClass(), "maxAttributeScore");
			maxAttributeScore= 1;
		}
		
		if(attributePoints == null){
			SagaLogger.nullField(getClass(), "attributePoints");
			attributePoints= new TwoPointFunction(0.0);
		}
		
		if(attributes == null){
			SagaLogger.nullField(getClass(), "attributes");
			attributes = new ArrayList<Attribute>();
		}
		if(attributes.remove(null)) SagaLogger.nullField(getClass(), "attributes element");
		
		for (Attribute attribute : attributes) {
			attribute.complete();
		}
		
		
	}
	
	
	
	
	// Getters:
	/**
	 * Gets the attributes.
	 * 
	 * @return attributes
	 */
	public ArrayList<Attribute> getAttributes() {
		return new ArrayList<Attribute>(attributes);
	}
	
	/**
	 * Gets the attribute names.
	 * 
	 * @return attribute names
	 */
	public ArrayList<String> getAttributeNames() {

		
		ArrayList<Attribute> attributes = getAttributes();
		ArrayList<String> attributeNames = new ArrayList<String>();
		
		Iterator<Attribute> it = attributes.iterator();
		while (it.hasNext()) {
			attributeNames.add(it.next().getName());
		}
		
		return attributeNames;
		
		
	}
	
	/**
	 * Gets the amount of attribute points available
	 * 
	 * @param level player level
	 * @return attribute points
	 */
	public Integer getAttributePoints(Integer level) {
		return attributePoints.intValue(level);
	}
	
	/**
	 * Find the maximum number of attribute points given.
	 * 
	 * @return maximum attribute points
	 */
	public int findMaxAttrPoints() {

		int max = 0;
		int val = getAttributePoints(0);
		for (int level = 1; level <= ExperienceConfiguration.config().maximumLevel; level++) {
			val = getAttributePoints(level) - getAttributePoints(level - 1);
			if(val > max) max = val;
		}
		return max;

	}
	
	/**
	 * Find the minimum number of attribute points given.
	 * 
	 * @return minimum attribute points
	 */
	public int findMinAttrPoints() {

		int min = AttributeConfiguration.config().maxAttributeScore.intValue();
		int val = getAttributePoints(0);
		for (int level = 1; level <= ExperienceConfiguration.config().maximumLevel; level++) {
			val = getAttributePoints(level) - getAttributePoints(level - 1);
			if(val < min) min = val;
		}
		return min;

	}

	
	
	
	// Load unload:
	/**
	 * Loads configuration.
	 * 
	 * @return configuration
	 */
	public static AttributeConfiguration load(){

		
		AttributeConfiguration config;
		try {
			
			config = WriterReader.read(Directory.ATTRIBUTE_CONFIG, AttributeConfiguration.class);
			
		} catch (FileNotFoundException e) {
			
			SagaLogger.severe(AttributeConfiguration.class, "configuration not found");
			config = new AttributeConfiguration();
			
		} catch (IOException e) {
			
			SagaLogger.severe(AttributeConfiguration.class, "failed to read configuration: " + e.getClass().getSimpleName());
			config = new AttributeConfiguration();
			
		} catch (JsonParseException e) {

			SagaLogger.severe(AttributeConfiguration.class, "failed to parse configuration: " + e.getClass().getSimpleName());
			SagaLogger.info("message: " + e.getMessage());
			config = new AttributeConfiguration();
			
		}
		
		// Set instance:
		instance = config;
		
		config.complete();
		
		return config;
		
		
	}
	
	/**
	 * Unloads the instance.
	 * 
	 */
	public static void unload(){
		instance = null;
	}
	
	
}
