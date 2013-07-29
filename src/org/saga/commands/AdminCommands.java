/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saga.commands;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.saga.Clock;
import org.saga.Clock.DaytimeTicker.Daytime;
import org.saga.Saga;
import org.saga.SagaLogger;
import org.saga.buildings.Building;
import org.saga.config.AttributeConfiguration;
import org.saga.config.ExperienceConfiguration;
import org.saga.config.SettlementConfiguration;
import org.saga.dependencies.EconomyDependency;
import org.saga.dependencies.PermissionsDependency;
import org.saga.exceptions.NonExistantSagaPlayerException;
import org.saga.exceptions.SagaPlayerNotLoadedException;
import org.saga.messages.AdminMessages;
import org.saga.messages.EconomyMessages;
import org.saga.messages.GeneralMessages;
import org.saga.messages.PlayerMessages;
import org.saga.messages.SettlementMessages;
import org.saga.messages.StatsMessages;
import org.saga.messages.effects.SettlementEffectHandler;
import org.saga.player.GuardianRune;
import org.saga.player.SagaPlayer;
import org.saga.saveload.Directory;
import org.saga.saveload.WriterReader;
import org.saga.settlements.Bundle;
import org.saga.settlements.BundleManager;
import org.saga.settlements.BundleToggleable;
import org.saga.settlements.Settlement;
import org.sk89q.Command;
import org.sk89q.CommandContext;
import org.sk89q.CommandPermissions;


