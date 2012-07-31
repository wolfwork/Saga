package org.saga.messages;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.saga.abilities.AbilityDefinition;
import org.saga.attributes.Attribute;
import org.saga.buildings.BuildingDefinition;
import org.saga.buildings.signs.AttributeSign;
import org.saga.buildings.signs.GuardianRuneSign;
import org.saga.config.AbilityConfiguration;
import org.saga.config.AttributeConfiguration;
import org.saga.config.EconomyConfiguration;
import org.saga.config.FactionConfiguration;
import org.saga.config.ProficiencyConfiguration;
import org.saga.config.SettlementConfiguration;
import org.saga.messages.PlayerMessages.ColourLoop;
import org.saga.player.Proficiency.ProficiencyType;
import org.saga.player.ProficiencyDefinition;
import org.saga.utility.text.RomanNumeral;
import org.saga.utility.text.StringBook;
import org.saga.utility.text.StringTable;

public class InfoMessages {

public static ChatColor veryPositive = ChatColor.DARK_GREEN; // DO NOT OVERUSE.
	



	public static ChatColor positive = ChatColor.GREEN;
	
	public static ChatColor negative = ChatColor.RED;
	
	public static ChatColor veryNegative = ChatColor.DARK_RED; // DO NOT OVERUSE.
	
	public static ChatColor unavailable = ChatColor.DARK_GRAY;
	
	public static ChatColor anouncment = ChatColor.AQUA;
	
	public static ChatColor normal1 = ChatColor.GOLD;
	
	public static ChatColor normal2 = ChatColor.YELLOW;
	
	public static ChatColor frame = ChatColor.DARK_GREEN;
	
	
	// Help:
	public static String ehelp(int page) {

//		
//		ColorCircle color = new ColorCircle().addColor(normal1).addColor(normal2);
//		StringBook book = new StringBook("building help", color, 9);
//		
//		// General:
//		book.addLine("Items can be bought and sold at a trading post building. See /bhelp for details.");
//
//		// Balance:
//		book.addLine("/stats to see how much coins you have in your wallet.");
//
//		// Limited items:
//		book.addLine("A trading post doesn't have unlimited coins/items. Everything is gained from players buying/selling and exporting/importing.");
//		
//		// Building info:
//		book.addLine("/tpost to see all buyable items, sellable items, exports, imports, available coins and available items.");
//		
//		// Set sell:
//		book.addLine("/bsetsell <item> <amount> <value> to set the minimum amount and value of a sold item");
//
//		// Set buy:
//		book.addLine("/bsetbuy <item> <amount> <value> to set the minimum amount and value of a bought item");
//		
//		// Signs:
//		book.addLine("Place \"=[sell]= | amount" + SellSign.MATERIAL_VALUE_DIV + "item\" and \"=[buy]= | amount" + SellSign.MATERIAL_VALUE_DIV + "item\" signs to sell and buy items.");
//
//		// Donate:
//		book.addLine("To get the trading post running, you will need to donate items or coins.");
//		
//		// Donate:
//		book.addLine("/donate, /donatec <amount> or /donateall <item> to donate item in hand, coins or all items of the given type.");
//		
//		// Export import:
//		book.addLine("A deal needs to be formed to export or import items.");
//
//		// Imports:
//		book.addLine("/eimports and /eexports to list all deals.");
//
//		// Expiration:
//		book.addLine("/bnewdeal <ID> to form a deal. Deal will expire after certain amout of items or time.");
//
//		// Timing:
//		book.addLine("After a deal is formed, the goods will get exported/imported each sunrise.");
//
//		// Goods sign:
//		book.addLine("" + TradingPost.GOODS_SIGN + " sign displays goods list.");
//
//		// Deals sign:
//		book.addLine("" + TradingPost.DEALS_SIGN + " sign displays deals list.");
//
//		return book.framed(page);
//		
		
		return "";
		
	}
	
