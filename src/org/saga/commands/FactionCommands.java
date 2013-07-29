package org.saga.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.saga.Saga;
import org.saga.config.EconomyConfiguration;
import org.saga.config.FactionConfiguration;
import org.saga.config.ProficiencyConfiguration;
import org.saga.config.ProficiencyConfiguration.InvalidProficiencyException;
import org.saga.dependencies.EconomyDependency;
import org.saga.exceptions.NonExistantSagaPlayerException;
import org.saga.factions.Faction;
import org.saga.factions.Faction.FactionPermission;
import org.saga.factions.FactionManager;
import org.saga.factions.WarManager;
import org.saga.messages.EconomyMessages;
import org.saga.messages.FactionMessages;
import org.saga.messages.GeneralMessages;
import org.saga.messages.HelpMessages;
import org.saga.messages.SettlementMessages;
import org.saga.messages.StatsMessages;
import org.saga.messages.WarMessages;
import org.saga.player.Proficiency;
import org.saga.player.Proficiency.ProficiencyType;
import org.saga.player.SagaPlayer;
import org.sk89q.Command;
import org.sk89q.CommandContext;
import org.sk89q.CommandPermissions;


public class FactionCommands {
	
	
	// Members:
	@Command(
			aliases = {"fform","fcreate"},
			usage = "<faction_name>",
			flags = "",
			desc = "Create a new faction.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.faction.create"})
	public static void create(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	    	
		
		// Arguments:
		String name = GeneralMessages.nameFromArg(args.getString(0));

		// Fix spaces:
		while(name.contains("  ")){
			name = name.replaceAll("  ", " ");
		}

		// Full faction member:
		Faction playerFaction = sagaPlayer.getFaction();
		if(playerFaction != null && !playerFaction.isLimitedMember(sagaPlayer)){
			sagaPlayer.message(FactionMessages.alreadyInFaction());
			return;
		}
		
    	// Validate name:
    	if(!validateName(name)){
    		sagaPlayer.message(FactionMessages.invalidName());
    		return;
    	}
    	
    	// Check name:
    	if(FactionManager.manager().getFaction(name) != null){
    		sagaPlayer.message(FactionMessages.inUse(name));
    		return;
    	}
    
    	// Cost:
	    Double cost = EconomyConfiguration.config().getFactionCreateCost();
	    if(cost > 0 && EconomyConfiguration.config().isEnabled()){

		    // Check coins:
		    if(EconomyDependency.getCoins(sagaPlayer) < cost){
		    	sagaPlayer.message(EconomyMessages.insufficient(cost));
		    	return;
		    }
		    
	    	// Take coins:
		    EconomyDependency.removeCoins(sagaPlayer, cost);
		    
		    // Inform:
		    sagaPlayer.message(EconomyMessages.spent(cost));
		    
	    }
    	
    	// Create faction:
    	Faction faction = Faction.create(name, sagaPlayer);
    	
    	// Broadcast:
    	sagaPlayer.message(FactionMessages.created(faction));
    	
	    	
	}

	@Command(
            aliases = {"finvite"},
            usage = "[faction_name] <player_name>",
            flags = "",
            desc = "Send a faction join invitation.",
            min = 1,
            max = 2
	)
	@CommandPermissions({"saga.user.faction.invite"})
	public static void invite(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		Faction selFaction = null;
		String playerName = null;
		
		// Arguments:
		if(args.argsLength() == 2){
			
			String factionName = GeneralMessages.nameFromArg(args.getString(0));
			selFaction = FactionManager.manager().matchFaction(factionName);
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
				return;
			}
			
			playerName = selFaction.matchName(args.getString(1));
			
			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
			playerName = selFaction.matchName(args.getString(0));
			
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.INVITE)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Force player:
		SagaPlayer selPlayer;
		try {
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
		} catch (NonExistantSagaPlayerException e) {
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
		}
		
		// Full faction member:
		Faction playerFaction = selPlayer.getFaction();
		if(playerFaction != null && !playerFaction.isLimitedMember(selPlayer)){
			sagaPlayer.message(FactionMessages.alreadyInFaction(selPlayer));
			return;
		}
		
		// Invite:
		selPlayer.addFactionInvite(selFaction.getId());
		
		// Inform:
		selFaction.information(FactionMessages.invited(selPlayer, selFaction));
		selPlayer.message(FactionMessages.wasInvited(selPlayer, selFaction));
		selPlayer.message(FactionMessages.informInvited());
		
		// Release:
		selPlayer.indicateRelease();

		
	}

	@Command(
            aliases = {"faccept"},
            usage = "[faction_name]",
            flags = "",
            desc = "Accept a faction join invitation.",
            min = 0,
            max = 1)
	@CommandPermissions({"saga.user.faction.accept"})
	public static void accept(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Faction selFaction = null;

		String strFaction = null;
		
		// Full faction member:
		Faction playerFaction = sagaPlayer.getFaction();
		if(playerFaction != null && !playerFaction.isLimitedMember(sagaPlayer)){
			sagaPlayer.message(FactionMessages.alreadyInFaction());
			return;
		}
		
    	// No invites:
    	ArrayList<Faction> inviteFactions = FactionManager.manager().getFactions(sagaPlayer.getFactionInvites());
    	if(inviteFactions.size() == 0){
    		sagaPlayer.message(FactionMessages.noInvites());
    		return;
    	}
    	
		// Arguments:
    	switch (args.argsLength()) {
			case 1:
				
				strFaction = args.getString(0);
				selFaction = FactionManager.manager().matchFaction(strFaction);
				
				if(selFaction == null){
					sagaPlayer.message(GeneralMessages.invalidFaction(strFaction));
					return;
				}
				
				if(!inviteFactions.contains(selFaction)){
					sagaPlayer.message(FactionMessages.noInvite(strFaction));
					return;
				}
				
				break;

			default:
				
				selFaction = inviteFactions.get(inviteFactions.size()-1);
			
				break;
				
		}
    	
		boolean formed = selFaction.isFormed();
		
    	// Inform:
    	selFaction.information(FactionMessages.joined(sagaPlayer, selFaction));
		sagaPlayer.message(FactionMessages.haveJoined(sagaPlayer, selFaction));
		
    	// Add to faction:
		selFaction.addMember(sagaPlayer);

//		// Set as owner:
//		if(!selFaction.hasOwner()){
//			selFaction.setOwner(sagaPlayer.getName());
//		}
		
		// Inform formation:
		if(!formed && selFaction.isFormed()){
			selFaction.information(FactionMessages.formed(selFaction));
		}
		
    	// Remove all invitations:
		ArrayList<Integer> invitationIds = sagaPlayer.getFactionInvites();
		for (Integer inviteId : invitationIds) {
			sagaPlayer.removeFactionInvite(inviteId);
		}
    	
    	
	}

	@Command(
            aliases = {"fdecline"},
            usage = "[faction_name]",
            flags = "",
            desc = "Decline a faction join invitation.",
            min = 0,
            max = 1
    )
	@CommandPermissions({"saga.user.faction.decline"})
	public static void decline(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String argsFaction = null;
		
		Faction selFaction = null;
		
    	// No invites:
    	ArrayList<Faction> inviteFactions = FactionManager.manager().getFactions(sagaPlayer.getFactionInvites());
    	if(inviteFactions.size() == 0){
    		sagaPlayer.message(FactionMessages.noInvites());
    		return;
    	}
    	
		// Arguments:
    	switch (args.argsLength()) {
			case 1:
				
				argsFaction = args.getString(0);
				selFaction = FactionManager.manager().matchFaction(argsFaction);
				
				if(selFaction == null){
					sagaPlayer.message(GeneralMessages.invalidFaction(argsFaction));
					return;
				}
				
				if(!inviteFactions.contains(selFaction)){
					sagaPlayer.message(FactionMessages.noInvite(argsFaction));
					return;
				}
				
				break;

			default:
				
				selFaction = inviteFactions.get(inviteFactions.size()-1);
			
				break;
				
		}

    	// Remove invite:
    	sagaPlayer.removeFactionInvite(selFaction.getId());
    	
    	// Inform:
    	sagaPlayer.message(FactionMessages.declinedInvite(selFaction));
		
		
	}
	
	@Command(
            aliases = {"fkick"},
            usage = "[faction] <member_name>",
            flags = "",
            desc = "Kick a member from faction.",
            min = 1,
            max = 2
		)
	@CommandPermissions({"saga.user.faction.kick"})
	public static void kick(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Faction selFaction = sagaPlayer.getFaction();
		String playerName = null;

		// Arguments:
		if(args.argsLength() == 2){
			
			String factionName = GeneralMessages.nameFromArg(args.getString(0));
			selFaction = FactionManager.manager().matchFaction(factionName);
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
				return;
			}
			
			playerName = selFaction.matchName(args.getString(1));
			
			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
			playerName = selFaction.matchName(args.getString(0));
			
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.KICK)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Force player:
		SagaPlayer selPlayer;
		try {
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
		} catch (NonExistantSagaPlayerException e) {
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
		}
		
		// Kick owner:
		if(selFaction.isOwner(selPlayer.getName())){
			sagaPlayer.message(FactionMessages.cantKickOwner(selFaction));
			return;
		}

		// Limited member:
		if(selFaction.isLimitedMember(selPlayer)){
			sagaPlayer.message(WarMessages.limitedMembersCantBeKicked(selFaction));
			return;
		}
		
		// Not faction member:
		if(!selFaction.equals(selPlayer.getFaction())){
			sagaPlayer.message(FactionMessages.notMember(selPlayer));
			return;
		}
		
		// Kicked yourself:
		if(selPlayer.equals(sagaPlayer)){
			sagaPlayer.message(FactionMessages.cantKickYourself(sagaPlayer, selFaction));
			return;
		}
		
		boolean formed = selFaction.isFormed();
		
		// Kick:
		selFaction.removeMember(selPlayer);
		
		// Inform unform:
		if(formed && !selFaction.isFormed()){
			selFaction.information(FactionMessages.unformed(selFaction));
		}
		
		// Inform:
		selFaction.information(FactionMessages.playerKicked(selPlayer, selFaction));
		selPlayer.message(FactionMessages.wasKicked(selPlayer, selFaction));

		// Release:
		selPlayer.indicateRelease();
		
		// Disband if no members left:
		if(selFaction.getMemberCount() <= 0){

			// Delete faction:
			selFaction.delete();
			
			// Inform:
			sagaPlayer.message(FactionMessages.disbandedOther(selFaction));
			
		}

		
	}

	@Command(
            aliases = {"factionquit"},
            usage = "",
            flags = "",
            desc = "Quit the faction.",
            min = 0,
            max = 0
		)
	@CommandPermissions({"saga.user.faction.quit"})
	public static void quit(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Part of a faction:
		Faction selFaction = sagaPlayer.getFaction();
		if(selFaction == null){
			
			sagaPlayer.removeFactionId();
			
			sagaPlayer.message(FactionMessages.notMember());
			return;
			
		}
		
		// Limited member:
		if(selFaction.isLimitedMember(sagaPlayer)){
			sagaPlayer.message(WarMessages.limitedMembersCantQuit(selFaction));
			sagaPlayer.message(WarMessages.limitedMemberCantQuitInfo());
			return;
		}
		
//		// Permission:
//		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.QUIT)){
//			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
//			return;
//		}

		// Owner:
		if(selFaction.isOwner(sagaPlayer.getName()) && selFaction.getMemberCount() > 1){
			sagaPlayer.message(FactionMessages.ownerCantQuit());
			sagaPlayer.message(FactionMessages.ownerCantQuitInfo());
			return;
		}
		
		boolean formed = selFaction.isFormed();
		
		// Quit:
		selFaction.removeMember(sagaPlayer);

		// Inform disband:
		if(formed && !selFaction.isFormed()){
			selFaction.information(FactionMessages.unformed(selFaction));
		}
		
		// Inform:
		selFaction.information(FactionMessages.quit(sagaPlayer, selFaction));
		sagaPlayer.message(FactionMessages.haveQuit(sagaPlayer, selFaction));

		// Disband if no players left:
		if(selFaction.getMemberCount() <= 0){

			// Delete faction:
			selFaction.delete();
			
			// Inform:
			sagaPlayer.message(FactionMessages.disbanded(selFaction));
	    	
		}
		
		
	}

	@Command(
		aliases = {"fresign"},
		usage = "[faction_name] <new_owner_name>",
		flags = "",
		desc = "Resign from the owner position.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.user.faction.resign"})
	public static void resign(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Faction selFaction = null;
		SagaPlayer selPlayer = null;
		
		String factionName = null;
		String targetName = null;
		
		// Arguments:
		switch (args.argsLength()) {
			
			case 2:
				
				// Faction:
				factionName = GeneralMessages.nameFromArg(args.getString(0));
				selFaction = FactionManager.manager().matchFaction(factionName);
				if(selFaction == null){
					sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
					return;
				}
				
				// New owner:
				targetName = selFaction.matchName(args.getString(1));
				if(!selFaction.isMember(targetName)){
					sagaPlayer.message(FactionMessages.notMember(selFaction, targetName));
					return;
				}
				
				break;

			default:
				
				selFaction = sagaPlayer.getFaction();
				if(selFaction == null){
					sagaPlayer.message(SettlementMessages.notMember());
					return;
				}

				targetName = selFaction.matchName(args.getString(0));
				if(!selFaction.isMember(targetName)){
					sagaPlayer.message(FactionMessages.notMember(selFaction, targetName));
					return;
				}
				
				break;
				
		}

		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.RESIGN)){
			sagaPlayer.message(GeneralMessages.noPermission());
			return;
		}
		
		try {
			selPlayer = Saga.plugin().forceSagaPlayer(targetName);
		} catch (NonExistantSagaPlayerException e) {
			sagaPlayer.message(GeneralMessages.invalidPlayer(targetName));
			return;
		}
		
		// Already owner:
		if(selFaction.isOwner(targetName)){
			
			if(selPlayer == sagaPlayer){
				sagaPlayer.message(SettlementMessages.alreadyOwner());
			}else{
				sagaPlayer.message(SettlementMessages.alreadyOwner(targetName));
			}
			
			return;
		}

		// Set owner:
		selFaction.setOwner(targetName);
		
