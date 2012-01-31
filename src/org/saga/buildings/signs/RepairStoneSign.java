package org.saga.buildings.signs;

import org.bukkit.Effect;
import org.bukkit.block.Sign;
import org.saga.buildings.Building;
import org.saga.buildings.BuildingMessages;
import org.saga.config.EconomyConfiguration;
import org.saga.economy.EconomyMessages;
import org.saga.player.GuardianRune;
import org.saga.player.PlayerMessages;
import org.saga.player.SagaPlayer;
import org.saga.statistics.StatisticsManager;
import org.saga.utility.Cooldown;


public class RepairStoneSign extends BuildingSign{

	
	/**
	 * Name for the sign.
	 */
	public static String SIGN_NAME = "=[RECHARGE]=";
	
	/**
	 * Parameter for the sign.
	 */
	public static String RUNE_TYPE = "guardian rune";
	
	
	// Initialization:
	/**
	 * Creates a stone sign.
	 * 
	 * @param type transaction type
	 * @param sign sign
	 * @param secondLine second line
	 * @param thirdLine third line
	 * @param fourthLine fourth line
	 * @param event event that created the sign
	 * @param building building
	 */
	protected RepairStoneSign(Sign sign, String secondLine, String thirdLine, String fourthLine, Building building){
	
		super(sign, SIGN_NAME, secondLine, thirdLine, fourthLine, building);
		
	}
	
	/**
	 * Creates the training sign.
	 * 
	 * @param sign bukkit sign
	 * @param firstLine first line
	 * @param secondLine second line
	 * @param thirdLine third line
	 * @param fourthLine fourth line
	 * @param building building
	 * @return training sign
	 */
	public static RepairStoneSign create(Sign sign, String secondLine, String thirdLine, String fourthLine, Building building) {
		return new RepairStoneSign(sign, secondLine, thirdLine, fourthLine, building);
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#getName()
	 */
	@Override
	public String getName() {
		return SIGN_NAME;
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#enable()
	 */
	@Override
	public void enable() {

		
		super.enable();
		
		Sign sign = getSign();

		// Fix name:
		if(getFirstParameter().equals("guardian stone")) setFirstParameter("guardian rune");
		
		// Check parameters:
		if(getFirstParameter().equalsIgnoreCase(RUNE_TYPE)){
			
			sign.setLine(1, RUNE_TYPE);
			
			Double price = EconomyConfiguration.config().guardianRuneRechargeCost;
			
			if(price > 0.0){
				sign.setLine(2, "for " + EconomyMessages.coins(price));
			}else{
				sign.setLine(2, "");
			}
			
		}else{
			
			invalidate();
			
		}
		
		// Update:
		sign.update();
		
		
	}
	
	
	// Events:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#onRightClick(org.saga.player.SagaPlayer)
	 */
	@Override
	protected void onRightClick(SagaPlayer sagaPlayer) {

		
		GuardianRune stone = sagaPlayer.getGuardianRune();
		
		// Already charged:
		if(stone.isCharged()){
			sagaPlayer.message(PlayerMessages.alreadyRecharged(stone));
			return;
		}

		// Enough coins:
		Double price = EconomyConfiguration.config().guardianRuneRechargeCost;
		if(sagaPlayer.getCoins() < price){
			sagaPlayer.message(EconomyMessages.notEnoughCoins());
			return;
		}
		
		// Cooldown:
		Building building = getBuilding();
		Cooldown cBuilding = null;
		if(building != null && building instanceof Cooldown){
			
			cBuilding = (Cooldown) getBuilding();
			
		}
		if(cBuilding != null && cBuilding.isOnCooldown()){
			sagaPlayer.message(BuildingMessages.cooldown(building.getName(), cBuilding.getCooldown()));
			return;
		}
		
		// Take coins:
		if(price > 0){
			sagaPlayer.removeCoins(price);
		}
		
		// Recharges rune:
		stone.recharge();
		
		// Inform:
		sagaPlayer.message(PlayerMessages.recharged(stone, price));

		// Statistics:
		StatisticsManager.manager().onGuardanRuneRecharge();
		
		// Play effect:
		sagaPlayer.playGlobalEffect(Effect.BLAZE_SHOOT, 0);
		sagaPlayer.playGlobalEffect(Effect.MOBSPAWNER_FLAMES, 6);
		
		
	}
	
	
}