	public static String phelp(int page) {
		
		
		ColourLoop messageColor = new ColourLoop().addColor(normal1).addColor(normal2);
		StringBook book = new StringBook("player help", messageColor);

		int maxAttr = AttributeConfiguration.config().findMaxAttrPoints();
		int minAttr = AttributeConfiguration.config().findMinAttrPoints();
		String attrGain = minAttr + "-" + maxAttr;
		if(minAttr == maxAttr) attrGain = minAttr + "";
		
		// Attributes:
		book.addLine( 
			"Player levels are gained from killing creatures, getting crafting materials and pvp. " +
			"Each level gives " + attrGain + " attribute points that can be used to increase attribute scores. " +
			"Higher attribute scores make you stronger and unlock new abilities. " +
			"Attributes can be increased by interacting with " + AttributeSign.SIGN_NAME + " signs. " +
			"Use " + GeneralMessages.command("/stats") + " to see your attributes."
		);
		
		book.addLine("");
		
		// Attribute table:
		StringTable attrTable = new StringTable(messageColor);
		attrTable.addLine(GeneralMessages.columnTitle("attribute"), GeneralMessages.columnTitle("description"), 0);
		ArrayList<Attribute> attributes = AttributeConfiguration.config().getAttributes();
		if(attributes.size() > 0){
			
			for (Attribute attribute : attributes) {
				attrTable.addLine(attribute.getName(), attribute.getDescription(), 0);
			}
			
		}else{
			attrTable.addLine("-", "-", 0);
		}
		attrTable.collapse();
		book.addTable(attrTable);
		
		book.nextPage();
		
		// Abilities:
		book.addLine("There are active and passive abilities. " +
			"Active abilities can be activated by clicking with a certain item. " +
			"Passive abilities are always active. "
		);
		
		book.addLine("");
		
		// Ability table:
		StringTable abilityTable = new StringTable(messageColor);
		abilityTable.addLine(new String[]{GeneralMessages.columnTitle("ability"), GeneralMessages.columnTitle("description"), GeneralMessages.columnTitle("usage")});
		ArrayList<AbilityDefinition> abilities = AbilityConfiguration.config().getDefinitions();
		if(abilities.size() > 0){
			
			for (AbilityDefinition ability : abilities) {
				abilityTable.addLine(new String[]{ability.getName(), ability.getDescription(), ability.getUsage()});
			}
			
		}else{
			abilityTable.addLine(new String[]{"-", "-", "-"});
		}
		abilityTable.collapse();
		book.addTable(abilityTable);
		
		book.nextPage();
		
		// Ability upgrades:
		book.addLine(
			"Abilities can be upgraded by increasing attributes. " +
			"Upgraded abilities are more efficient and have lower cooldown times. " +
			"Some abilities require certain buildings."
		);
		
		book.addLine("");
		
		// Ability upgrade table:
		StringTable upgrTable = new StringTable(messageColor);
		Integer score1 = 1;
		Integer score3 = AbilityConfiguration.config().maxAbilityScore;
		Integer score2 = new Double((score1.doubleValue() + score3.doubleValue())/2.0).intValue();
		
		upgrTable.addLine(new String[]{GeneralMessages.columnTitle("ability"),
				GeneralMessages.columnTitle("required " + RomanNumeral.binaryToRoman(score1)),
				GeneralMessages.columnTitle("required " + RomanNumeral.binaryToRoman(score2)),
				GeneralMessages.columnTitle("required " + RomanNumeral.binaryToRoman(score3))
		});
		
		if(abilities.size() > 0){
			
			for (AbilityDefinition ability : abilities) {
				upgrTable.addLine(new String[]{ability.getName(),
					StatsMessages.requirements(ability, score1),
					StatsMessages.requirements(ability, score2),
					StatsMessages.requirements(ability, score3)
				});
			}
			
		}else{
			upgrTable.addLine(new String[]{"-", "-", "-", "-"});
		}
		upgrTable.collapse();
		book.addTable(upgrTable);
		
		book.nextPage();
		
		// Guardian runes:
		String rechargeCost = "";
		if(EconomyConfiguration.config().guardianRuneRechargeCost > 0) rechargeCost = "Recharge costs " + EconomyMessages.coins(EconomyConfiguration.config().guardianRuneRechargeCost) + ". ";
		book.addLine("The guardian rune will restore all carried items after death. " +
			"The rune needs to be recharged after every use. " +
			"Recharging is done by interacting with a " + GuardianRuneSign.SIGN_NAME + " sign. " + 
			rechargeCost + 
			"Enable or disable the rune with " + GeneralMessages.command("/grenable") + " and " + GeneralMessages.command("/grdisable") + ". "
		);
		
		return book.framedPage(page);
		
		
	}
	
