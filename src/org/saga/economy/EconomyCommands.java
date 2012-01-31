package org.saga.economy;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.saga.Saga;
import org.saga.SagaMessages;
import org.saga.buildings.Building;
import org.saga.buildings.BuildingDefinition.BuildingPermission;
import org.saga.buildings.TradingPost;
import org.saga.chunkGroups.ChunkGroup;
import org.saga.chunkGroups.ChunkGroupMessages;
import org.saga.chunkGroups.SagaChunk;
import org.saga.config.EconomyConfiguration;
import org.saga.economy.EconomyManager.InvalidWorldException;
import org.saga.economy.EconomyManager.TransactionType;
import org.saga.exceptions.NonExistantSagaPlayerException;
import org.saga.player.SagaPlayer;
import org.sk89q.Command;
import org.sk89q.CommandContext;
import org.sk89q.CommandPermissions;


public class EconomyCommands {

	
	// Trading post:
	public static void setSell(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		// Permission:
		if(!selectedBuilding.checkBuildingPermission(sagaPlayer, BuildingPermission.HIGH)){
			sagaPlayer.message(SagaMessages.noPermission(selectedBuilding));
			return;
		}
		
		// Material:
		Material material = Material.matchMaterial(args.getString(0));
		if(material == null){
			sagaPlayer.message( EconomyMessages.invalidMaterial(args.getString(0)) );
			return;
		}
		
		// Amount and value:
		Integer amount;
		Double value;
		try {
			amount = Integer.parseInt(args.getString(1));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.invalidAmount(args.getString(1)));
			return;
		}
		try {
			value = Double.parseDouble(args.getString(2));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.invalidAmount(args.getString(2)));
			return;
		}
		
		Transaction transaction = new Transaction(TransactionType.SELL, material, amount, value);
		
		// Add transaction:
		selectedBuilding.addTransaction(transaction);
		
		// Inform:
		SagaChunk sagaChunk = selectedBuilding.getOriginChunk();
		ChunkGroup chunkGroup = null;
		if(sagaChunk != null){
			chunkGroup = sagaChunk.getChunkGroup();
		}
		if(chunkGroup != null){
			chunkGroup.broadcast(EconomyMessages.addedTransactionBroadcast(transaction, chunkGroup, sagaPlayer));
		}
		
		
	}

	
	// Trade deals:
	@Command(
			aliases = {"exportimport", "edeals", "elistdeals"},
			usage = "[page]",
			flags = "",
			desc = "List all available trade deals.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.tradedeals"})
	public static void listDeals(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Integer page;
		
		// Retrieve location:
		Location location = sagaPlayer.getLocation();
		if(location == null){
			sagaPlayer.error("failed to retrieve location");
			Saga.severe(EconomyCommands.class, "failed to retrieve location for " + sagaPlayer + " saga player", "ignoring command");
			return;
		}
		
		// Retrieve trade deals:
		ArrayList<TradeDeal> tradeDeals;
		try {
			tradeDeals = EconomyManager.manager(location.getWorld().getName()).getTradingDeals();
		} catch (InvalidWorldException e) {
			sagaPlayer.error("failed to retrieve " + EconomyManager.class.getSimpleName() + " for " + location.getWorld().getName() + " world.");
			Saga.severe(EconomyCommands.class, "failed to retrieve " + EconomyManager.class.getSimpleName() + " for " + location.getWorld().getName() + " world", "ignoring command");
			return;
		}
		
		// Arguments:
		if(args.argsLength() == 1){
			
			try {
				page = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(EconomyMessages.invalidAmount(args.getString(1)));
				return;
			}
			
		}else{
			page = 0;
		}
		
		// Inform:
		sagaPlayer.message(EconomyMessages.listDeals(tradeDeals, page - 1));
		
		
	}

	
	// Exchange:
	@Command(
			aliases = {"spay", "pay"},
			usage = "<name> <amount>",
			flags = "",
			desc = "Gives money to someone.",
			min = 2,
			max = 2
	)
	@CommandPermissions({"saga.user.pay"})
	public static void pay(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Arguments:
		SagaPlayer targetPlayer = Saga.plugin().getSagaPlayer(args.getString(0));
		if(targetPlayer == null){
			sagaPlayer.message(EconomyMessages.notOnline(args.getString(0)));
			return;
		}
		
		Double amount = null;
		try {
			amount = Double.parseDouble(args.getString(1));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.notNumber(args.getString(1)));
			return;
		}
		
		if(amount < 0){
			amount *= -1;
		}
		
		// Enough currency:
		if(sagaPlayer.getCoins() < amount){
			sagaPlayer.message(EconomyMessages.notEnoughCoins());
			return;
		}
		
		// Not online:
		Location playerLocation = sagaPlayer.getLocation();
		Location targetLocation = targetPlayer.getLocation();
		if(playerLocation == null || targetLocation == null){
			sagaPlayer.message(EconomyMessages.tooFarPay());
			return;
		}
		
		double distance = playerLocation.distance(targetLocation);
		double maxDistance = EconomyConfiguration.config().exchangeDistance;
		if(distance > maxDistance){
			sagaPlayer.message(EconomyMessages.tooFarPay(maxDistance));
			return;
		}
		
		// Pay:
		sagaPlayer.removeCoins(amount);
		targetPlayer.addCoins(amount);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.paid(targetPlayer, amount));
		targetPlayer.message(EconomyMessages.gotPaid(sagaPlayer, amount));
		
		
	}
	
	@Command(
			aliases = {"asetwallet"},
			usage = "<name> <amount>",
			flags = "",
			desc = "Gives money to someone.",
			min = 2,
			max = 2
	)
	@CommandPermissions({"saga.admin.setwallet"})
	public static void setWallet(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Arguments:
		String targetName = args.getString(0);
		
		Double amount = null;
		try {
			amount = Double.parseDouble(args.getString(1));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.notNumber(args.getString(1)));
			return;
		}
		
		// Force player:
		SagaPlayer targetPlayer;
		try {
			targetPlayer = Saga.plugin().forceSagaPlayer(targetName);
		} catch (NonExistantSagaPlayerException e) {
			sagaPlayer.message(SagaMessages.invalidPlayer(targetName));
			return;
		}

		// Set wallet:
		targetPlayer.setCoins(amount);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.setWallet(sagaPlayer, amount));
		if(targetPlayer != sagaPlayer){
			targetPlayer.message(EconomyMessages.walletWasSet(amount));
		}
		
		// Unforce:
		Saga.plugin().unforceSagaPlayer(targetName);
		return;
		
	}
	
	@Command(
			aliases = {"balance","wallet","bal","emoney"},
			usage = "",
			flags = "",
			desc = "See how much currency you have.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.balance"})
	public static void balance(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		sagaPlayer.message(EconomyMessages.wallet(sagaPlayer));
		
	}
		
	@Command(
			aliases = {"ehelp"},
			usage = "[page number]",
			flags = "",
			desc = "Display economy help.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.settlement.help"})
	public static void help(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		Integer page = null;
		
		// Arguments:
		if(args.argsLength() == 1){
			try {
				page = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(ChunkGroupMessages.invalidPage(args.getString(0)));
				return;
			}
		}else{
			page = 0;
		}
		
		// Inform:
		sagaPlayer.message(EconomyMessages.help(page - 1));

		
	}

	
	
}