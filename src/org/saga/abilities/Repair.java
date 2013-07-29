package org.saga.abilities;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.saga.messages.AbilityMessages;
import org.saga.player.SagaLiving;

public class Repair extends Ability{

	/**
	 * Consume chance key.
	 */
	private static String CONSUME_CHANCE_KEY = "consume chance";
	
	/**
	 * Enchantment level cost key.
	 */
	private static String ENCHANTMENT_COST_KEY = "ench level cost";
	
	/**
	 * Enchantment repair multiplier.
	 */
	private static String ENCHANTMENT_REPAIR_KEY = "ench multiplier";
	
	
	
	// Initialisation:
	/**
	 * Initialises using definition.
	 * 
	 * @param definition ability definition
	 */
	public Repair(AbilityDefinition definition) {
		
        super(definition);
	
	}
	
	
	
	// Usage:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#handleInteractPreTrigger(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteractPreTrigger(PlayerInteractEvent event) {
		return handlePreTrigger();
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.abilities.Ability#trigger()
	 */
	@Override
	public boolean triggerInteract(PlayerInteractEvent event) {
		
		
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getItemInHand();
		
		// Anvil:
		if(event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.IRON_BLOCK){
			return false;
		}
		
		// Check item:
		if(item == null || item.getType() == Material.AIR){
			return false;
		}
		
		Integer skillLevel = getScore();
		SagaLiving sagaPlayer = getSagaLiving();
		
		// Requirements:
		Material usedMaterial = getRepairMaterial(item.getType());
		Integer enchScore = getItemEnchantLevel(item);
		
		// Enchant requirements:
		Integer enchLevelsMax = getDefinition().getFunction(ENCHANTMENT_COST_KEY).intValueCeil(enchScore);
		Integer enchLevels = getDefinition().getFunction(ENCHANTMENT_COST_KEY).randomIntValue(enchScore);
		Double enchMult = getDefinition().getFunction(ENCHANTMENT_REPAIR_KEY).value(enchScore);
		
		// Repair amount:
		short repair = (short)(item.getType().getMaxDurability() * getRepairPercent(item.getType()));
		if(enchScore > 0) repair*= enchMult;
		
		// Normalise:
		if(item.getDurability() - repair < 0){
			repair = item.getDurability();
		}
		
		// Already repaired:
		if(item.getDurability() <= 0){
			sagaPlayer.message(AbilityMessages.repairAlreadyRepaired(item.getType()));
			return false;
		}

		// Can't be repaired:
		if(repair == 0 || usedMaterial == Material.AIR){
			sagaPlayer.message(AbilityMessages.repairCantRepair(item.getType()));
			return false;
		}
		
		// TODO: Remove repair ability.
		int a = 2;
		if(a>1) return false;
		

//		// Check item consumption:
//		if(!checkCost(usedMaterial, 1)){
//			sagaPlayer.message(AbilityMessages.insufficientItems(this, usedMaterial, 1));
//			return false;
//		}

		// Check enchant levels:
		if(enchScore > 0){

			if(player.getLevel() < enchLevelsMax){
				sagaPlayer.message(AbilityMessages.repairLevelsRequired(enchLevelsMax));
				return false;
			}
			
		}
		
		// Use enchant levels:
		if(enchScore > 0) player.setLevel(player.getLevel() - enchLevels);

		// Use items:
		Boolean consume = getDefinition().getFunction(CONSUME_CHANCE_KEY).randomBooleanValue(skillLevel);
		if(consume) useItems(usedMaterial, 1);
		
		// Repair:
		item.setDurability((short) (item.getDurability() - repair));

		// Play effect:
		Location location = event.getClickedBlock().getLocation();
		Random random = new Random();
		
		sagaPlayer.playGlobalEffect(Effect.STEP_SOUND, Material.IRON_BLOCK.getId(), location);
		
		for (int i = 5; i <= 12; i++) {
			
			if(random.nextBoolean()) continue;
			
			sagaPlayer.playGlobalEffect(Effect.SMOKE, i, location);
			
		}
		
		return true;
		
		
	}
	
	
	/**
	 * Gets the repair percent.
	 * 
	 * @param type type
	 */
	private static double getRepairPercent(Material type) {

		
		// Pick axe:
		if(type == Material.STONE_PICKAXE || type == Material.IRON_PICKAXE || type == Material.GOLD_PICKAXE || type == Material.DIAMOND_PICKAXE){
			return 1.0/3.0;
		}
		
		// Axe:
		if(type == Material.STONE_AXE || type == Material.IRON_AXE || type == Material.GOLD_AXE || type == Material.DIAMOND_AXE){
			return 1.0/3.0;
		}

		// Hoe:
		if(type == Material.STONE_HOE || type == Material.IRON_HOE || type == Material.GOLD_HOE || type == Material.DIAMOND_HOE){
			return 1.0/2.0;
		}

		// Shovel:
		if(type == Material.STONE_SPADE || type == Material.IRON_SPADE || type == Material.GOLD_SPADE || type == Material.DIAMOND_SPADE){
			return 1.0/1.0;
		}

		// Sword:
		if(type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.GOLD_SWORD || type == Material.DIAMOND_SWORD){
			return 1.0/2.0;
		}
		
		// Helmet:
		if(type == Material.LEATHER_HELMET || type == Material.CHAINMAIL_HELMET || type == Material.IRON_HELMET || type == Material.GOLD_HELMET || type == Material.DIAMOND_HELMET){
			return 1.0/5.0;
		}

		// Chest plate:
		if(type == Material.LEATHER_CHESTPLATE || type == Material.CHAINMAIL_CHESTPLATE || type == Material.IRON_CHESTPLATE || type == Material.GOLD_CHESTPLATE || type == Material.DIAMOND_CHESTPLATE){
			return 1.0/8.0;
		}

		// Leggings:
		if(type == Material.LEATHER_LEGGINGS || type == Material.CHAINMAIL_LEGGINGS || type == Material.IRON_LEGGINGS || type == Material.GOLD_LEGGINGS || type == Material.DIAMOND_LEGGINGS){
			return 1.0/7.0;
		}

		// Boots:
		if(type == Material.LEATHER_BOOTS || type == Material.CHAINMAIL_BOOTS || type == Material.IRON_BOOTS || type == Material.GOLD_BOOTS || type == Material.DIAMOND_BOOTS){
			return 1.0/4.0;
		}

		// Bow:
		if(type == Material.BOW){
			return 1.0/3.0;
		}
		
		return 0.0;
		
	}