//		// Set owner rank:
//		String roleName = FactionConfiguration.config().getOwnerRank();
//		
//		// Get rank:
//		Proficiency rank;
//		try {
//			rank = ProficiencyConfiguration.config().createProficiency(roleName);
//		} catch (InvalidProficiencyException e) {
//			sagaPlayer.message(SettlementMessages.invalidRole(roleName));
//			return;
//		}
//		
//		// Set rank:
//		if(selFaction.isRankAvailable(rank.getName())) selFaction.setRank(selPlayer, rank);

		// Inform:
		selFaction.information(FactionMessages.newOwner(selFaction, targetName));
		
		 
	}
	
	@Command(
            aliases = {"fsetrank"},
            usage = "[faction_name] <member_name> <rank>",
            flags = "",
            desc = "Assign a rank.",
            min = 2,
            max = 3
	)
	@CommandPermissions({"saga.user.faction.setrank"})
	public static void setRank(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String targetName = null;
		String rankName = null;
		
		// Part of a faction:
		Faction selFaction = sagaPlayer.getFaction();
		if(selFaction == null){
			sagaPlayer.message(FactionMessages.notMember());
			return;
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.SET_RANK)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}

		// Arguments:
		 if(args.argsLength() == 3){
			
			String factionName = GeneralMessages.nameFromArg(args.getString(0));
			selFaction = FactionManager.manager().matchFaction(factionName);
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
				return;
			}
			
			targetName = selFaction.matchName(args.getString(1));
			rankName = GeneralMessages.nameFromArg(args.getString(2));
			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
			targetName = selFaction.matchName(args.getString(0));
			rankName = GeneralMessages.nameFromArg(args.getString(1)).toLowerCase();
			
		}

		// Formed:
		if(!selFaction.isFormed()){
			sagaPlayer.message(FactionMessages.notFormed(selFaction));
			sagaPlayer.message(FactionMessages.notFormedInfo(selFaction));
			return;
		}
		
		// Force player:
		SagaPlayer selPlayer;
		try {
			selPlayer = Saga.plugin().forceSagaPlayer(targetName);
		} catch (NonExistantSagaPlayerException e) {
			sagaPlayer.message(GeneralMessages.invalidPlayer(targetName));
			return;
		}

		// Limited member:
		if(selFaction.isLimitedMember(selPlayer)){
			sagaPlayer.message(WarMessages.limitedMembersCantHaveRanks(selFaction));
			return;
		}
		
		// Not a member:
		if(selFaction != selPlayer.getFaction()){
			sagaPlayer.message(FactionMessages.notMember(selPlayer));
			return;
		}

		// Create rank:
		Proficiency rank;
		try {
			rank = ProficiencyConfiguration.config().createProficiency(rankName);
		} catch (InvalidProficiencyException e) {
			sagaPlayer.message(FactionMessages.invalidRank(rankName));
			return;
		}

		// Not a rank:
		if(rank.getDefinition().getType() != ProficiencyType.RANK){
			sagaPlayer.message(FactionMessages.invalidRank(rankName));
			return;
		}
		
		// Rank available:
		if(!selFaction.isRankAvailable(rank.getName())){
			sagaPlayer.message(FactionMessages.rankUnavailable(selFaction, rankName));
			return;
		}
		
		// Set rank:
		selFaction.setRank(selPlayer, rank);
		
		// Inform:
		selFaction.information(FactionMessages.newRank(selFaction, rankName, selPlayer));

		// Release:
		selPlayer.indicateRelease();
		
		
	}
	
	@Command(
            aliases = {"fdisband"},
            usage = "[faction_name or prefix]",
            flags = "",
            desc = "Disband the faction.",
            min = 0,
            max = 1
        )
	@CommandPermissions({"saga.user.faction.delete"}
	)
	public static void disband(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		Faction selFaction = null;
		
		// Arguments:
		if(args.argsLength() == 1){
			
			selFaction = FactionManager.manager().getFaction(args.getString(0));
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(args.getString(0)));
				return;
			}
			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.DELETE)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Delete:
		selFaction.delete();
		
		// Inform:
		if(selFaction == sagaPlayer.getFaction()){
			sagaPlayer.message(FactionMessages.disbanded(selFaction));
		}else{
			sagaPlayer.message(FactionMessages.disbandedOther(selFaction));
		}
		
		
	}

	
	
	// Stats:
	@Command(
            aliases = {"fstats"},
            usage = "[faction_name] [page]",
            flags = "",
            desc = "List faction stats.",
            min = 0,
            max = 2
        )
        @CommandPermissions({"saga.user.faction.stats"})
	public static void stats(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Integer page = null;
		Faction selFaction = null;
		
		String argsPage = null;
		String factionName = null;
		
		// Arguments:
		switch (args.argsLength()) {
			
			case 2:
				
				// Faction:
				factionName = GeneralMessages.nameFromArg(args.getString(0));
				selFaction = FactionManager.manager().matchFaction(factionName);
				if(selFaction == null){
					sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
					return;
				}
				
				// Page:
				argsPage = args.getString(1);
				try {
					page = Integer.parseInt(argsPage);
				}
				catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsPage));
					return;
				}
				break;

			case 1:

				// Chunk group:
				selFaction = sagaPlayer.getFaction();
				if(selFaction == null){
					sagaPlayer.message(FactionMessages.notMember());
					return;
				}

				// Page:
				argsPage = args.getString(0);
				try {
					page = Integer.parseInt(argsPage);
				}
				catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsPage));
					return;
				}
				
				break;

			default:

				// Chunk group:
				selFaction = sagaPlayer.getFaction();
				if(selFaction == null){
					sagaPlayer.message(FactionMessages.notMember());
					return;
				}
				
				// Page:
				page = 1;
				
				break;
				
		}
		
		// Inform:
		sagaPlayer.message(StatsMessages.stats(selFaction, page -1));
		
		
	}
	
	@Command(
		aliases = {"flist"},
		usage = "[faction_name or prefix]",
		flags = "",
		desc = "List faction memebers.",
		min = 0,
		max = 1
	)
	@CommandPermissions({"saga.user.faction.list"})
	public static void list(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	
		
		Faction selFaction = null;
	
		// Arguments:
		if(args.argsLength() == 1){
			
			String factionName = GeneralMessages.nameFromArg(args.getString(0));
			selFaction = FactionManager.manager().matchFaction(factionName);
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
				return;
			}
			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
		}
		
		// Inform:
		sagaPlayer.message(StatsMessages.list(selFaction));
		
	
	}
	
	
	
	// Messages:
	@Command(
            aliases = {"f"},
            usage = "<message>",
            flags = "",
            desc = "Send a faction chat message.",
            min = 1
	)
	@CommandPermissions({"saga.user.faction.chat"})
	public static void chat(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

	
		Faction selFaction = null;
		
		// Faction:
		selFaction = sagaPlayer.getFaction();
		
		if(selFaction == null){
			sagaPlayer.message(FactionMessages.notMember());
			return;
		}
		
		// Formed:
		if(!selFaction.isFormed()){
			sagaPlayer.message(FactionMessages.notFormed(selFaction));
			sagaPlayer.message(FactionMessages.notFormedInfo(selFaction));
			return;
		}
		
		// Create message:
		String message = args.getJoinedStrings(0);
		
		// Inform:
		selFaction.chat(sagaPlayer, message);
		
		// Inform allies:
		Collection<Faction> allyFactions = WarManager.manager().getAllyDeclarations(selFaction.getId());
		for (Faction allyFaction : allyFactions) {
			allyFaction.chat(sagaPlayer, message);
		}
		
		
	}
	
	
	
	// Appearance:
	@Command(
		aliases = {"fsetcolour1","fsetcolor1","fsetprimary"},
		usage = "<colour name>",
		flags = "",
		desc = "Set factions primary colour.",
		min = 1,
		max = 4
	)
	@CommandPermissions({"saga.user.faction.setcolour1"})
	public static void setColour1(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Part of a faction:
		Faction selFaction = sagaPlayer.getFaction();
		if(selFaction == null){
			sagaPlayer.message(FactionMessages.notMember());
			return;
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.SET_COLOR)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Retrieve colour:
		ChatColor color = null;
		String colorName = args.getJoinedStrings(0);
		String cColorName = colorName.replace(" ", "_").toUpperCase();
		ChatColor[] colors = getAvailableColours();
		
		for (int i = 0; i < colors.length; i++) {
			if(colors[i].name().equals(cColorName)){
				color = colors[i];
				break;
			}
		}
		
		if(color == null || color == ChatColor.MAGIC){
			sagaPlayer.message(FactionMessages.invalidColor(colorName));
			sagaPlayer.message(FactionMessages.possibleColors(colors, selFaction));
			return;
		}
		
		// Set color:
		selFaction.setColor1(color);
		
		// Inform:
		selFaction.information(FactionMessages.colour1Set(selFaction));
		
		
	}
	
	@Command(
			aliases = {"fsetcolour2","fsetcolor2","fsetsecondary"},
			usage = "<colour name>",
			flags = "",
			desc = "Set factions secondary colour.",
			min = 1,
			max = 4
	)
	@CommandPermissions({"saga.user.faction.setcolour2"})
	public static void setColour2(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Part of a faction:
		Faction selFaction = sagaPlayer.getFaction();
		if(selFaction == null){
			sagaPlayer.message(FactionMessages.notMember());
			return;
		}
		
		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.SET_COLOR)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Retrieve color:
		ChatColor color = null;
		String colorName = args.getJoinedStrings(0);
		String cColorName = colorName.replace(" ", "_").toUpperCase();
		ChatColor[] colors = getAvailableColours();
		
		for (int i = 0; i < colors.length; i++) {
			if(colors[i].name().equals(cColorName)){
				color = colors[i];
				break;
			}
		}
		
		if(color == null){
			sagaPlayer.message(FactionMessages.invalidColor(colorName));
			sagaPlayer.message(FactionMessages.possibleColors(colors, selFaction));
			return;
		}
		
		// Set color:
		selFaction.setColor2(color);
		
		// Inform:
		selFaction.information(FactionMessages.colour2Set(selFaction));
		
		
	}
	
	private static ChatColor[] getAvailableColours(){
		
		ChatColor[] allColours = ChatColor.values();
		ArrayList<ChatColor> availableColours = new ArrayList<ChatColor>();
		
		for (int i = 0; i < allColours.length; i++) {
			
			if(allColours[i].isColor()) availableColours.add(allColours[i]);
			
		}
		
		return availableColours.toArray(new ChatColor[availableColours.size()]);
		
		
	}
	
	
	
	// Other:
	@Command(
			aliases = {"frename"},
			usage = "[faction_name] <new_name>",
			flags = "",
			desc = "Rename the faction.",
			min = 1,
			max = 2
	)
	@CommandPermissions({"saga.user.faction.rename"})
	public static void rename(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	    	
		
		Faction selFaction = null;
		String newName = null;
		
		// Arguments:
		if(args.argsLength() == 2){
			
			String factionName = GeneralMessages.nameFromArg(args.getString(0));
			selFaction = FactionManager.manager().matchFaction(factionName);
			
			if(selFaction == null){
				sagaPlayer.message(GeneralMessages.invalidFaction(factionName));
				return;
			}
			
	    	newName = GeneralMessages.nameFromArg(args.getString(1));

			
		}else{
			 
			selFaction = sagaPlayer.getFaction();
			
			if(selFaction == null){
				sagaPlayer.message(FactionMessages.notMember());
				return;
			}
			
	    	newName = GeneralMessages.nameFromArg(args.getString(0));

		}

		// Permission:
		if(!selFaction.hasPermission(sagaPlayer, FactionPermission.RENAME)){
			sagaPlayer.message(GeneralMessages.noPermission(selFaction));
			return;
		}
		
		// Fix spaces:
		while(newName.contains("  ")){
			newName = newName.replaceAll("  ", " ");
		}
	    	
	    // Validate name:
	    if(!validateName(newName)){
	    	sagaPlayer.message(FactionMessages.invalidName());
	    	return;
	    }
	    	
	    // Check name:
	    if(!newName.equals(selFaction.getName()) && FactionManager.manager().matchFaction(newName) != null){
	    	sagaPlayer.message(FactionMessages.inUse(newName));
	    	return;
	    }
	    
	    Double cost = EconomyConfiguration.config().getFactionRenameCost();
	    if(selFaction.isFormed() && cost > 0 && EconomyConfiguration.config().isEnabled()){

		    // Check coins:
		    if(EconomyDependency.getCoins(sagaPlayer) < cost){
		    	sagaPlayer.message(EconomyMessages.insufficient(cost));
		    	return;
		    }
		    
	    	// Take coins:
		    EconomyDependency.removeCoins(sagaPlayer, cost);
		    
		    // Inform:
		    sagaPlayer.message(EconomyMessages.spent(cost));
		    
	    }
	    
	    selFaction.setName(newName);
	    
	    // Inform:
	    selFaction.information(FactionMessages.renamed(selFaction));
	    	
	    	
	}
	
	
	
	// Info:
	@Command(
			aliases = {"fhelp"},
			usage = "[page]",
			flags = "",
			desc = "Display faction help.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.help.faction"})
	public static void help(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Integer page = null;
		
		// Arguments:
		if(args.argsLength() == 1){
			try {
				page = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(0)));
				return;
			}
		}else{
			page = 0;
		}
		
		// Inform:
		sagaPlayer.message(HelpMessages.fhelp(page - 1));
	

	}
	
	
	
	// Utility:
	public static boolean validateName(String str) {

         if(org.saga.utility.chat.ChatUtil.getComparisonString(str).length() < FactionConfiguration.config().getMinNameLength()) {
        	 return false;
         }

         if(str.length() > FactionConfiguration.config().getMaxNameLength()) {
        	 return false;
         }

         for (char c : str.toCharArray()) {
                 if ( ! org.saga.utility.chat.ChatUtil.SUBSTANCE_CHARS.contains(String.valueOf(c))) {
                	 return false;
                 }
         }

         return true;

 }
	
	
}
