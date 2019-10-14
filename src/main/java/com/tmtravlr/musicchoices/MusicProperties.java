package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.MChHelper.BackgroundMusic;
import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;

/**
 * Holds info about each property for the rest of the mod to access.
 * Also holds some static methods for selecting music from it's lists.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicProperties {
	
	private static Random rand = new Random();
	private static Minecraft mc = Minecraft.getMinecraft();
	
	//Some quick access static lists
	
	public static ArrayList<MusicProperties> menuList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> creditsList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> loginList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> deathList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> respawnList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> sunriseList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> sunsetList = new ArrayList<MusicProperties>();
	
	public static HashMap<String, ArrayList<MusicProperties>> achievementMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> bossMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> bossStopMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> victoryMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<String, ArrayList<MusicProperties>> battleMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static ArrayList<MusicProperties> battleBlacklisted = new ArrayList<MusicProperties>();
	
	public static HashMap<String, ArrayList<MusicProperties>> battleStopMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static ArrayList<MusicProperties> ingameList = new ArrayList<MusicProperties>();
	
	
	//Static methods to do useful things.
	
	public static void clearAllLists() {
		menuList.clear();
		creditsList.clear();
		loginList.clear();
		deathList.clear();
		respawnList.clear();
		sunriseList.clear();
		sunsetList.clear();
		achievementMap.clear();
		bossMap.clear();
		bossStopMap.clear();
		victoryMap.clear();
		battleMap.clear();
		battleBlacklisted.clear();
		battleStopMap.clear();
		ingameList.clear();
	}
	
	//Find a music track that should be playing based on the state the game is currently in
	public static MusicProperties findTrackForCurrentSituation() {
		
		if (!menuList.isEmpty() && (mc.currentScreen instanceof GuiMainMenu || mc.player == null)) {
			return menuList.get(rand.nextInt(menuList.size()));
		}
		
		if (!ingameList.isEmpty() && mc.world != null && mc.player != null) {
			return findTrackForCurrentSituationFromList(ingameList);
		}
		
		return null;
	}
	
	//Attempts to find a music track from the given map that applies to the given entity.
	public static MusicProperties findMusicFromNBTMap(EntityLivingBase entity, HashMap<NBTTagCompound, ArrayList<MusicProperties>> nbtMap) {
		ArrayList<ArrayList<MusicProperties>> applicableLists = new ArrayList<ArrayList<MusicProperties>>();
		
		for(NBTTagCompound currentTag : nbtMap.keySet()) {
			ArrayList<MusicProperties> currentList = nbtMap.get(currentTag);
			
			if(currentList != null && !currentList.isEmpty()) {
				NBTTagCompound entityTag = new NBTTagCompound();
				entity.writeToNBT(entityTag);
				if(EntityList.getEntityString(entity) != null && !EntityList.getEntityString(entity).equals("")) {
					entityTag.setString("id", EntityList.getEntityString(entity));
				}
				
				MusicChoicesMod.logger.debug("[Music Choices] Entity tag: " + entityTag);
				
				//Check that the entity has all tags
				if(hasAllTags(currentTag, entityTag)) {
					applicableLists.add(currentList);
				}
			}
		}
		
		if(!applicableLists.isEmpty()) {
			ArrayList<MusicProperties> returnList = applicableLists.get(rand.nextInt(applicableLists.size()));
			return findTrackForCurrentSituationFromList(returnList);
		}
		
		return null;
	}
	
	public static MusicProperties findMusicFromStringMap(String string, HashMap<String, ArrayList<MusicProperties>> stringMap) {
		ArrayList<MusicProperties> musicList = stringMap.get(string);
		
		if(musicList != null) {
			return findTrackForCurrentSituationFromList(musicList);
		}
		
		return null;
	}
	
	//Check that the target nbt tag has all the ones it should have
	public static boolean hasAllTags(NBTTagCompound tagToHave, NBTTagCompound target) {
		Set<String> tagMap = tagToHave.getKeySet();
		for(String tag : tagMap) {
			if(tagToHave.hasKey(tag, 10) && target.hasKey(tag, 10)) {
				return hasAllTags(tagToHave.getCompoundTag(tag), target.getCompoundTag(tag));
			}
			if(!tagToHave.getTag(tag).equals(target.getTag(tag))) {
				return false;
			}
		}

		return true;
	}
	
	//Find a music track that should be playing in the player's current situation from the given list
	public static MusicProperties findTrackForCurrentSituationFromList(ArrayList<MusicProperties> propertyList) {
		
		int maxPriority = 1;
		ArrayList<MusicProperties> releventList = new ArrayList<MusicProperties>();
		
		for(MusicProperties music : propertyList) {
			
			if(checkIfPropertiesApply(music.propertyList)) {
				//If it all checks out, add it to the list
				
				if(music.propertyList.priority > maxPriority) {
					//We have a higher-priority track. Clear out all others.
					releventList.clear();
					maxPriority = music.propertyList.priority;
				}
				
				if(music.propertyList.priority == maxPriority) {
					releventList.add(music);
				}
			}
			
		}
		
		if(!releventList.isEmpty()) {
			return releventList.get(rand.nextInt(releventList.size()));
		}
		
		return null;
	}
	
	public static MusicProperties findBattleMusicFromBlacklist(String entityName) {
		ArrayList<MusicProperties> releventList = new ArrayList<MusicProperties>();
		
		for(MusicProperties music : battleBlacklisted) {
			if(music.propertyList.battleBlacklistEntities != null && !music.propertyList.battleBlacklistEntities.contains(entityName)) {
				releventList.add(music);
			}
		}
		
		if(!releventList.isEmpty()) {
			return findTrackForCurrentSituationFromList(releventList);
		}
		
		return null;
	}
	
	public static MusicProperties findTrackForAchievement(String achName) {
		if(!achievementMap.containsKey(achName)) {
			achName = "all";
		}
		
		return findMusicFromStringMap(achName, achievementMap);
	}
	
	public static boolean checkIfMusicStillApplies(BackgroundMusic music, MusicTicker.MusicType vanillaMusicType) {
		
		//If properties are null, assume this is a vanilla track.
		if(music.properties == null) {
			return vanillaMusicType.getMusicLocation().equals(music.music.getSoundLocation());
		}
		
		//First check for menu or credits music
		
		if(music.properties.menu) {
			return mc.currentScreen instanceof GuiMainMenu || mc.player == null;
		}
		
		if(music.properties.credits) {
			return mc.currentScreen instanceof GuiWinGame;
		}
		
		//Then check for music that should play in-game
		
		if(mc.world != null && mc.player != null) {
			if(!checkIfPropertiesApply(music.properties)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean checkIfPropertiesApply(MusicPropertyList properties) {
		int dimension = mc.world.provider.getDimension();
		int x = MathHelper.floor(mc.player.posX);
		int y = MathHelper.floor(mc.player.posY);
		int z = MathHelper.floor(mc.player.posZ);
		Biome biome = mc.world.getBiome(new BlockPos(x, 0, z));
		boolean isCreative = mc.player.capabilities.isCreativeMode;
		Chunk chunk = mc.world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
		//Note for the two below: if below the world, assume it's "underground", and if above the world, assume it's open sky
		boolean isArtificialLight = (y >= 0 && y < 256) ? chunk.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(x & 15, y, z & 15)) >= 7 : false;
		boolean isSky = (y >= 0 && y < 256) ? chunk.getLightFor(EnumSkyBlock.SKY, new BlockPos(x & 15, y, z & 15)) >= 7 : y < 0 ? false : true;
		boolean isDay = mc.world.getSunBrightness(1.0F) > 0.5F;
		boolean isRain = mc.world.isRaining() && !mc.world.isThundering();
		boolean isStorm = mc.world.isThundering();
		boolean isClear = !(isRain || isStorm);
		
		//Check if the player is in the right gamemode
		if(!properties.allGamemodes) {
			//If they are different
			if(isCreative != properties.creative) {
				return false;
			}
		}
		
		//Make sure this biome is allowed
		if(properties.biomes != null && !properties.biomes.contains(biome.getBiomeName())) {
			return false;
		}
		
		if(properties.biomeBlacklist != null && properties.biomeBlacklist.contains(biome.getBiomeName())) {
			return false;
		}
		
		//Make sure at least one of this biome's types are allowed
		if(properties.biomeTypes != null) {
			boolean hasType = false;
			
			for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
				if(properties.biomeTypes.contains(type.getName())) {
					hasType = true;
					break;
				}
			}
			
			if(!hasType) {
				return false;
			}
		}
		
		if(properties.biomeTypeBlacklist != null) {
			for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
				if(properties.biomeTypeBlacklist.contains(type.getName())) {
					return false;
				}
			}
		}
		
		//Make sure this dimension is allowed
		if(properties.dimensions != null && !properties.dimensions.contains(dimension)) {
			return false;
		}
		
		if(properties.dimensionBlacklist != null && properties.dimensionBlacklist.contains(dimension)) {
			return false;
		}
		
		//Check the lighting
		if(properties.lighting != null) {
			if(isSky && isDay && !properties.lighting.contains("sun")) {
				return false;
			}
			
			if(isSky && !isDay && !properties.lighting.contains("moon")) {
				return false;
			}
			
			if(!isSky && isArtificialLight && !properties.lighting.contains("light")) {
				return false;
			}
			
			if(!isSky && !isArtificialLight && !properties.lighting.contains("dark")) {
				return false;
			}
		}
		
		//Check the time
		if(properties.time != null) {
			if(isDay && !properties.time.contains("day")) {
				return false;
			}
			
			if(!isDay && !properties.time.contains("night")) {
				return false;
			}
		}
		
		if(properties.weather != null) {
			if(isRain && !properties.weather.contains("rain")) {
				return false;
			}
			
			if(isStorm && !properties.weather.contains("storm")) {
				return false;
			}
			
			if(isClear && !properties.weather.contains("clear")) {
				return false;
			}
		}
		
		//Check the height
		
		if(y < properties.heightMin) {
			return false;
		}
		
		if(y > properties.heightMax) {
			return false;
		}
		
		return true;
	}
	
	
	
	
	//Actual music properties class start:
	
	//The sound
	public ResourceLocation location = null;
	
	//The properties of the music track(s) loaded in.
	public MusicPropertyList propertyList;
	
	public MusicProperties(ResourceLocation locationToSet, MusicPropertyList listToSet) {
		location = locationToSet;
		propertyList = listToSet;
	}
	
}
