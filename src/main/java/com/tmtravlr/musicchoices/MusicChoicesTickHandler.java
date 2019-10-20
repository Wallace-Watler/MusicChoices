package com.tmtravlr.musicchoices;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

/**
 * Tick handler to handle things that need to be updated per tick.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicChoicesTickHandler {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	private static Random rand = new Random();
	
	public static boolean dead = false;
	//A negative value means that it needs resetting
	public static float sunBrightness = -1.0f;
	public static int dimensionId = 0;

	private Field advancementToProgress;
	
	//Cooldown for the music so it doesn't check every tick
	private int achievementCooldown = 10;
	private int dayCheckCooldown = 9;
	private int deathCooldown = 8;
	private int bossCooldown = 7;
	
	public int menuTickDelay = 10;

	public MusicChoicesTickHandler() {
		try {
			advancementToProgress = ClientAdvancementManager.class.getDeclaredField("advancementToProgress");
			advancementToProgress.setAccessible(true);
			MusicChoicesMod.logger.debug("[Music Choices] Found advancementsToProgress in ClientAdvancementManager class.");
		} catch(NoSuchFieldException e) {
			MusicChoicesMod.logger.error("[Music Choices] Could not find 'advancementsToProgress' in ClientAdvancementManager class. Music set to play upon obtaining advancements will not work.");
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		
		//Handle Advancements
		
		if(mc.world != null && mc.player != null) {

			ClientAdvancementManager clientAdvancementManager = mc.player.connection.getAdvancementManager();

			if(!MusicChoicesMod.worldLoaded) {

				if(advancementToProgress != null) {
					for (Advancement ach : clientAdvancementManager.getAdvancementList().getAdvancements())
					{
						try {
							if (((Map<Advancement, AdvancementProgress>) advancementToProgress.get(clientAdvancementManager)).get(ach).isDone()) {
								MusicChoicesMod.advancementsUnlocked.put(ach, true);
							}
							else {
								MusicChoicesMod.advancementsUnlocked.put(ach, false);
							}
						} catch(IllegalAccessException e) {
							MusicChoicesMod.logger.error("[Music Choices] Could not access 'advancementsToProgress' in ClientAdvancementManager class. This should never happen; contact the mod author.");
						}
					}
				}
				
				if(!MusicProperties.loginList.isEmpty()) {
					MusicChoicesMod.logger.debug("[Music Choices] Looking for login music to play.");
					MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.loginList);
					if(toPlay != null) {
						MusicChoicesMod.ticker.playOvertopMusic(toPlay);
					}
				}
				
				MusicChoicesMod.worldLoaded = true;
			}
			
			if (this.achievementCooldown-- <= 0)
			{
				this.achievementCooldown = 10;

				if(advancementToProgress != null) {
					for (Advancement ach : clientAdvancementManager.getAdvancementList().getAdvancements())
					{
						try {
							if (((Map<Advancement, AdvancementProgress>) advancementToProgress.get(clientAdvancementManager)).get(ach).isDone() && !MusicChoicesMod.advancementsUnlocked.getOrDefault(ach, false)) {
								MusicChoicesMod.logger.debug("[Music Choices] Looking for achievement music for achievement " + ach.getId());

								MusicChoicesMod.advancementsUnlocked.put(ach, true);
								MusicProperties toPlay = MusicProperties.findTrackForAchievement(ach.getId().toString());

								if (toPlay != null)
								{
									MusicChoicesMod.ticker.playOvertopMusic(toPlay);
								}
							}
						} catch(IllegalAccessException e) {
							MusicChoicesMod.logger.error("[Music Choices] Could not access 'advancementsToProgress' in ClientAdvancementManager class. This should never happen; contact the mod author.");
						}
					}
				}
			}
			
			if(dayCheckCooldown-- <= 0) {
				dayCheckCooldown = 10;
				
				if(dimensionId != mc.world.provider.getDimension()) {
					//We changed dimensions, so reset the brightness
					sunBrightness = -1.0F;
					dimensionId = mc.world.provider.getDimension();
					dayCheckCooldown = 100;
				}
				else {
					if(sunBrightness >= 0) {
						if(sunBrightness > 0.5F && mc.world.getSunBrightness(1.0F) < 0.5F) {
							//It went from day to night
							if(!MusicProperties.sunsetList.isEmpty()) {
								MusicChoicesMod.logger.debug("[Music Choices] Looking for sunset music to play.");
								MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.sunsetList);
								if(toPlay != null) {
									MusicChoicesMod.ticker.playOvertopMusic(toPlay);
								}
							}
						}
						if(sunBrightness < 0.5F && mc.world.getSunBrightness(1.0F) > 0.5F) {
							//It went from night to day
							if(!MusicProperties.sunriseList.isEmpty()) {
								MusicChoicesMod.logger.debug("[Music Choices] Looking for sunrise music to play.");
								MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.sunriseList);
								if(toPlay != null) {
									MusicChoicesMod.ticker.playOvertopMusic(toPlay);
								}
							}
						}
					}
					
					sunBrightness = mc.world.getSunBrightness(1.0F);
				}
			}
			
			//Handle death music
			
			if(deathCooldown-- <= 0) {
				deathCooldown = 10;
				
				if(!dead && mc.player.isDead) {
					dead = true;
					
					if(!MusicProperties.deathList.isEmpty()) {
						MusicChoicesMod.logger.debug("[Music Choices] Looking for death music to play.");
						MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.deathList);
						if(toPlay != null) {
							MusicChoicesMod.ticker.playOvertopMusic(toPlay);
						}
					}
				}
				else if(dead && !mc.player.isDead) {
					dead = false;
					
					if(!MusicProperties.respawnList.isEmpty()) {
						MusicChoicesMod.logger.debug("[Music Choices] Looking for respawn music to play.");
						MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.respawnList);
						if(toPlay != null) {
							MusicChoicesMod.ticker.playOvertopMusic(toPlay);
						}
					}
				}
			}
			
			//Boss music
			if(!MChHelper.isPlayingBossMusic() && !MusicProperties.bossMap.isEmpty() && bossCooldown-- <= 0) {
				bossCooldown = 10;
				
				//See what entity the player is looking at, and play boss music if applicable. 
				//MovingObjectPosition mop = mc.objectMouseOver;//this.mc.renderViewEntity.rayTrace(1000, 0.0f);

				EntityLivingBase lookedAt = findEntityLookedAt();
				
				if(lookedAt != null) {
					MusicChoicesMod.logger.debug("[Music Choices] Entity looked at is " + lookedAt + ", with id " + EntityList.getEntityString(lookedAt));
					
					if(!lookedAt.isDead && lookedAt.getHealth() > 0) {
						MusicProperties toPlay = MusicProperties.findMusicFromNBTMap(lookedAt, MusicProperties.bossMap);
						
						if(toPlay != null) {
							MusicChoicesMod.ticker.playBossMusic(toPlay);
							MusicChoicesMod.ticker.bossEntity = lookedAt;
						}
					}
				}
			}
			
		}
		else {
			MusicChoicesMod.worldLoaded = false;
		}
		
		
	}
	
	private EntityLivingBase findEntityLookedAt() {
		final double maxDistance = 100;
		final Vec3d lookVec = mc.player.getLook(0);
		final Vec3d playerPos = mc.player.getPositionVector();
		final Vec3d playerEyes = mc.player.getPositionEyes(0);
		final Vec3d horizontalPlane = lookVec.crossProduct(new Vec3d(lookVec.z, 0, -lookVec.x));

		// Get all entities along line of sight
		List<EntityLivingBase> entitiesOnLineOfSight = mc.world.getEntities(EntityLivingBase.class, entity -> {
			if(entity == null || entity instanceof EntityPlayerSP || entity.getDistance(mc.player) > maxDistance) return false;

			final AxisAlignedBB bb = entity.getEntityBoundingBox().offset(playerEyes.scale(-1));
			boolean entityInFrontOfPlayer = lookVec.dotProduct(entity.getPositionVector().subtract(playerPos)) > 0;

			// If player's eyes are completely within the entity's bounds or are within the horizontal bounds
			// and player is looking vertically, entity is being looked at
			if(0 > bb.minX && 0 < bb.maxX && 0 > bb.minZ && 0 < bb.maxZ
					&& ((0 > bb.minY && 0 < bb.maxY) || (lookVec.x == 0 && lookVec.z == 0 && entityInFrontOfPlayer))) return true;

			if(!entityInFrontOfPlayer) return false;

			// If the entity is horizontally centered on the screen, continue
			// (More specifically, if the bottom corners of the bounding box straddle the vertical plane that cuts the
			// field of view in half)
			byte flags = 0;
			flags |= lookVec.z * bb.minX - lookVec.x * bb.minZ > 0 ? 0b0001 : 0;
			flags |= lookVec.z * bb.minX - lookVec.x * bb.maxZ > 0 ? 0b0010 : 0;
			flags |= lookVec.z * bb.maxX - lookVec.x * bb.minZ > 0 ? 0b0100 : 0;
			flags |= lookVec.z * bb.maxX - lookVec.x * bb.maxZ > 0 ? 0b1000 : 0;
			if(flags == 0 || flags == 15) return false;

			// If the entity is vertically centered on the screen, return true
			// (More specifically, if the corners of the bounding box straddle the horizontal plane that cuts the
			// field of view in half)
			flags = 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.minX, bb.minY, bb.minZ)) > 0 ? 0b00000001 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.minX, bb.minY, bb.maxZ)) > 0 ? 0b00000010 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.minX, bb.maxY, bb.minZ)) > 0 ? 0b00000100 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.minX, bb.maxY, bb.maxZ)) > 0 ? 0b00001000 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.maxX, bb.minY, bb.minZ)) > 0 ? 0b00010000 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.maxX, bb.minY, bb.maxZ)) > 0 ? 0b00100000 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.maxX, bb.maxY, bb.minZ)) > 0 ? 0b01000000 : 0;
			flags |= horizontalPlane.dotProduct(new Vec3d(bb.maxX, bb.maxY, bb.maxZ)) > 0 ? 0b10000000 : 0;
			return !(flags == 0 || flags == -128);
		});

		if(entitiesOnLineOfSight.size() == 0) return null;
		entitiesOnLineOfSight.sort(Comparator.comparingDouble(entity -> entity.getDistanceSq(playerEyes.x, playerEyes.y, playerEyes.z)));
		return entitiesOnLineOfSight.get(0);
	}
}
