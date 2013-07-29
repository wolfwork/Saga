package org.saga.messages;

import org.saga.factions.Faction;
import org.saga.factions.SiegeManager;
import org.saga.messages.colours.Colour;
import org.saga.settlements.Bundle;
import org.saga.utility.Duration;

public class WarMessages {

	// Sieges:
	public static String siegeDeclared(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Siege declared on " + bundle.getName() + ".";
	}
	
	public static String siegeWasDeclared(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Siege was declared on " + bundle.getName() + ".";
	}
	
	public static String siegeAlreadyDeclared(Faction faction, Bundle bundle) {
		return Colour.negative + "Siege already declared on " + bundle.getName() + ".";
	}
	
	
	public static String siegeAttackSuccess(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Successfully sieged " + bundle.getName() + " settlement.";
	}
	
	public static String siegeAttackFailure(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Failed to siege " + bundle.getName() + " settlement.";
	}
	
	public static String siegeDefendSuccess(Faction defender, Bundle bundle) {
		return defender.getColour2() + "Successfully defended " + bundle.getName() + " settlement.";
	}
	
	public static String siegeDefendFailure(Faction defender, Bundle bundle) {
		return defender.getColour2() + "Failed to defend " + bundle.getName() + " settlement.";
	}
	
	
	public static String siegeDefeated(Faction attacker, Faction defender) {
		return attacker.getColour2() + "Defeated " + FactionMessages.faction(defender) + ".";
	}
	
	public static String siegeGotDefeated(Faction attacker, Faction defender) {
		return defender.getColour2() + "The faction was defeated by " + FactionMessages.faction(attacker) + ".";
	}
	
	
	public static String alreadyOwned(Faction faction, Bundle bundle) {
		return Colour.negative + "The faction already owns " + bundle.getName() + ".";
	}
	
	public static String notOwned(Faction faction, Bundle bundle) {
		return Colour.negative + "The faction doesn't own " + bundle.getName() + " settlement.";
	}
	
	public static String siegeSpawnDeny(Faction faction){
		return Colour.negative + "Can't spawn in settlements that are under siege.";
	}
	
	
	
	// Siege reminders:
	public static String siegeAttackReminder(Faction faction, Bundle bundle) {
		
		Integer minutes = SiegeManager.manager().getSiegeRemainingMinutes(bundle.getId());
		
		if(minutes > 0){
			
			Duration durationHM = new Duration(minutes*60000);
			return faction.getColour2() + "Siege on " + bundle.getName() + " settlement starts in " + GeneralMessages.durationDHM(durationHM) + ".";
		
		}else{
			
			return faction.getColour2() + "Attack " + bundle.getName() + " settlement!";
			
		}
		
	}
	
	public static String siegeDefendReminder(Faction faction, Bundle bundle) {
		
		Integer minutes = SiegeManager.manager().getSiegeRemainingMinutes(bundle.getId());
		
		if(minutes > 0){
			
			Duration durationHM = new Duration(minutes*60000);
			return faction.getColour2() + "Defence of " + bundle.getName() + " settlement starts in " + GeneralMessages.durationDHM(durationHM) + ".";
		
		}else{
			
			return faction.getColour2() + "Defend " + bundle.getName() + " settlement!";
			
		}
		
	}
	
	
	
	// Wars:
	public static String warAlreadyDeclared(Faction faction, Faction target) {
		return Colour.negative + "War already declared on " + FactionMessages.faction(target) + " faction.";
	}
	
	public static String warDeclaredOn(Faction faction, Faction target) {
		return faction.getColour2() + "War declared on " + FactionMessages.faction(target) + " faction.";
	}
	
	public static String warDeclaredBy(Faction faction, Faction target) {
		return faction.getColour2() + "Faction " + FactionMessages.faction(target) + " declared war.";
	}
	
	public static String isAtWarDeny(Faction faction, Faction target) {
		return Colour.negative + "The faction is at war with " + FactionMessages.faction(target) + ".";
	}
	
	public static String warCantBeDeclaredOnSelf(Faction faction) {
		return Colour.negative + "Can't declare war on self.";
	}

	public static String warDeclareWait(Faction faction, Faction target, Duration duration) {
		return Colour.negative + "Need to wait " + GeneralMessages.durationDHM(duration) + " before a war can be declared on " + FactionMessages.faction(target) + " faction.";
	}
	
	
	
	// Peace:
	public static String peaceDeclaredOn(Faction faction, Faction target) {
		return faction.getColour2() + "Peace declared with " + FactionMessages.faction(target) + " faction.";
	}
	
	public static String peaceDeclaredBy(Faction faction, Faction target) {
		return faction.getColour2() + "Faction " + FactionMessages.faction(target) + " declared peace.";
	}
	
	public static String peaceDeny(Faction faction, Faction target) {
		return Colour.negative + "The faction is not at war with " + FactionMessages.faction(target) + ".";
	}

	public static String peaceCantBeDeclaredOnSelf(Faction faction) {
		return Colour.negative + "Can't declare peace on self.";
	}
	
	
	
	// Limited membership:
	public static String limitedMembersCantQuit(Faction faction) {
		return Colour.negative + "Limited members can't quit.";
	}
	
	public static String limitedMemberCantQuitInfo() {
		return Colour.normal1 + "To leave the faction either quit the settlement or join another faction.";
	}
	
	public static String limitedMembersCantBeKicked(Faction faction) {
		return Colour.negative + "Can't kick limited members.";
	}
	
	public static String limitedMembersCantHaveRanks(Faction faction) {
		return Colour.negative + "Can't assign ranks to limited members.";
	}
	
	
	
	// Settlement affiliation:
	public static String affiliationSet(Bundle bundle, Faction faction) {

		return Colour.normal2 + "The settlement is now affiliated with " + FactionMessages.faction(faction) + " faction.";
		
	}
	
	public static String affiliationRemoved(Bundle bundle) {

		return Colour.normal2 + "The settlement is no longer affiliated with a faction.";
		
	}
	
	
	public static String affiliationAlreadySet(Bundle bundle, Faction faction) {

		return Colour.negative + "The settlement is already affiliated with " + FactionMessages.faction(faction) + " faction.";
		
	}
	
	public static String affiliationNotSet(Bundle bundle) {

		return Colour.negative + "The settlement has no affiliation.";
		
	}
	
	
	public static String affiliationJoined(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Settlement " + bundle.getName() + " willingly joined the faction.";
	}
	
	
	
	// Capital settlement:
	public static String capitalSet(Faction faction, Bundle bundle) {
		return faction.getColour2() + "Settlement " + bundle.getName() + " is now the capital.";
	}
	
	public static String capitalRemoved(Faction faction) {
		return faction.getColour2() + "The faction no longer has a capital settlement.";
	}
	
	public static String capitalAlreadySet(Faction faction, Bundle bundle) {
		return Colour.negative + "Settlement " + bundle.getName() + " is already the capital.";
	}
	
	public static String capitalNotSet(Faction faction) {
		return Colour.negative + "Faction capital settlement not set.";
	}
	
	public static String capitalNoTownSquare(Faction faction) {
		return Colour.negative + "The capital settlement has no " + HelpMessages.townSquare() + ".";
	}
	
	
}
