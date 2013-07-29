package org.saga.messages;

import org.bukkit.ChatColor;
import org.saga.factions.Faction;
import org.saga.messages.colours.Colour;
import org.saga.settlements.Bundle;
import org.saga.utility.chat.ChatUtil;

public class ClaimMessages {

	
	// Faction notifications:
	public static String loosing(Bundle bundle, Faction defenderFaction, Faction attackerFaction, Double progress){
		return defenderFaction.getColour2() + "Loosing " + bundle.getName() + " to " + FactionMessages.faction(attackerFaction) + "." + " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
	}

	public static String claiming(Bundle bundle, Faction attackerFaction, Faction defenderFaction, Double progress){
		return attackerFaction.getColour2() + "Seizing " + bundle.getName() + " from " + FactionMessages.faction(defenderFaction)+ "." + " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
	}
	
	public static String claiming(Bundle bundle, Faction attackerFaction, Double progress){
		return attackerFaction.getColour2() + "Claiming " + bundle.getName() + "." + " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
	}

	
	
	// Town square notifications:
	public static String claimingTownSquare(Bundle bundle, Faction faction, Double progress){
		
		String claimed = "";
		if(progress > 0){
			claimed = " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
		}
		
		return Colour.normal1 + "[" + "->" + FactionMessages.faction(faction) + "]" + claimed;
	
	}

	public static String claimingTownSquare(Bundle bundle, Faction attackerFaction, Faction defenderFaction, Double progress){

		String claimed = "";
		if(progress > 0){
			claimed = " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
		}
		
		return Colour.normal1 + "[" + FactionMessages.faction(defenderFaction) + "->" + FactionMessages.faction(attackerFaction) + "]" + claimed;

	}
	
	public static String unclaimingTownSquare(Bundle bundle, Faction faction, Double progress){
		
		String claimed = "";
		if(progress > 0){
			claimed = " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
		}
		
		return Colour.normal1 + "[" + "<-" + FactionMessages.faction(faction) + "]" + claimed;
	
	}

	public static String unclaimingTownSquare(Bundle bundle, Faction attackerFaction, Faction defenderFaction, Double progress){

		String claimed = "";
		if(progress > 0){
			claimed = " " + ChatUtil.round(progress * 100, 1) + "% claimed.";
		}
		
		return Colour.normal1 + "[" + FactionMessages.faction(defenderFaction) + "<-" + FactionMessages.faction(attackerFaction) + "]" + claimed;

	}
	
	
	
	// Broadcast:
	public static String claimedBcast(Bundle bundle, Faction faction){
		return Colour.announce + "" + "Settlement " + bundle.getName() + " was claimed by " + FactionMessages.faction(ChatColor.UNDERLINE, faction)+".";
	}
	
	
}
