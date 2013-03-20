package org.saga.messages.effects;

import net.minecraft.server.v1_5_R2.Packet18ArmAnimation;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.saga.SagaLogger;
import org.saga.abilities.Ability;
import org.saga.player.SagaLiving;
import org.saga.player.SagaPlayer;

public class StatsEffectHandler {

	
	public static void playAnimateArm(SagaPlayer sagaPlayer) {

		Player player = sagaPlayer.getPlayer();
		
		Location loc = player.getLocation();
		
		((CraftServer)Bukkit.getServer()).getServer().getPlayerList().sendPacketNearby(loc.getX(),loc.getY(),loc.getZ(),64,((CraftWorld)loc.getWorld()).getHandle().dimension, new Packet18ArmAnimation(((CraftPlayer)player).getHandle(), 1));
		
	}
	
	public static void playParry(SagaLiving<LivingEntity> sagaliving) {
		
		LivingEntity living = sagaliving.getLivingEntity();
		
		Location loc = living.getLocation();
		
		loc.getWorld().playSound(loc, Sound.ANVIL_LAND, 1.0f, 2.0f);
		
	}
	
	public static void playParry(SagaPlayer sagaliving) {
		
		LivingEntity living = sagaliving.getLivingEntity();
		
		Location loc = living.getLocation();
		
		loc.getWorld().playSound(loc, Sound.ANVIL_LAND, 1.0f, 2.0f);
		
	}
	
	public static void playLevelUp(SagaPlayer sagaPlayer) {

		sagaPlayer.playGlobalSound(Sound.LEVEL_UP, 1.0F, 0.5F);
		
	}
	
	public static void playAbility(SagaPlayer sagaPlayer, Ability ability) {
		
		Integer colour = ability.getDefinition().getColour();
		if(colour < 1) return;
		
		try {
			addPotionGraphicalEffect(sagaPlayer.getPlayer(), colour, 30);
		}
		catch (Throwable e) {
			SagaLogger.severe(StatsEffectHandler.class, "failed to play ability effect: " + e.getClass().getSimpleName() + ":" + e.getMessage());
		}
		
	}
	
	public static void playSpellCast(SagaLiving<?> sagaLiving) {
		
		// Smoke:
		for (int i = 5; i <= 12; i++) {
			sagaLiving.playGlobalEffect(Effect.SMOKE, i);
		}
		
		// Sound:
		sagaLiving.playGlobalEffect(Effect.GHAST_SHOOT, 0);
		
	}
	
	public static void playCrush(SagaLiving<?> sagaLiving) {
		
		Location loc = sagaLiving.getLocation();
		
		double deg = 0.0;
		double radius = 0.5;
		
		int[] datas = new int[]{5, 2, 1, 0, 3, 6, 7, 8};
		
		for (int i = 0; i < 8; i++) {
			
			double nx = radius * Math.cos(deg);
			double nz = radius * -Math.sin(deg);
			Location target = loc.clone().add(nx, 0.0, nz);
			loc.getWorld().playEffect(target, Effect.SMOKE, datas[i]);
			deg+= Math.PI / 4.0;
			
		}
		
		// Sound:
		loc.getWorld().playSound(loc, Sound.FALL_BIG, 0.5f, 0.5f);
		
	}
	
	public static void playRecharge(SagaLiving<?> sagaLiving) {
	
		// Flames:
		sagaLiving.playGlobalEffect(Effect.BLAZE_SHOOT, 0);
		sagaLiving.playGlobalEffect(Effect.MOBSPAWNER_FLAMES, 6);
		
		// Sound:
		sagaLiving.playGlobalEffect(Effect.GHAST_SHOOT, 0);
	
	}
	
	public static void playSign(SagaPlayer sagaPlayer) {
		
		// Flames:
//		sagaPlayer.playGlobalEffect(Effect.BLAZE_SHOOT, 0);
		sagaPlayer.playGlobalEffect(Effect.MOBSPAWNER_FLAMES, 6);
		
		// Sound:
		sagaPlayer.playEffect(Effect.CLICK1, 0);
	
	}
	
	/**
	 * Adds a potion graphical effect to the entity.
	 * 
	 * @author nisovin
	 * 
	 * @param entity entity
	 * @param color colour
	 * @param duration duration in 1/20 seconds
	 */
	public static void addPotionGraphicalEffect(LivingEntity entity, int color, int duration) {
		
		
//		final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
//		final DataWatcher dw = el.getDataWatcher();
//		
//		dw.watch(8, Integer.valueOf(color));
//		 
//		// TODO: Bring back potion effects or remove them entirely
//		
//		Bukkit.getScheduler().scheduleSyncDelayedTask(Saga.plugin(), new Runnable() {
//		
//			public void run() {
//			
//				int c = 0;
//				if (!el.effects.isEmpty()) {
//					c = net.minecraft.server.PotionBrewer.a(el.effects.values());
//				}
//				dw.watch(8, Integer.valueOf(c));
//				
//			}
//			
//		}, duration);
		
		
	}
	
	
}