	/**
	 * Gets the repair material.
	 * 
	 * @param type type
	 */
	private static Material getRepairMaterial(Material type) {


		// Diamond tools:
		if(type == Material.DIAMOND_PICKAXE || type == Material.DIAMOND_SPADE || type == Material.DIAMOND_AXE || type == Material.DIAMOND_HOE || type == Material.DIAMOND_SWORD){
			return Material.DIAMOND;
		}

		// Gold tools:
		if(type == Material.GOLD_PICKAXE || type == Material.GOLD_SPADE || type == Material.GOLD_AXE || type == Material.GOLD_HOE || type == Material.GOLD_SWORD){
			return Material.GOLD_INGOT;
		}
		
		// Iron tools:
		if(type == Material.IRON_PICKAXE || type == Material.IRON_SPADE || type == Material.IRON_AXE || type == Material.IRON_HOE || type == Material.IRON_SWORD){
			return Material.IRON_INGOT;
		}
		
		// Stone tools:
		if(type == Material.STONE_PICKAXE || type == Material.STONE_SPADE || type == Material.STONE_AXE || type == Material.STONE_HOE || type == Material.STONE_SWORD){
			return Material.COBBLESTONE;
		}
		
		// Diamond armour:
		if(type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE || type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS){
			return Material.DIAMOND;
		}
		
		// Iron armour:
		if(type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE || type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS){
			return Material.IRON_INGOT;
		}

		// Chain mail:
		if(type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_CHESTPLATE || type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_BOOTS){
			return Material.IRON_INGOT;
		}

		// Gold armour:
		if(type == Material.GOLD_HELMET || type == Material.GOLD_CHESTPLATE || type == Material.GOLD_LEGGINGS || type == Material.GOLD_BOOTS){
			return Material.GOLD_INGOT;
		}

		// Leather armour:
		if(type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS){
			return Material.LEATHER;
		}

		// Bow:
		if(type == Material.BOW){
			return Material.STRING;
		}
		
		return Material.AIR;
		
		
	}
	
	/**
	 * Gets item enchant level.
	 * 
	 * @param item item 
	 * @return enchant level
	 */
	private Integer getItemEnchantLevel(ItemStack item) {

		Integer itemEnch = 0;
		Collection<Integer> enchantments = item.getEnchantments().values();
		for (Integer enchLevel : enchantments) {
			itemEnch += enchLevel;
		}
		
		return itemEnch;

	}
	
}
