package com.tmtravlr.musicchoices;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Event handler to play music when events happen.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicChoicesEventHandler {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private Random rand = new Random();
	
	@SubscribeEvent
	public void onSound(PlaySoundEvent event) {

		//Set any 
		if(event.getResultSound() != null && (event.getSound().getCategory() == SoundCategory.MUSIC || event.getSound().getCategory() == SoundCategory.RECORDS) && !event.getResultSound().getSoundLocation().toString().contains("note.")) {
			
			if(!MChHelper.isSoundTracked(event.getResultSound())) {
				MusicChoicesMod.ticker.setOvertopMusic(event.getResultSound(), null);
			}
		}
		
	}
	
	/**
	 * Plays the credits music when the credits GUI is opened.
	 */
	@SubscribeEvent
	public void onCredits(GuiOpenEvent event) {
		if (event.getGui() instanceof GuiWinGame && MusicChoicesMod.ticker.creditsMusic == null) {
			
			if(!MusicProperties.creditsList.isEmpty()) {
				
				MusicProperties credits = MusicProperties.creditsList.get(rand.nextInt(MusicProperties.creditsList.size()));
				
				if(credits != null) {
					MusicChoicesMod.ticker.playCreditsMusic(credits);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		
		if(event.getTarget() instanceof EntityLivingBase) {
			triggerBattleMusic(event.getEntityLiving(), (EntityLivingBase) event.getTarget());
		}
	}
	
	private void triggerBattleMusic(EntityLivingBase attacker, EntityLivingBase target) {
		
		//Don't bother checking if there is no target
		if(attacker == null) {
			return;
		}
		
		//First try to play boss music
		if(!MChHelper.isPlayingBossMusic()) {
			//First check the entity getting hit 
			if(target != mc.player && attacker instanceof EntityPlayer) {
				if(playBossMusicForEntity(target)) {
					return;
				}
			}
			
			//Next check the entity hitting
			if(target instanceof EntityPlayer && attacker != mc.player) {
				if(playBossMusicForEntity(attacker)) {
					return;
				}
			}
		}
		
		//Then if nothing plays there, try to play battle music.
		if(!MChHelper.isPlayingBattleMusic() && !MChHelper.isPlayingBossMusic()) {
			//First check the entity getting hit 
			if(target != mc.player && attacker instanceof EntityPlayer) {
				if(playBattleMusicForEntity(target)) {
					return;
				}
			}
			
			//Next check the entity hitting
			if(target instanceof EntityPlayer && attacker != mc.player) {
				if(playBattleMusicForEntity(attacker)) {
					return;
				}
			}
		}
	}
	
	private boolean playBossMusicForEntity(EntityLivingBase entity) {
		if(!entity.isDead && entity.getHealth() > 0) {
			MusicProperties toPlay = MusicProperties.findMusicFromNBTMap(entity, MusicProperties.bossMap);
			
			if(toPlay != null) {
				MusicChoicesMod.ticker.playBossMusic(toPlay);
				MusicChoicesMod.ticker.bossEntity = entity;
				return true;
			}
		}
		
		return false;
	}
	
	private boolean playBattleMusicForEntity(EntityLivingBase entity) {
		String entityName = MChHelper.getNameFromEntity(entity);
		
		MusicChoicesMod.logger.debug("[Music Choices] Searching for battle music for " + entityName);
		
		if(!entity.isDead && entity.getHealth() > 0 && MChHelper.isEntityInBattleRange(entity)) {
			MusicProperties toPlay = MusicProperties.findMusicFromStringMap(entityName, MusicProperties.battleMap);
			
			if(toPlay == null) {
				if(!MusicChoicesMod.battleMonsterOnly || entity.isCreatureType(EnumCreatureType.MONSTER, false)) {
					toPlay = MusicProperties.findBattleMusicFromBlacklist(entityName);
				}
			}
			
			if(toPlay != null) {
				MusicChoicesMod.ticker.playBattleMusic(toPlay);
				MusicChoicesMod.ticker.battleEntityType = entityName;
				return true;
			}
			
		}
		
		return false;
	}
	
}
