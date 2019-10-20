package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.MChHelper.BackgroundMusic;
import com.tmtravlr.musicchoices.MChHelper.OvertopMusic;
import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

/**
 * Music ticker based on the vanilla MusicTicker class which plays music based on the current
 * situation. This music ticker also tracks music playing over top of the background music, and
 * plays background music with "fade" support.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicChoicesMusicTicker extends MusicTicker {

	private static final Random rand = new Random();
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public LinkedList<BackgroundMusic> backgroundQueue = new LinkedList<>();
	public LinkedList<OvertopMusic> overtopQueue = new LinkedList<>();
	public LinkedList<BackgroundMusic> battleQueue = new LinkedList<>();
	
	public BackgroundMusic bossMusic;
	public EntityLivingBase bossEntity;
	//prevents a bug with the enderdragon
	public int prevBossAge = -1;
	public int sameCount = 0;
	
	public BackgroundMusic battleMusic;
	public String battleEntityType;
	
	public BackgroundMusic creditsMusic;
	
	public float globalBgFadeVolume = 1.0f;
	
	public int delay = 100;
	
	public MusicChoicesMusicTicker(Minecraft minecraft) {
		super(minecraft);
	}

	public void update() {
		MusicType vanillaMusicType = mc.getAmbientMusicType();

		//Check if the current music track is still valid

		String start = "Start: ( Delay: " + delay + ", Primary: " + !backgroundQueue.isEmpty()  + ", Overtop: " + !overtopQueue.isEmpty() + ") ";
		
		if(!backgroundQueue.isEmpty()) {
			
			boolean stillApplies = false;
			boolean primaryPlaying = true;
			
			Iterator it = backgroundQueue.iterator();
			while(it.hasNext()) {
				BackgroundMusic backMusic = (BackgroundMusic) it.next();
				
				//Check if the music currently playing still applies.
				if(backMusic.music.primary && MusicProperties.checkIfMusicStillApplies(backMusic, vanillaMusicType)) {
					stillApplies = true;
				}
				
				//Update the fade volume
				
				backMusic.music.fadeVolume = this.battleQueue.isEmpty() ? this.globalBgFadeVolume : 0.001f;
				
				//Check if the sound is actually still playing
			
				if (!mc.getSoundHandler().isSoundPlaying(backMusic.music))
				{
					MusicChoicesMod.logger.debug("[Music Choices] Background music stopped.");
					
					//If this is the primary music, stop all others
					if(backMusic.music.primary) {
						primaryPlaying = false;
					}
					
					it.remove();
					if(mc.currentScreen instanceof GuiMainMenu || mc.player == null) {
						this.delay = Math.min(MathHelper.getInt(rand, MusicChoicesMod.menuTickDelayMin/*vanillaMusicType.func_148634_b()*/, MusicChoicesMod.menuTickDelayMax/*vanillaMusicType.func_148633_c()*/), this.delay);
					}
					else {
						this.delay = Math.min(MathHelper.getInt(rand, MusicChoicesMod.ingameTickDelayMin/*vanillaMusicType.func_148634_b()*/, MusicChoicesMod.ingameTickDelayMax/*vanillaMusicType.func_148633_c()*/), this.delay);
					}
				}
			}
			
			if(!stillApplies && MusicChoicesMod.stopTracks) {
				playBackgroundMusic();
			}
			
			//If not playing primary music, stop all others too.
			if(!primaryPlaying) {
				it = backgroundQueue.iterator();
				while(it.hasNext()) {
					BackgroundMusic backMusic = (BackgroundMusic) it.next();
					mc.getSoundHandler().stopSound(backMusic.music);
					it.remove();
				}
			}
		}
		
		//Handle the battle music
		
		//If the world or player is null (exited out of the game) stop battle music
		if(battleMusic != null && (mc.world == null || mc.player == null)) {
			battleMusic.music.fadeVolume = 0.0f;
			
			//Play battle finishing music
			
			MusicProperties stopMusic = MusicProperties.findMusicFromStringMap(battleEntityType, MusicProperties.battleStopMap);
			
			if(stopMusic != null) {
				this.playOvertopMusic(stopMusic);
			}
			
			this.battleEntityType = null;
		}
		
		//Otherwise update battle music
		if(!this.battleQueue.isEmpty()) {
			boolean primaryPlaying = true;
			
			Iterator it = this.battleQueue.iterator();
			while(it.hasNext()) {
				BackgroundMusic battleMusic = (BackgroundMusic) it.next();
				
				//Update the fade volume
				
				battleMusic.music.fadeVolume = this.globalBgFadeVolume;
				
				//Remove the music if it's no longer playing
				
				if(!mc.getSoundHandler().isSoundPlaying(battleMusic.music)) {
					MusicChoicesMod.logger.debug("[Music Choices] Stopping battle music track.");
					
					if(battleMusic.music.primary) {
						primaryPlaying = false;
					}
					
					it.remove();
				}
			}
			
			//If there is no primary battle music, set the first entry to true.
			if(!primaryPlaying && !this.battleQueue.isEmpty()) {
				this.battleQueue.getFirst().music.primary = true;
			}
		}
		
		//Handle the overtop music stopping
		
		if(!this.overtopQueue.isEmpty()) {
			Iterator it = this.overtopQueue.iterator();
			while(it.hasNext()) {
				OvertopMusic overtop = (OvertopMusic) it.next();
				
				if(!this.mc.getSoundHandler().isSoundPlaying(overtop.music)) {
					MusicChoicesMod.logger.debug("[Music Choices] Stopping overtop music track.");
					
					it.remove();
				}
				
			}
		}
		
		//Get rid of overtop fades if we aren't playing overtop music
		
		if(this.overtopQueue.isEmpty()) {
			this.globalBgFadeVolume = 1.0f;
		}
		
		//Handle new music playing; however, only play if there is no music playing over top.
		
		if(overtopQueue.isEmpty() && this.battleQueue.isEmpty()) {
			
			if (backgroundQueue.isEmpty() && this.delay-- <= 0) {
				this.delay = Integer.MAX_VALUE;
				
				if(!playBackgroundMusic()) {
					this.delay = 100;
				}
			}
		}
		
		//Handle credits music
		
		if(creditsMusic != null) {
			if(!(mc.currentScreen instanceof GuiWinGame)) {
				creditsMusic.music.fadeVolume = 0.0f;
			}
			if(!mc.getSoundHandler().isSoundPlaying(creditsMusic.music)) {
				creditsMusic = null;
			}
		}
		
		//Handle "boss" music
		
		if(bossMusic != null) {
			//if(MusicChoicesMod.debug) System.out.println("[Music Choices] Boss: " + bossEntity + ", dead? " + (bossEntity == null ? null : bossEntity.isDead) + ", health? " + (bossEntity == null ? null : bossEntity.getHealth()) + ", age? " + (bossEntity == null ? null : bossEntity.ticksExisted) + ", sameCount: " + sameCount);
			
			if(bossEntity != null) {
				if(prevBossAge != bossEntity.ticksExisted) {
					prevBossAge = bossEntity.ticksExisted;
					sameCount = 0;
				}
				else {
					sameCount++;
				}
			}
			
			if((bossEntity == null || bossEntity.isDead || bossEntity.getHealth() <= 0) || sameCount > 10) {
				//Stop the boss music and play the victory music
				bossMusic.music.fadeVolume = 0.0f;
				
				if(bossEntity != null) {
					if(bossEntity.getHealth() <= 0) {
						//Play victory music
						
						MusicProperties victory = MusicProperties.findMusicFromNBTMap(bossEntity, MusicProperties.victoryMap);
						
						if(victory != null) {
							this.playOvertopMusic(victory);
						}
					}
					else {
						//Play non-victory finishing music
						
						MusicProperties stopMusic = MusicProperties.findMusicFromNBTMap(bossEntity, MusicProperties.bossStopMap);
						
						if(stopMusic != null) {
							this.playOvertopMusic(stopMusic);
						}
					}
				}
				
				this.bossEntity = null;
				this.prevBossAge = -1;
				this.sameCount = 0;
			}
			
			if(!mc.getSoundHandler().isSoundPlaying(this.bossMusic.music)) {
				MusicChoicesMod.logger.debug("[Music Choices] Boss music stopped.");
				this.bossMusic = null;
				this.bossEntity = null;
				this.prevBossAge = -1;
				this.sameCount = 0;
			}
		}
		else if(this.bossEntity != null) {
			MusicChoicesMod.logger.debug("[Music Choices] Hmm, should never reach here...");
			this.bossEntity = null;
			this.prevBossAge = -1;
			this.sameCount = 0;
		}
		
		//Handle battle music
		
		if(battleMusic != null) {
			if(this.battleEntityType != null) {
				Class<? extends Entity> entityClass = MChHelper.getEntityClassFromName(this.battleEntityType);
				
				List<Entity> entitiesNear = mc.world.getEntitiesWithinAABB(entityClass, new AxisAlignedBB(mc.player.posX - MusicChoicesMod.battleDistance, mc.player.posY - MusicChoicesMod.battleDistance, mc.player.posZ - MusicChoicesMod.battleDistance, mc.player.posX + MusicChoicesMod.battleDistance, mc.player.posY + MusicChoicesMod.battleDistance, mc.player.posZ + MusicChoicesMod.battleDistance));
				
				if(this.battleEntityType.equals("Player")) {
					if(entitiesNear.size() == 1 && entitiesNear.contains(mc.player)) {
						entitiesNear.clear();
					}
				}
				
				//If it didn't find an entity of the tracked type, check for any other entities that can apply
				if(entitiesNear.isEmpty() && this.battleMusic.properties != null) {
					
					//First check for normal battle entities
					Iterator it = this.battleMusic.properties.battleEntities.iterator();
					while(it.hasNext() && entitiesNear.isEmpty()) {
						String entityName = (String) it.next();
						entityClass = MChHelper.getEntityClassFromName(entityName);
						
						entitiesNear = mc.world.getEntitiesWithinAABB(entityClass, new AxisAlignedBB(mc.player.posX - MusicChoicesMod.battleDistance, mc.player.posY - MusicChoicesMod.battleDistance, mc.player.posZ - MusicChoicesMod.battleDistance, mc.player.posX + MusicChoicesMod.battleDistance, mc.player.posY + MusicChoicesMod.battleDistance, mc.player.posZ + MusicChoicesMod.battleDistance));
						
						if(entityName.equals("Player")) {
							if(entitiesNear.size() == 1 && entitiesNear.contains(mc.player)) {
								entitiesNear.clear();
							}
						}
						//Set the new entity found
						if(!entitiesNear.isEmpty()) {
							this.battleEntityType = entityName;
						}
					}
					
					//Then check for non-blacklisted entities
					if(entitiesNear.isEmpty() && this.battleMusic.properties.battleBlacklistEntities != null) {
						boolean isFound = false;
						
						entitiesNear = mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(mc.player.posX - MusicChoicesMod.battleDistance, mc.player.posY - MusicChoicesMod.battleDistance, mc.player.posZ - MusicChoicesMod.battleDistance, mc.player.posX + MusicChoicesMod.battleDistance, mc.player.posY + MusicChoicesMod.battleDistance, mc.player.posZ + MusicChoicesMod.battleDistance));
							
						for(Entity entity : entitiesNear) {
							if(entity != mc.player && entity instanceof EntityLivingBase && (!MusicChoicesMod.battleMonsterOnly || entity.isCreatureType(EnumCreatureType.MONSTER, false))) {
								String entityName = MChHelper.getNameFromEntity(entity);
								
								//Set the new entity found
								if(!this.battleMusic.properties.battleBlacklistEntities.contains(entityName)) {
									isFound = true;
									this.battleEntityType = entityName;
								}
							}
						}
						
						if(!isFound) {
							entitiesNear.clear();
						}
					}
				}
				
				//If no entities that this battle music can play for were found, fade it out.
				if(entitiesNear.isEmpty()) {
					battleMusic.music.fadeVolume = 0.0f;
					
					//Play battle finishing music
					
					MusicProperties stopMusic = MusicProperties.findMusicFromStringMap(battleEntityType, MusicProperties.battleStopMap);
					
					if(stopMusic != null) {
						this.playOvertopMusic(stopMusic);
					}
					
					this.battleEntityType = null;
				}
			}
			else {
				battleMusic.music.fadeVolume = 0.0f;
			}
			
			if(!mc.getSoundHandler().isSoundPlaying(this.battleMusic.music)) {
				MusicChoicesMod.logger.debug("[Music Choices] Battle music stopped.");
				this.battleMusic = null;
				this.battleEntityType = null;
			}
		}
		else if(this.battleEntityType != null) {
			MusicChoicesMod.logger.debug("[Music Choices] Hmm, should never reach here...");
			this.battleEntityType = null;
		}
		
		MusicChoicesMod.logger.debug(start + "End: ( Delay: " + delay + ", Primary: " + !backgroundQueue.isEmpty()  + ", Overtop: " + !overtopQueue.isEmpty() + ") ");

	}
	
	public void playCreditsMusic(MusicProperties prop) {
		
		//Stop all background music first... since this seems not to happen...
		for(BackgroundMusic bgMusic : backgroundQueue) {
			mc.getSoundHandler().stopSound(bgMusic.music);
		}
		
		MusicTickable toPlay = new MusicTickable(prop.location, true);
		
		this.creditsMusic = new BackgroundMusic(toPlay, prop.propertyList);
		setOvertopMusic(toPlay, prop.propertyList);
		
		MusicChoicesMod.logger.debug("[Music Choices] Playing credits music track called " + toPlay.getSoundLocation());
		mc.getSoundHandler().playSound(toPlay);
	}
	
	public void playBattleMusic(MusicProperties prop) {
		if(battleQueue.isEmpty()) {
			MusicTickable toPlay = new MusicTickable(prop.location);
			
			this.battleMusic = new BackgroundMusic(toPlay, prop.propertyList);
			battleQueue.add(this.battleMusic);
			
			MusicChoicesMod.logger.debug("[Music Choices] Playing battle music track called " + toPlay.getSoundLocation());
			mc.getSoundHandler().playSound(toPlay);
		}
	}
	
	public void playBossMusic(MusicProperties prop) {
		MusicTickable toPlay = new MusicTickable(prop.location, true);
		
		//First stop any battle music playing
		if(!this.battleQueue.isEmpty()) {
			for(BackgroundMusic bMusic : this.battleQueue) {
				bMusic.music.primary = false;
			}
		}
		
		this.bossMusic = new BackgroundMusic(toPlay, prop.propertyList);
		this.battleQueue.add(new BackgroundMusic(toPlay, prop.propertyList));
		
		MusicChoicesMod.logger.debug("[Music Choices] Playing boss music track called " + toPlay.getSoundLocation());
		mc.getSoundHandler().playSound(toPlay);
	}
	
	public boolean playBackgroundMusic() {
		MusicType vanillaMusicType = mc.getAmbientMusicType();
		boolean foundCurrentlyPlaying = false;
		
		//Fade out any other tracks playing
		
		if(!backgroundQueue.isEmpty()) {
			for (BackgroundMusic backMusic : backgroundQueue) {
				backMusic.music.primary = false;
			}
		}
		
		//Search for a track currently playing that now applies
		
		if(!backgroundQueue.isEmpty()) {
			for (BackgroundMusic backMusic : backgroundQueue) {
				if (MusicProperties.checkIfMusicStillApplies(backMusic, vanillaMusicType)) {
					backMusic.music.primary = true;
					foundCurrentlyPlaying = true;
					break;
				}
			}
		}
		
		if(!foundCurrentlyPlaying) {
			
			//Remove the first music which isn't playing if we reached our limit
			
			if(backgroundQueue.size() >= MusicChoicesMod.maxBackground) {
				Iterator it = backgroundQueue.iterator();
				
				while(it.hasNext()) {
					BackgroundMusic backMusic = (BackgroundMusic) it.next();
					
					if(!backMusic.music.primary) {
						mc.getSoundHandler().stopSound(backMusic.music);
						it.remove();
						break;
					}
				}
				
			}
		
			MusicProperties musicProperties = MusicProperties.findTrackForCurrentSituation();
			ResourceLocation location = MusicChoicesMod.playVanilla ? vanillaMusicType.getMusicLocation().getSoundName() : null;
			
			if (musicProperties != null && musicProperties.location != null) {
				location = musicProperties.location;
			}
			
			if(location != null) {
				BackgroundMusic toPlay = new BackgroundMusic(new MusicTickable(location), null);
				
				if(musicProperties != null && musicProperties.propertyList != null) {
					toPlay.properties = musicProperties.propertyList;
				}
				
				MusicChoicesMod.logger.debug("Playing music track called " + toPlay.music.getSoundLocation());
				backgroundQueue.addLast(toPlay);
				mc.getSoundHandler().playSound(toPlay.music);
				
				return true;
			}
			
		}
		
		return false;
	}
	
	public void playOvertopMusic(MusicProperties prop) {
		MusicTickable toPlay = new MusicTickable(prop.location);
		
		setOvertopMusic(toPlay, prop.propertyList);
		
		MusicChoicesMod.logger.debug("[Music Choices] Playing over-top music track called " + toPlay.getSoundLocation());
		mc.getSoundHandler().playSound(toPlay);
	}
	
	public boolean setOvertopMusic(ISound sound, MusicPropertyList properties) {
		//Stop the current background music if not set to overlap
		
		if(properties == null || !properties.overlap) {
			this.globalBgFadeVolume = 0.001f;
			
			if(this.overtopQueue.size() >= MusicChoicesMod.maxOvertop) {
				//We reached the limit! Replace the oldest one.
				OvertopMusic toRemove = overtopQueue.removeFirst();
				
				if(mc.getSoundHandler().isSoundPlaying(toRemove.music)) {
					mc.getSoundHandler().stopSound(toRemove.music);
				}
			}
		}
		else {
			this.globalBgFadeVolume = Math.min(MusicChoicesMod.backgroundFade, this.globalBgFadeVolume);
		}
		
		//Add the music track to the end of the queue.
		
		OvertopMusic toPlay = new OvertopMusic(sound, properties);
		overtopQueue.addLast(toPlay);
		MusicChoicesMod.logger.debug("[Music Choices] Tracking over-top music track called " + sound.getSoundLocation());
		
		return true;
	}

}