	public static String shelp(int page) {
		
		
		ColourLoop messageColor = new ColourLoop().addColor(normal1).addColor(normal2);
		StringBook book = new StringBook("settlement help", messageColor);

		// Land:
		book.addLine("A settlement will protect your land. " +
			"Use " + GeneralMessages.command("/ssettle") + " and " + GeneralMessages.command("/sclaim") + " to create the settlement and claim more land. " +
			"Land is claimed in 16x16 chunks. " +
			"More claim points become available when the settlements level up. " +
			"Use " + GeneralMessages.command("/sunclaim") + " to abandon land. " +
			"Use " + GeneralMessages.command("/map") + " to see what chunks have already been claimed." 
		);
		
		// Levels:
		book.addLine("Levels are gained over time. " +
			"The speed at which they are gained is determined by the number of members. " +
			"A certain amount of members is required for the settlement to gain levels. " +
			"Use " + GeneralMessages.command("/sstats") + " to see settlement level, requirements and other stats."
		);
		
		// Invite:
		book.addLine("Use " + GeneralMessages.command("/sinvite") + " to invite another player to the settlement. " +
			"Settlement invitations can be accepted with " + GeneralMessages.command("/saccept") + " and declined with " + GeneralMessages.command("/sdecline") + ". " +
			"A player can only be in a single settlement. " +
			"Use " + GeneralMessages.command("/settlementquit") + " to leave a settlement. " +
			"Troublemakers can be kicked by using " + GeneralMessages.command("/skick") + ". "
		);
		
		book.nextPage();
		
		// Roles:
		book.addLine("Use " + GeneralMessages.command("/ssetrole") + " to assign a role to a member. " +
			"Each role gives certain attribute bonuses. " +
			"The amount of available roles increases when the settlement gains levels."
		);
		
		book.addLine("");

		// Role table:
		StringTable rolesTable = new StringTable(messageColor);
		ArrayList<ProficiencyDefinition> roles = ProficiencyConfiguration.config().getDefinitions(ProficiencyType.ROLE);
			
		// Titles:
		rolesTable.addLine(new String[]{GeneralMessages.columnTitle("role"), GeneralMessages.columnTitle("bonus")});

		// Values:
		if(roles.size() != 0){
			
			for (ProficiencyDefinition definition : roles) {
				
				String roleName = definition.getName();
				String bonuses = bonuses(definition);
				if(bonuses.length() == 0) bonuses = "none";
				
				rolesTable.addLine(new String[]{roleName, bonuses});
				
			}

		}else{
			
			rolesTable.addLine(new String[]{"-", "-"});

		}
		
		rolesTable.collapse();
		book.addTable(rolesTable);
		
		book.nextPage();
		
		// Buildings:
		book.addLine("Use " + GeneralMessages.command("/bset") + " to set a building on the chunk and " + GeneralMessages.command("/bremove") + " to remove it. " +
				"Each building requires a certain amount of build points. " +
				"Build points and new buildings become available when the settlement gains levels. "
		);
		
		book.addLine("");

		// Buildings table:
		StringTable bldgsTable = new StringTable(messageColor);
		ArrayList<BuildingDefinition> bldgsDefinitions = SettlementConfiguration.config().getBuildingDefinitions();
			
		// Titles:
		bldgsTable.addLine(new String[]{GeneralMessages.columnTitle("building"), GeneralMessages.columnTitle("points"), GeneralMessages.columnTitle("description")});

		if(bldgsDefinitions.size() != 0){
			
			for (BuildingDefinition bldgDefinition : bldgsDefinitions) {
				
				String name = bldgDefinition.getName();
				String points = bldgDefinition.getBuildPoints().toString();
				String description = bldgDefinition.getDescription();
				
				bldgsTable.addLine(new String[]{name, points, description});
				
			}
			
		}else{
		
			bldgsTable.addLine(new String[]{"-", "-", "-"});

		}
		
		bldgsTable.collapse();
		book.addTable(bldgsTable);
		
		book.nextPage();
		
		book.addLine("To prevent griefing from settlement members, restrict building by setting homes with " + GeneralMessages.command("/bset home") + ". " +
			"Only the owner and residents can build in homes. " +
			"Residents can be added and removed with " + GeneralMessages.command("/baddresident") + " and " + GeneralMessages.command("/bremoveresident") + ". "
		);
		
		// Other:
		book.addLine("Use " + GeneralMessages.command("/srename") + " to rename the settlement.");
		book.addLine("Use " + GeneralMessages.command("/sresign") + " to declare someone else as the settlement owner.");
		
		return book.framedPage(page);
		
		
	}
	
