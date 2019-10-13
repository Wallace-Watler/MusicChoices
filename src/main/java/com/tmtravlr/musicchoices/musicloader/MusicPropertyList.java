package com.tmtravlr.musicchoices.musicloader;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashSet;

/**
 * List to hold info about the music properties.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicPropertyList {

	//Music Options
	
	//Whether or not this is mean to be played with Music Choices
	public boolean isMusic = false;
	
	//Whether or not it should overlap with other music tracks
	public boolean overlap = false;
	
	//Priority of the music. Higher priorities will try to play first
	public int priority = 1;

	//If the music should play in the menu
	public boolean menu = false;

	//If the music should play in the credits
	public boolean credits = false;

	//Which bosses the boss battle music should play for
	public HashSet<NBTTagCompound> bossTags = new HashSet<>();

	//Which bosses the victory music should play for
	public HashSet<NBTTagCompound> victoryTags = new HashSet<>();
	
	//What to play if boss music finished without a victory
	public HashSet<NBTTagCompound> bossStopTags = new HashSet<>();
	
	//Which entities to play battle music for
	public HashSet<String> battleEntities = new HashSet<>();
	
	//Which entities to play battle music for
	public HashSet<String> battleBlacklistEntities = null;
	
	//Which entities to play battle end music for
	public HashSet<String> battleStopEntities = new HashSet<>();

	//Play for all achievements?
	public boolean allAchievements = false;

	//Set of specific achievements this music should play for
	public HashSet<String> achievements = new HashSet<>();

	//Specific event to play this music at
	public String event = null;

	//Whether the music should play when in creative mode (if allGamemodes is true, it will play regardless)
	public boolean allGamemodes = true;
	public boolean creative = true;
	
	//Which biomes you should be in for this music to play
	public HashSet<String> biomes = null;
	
	//Which biomes you should _not_ be in for this music to play
	public HashSet<String> biomeBlacklist = new HashSet<>();
	
	//Which biome types you should be in for this music to play
	public HashSet<String> biomeTypes = null;
	
	//Which biome types you should _not_ be in for this music to play
	public HashSet<String> biomeTypeBlacklist = new HashSet<>();
	
	//Which dimensions you should be in for this music to play
	public HashSet<Integer> dimensions = null;
	
	//Which dimensions you should _not_ be in for this music to play
	public HashSet<Integer> dimensionBlacklist = new HashSet<>();

	//Which lighting you should be in for this music to play
	public HashSet<String> lighting = null;
	
	//Which time it should be for this music to play
	public HashSet<String> time = null;
	
	//What weather the music will play in
	public HashSet<String> weather = null;
	
	//The minimum height the music will play at
	public int heightMin = Integer.MIN_VALUE;
	
	//The maximum height the music will play at
	public int heightMax = Integer.MAX_VALUE;
	
	
	
	//Options entries
	
	//Whether this is an options entry
	public boolean isOptions;
	
	/** Maximum number of "background" tracks that can play at once. */
	public int maxBackground = -1;
	
	/** Maximum number of "overtop" tracks that can play at once that don't have overlap set to true. */
	public int maxOvertop = -1;
	
	/** How much the background music should fade when music plays over top of it. */
	public float backgroundFade = -1.0f;
	
	/** How fast the background music fades */
	public int fadeStrength = -1;
	
	/** Tick delay for the menu music */
	public int menuTickDelayMin = -1;
	public int menuTickDelayMax = -1;
	
	/** Tick delay for all ingame music */
	public int ingameTickDelayMin = -1;
	public int ingameTickDelayMax = -1;
	
	/** Play vanilla tracks */
	public boolean doPlayVanilla = false;
	public boolean playVanilla = true;
	
	/** Stop tracks that no longer apply */
	public boolean doStopTracks = false;
	public boolean stopTracks = true;
	
	/** Distance to stop battle music */
	public int battleDistance = -1;
	
	/** if this should play for "non-monster" entities */
	public boolean doBattleMonsterOnly = false;
	public boolean battleMonsterOnly = true;
	
}
