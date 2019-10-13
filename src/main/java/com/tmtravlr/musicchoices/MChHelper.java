package com.tmtravlr.musicchoices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;

import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

/**
 * Contains a few static helper methods.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MChHelper {
	
	private static Minecraft mc = Minecraft.getMinecraft(); 
	
	//Classes to hold info about the background and overtop music
	
	public static class BackgroundMusic {
		public MusicTickable music;
		public MusicPropertyList properties;
		
		public BackgroundMusic(MusicTickable musicToSet, MusicPropertyList propToSet) {
			music = musicToSet;
			properties = propToSet;
		}
	}
	
	public static class OvertopMusic {
		public ISound music;
		public MusicPropertyList properties;
		
		public OvertopMusic(ISound musicToSet, MusicPropertyList propToSet) {
			music = musicToSet;
			properties = propToSet;
		}
	}
	
	//Static helper methods

	public static boolean isSoundTracked(ISound sound) {
		return isBackgroundTracked(sound) || isOvertopTracked(sound) || isBattleTracked(sound);
	}
	
	public static boolean isBackgroundTracked(ISound sound) {
		for(BackgroundMusic music : MusicChoicesMod.ticker.backgroundQueue) {
			if(music.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBattleTracked(ISound sound) {
		for(BackgroundMusic music : MusicChoicesMod.ticker.battleQueue) {
			if(music.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOvertopTracked(ISound sound) {
		for(OvertopMusic overtop : MusicChoicesMod.ticker.overtopQueue) {
			if(overtop.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPlayingBossMusic() {
		return MusicChoicesMod.ticker.bossMusic != null || MusicChoicesMod.ticker.bossEntity != null;
	}
	
	public static boolean isPlayingBattleMusic() {
		return MusicChoicesMod.ticker.battleMusic != null || MusicChoicesMod.ticker.battleEntityType != null;
	}
	
	public static String getNameFromEntity(Entity entity) {
		if(entity == null) return "null";
		return entity instanceof EntityPlayer ? "Player" : entity.getName();
	}
	
	public static Class getEntityClassFromName(String name) {
		if(name.equals("Player")) return EntityPlayer.class;
		return EntityList.getClassFromName(name);
	}
	
	public static boolean isEntityInBattleRange(Entity entity) {
		return Math.abs(entity.posX - mc.player.posX) <= MusicChoicesMod.battleDistance && Math.abs(entity.posY - mc.player.posY) <= MusicChoicesMod.battleDistance && Math.abs(entity.posZ - mc.player.posZ) <= MusicChoicesMod.battleDistance;
	}
}
