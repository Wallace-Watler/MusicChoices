package com.tmtravlr.musicchoices;

import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
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
	
	//Cooldown for the music so it doesn't check every tick
	private int achievementCooldown = 10;
	private int dayCheckCooldown = 9;
	private int deathCooldown = 8;
	private int bossCooldown = 7;
	
	public int menuTickDelay = 10;

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		
		//Handle Advancements
		
		if(mc.world != null && mc.player != null) {
			
			if(!MusicChoicesMod.worldLoaded) {

				for (Object a : AchievementList.achievementList)
				{
					Achievement ach = (Achievement)a;
	
					if (mc.player.getStatFileWriter().hasAchievementUnlocked(ach)) {
						MusicChoicesMod.advancementsUnlocked.put(ach, true);
					}
					else {
						MusicChoicesMod.advancementsUnlocked.put(ach, false);
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
	
				for (Object a : AchievementList.achievementList)
				{
					Achievement ach = (Achievement)a;
	
					if (mc.player.getStatFileWriter().hasAchievementUnlocked(ach) && !((Boolean)MusicChoicesMod.advancementsUnlocked.get(ach)).booleanValue())
					{
						MusicChoicesMod.logger.debug("[Music Choices] Looking for achievement music for achievement " + ach.statId);
						
						MusicChoicesMod.advancementsUnlocked.put(ach, Boolean.valueOf(true));
						MusicProperties toPlay = MusicProperties.findTrackForAchievement(ach.statId);
	
						if (toPlay != null)
						{
							MusicChoicesMod.ticker.playOvertopMusic(toPlay);
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
				
				Entity lookedAt = findEntityLookedAt();
				
				if(lookedAt != null && lookedAt instanceof EntityLivingBase) {//mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.entityHit instanceof EntityLivingBase) {
					MusicChoicesMod.logger.debug("[Music Choices] Entity looked at is " + lookedAt + ", with id " + EntityList.getEntityString(lookedAt));
					
					EntityLivingBase entity = (EntityLivingBase) lookedAt;
					
					if(!entity.isDead && entity.getHealth() > 0) {
						MusicProperties toPlay = MusicProperties.findMusicFromNBTMap(entity, MusicProperties.bossMap);
						
						if(toPlay != null) {
							MusicChoicesMod.ticker.playBossMusic(toPlay);
							MusicChoicesMod.ticker.bossEntity = entity;
						}
					}
				}
			}
			
		}
		else {
			MusicChoicesMod.worldLoaded = false;
		}
		
		
	}
	
	private Entity findEntityLookedAt() {
		int distance = 1000;
		
		Vec3d vecPos = new Vec3d(this.mc.player.getPosition().getX(),this.mc.player.getPosition().getY(),this.mc.player.getPosition().getZ());
		Vec3d vecLook = this.mc.player.getLook(0);
        Vec3d vecPosLook = vecPos.addVector(vecLook.x * distance, vecLook.y * distance, vecLook.z * distance);
        Entity pointedEntity = null;
        Vec3d vecHit = null;
        float expansion = 1.0F;
        List entityList = this.mc.world.getEntitiesWithinAABBExcludingEntity(this.mc.player, this.mc.player.getEntityBoundingBox().offset(vecLook.x * distance, vecLook.y * distance, vecLook.z * distance).expand((double)expansion, (double)expansion, (double)expansion));
        double d2 = distance;

        for (int i = 0; i < entityList.size(); ++i)
        {
            Entity entity = (Entity)entityList.get(i);

            if (entity.canBeCollidedWith())
            {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.getCollisionBoundingBox().expand((double)f2, (double)f2, (double)f2);
                RayTraceResult rayTraceResult = axisalignedbb.calculateIntercept(vecPos, vecPosLook);

                if (axisalignedbb.contains(vecPos))
                {
                    if (0.0D < d2 || d2 == 0.0D)
                    {
                        pointedEntity = entity;
                        vecHit = rayTraceResult == null ? vecPos : rayTraceResult.hitVec;
                        d2 = 0.0D;
                    }
                }
                else if (rayTraceResult != null)
                {
                    double d3 = vecPos.distanceTo(rayTraceResult.hitVec);

                    if (d3 < d2 || d2 == 0.0D)
                    {
                        if (entity == this.mc.player.getRidingEntity() && !entity.canRiderInteract())
                        {
                            if (d2 == 0.0D)
                            {
                                pointedEntity = entity;
                                vecHit = rayTraceResult.hitVec;
                            }
                        }
                        else
                        {
                            pointedEntity = entity;
                            vecHit = rayTraceResult.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
        }
        
        return pointedEntity;
	}
	
}