public class AdminCommands {
	
	
	// Stats:
	@Command(
		aliases = {"astatsother","astatso"},
		usage = "<player_name> [page]",
		flags = "o",
		desc = "Show other player stats. With -o flag offline players are also included.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.admin.player.statsother"})
	public static void statsOther(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		String name = null;
		Integer page = null;
		
		SagaPlayer selPlayer = null;
		
		// Arguments:
		if(args.argsLength() == 2){
			
			name = args.getString(0);
			
			String argsPage = args.getString(1);
			
			try {
				page = Integer.parseInt(argsPage);
			}
			catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(argsPage));
				return;
			}
			
		}else{
			
			name = args.getString(0);
			
			page = 1;
			
		}
		
		// Force:
		if(args.hasFlag('o')){
			
			try {
				selPlayer = Saga.plugin().forceSagaPlayer(name);
			}
			catch (NonExistantSagaPlayerException e) {
				sagaPlayer.message(GeneralMessages.invalidPlayer(name));
				return;
			}
			
		}
		
		// Loaded:
		else{
			
			try {
				selPlayer = Saga.plugin().matchPlayer(name);
			} catch (SagaPlayerNotLoadedException e) {
				sagaPlayer.message(GeneralMessages.notOnline(name));
				return;
			}
			
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.statsTargetName(selPlayer));
		sagaPlayer.message(StatsMessages.stats(selPlayer, page-1));
		
		
	}
	


	// Attributes and levels:
	@Command(
		aliases = {"asetexp"},
		usage = "[player_name] <exp>",
		flags = "",
		desc = "Set players experience.",
		min = 1,
		max = 2)
	@CommandPermissions({"saga.admin.player.setexp"})
	public static void setLevel(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		SagaPlayer selPlayer = null;
		Integer exp = null;
		
		String playerName = null;
		String argsLevel = null;

		// Arguments:
		switch (args.argsLength()) {
			
			case 1:
				
				playerName = sagaPlayer.getName();
				
				argsLevel = args.getString(0);
				try {
					exp = Integer.parseInt(argsLevel);
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsLevel));
				}
				
				break;

			default:

				playerName = args.getString(0);
				
				argsLevel = args.getString(1);
				try {
					exp = Integer.parseInt(argsLevel);
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsLevel));
				}
				
				break;
				
		}
		
		// Derived arguments:
		try {
			
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
			
		} catch (NonExistantSagaPlayerException e) {
			
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
			
		}
		
		// Invalid exp:
		if(exp < 0 || exp > ExperienceConfiguration.config().getMaxExp()){
			
			sagaPlayer.message(AdminMessages.playerExpOutOfRange(exp + ""));
			return;
			
		}
		
		// Set exp:
		selPlayer.setExp(exp);
		
		// Inform:
		selPlayer.message(AdminMessages.playerExpSet(exp));
		if(selPlayer != sagaPlayer){
			sagaPlayer.message(AdminMessages.playerExpSet(exp, selPlayer));
		}

		// Release:
		selPlayer.indicateRelease();

		
	}
	
	@Command(
		aliases = {"asetattribute","asetattr"},
		usage = "[player_name] <attribute> <score>",
		flags = "",
		desc = "Set players attribute score.",
		min = 2,
		max = 3)
	@CommandPermissions({"saga.admin.player.setattribute"})
	public static void setAttribute(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		SagaPlayer selPlayer = null;
		String attrName = null;
		Integer score = null;
		
		String playerName = null;
		
		// Arguments:
		switch (args.argsLength()) {
			case 2:
				
				playerName = sagaPlayer.getName();
				
				attrName = GeneralMessages.nameFromArg(args.getString(0));
				
				try {
					score = Integer.parseInt(args.getString(1));
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(args.getString(1)));
				}
				
				break;

			default:

				playerName = args.getString(0);
				
				attrName = GeneralMessages.nameFromArg(args.getString(1));
				
				try {
					score = Integer.parseInt(args.getString(2));
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(args.getString(1)));
				}
				
				break;
				
		}
		
		try {
			
			// Force:
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
			
		} catch (NonExistantSagaPlayerException e) {
			
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
			
		}

		// Invalid attribute:
		if(!AttributeConfiguration.config().getAttributeNames().contains(attrName)){
			sagaPlayer.message(AdminMessages.attributeInvalid(attrName));
			return;
		}
		
		// Invalid score:
		if(score < 0 || score > selPlayer.getAttributeCap(attrName)){
			
			sagaPlayer.message(AdminMessages.attributeScoreOutOfRange(score + ""));
			return;
			
		}
		
		// Set attribute:
		selPlayer.setAttributeScore(attrName, score);
		
		// Inform:
		selPlayer.message(AdminMessages.attributeSet(attrName, score));
		if(selPlayer != sagaPlayer){
			sagaPlayer.message(AdminMessages.attributeSet(attrName, score, selPlayer));
		}

		// Release:
		selPlayer.indicateRelease();

		
	}

	
	// Health:

	@Command(
		aliases = {"aheal"},
		usage = "[player_name]",
		flags = "",
		desc = "Heal a player.",
		min = 0,
		max = 1)
	@CommandPermissions({"saga.admin.player.heal"})
	public static void heal(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		SagaPlayer selPlayer = null;
		
		String playerName = null;
		
		// Arguments:
		switch (args.argsLength()) {
			case 0:
				
				playerName = sagaPlayer.getName();
				
				break;

			default:

				playerName = args.getString(0);
				
				break;
				
		}
		
		try {
			
			// Force:
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
			
		} catch (NonExistantSagaPlayerException e) {
			
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
			
		}
		
		// Heal:
		selPlayer.restoreHealth();
		
		// Inform:
		if(selPlayer != sagaPlayer){
			selPlayer.message(AdminMessages.healed());
			sagaPlayer.message(AdminMessages.healed(selPlayer));
		}else{
			selPlayer.message(AdminMessages.healed());
		}

		// Release:
		selPlayer.indicateRelease();

		
	}

	
	
	// Economy:
	@Command(
			aliases = {"amodwallet","amodifywallet"},
			usage = "[player_name] <amount>",
			flags = "",
			desc = "Modifies players balance. Negative values to subtract.",
			min = 1,
			max = 2
	)
	@CommandPermissions({"saga.admin.player.modifywallet"})
	public static void modWallet(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		SagaPlayer selPlayer = null;
		Double amount = null;
		
		String argsName = null;
		String argsAmount = null;
		
		
		// Arguments:
		switch (args.argsLength()) {
			case 2:
				
				argsName = args.getString(0);
				try {
					selPlayer = Saga.plugin().forceSagaPlayer(argsName);
				} catch (NonExistantSagaPlayerException e) {
					sagaPlayer.message(GeneralMessages.invalidPlayer(argsName));
					return;
				}
				
				argsAmount = args.getString(1);
				try {
					amount = Double.parseDouble(argsAmount);
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsAmount));
					return;
				}
				
				break;

			default:
				
				selPlayer = sagaPlayer;
				
				argsAmount = args.getString(0);
				try {
					amount = Double.parseDouble(argsAmount);
				} catch (NumberFormatException e) {
					sagaPlayer.message(GeneralMessages.notNumber(argsAmount));
					return;
				}
				
				break;
				
		}

		// Normalise:
		if(amount < 0 && EconomyDependency.getCoins(selPlayer) < -amount) amount = -EconomyDependency.getCoins(selPlayer); 
		
		if(amount == -0.0) amount = 0.0; // Really?
		
		// Modify wallet:
		if(amount >= 0){
			EconomyDependency.addCoins(selPlayer, amount);
		}
		else{
			EconomyDependency.removeCoins(selPlayer, -1 * amount);
		}
		
		// Inform:
		if(selPlayer != sagaPlayer){
			sagaPlayer.message(EconomyMessages.walletModified(selPlayer, amount));
			selPlayer.message(EconomyMessages.walletModified(amount));
		}else{
			selPlayer.message(EconomyMessages.walletModified(amount));
		}

		// Release:
		selPlayer.indicateRelease();

		
	}
	

	
	// Leveling:
	@Command(
		aliases = {"assetclaims"},
		usage = "[settlement_name] <claims>",
		flags = "",
		desc = "Set settlement claims.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.admin.settlement.setclaims"})
	public static void setSettlementClaims(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		Double claims = null;
		Bundle selBundle = null;

		// Arguments:
		if(args.argsLength() == 2){
			
			// Bundle:
			String bundleName = GeneralMessages.nameFromArg(args.getString(0));
			selBundle = BundleManager.manager().matchBundle(bundleName);
			if(selBundle == null){
				sagaPlayer.message(GeneralMessages.invalidSettlement(bundleName));
				return;
			}

			try {
				claims = Double.parseDouble(args.getString(1));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(1)));
				return;
			}
			
		}else{
			
			// Bundle:
			selBundle = sagaPlayer.getBundle();
			if(selBundle == null){
				sagaPlayer.message(SettlementMessages.notMember());
				return;
			}

			try {
				claims = Double.parseDouble(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(0)));
				return;
			}
			
		}
		
		// Is a settlement:
		if(!(selBundle instanceof Settlement)){
			sagaPlayer.message(GeneralMessages.notSettlement(selBundle));
			return;
		}
		Settlement selectedSettlement = (Settlement) selBundle;

		// Invalid claims:
		if(claims < 0.0 || claims > SettlementConfiguration.config().getMaxClaims()){
			
			sagaPlayer.message(AdminMessages.settleClaimsOutOfRange(claims + ""));
			return;
			
		}
		
		// Set claims:
		selectedSettlement.setClaims(claims);
		
		// Inform:
		sagaPlayer.message(AdminMessages.setClaims(selectedSettlement));
		
		
	}
	

	@Command(
		aliases = {"assetbuildpoints","assetbuild"},
		usage = "[settlement_name] <build_points>",
		flags = "",
		desc = "Set settlement build points.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.admin.settlement.setbuildpoints"})
	public static void setSettlementBuildPoints(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		Double bldPts = null;
		Bundle selBundle = null;

		// Arguments:
		if(args.argsLength() == 2){
			
			// Bundle:
			String bundleName = GeneralMessages.nameFromArg(args.getString(0));
			selBundle = BundleManager.manager().matchBundle(bundleName);
			if(selBundle == null){
				sagaPlayer.message(GeneralMessages.invalidSettlement(bundleName));
				return;
			}

			try {
				bldPts = Double.parseDouble(args.getString(1));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(1)));
				return;
			}
			
		}else{
			
			// Bundle:
			selBundle = sagaPlayer.getBundle();
			if(selBundle == null){
				sagaPlayer.message(SettlementMessages.notMember());
				return;
			}

			try {
				bldPts = Double.parseDouble(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(GeneralMessages.notNumber(args.getString(0)));
				return;
			}
			
		}
		
		// Is a settlement:
		if(!(selBundle instanceof Settlement)){
			sagaPlayer.message(GeneralMessages.notSettlement(selBundle));
			return;
		}
		Settlement selectedSettlement = (Settlement) selBundle;

		// Invalid build points:
		if(bldPts < 0.0 || bldPts > SettlementConfiguration.config().getMaxBuildPoints()){
			
			sagaPlayer.message(AdminMessages.settleBuildPointsOutOfRange(bldPts + ""));
			return;
			
		}
		
		// Set build points:
		selectedSettlement.setBuildPoints(bldPts);
		
		// Inform:
		sagaPlayer.message(AdminMessages.setBuildPoints(selectedSettlement));
		
		
	}
	
	@Command(
		aliases = {"asenableoption", "asenableopt", "aenableopt"},
		usage = "[settlement_name] <option>",
		flags = "",
		desc = "Enable settlement option.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.admin.settlement.options"})
	public static void enableOption(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		Bundle selChunkBundle = null;
		BundleToggleable option = null;

		String aOption = null;
		
		// Arguments:
		switch (args.argsLength()) {
			case 2:
				
				// Chunk bundle:
				String bundleName = GeneralMessages.nameFromArg(args.getString(0));
				selChunkBundle = BundleManager.manager().matchBundle(bundleName);
				if(selChunkBundle == null){
					sagaPlayer.message(GeneralMessages.invalidSettlement(bundleName));
					return;
				}
				
				// Option:
				aOption = args.getString(1);
				option = BundleToggleable.match(aOption);
				if(option == null){
					sagaPlayer.message(SettlementMessages.optionInvalid(args.getString(1)));
					sagaPlayer.message(SettlementMessages.optionInvalidInfo());
					return;
				}
				
				break;

			default:
			
				// Chunk group:
				selChunkBundle = sagaPlayer.getBundle();
				if(selChunkBundle == null){
					sagaPlayer.message(SettlementMessages.notMember());
					return;
				}
				
				// Option:
				aOption = args.getString(0);
				option = BundleToggleable.match(aOption);
				if(option == null){
					sagaPlayer.message(SettlementMessages.optionInvalid(aOption));
					sagaPlayer.message(SettlementMessages.optionInvalidInfo());
					return;
				}
				
				break;
				
		}
		
		// Already enabled:
		if(selChunkBundle.isOptionEnabled(option)){
			sagaPlayer.message(SettlementMessages.optionAlreadyEnabled(selChunkBundle, option));
			return;
		}
		
		// Enable:
		selChunkBundle.enableOption(option);
		
		// Inform:
		sagaPlayer.message(SettlementMessages.optionToggle(selChunkBundle, option));
		
		
	}
	
	@Command(
		aliases = {"asdisableoption", "asdisableopt", "adisableopt"},
		usage = "[settlement_name] <option>",
		flags = "",
		desc = "Disable settlement option.",
		min = 1,
		max = 2
	)
	@CommandPermissions({"saga.admin.settlement.options"})
	public static void disableOption(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		Bundle selChunkBundle = null;
		BundleToggleable option = null;
		
		String aOption = null;
		
		// Arguments:
		switch (args.argsLength()) {
			case 2:
				
				// Chunk bundle:
				String bundleName = GeneralMessages.nameFromArg(args.getString(0));
				selChunkBundle = BundleManager.manager().matchBundle(bundleName);
				if(selChunkBundle == null){
					sagaPlayer.message(GeneralMessages.invalidSettlement(bundleName));
					return;
				}
				
				// Option:
				aOption = args.getString(1);
				option = BundleToggleable.match(aOption);
				if(option == null){
					sagaPlayer.message(SettlementMessages.optionInvalid(args.getString(1)));
					sagaPlayer.message(SettlementMessages.optionInvalidInfo());
					return;
				}
				
				break;

			default:
			
				// Chunk group:
				selChunkBundle = sagaPlayer.getBundle();
				if(selChunkBundle == null){
					sagaPlayer.message(SettlementMessages.notMember());
					return;
				}
				
				// Option:
				aOption = args.getString(0);
				option = BundleToggleable.match(aOption);
				if(option == null){
					sagaPlayer.message(SettlementMessages.optionInvalid(aOption));
					sagaPlayer.message(SettlementMessages.optionInvalidInfo());
					return;
				}
				
				break;
				
		}
		
		// Already disabled:
		if(!selChunkBundle.isOptionEnabled(option)){
			sagaPlayer.message(SettlementMessages.optionAlreadyDisabled(selChunkBundle, option));
			return;
		}
		
		// Disabled:
		selChunkBundle.disableOption(option);
		
		// Inform:
		sagaPlayer.message(SettlementMessages.optionToggle(selChunkBundle, option));
		
		
	}
	
	
	
	// Guard rune:
	@Command(
		aliases = {"agrrecharge","agrrech"},
		usage = "[player_name]",
		flags = "",
		desc = "Recharge a guard rune.",
		min = 0,
		max = 1)
	@CommandPermissions({"saga.admin.player.guardrune"})
	public static void rechargeGuardDune(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
	
		SagaPlayer selPlayer = null;
		
		String playerName = null;
		

		// Arguments:
		switch (args.argsLength()) {
			case 0:
				
				playerName = sagaPlayer.getName();
				
				break;

			default:

				playerName = args.getString(0);
				
				break;
				
		}
		
		// Derived arguments:
		try {
			
			// Force:
			selPlayer = Saga.plugin().forceSagaPlayer(playerName);
			
		} catch (NonExistantSagaPlayerException e) {
			
			sagaPlayer.message(GeneralMessages.invalidPlayer(playerName));
			return;
			
		}
		
		// Already charged:
		GuardianRune rune = selPlayer.getGuardRune();
		if(rune.isCharged()){
			sagaPlayer.message(PlayerMessages.alreadyRecharged(rune));
			return;
		}
		
		// Recharge:
		rune.recharge();
		
		// Inform:
		if(selPlayer != sagaPlayer){
			selPlayer.message(AdminMessages.runeRecharged(rune,sagaPlayer));
			sagaPlayer.message(AdminMessages.runeRecharged(rune));
		}else{
			sagaPlayer.message(AdminMessages.runeRecharged(rune));
		}

		// Release:
		selPlayer.indicateRelease();

		
	}
	
	
	
	// Chat:
	@Command(
		aliases = {"a"},
		usage = "<message>",
		flags = "",
		desc = "Send an admin chat message.",
		min = 1)
	@CommandPermissions({"saga.admin.chat"})
	public static void adminChat(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Send admin message:
		String message = AdminMessages.chatMessage(sagaPlayer.getName(), args.getJoinedStrings(0));
		
		chatMessage(message);
		
		
	}
	
	public static void chatWarning(String message){
		

		// Send admin message:
		AdminMessages.chatMessage("WARNING", message);
		
		
	}
	
	public static void chatServer(String message){
		

		// Send admin message:
		AdminMessages.chatMessage("SERVER", message);
		
		
	}
	
	private static void chatMessage(String message) {


		// Send the message to all players who have the correct permission:
		Collection<SagaPlayer> allPlayers = Saga.plugin().getLoadedPlayers();
		
		for (SagaPlayer loadedPlayer : allPlayers) {
			
			if(PermissionsDependency.hasPermission(loadedPlayer, PermissionsDependency.ADMIN_CHAT_PERMISSION)){
				loadedPlayer.message(message);
			}
			
		}
		
		// Log:
		SagaLogger.message(message);

		
	}
	
	
	
	// Debug:
	@Command(
		aliases = {"debuginfo", "adc"},
		usage = "",
		flags = "",
		desc = "Debug assist command.",
		min = 0,
		max = 0
	)
	@CommandPermissions({"saga.debug.admin.dinfo"})
	public static void debugInfo(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		Building bld = sagaPlayer.getSagaChunk().getBuilding();
		HashSet<Building> adjacent = new HashSet<Building>();
		
		bld.collectAdjacentBuildings(bld, adjacent);
		
		for (Building building2 : adjacent) {
			SettlementEffectHandler.playBuildingUpgrade(sagaPlayer, building2);
		}
		
		System.out.println(adjacent);
		
		sagaPlayer.message("debug info command");
		
	}
	
	@Command(
		aliases = {"debugcommand","ada"},
		usage = "",
		flags = "",
		desc = "Debug assist command.",
		min = 0
	)
	@CommandPermissions({"saga.debug.admin.dcommand"})
	public static void debugCommand(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		
		Double numb1 = 1.0;
		if(args.argsLength() > 0){
			try {
				numb1 = args.getDouble(0);
			}
			catch (NumberFormatException e) {
				sagaPlayer.message(ChatColor.RED + args.getString(0) + " must be a number!");
			}
		}
		
		Double numb2 = 1.0;
		if(args.argsLength() > 1){
			try {
				numb2 = args.getDouble(1);
			}
			catch (NumberFormatException e) {
				sagaPlayer.message(ChatColor.RED + args.getString(0) + " must be a number!");
			}
		}
		
		Player player = sagaPlayer.getPlayer();
		player.setNoDamageTicks(player.getMaximumNoDamageTicks());
		player.setMaxHealth(numb1.intValue());
		player.setHealth(numb2.intValue());
		
		
	}
	
	@Command(
		aliases = {"damagetool","adt"},
		usage = "",
		flags = "",
		desc = "Debug assist command.",
		min = 0,
		max = 1000
	)
	@CommandPermissions({"saga.debug.admin.dcommand"})
	public static void damageTool(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		ItemStack item = sagaPlayer.getPlayer().getItemInHand();
		
		if(item != null)
		item.setDurability((short) (item.getType().getMaxDurability() - 1));
				
		
	}
	
	
	
	// Forcing:
	@Command(
		aliases = {"anextdaytime"},
		usage = "",
		flags = "",
		desc = "Force the next daytime.",
		min = 0,
		max = 0
	)
	@CommandPermissions({"saga.admin.world.forcenexdaytime"})
	public static void forceNextDaytime(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		// Next daytime:
		World world = sagaPlayer.getLocation().getWorld();
		Daytime daytime = Clock.clock().forceNextDaytime(world);
		
		// Inform:
		sagaPlayer.message(AdminMessages.nextDaytime(world, daytime));
		
		
	}
	
	@Command(
		aliases = {"aforcework"},
		usage = "",
		flags = "",
		desc = "Force the next work tick for settlements.",
		min = 0,
		max = 0
		)
	@CommandPermissions({"saga.admin.settlements.forcework"})
	public static void forceWorkTick(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Force tick:
		Collection<Bundle> bundles = BundleManager.manager().getAllBundles();
		
		for (Bundle bundle : bundles) {
			
			if(!(bundle instanceof Settlement)) continue;
			
			Settlement settlement = (Settlement) bundle;
			settlement.handleWork();
			
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.forcedWork());
		
		
	}

	@Command(
		aliases = {"aforcecollect"},
		usage = "",
		flags = "",
		desc = "Force the next collect tick for settlements.",
		min = 0,
		max = 0
		)
	@CommandPermissions({"saga.admin.settlements.forcecollect"})
	public static void forceCollectTick(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Force tick:
		Collection<Bundle> bundles = BundleManager.manager().getAllBundles();
		
		for (Bundle bundle : bundles) {
			
			if(!(bundle instanceof Settlement)) continue;
			
			Settlement settlement = (Settlement) bundle;
			settlement.handleCollect();
			
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.forcedCollect());
		
		
	}
	
	@Command(
		aliases = {"aforceproduce"},
		usage = "",
		flags = "wcd",
		desc = "Force the next production tick for settlements. The -w and -c flags will also force work and collect ticks first. The -d flag forces the amount of tick equivalent of a Minecraft day (20).",
		min = 0,
		max = 0
		)
	@CommandPermissions({"saga.admin.settlements.forceproduce"})
	public static void forceProduceTick(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Force tick:
		Collection<Bundle> bundles = BundleManager.manager().getAllBundles();
		
		for (Bundle bundle : bundles) {
			
			if(!(bundle instanceof Settlement)) continue;
			
			Settlement settlement = (Settlement) bundle;
			
			if(args.hasFlag('d')){
				
				for (int i = 0; i < 20; i++){
					if(args.hasFlag('w')) settlement.handleWork();
					if(args.hasFlag('c')) settlement.handleCollect();
					settlement.handleProduction();
				}
				
			}
			
			else{
				if(args.hasFlag('w')) settlement.handleWork();
				if(args.hasFlag('c')) settlement.handleCollect();
				settlement.handleProduction();
			}
			
			
			
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.forcedProduction());
		
		
	}
	
	
	
	// Administration mode:
	@Command(
		aliases = {"aenable"},
		usage = "",
		flags = "",
		desc = "Enable Saga admin mode.",
		min = 0,
		max = 0)
	@CommandPermissions({"saga.admin.adminmode"})
	public static void enableAdminmode(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Already enabled:
		if(sagaPlayer.isAdminMode()){
			sagaPlayer.message(AdminMessages.adminModeAlreadyEnabled());
			return;
		}
		
		// Enable:
		sagaPlayer.enableAdminMode();
		
		// Inform:
		sagaPlayer.message(AdminMessages.adminModeChanged(sagaPlayer));
		
		
	}
	
	@Command(
		aliases = {"adisable"},
		usage = "",
		flags = "",
		desc = "Disable Saga admin mode.",
		min = 0,
		max = 0)
	@CommandPermissions({"saga.admin.adminmode"})
	public static void disableAdminmode(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Already disabled:
		if(!sagaPlayer.isAdminMode()){
			sagaPlayer.message(AdminMessages.adminModeAlreadyDisabled());
			return;
		}
		
		// Enable:
		sagaPlayer.disableAdminMode();
		
		// Inform:
		sagaPlayer.message(AdminMessages.adminModeChanged(sagaPlayer));
		
		
	}
	
	
	
	// Saving:
	@Command(
		aliases = {"asave"},
		usage = "",
		flags = "",
		desc = "Saves Saga information.",
		min = 0,
		max = 0
	)
	@CommandPermissions({"saga.admin.save"})
	public static void save(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

			// Inform:
			sagaPlayer.message(AdminMessages.saving());
			
			// Save:
			Saga.plugin().save();
			
			// Inform:
			sagaPlayer.message(AdminMessages.saved());

	}
	
	
	
	// Wiki:
	@Command(
		aliases = {"awritecommands"},
		usage = "",
		flags = "c",
		desc = "Write all commands in MediaWiki format. The c flag changes the format to WikiCreole.",
		min = 0
	)
	@CommandPermissions({"saga.admin.wiki.writecommands"})
	public static void writeCommands(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		ArrayList<Method> commandMethods = new ArrayList<Method>(PermissionsDependency.getCommandMap().getCommandMethods());
		
		String wikiText = "";
		
		// WikiCreole:
		if(args.hasFlag('c')){
			wikiText = AdminMessages.wikiCommandsCreole(commandMethods);
		}
		
		// MediaWiki:
		else{
			wikiText = AdminMessages.wikiCommands(commandMethods);
		}
		
		Directory dir = Directory.WIKI;
		String name = "commands";
		
		try {
			WriterReader.writeString(dir, name, wikiText);
		}
		catch (IOException e) {
			sagaPlayer.error("Failed to write wiki " + name);
			SagaLogger.severe(AdminCommands.class, "failed to write wiki " + name + ": " + e.getClass().getSimpleName() + ":" + e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.writeDone(dir, name));
		
		
	}
	
	@Command(
			aliases = {"awritepermissions"},
			usage = "",
			flags = "c",
			desc = "Write all permissions in MediaWiki format. The c flag changes the format to WikiCreole.",
			min = 0
	)
	@CommandPermissions({"saga.admin.wiki.writepermissions"})
	public static void writePermissions(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		ArrayList<Method> commandMethods = new ArrayList<Method>(PermissionsDependency.getCommandMap().getCommandMethods());
			
		String wikiText = "";
		
		// WikiCreole:
		if(args.hasFlag('c')){
			wikiText = AdminMessages.wikiPermissionsCreole(commandMethods);
		}
		
		// MediaWiki:
		else{
			wikiText = AdminMessages.wikiPermissions(commandMethods);
		}
			
		Directory dir = Directory.WIKI;
		String name = "permissions";
		
		try {
			WriterReader.writeString(dir, name, wikiText);
		}
		catch (IOException e) {
			sagaPlayer.error("Failed to write wiki " + name);
			SagaLogger.severe(AdminCommands.class, "failed to write wiki " + name + ": " + e.getClass().getSimpleName() + ":" + e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.writeDone(dir, name));
		
		
	}
	
	@Command(
			aliases = {"awriteattributes"},
			usage = "",
			flags = "c",
			desc = "Write all attributes in MediaWiki format. The c flag changes the format to WikiCreole.",
			min = 0
	)
	@CommandPermissions({"saga.admin.wiki.writeattributes"})
	public static void writeAttributes(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String wikiText = "";
		
		// WikiCreole:
		if(args.hasFlag('c')){
			wikiText = AdminMessages.wikiAttributesCreole();
		}
		
		// MediaWiki:
		else{
			wikiText = AdminMessages.wikiAttributes();
		}
			
		Directory dir = Directory.WIKI;
		String name = "attributes";
		
		try {
			WriterReader.writeString(dir, name, wikiText);
		}
		catch (IOException e) {
			sagaPlayer.error("Failed to write wiki " + name);
			SagaLogger.severe(AdminCommands.class, "failed to write wiki " + name + ": " + e.getClass().getSimpleName() + ":" + e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.writeDone(dir, name));
		
		
	}
	
	@Command(
			aliases = {"awriteabilities"},
			usage = "",
			flags = "c",
			desc = "Write all abilities in MediaWiki format. The c flag changes the format to WikiCreole.",
			min = 0
	)
	@CommandPermissions({"saga.admin.wiki.writeabilities"})
	public static void writeAbilities(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String wikiText = "";
		
		// WikiCreole:
		if(args.hasFlag('c')){
			wikiText = AdminMessages.wikiAbilitiesCreole();
		}
		
		// MediaWiki:
		else{
			wikiText = AdminMessages.wikiAbilities();
		}
			
		Directory dir = Directory.WIKI;
		String name = "abilities";
		
		try {
			WriterReader.writeString(dir, name, wikiText);
		}
		catch (IOException e) {
			sagaPlayer.error("Failed to write wiki " + name);
			SagaLogger.severe(AdminCommands.class, "failed to write wiki " + name + ": " + e.getClass().getSimpleName() + ":" + e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.writeDone(dir, name));
		
		
	}

	@Command(
			aliases = {"awritehelpmessages"},
			usage = "",
			flags = "c",
			desc = "Write all help messages in MediaWiki format. The c flag changes the format to WikiCreole.",
			min = 0
	)
	@CommandPermissions({"saga.admin.wiki.writehelpmessages"})
	public static void writeHelpMessages(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		String wikiText = "";
		
		// WikiCreole:
		if(args.hasFlag('c')){
			wikiText = AdminMessages.wikiHelpMessagesCreole();
		}
		
		// MediaWiki:
		else{
			wikiText = AdminMessages.wikiHelpMessages();
		}
			
		Directory dir = Directory.WIKI;
		String name = "help messages";
		
		try {
			WriterReader.writeString(dir, name, wikiText);
		}
		catch (IOException e) {
			sagaPlayer.error("Failed to write wiki " + name);
			SagaLogger.severe(AdminCommands.class, "failed to write wiki " + name + ": " + e.getClass().getSimpleName() + ":" + e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(AdminMessages.writeDone(dir, name));
		
		
	}
	
	
}
