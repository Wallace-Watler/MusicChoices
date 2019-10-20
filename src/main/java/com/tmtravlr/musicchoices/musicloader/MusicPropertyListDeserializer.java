package com.tmtravlr.musicchoices.musicloader;

import java.lang.reflect.Type;
import java.util.HashSet;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import net.minecraft.util.SoundCategory;
import org.apache.commons.lang3.Validate;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmtravlr.musicchoices.MusicChoicesMod;

/**
 * Deserializer to create a MusicPropertyList from a JSON entry.
 * 
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicPropertyListDeserializer implements JsonDeserializer
{
	public MusicPropertyList deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
	{
		MusicChoicesMod.logger.debug("[Music Choices] Found an entry!");
		
		JsonElement otherElement;
		JsonObject jsonObject = JsonUtils.getJsonObject(jsonElement, "entry");
		MusicPropertyList properties = new MusicPropertyList();
		
		properties.isOptions = JsonUtils.getBoolean(jsonObject, "options", false);

		if(properties.isOptions) {
			
			//Load options
			MusicChoicesMod.logger.debug("[Music Choices] Found a Music Choices options entry!");
			
			if(jsonObject.has("maximum background tracks")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found maximum background tracks entry.");
				properties.maxBackground = JsonUtils.getInt(jsonObject, "maximum background tracks", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - maximum background tracks is " + properties.maxBackground);
			}
			
			if(jsonObject.has("maximum overtop tracks")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found maximum overtop tracks entry.");
				properties.maxOvertop = JsonUtils.getInt(jsonObject, "maximum overtop tracks", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - maximum overtop tracks is " + properties.maxOvertop);
			}
			
			if(jsonObject.has("background fade")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found background fade entry.");
				properties.backgroundFade = JsonUtils.getFloat(jsonObject, "background fade", -1.0f);
				MusicChoicesMod.logger.debug("[Music Choices]     - background fade is " + properties.backgroundFade);
			}
			
			if(jsonObject.has("fade strength")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found fade strength entry.");
				properties.fadeStrength = JsonUtils.getInt(jsonObject, "fade strength", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - fade strength is " + properties.fadeStrength);
			}
			
			if(jsonObject.has("menu music delay minimum")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found menu music delay minimum entry.");
				properties.menuTickDelayMin = JsonUtils.getInt(jsonObject, "menu music delay minimum", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - menu music delay minimum is " + properties.menuTickDelayMin);
			}
			
			if(jsonObject.has("menu music delay maximum")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found menu music delay maximum entry.");
				properties.menuTickDelayMax = JsonUtils.getInt(jsonObject, "menu music delay maximum", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - menu music delay maximum is " + properties.menuTickDelayMax);
			}
			
			if(jsonObject.has("ingame music delay minimum")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found ingame music delay minimum entry.");
				properties.ingameTickDelayMin = JsonUtils.getInt(jsonObject, "ingame music delay minimum", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - ingame music delay minimum is " + properties.ingameTickDelayMin);
			}
			
			if(jsonObject.has("ingame music delay maximum")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found ingame music delay maximum entry.");
				properties.ingameTickDelayMax = JsonUtils.getInt(jsonObject, "ingame music delay maximum", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - ingame music delay maximum is " + properties.ingameTickDelayMax);
			}
			
			if(jsonObject.has("play vanilla")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found play vanilla entry.");
				properties.doPlayVanilla = true;
				properties.playVanilla = JsonUtils.getBoolean(jsonObject, "play vanilla", true);
				MusicChoicesMod.logger.debug("[Music Choices]     - play vanilla is " + properties.playVanilla);
			}
			
			if(jsonObject.has("stop tracks")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found stop tracks entry.");
				properties.doStopTracks = true;
				properties.stopTracks = JsonUtils.getBoolean(jsonObject, "stop tracks", true);
				MusicChoicesMod.logger.debug("[Music Choices]     - stop tracks is " + properties.stopTracks);
			}
			
			if(jsonObject.has("battle max distance")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found battle max distance entry.");
				properties.battleDistance = JsonUtils.getInt(jsonObject, "battle max distance", -1);
				MusicChoicesMod.logger.debug("[Music Choices]     - battle max distance is " + properties.battleDistance);
			}
			
			if(jsonObject.has("battle music for only monsters")) {
				MusicChoicesMod.logger.debug("[Music Choices] - Found battle music for only monsters entry.");
				properties.doBattleMonsterOnly = true;
				properties.battleMonsterOnly = JsonUtils.getBoolean(jsonObject, "battle music for only monsters", true);
				MusicChoicesMod.logger.debug("[Music Choices]     - battle music for only monsters is " + properties.battleMonsterOnly);
			}
			
			return properties;
		}
		
		//If it reaches here, this must be music, not options.
		
		//Load music

		SoundCategory category = SoundCategory.getByName(JsonUtils.getString(jsonObject, "category", SoundCategory.MASTER.getName()));
		Validate.notNull(category, "Invalid category");

		if(category != SoundCategory.MUSIC) {
			//Don't do anything if this isn't a music entry!
			return null;
		}

		MusicChoicesMod.logger.debug("[Music Choices] Found a music entry!");

		//Load in the properties

		properties.isMusic = JsonUtils.getBoolean(jsonObject, "musicchoices", false);

		if(!properties.isMusic) {
			//Don't do anything if this shouldn't be handled by Music Choices!
			return null;
		}

		MusicChoicesMod.logger.debug("[Music Choices] Found a Music Choices music entry!");

		if(jsonObject.has("overlap")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as overlapping.");
			properties.overlap = JsonUtils.getBoolean(jsonObject, "overlap", false);
			MusicChoicesMod.logger.debug("[Music Choices]     - overlapping is " + properties.overlap);
		}
		
		if(jsonObject.has("priority")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Has a priority.");
			properties.priority = JsonUtils.getInt(jsonObject, "priority", 1);
			MusicChoicesMod.logger.debug("[Music Choices]     - priority is " + properties.priority);
		}

		if(jsonObject.has("menu")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as menu music.");
			properties.menu = JsonUtils.getBoolean(jsonObject, "menu", false);
			MusicChoicesMod.logger.debug("[Music Choices]     - menu is " + properties.menu);
		}

		if(jsonObject.has("credits")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as credits music.");
			properties.credits = JsonUtils.getBoolean(jsonObject, "credits", false);
			MusicChoicesMod.logger.debug("[Music Choices]     - credits is " + properties.credits);
		}

		if(jsonObject.has("boss")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as boss music.");
			if(JsonUtils.isJsonArray(jsonObject, "boss")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "boss");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getString(otherElement, "boss entry");
					loadNBTEntry(nbtString, properties.bossTags);
				}
			}
		}
		
		if(jsonObject.has("boss stop")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as boss stop music.");
			if(JsonUtils.isJsonArray(jsonObject, "boss stop")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "boss stop");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getString(otherElement, "boss stop entry");
					loadNBTEntry(nbtString, properties.bossStopTags);
				}
			}
		}

		if(jsonObject.has("victory")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as victory music.");
			if(JsonUtils.isJsonArray(jsonObject, "victory")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "victory");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getString(otherElement, "victory entry");
					loadNBTEntry(nbtString, properties.victoryTags);
				}
			}
		}
		
		if(jsonObject.has("battle")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as battle music.");
			if(JsonUtils.isJsonArray(jsonObject, "battle")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "battle");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getString(otherElement, "battle entry");
					properties.battleEntities.add(entity);
				}
			}
		}
		
		if(jsonObject.has("battle blacklist")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as battle blacklist music.");
			if(properties.battleBlacklistEntities == null) {
				properties.battleBlacklistEntities = new HashSet<>();
			}
			if(JsonUtils.isJsonArray(jsonObject, "battle blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "battle blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getString(otherElement, "battle blacklist entry");
					properties.battleBlacklistEntities.add(entity);
				}
			}
		}
		
		if(jsonObject.has("battle stop")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as battle stop music.");
			if(JsonUtils.isJsonArray(jsonObject, "battle stop")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "battle stop");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getString(otherElement, "battle stop entry");
					properties.battleStopEntities.add(entity);
				}
			}
		}

		if(jsonObject.has("achievements")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as achievement music.");

			otherElement = jsonObject.get("achievements");
			if(JsonUtils.isString(otherElement)) {
				MusicChoicesMod.logger.debug("[Music Choices]     - It's a string.");
				String value = JsonUtils.getString(otherElement, "achievements");
				if(value.equals("all")) {
					properties.allAchievements = true;
					MusicChoicesMod.logger.debug("[Music Choices]         - Marked as 'all'");
				}
				else {
					properties.achievements.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]         - achievement is " + value);
				}
			}
			else if(JsonUtils.isJsonArray(jsonObject, "achievements")) {
				MusicChoicesMod.logger.debug("[Music Choices]     - It's an array of strings.");
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "achievements");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "achievements");
					properties.achievements.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]         - achievement is " + value);
				}
			}
		}

		if(jsonObject.has("event")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as event music.");
			properties.event = JsonUtils.getString(jsonObject, "event");
			MusicChoicesMod.logger.debug("[Music Choices]     - event is " + properties.event);
		}

		if(jsonObject.has("creative")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as creative/not creative music.");
			if(JsonUtils.isString(jsonElement)) {
				String value = JsonUtils.getString(jsonElement, "creative");
				if(value.equalsIgnoreCase("true")) {
					properties.allGamemodes = false;
					properties.creative = true;
					MusicChoicesMod.logger.debug("[Music Choices]     - creative is true");
				}
				else if(value.equalsIgnoreCase("false")) {
					properties.allGamemodes = false;
					properties.creative = false;
					MusicChoicesMod.logger.debug("[Music Choices]     - creative is false");
				}
				else {
					properties.allGamemodes = true;
					MusicChoicesMod.logger.debug("[Music Choices]     - creative is all gamemodes.");
				}
			}
			else {
				properties.allGamemodes = false;
				properties.creative = JsonUtils.getBoolean(jsonObject, "creative", false);
				MusicChoicesMod.logger.debug("[Music Choices]     - creative is " + properties.creative);
			}
		}

		if(jsonObject.has("biomes")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as biome music.");
			if(JsonUtils.isJsonArray(jsonObject, "biomes")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "biomes");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "biomes");
					if(properties.biomes == null) {
						properties.biomes = new HashSet<>();
					}
					properties.biomes.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - biome is " + value);
				}
			}
		}
		
		if(jsonObject.has("biome blacklist")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as biome blacklist music.");
			if(JsonUtils.isJsonArray(jsonObject, "biome blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "biome blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "biome blacklist entry");
					properties.biomeBlacklist.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - biome is " + value);
				}
			}
		}

		if(jsonObject.has("biome types")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as biome type music.");
			if(JsonUtils.isJsonArray(jsonObject, "biome types")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "biome types");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "biome types");
					if(properties.biomeTypes == null) {
						properties.biomeTypes = new HashSet<>();
					}
					properties.biomeTypes.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - biome type is " + value);
				}
			}
		}
		
		if(jsonObject.has("biome type blacklist")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as biome type blacklist music.");
			if(JsonUtils.isJsonArray(jsonObject, "biome type blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "biome type blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "biome type blacklist entry");
					properties.biomeTypeBlacklist.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - biome type is " + value);
				}
			}
		}

		if(jsonObject.has("dimensions")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as dimension music.");
			if(JsonUtils.isJsonArray(jsonObject, "dimensions")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "dimensions");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					int value = JsonUtils.getInt(otherElement, "dimensions entry");
					if(properties.dimensions == null) {
						properties.dimensions = new HashSet<>();
					}
					properties.dimensions.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - dimension is " + value);
				}
			}
		}

		if(jsonObject.has("dimension blacklist")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as dimension blacklist music.");
			if(JsonUtils.isJsonArray(jsonObject, "dimension blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "dimension blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					int value = JsonUtils.getInt(otherElement, "dimension blacklist entry");
					properties.dimensionBlacklist.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - dimension is " + value);
				}
			}
		}

		if(jsonObject.has("lighting")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as lighting music.");
			if(JsonUtils.isJsonArray(jsonObject, "lighting")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "lighting");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "lighting");
					if(properties.lighting == null) {
						properties.lighting = new HashSet<>();
					}
					properties.lighting.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - lighting is " + value);
				}
			}
		}
		
		if(jsonObject.has("time")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as time music.");
			if(JsonUtils.isJsonArray(jsonObject, "time")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "time");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "time");
					if(properties.time == null) {
						properties.time = new HashSet<>();
					}
					properties.time.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - time is " + value);
				}
			}
		}
		
		if(jsonObject.has("weather")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Marked as weather music.");
			if(JsonUtils.isJsonArray(jsonObject, "weather")) {
				JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "weather");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getString(otherElement, "weather");
					if(properties.weather == null) {
						properties.weather = new HashSet<>();
					}
					properties.weather.add(value);
					MusicChoicesMod.logger.debug("[Music Choices]     - weather is " + value);
				}
			}
		}
		
		if(jsonObject.has("height minimum")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Height minimum specified.");
			properties.heightMin = JsonUtils.getInt(jsonObject, "height minimum");
			MusicChoicesMod.logger.debug("[Music Choices]     - height minimum is " + properties.heightMin);
		}
		
		if(jsonObject.has("height maximum")) {
			MusicChoicesMod.logger.debug("[Music Choices] - Height maximum specified.");
			properties.heightMax = JsonUtils.getInt(jsonObject, "height maximum");
			MusicChoicesMod.logger.debug("[Music Choices]     - height maximum is " + properties.heightMax);
		}

		//Done!

		return properties;
	}
	
	private void loadNBTEntry(String nbtString, HashSet<NBTTagCompound> tagSet) {
		NBTTagCompound tag = null;
		try {
			tag = JsonToNBT.getTagFromJson(nbtString);
		}
        catch (NBTException nbtexception)
        {
        	MusicChoicesMod.logger.error("[Music Choices]     - Problem while loading NBT tag!");
        	MusicChoicesMod.logger.trace(nbtexception.getStackTrace());
        }
		if(tag != null) {
			tagSet.add(tag);
			
			if(tag.hasKey("id")) {
				MusicChoicesMod.logger.debug("[Music Choices]     - entity is " + tag.getString("id"));
			}
			else {
				MusicChoicesMod.logger.debug("[Music Choices]     - entity tag is " + nbtString);
			}
		}
		else {
			MusicChoicesMod.logger.debug("[Music Choices]     - didn't recognize entity tag. =(");
		}
	}
}