	public static String fhelp(int page) {
		
		
		ColourLoop messageColor = new ColourLoop().addColor(normal1).addColor(normal2);
		StringBook book = new StringBook("Faction help", messageColor);
		
		// Pvp:
		if(FactionConfiguration.config().factionOnlyPvp){
			book.addLine(veryNegative + "Only factions can take part in pvp.");
		}
		
		// Create:
		book.addLine("/fcreate <name> creates a new faction.");
		
		// Formation:
		if(FactionConfiguration.config().formationAmount > 1){
			book.addLine("A faction will not be formed until it has " + FactionConfiguration.config().formationAmount + " members.");
		}
		
		// Invite:
		book.addLine("/finvite <name> to invite someone to the faction.");
		
		// Accept:
		book.addLine("/faccept to accept a faction invitation.");
		
		// Decline
		book.addLine("/fdeclineall to decline all faction invitations.");

		// Kick:
		book.addLine("/fkick <name> to kick someone out from the faction.");
		
		// Quit:
		book.addLine("/factionquit to quit a faction.");

		// List:
		book.addLine("/flist to list all faction members.");

		// Primary color:
		book.addLine("/fsetprimarycolor <color> to set the factions primary color.");

		// Secondary color:
		book.addLine("/fsetsecondarycolor <color> to set the factions secondary color.");

		// Chat:
		book.addLine("/f <message> to send a message in the faction chat.");

		// Stats:
		book.addLine("/fstats to see available ranks and other stats.");

		// Set rank:
		book.addLine("/fsetrank <name> <rank> to assign a rank to somebody.");

		// Rename:
		book.addLine("/frename <name> to rename the faction. Costs " + EconomyMessages.coins(EconomyConfiguration.config().factionRenameCost) + ".");
		
		// Request alliance:
		book.addLine("/frequestally <faction_name> to request an alliance.");

		// Accept alliance:
		book.addLine("/facceptally <faction_name> to accept an alliance.");

		// Decline alliance:
		book.addLine("/fdeclinetally <faction_name> to deline an alliance.");
		
		// Decline alliance:
		book.addLine("/fremoveally <faction_name> to break an alliance.");
		
		return book.framedPage(page);
		
		
	}


	
	public static String bonuses(ProficiencyDefinition definition) {

		
		StringBuffer result = new StringBuffer();
		
		// Attributes:
		ArrayList<String> attributeNames = AttributeConfiguration.config().getAttributeNames();
		
		for (String attribute : attributeNames) {
			
			Integer bonus = definition.getAttributeBonus(attribute);
			if(bonus <= 0) continue;
			
			if(result.length() > 0) result.append(", ");
			
			result.append("+" + bonus + " " + GeneralMessages.attrAbrev(attribute));
			
		}

		return result.toString();
		
	
	}

	
}
