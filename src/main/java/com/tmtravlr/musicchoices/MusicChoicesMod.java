package com.tmtravlr.musicchoices;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
@Mod(modid = MusicChoicesMod.MODID, name = MusicChoicesMod.NAME, version = MusicChoicesMod.VERSION, clientSideOnly = true)
public class MusicChoicesMod {

    public static final String MODID = "musicchoices";
    public static final String NAME = "Music Choices";
    public static final String VERSION = "1.12.2-2.0.0.0-beta1";

    @Mod.Instance(MODID)
    public static MusicChoicesMod musicChoices;

    private static Minecraft mc = Minecraft.getMinecraft();

    public static final Logger logger = LogManager.getLogger(MusicChoicesMod.MODID);

    //---- Options! ----//

    public static boolean overrideJsonOptions = false;

    /** Maximum number of "background" tracks that can play at once. */
    public static int maxBackground = 3;

    /** Maximum number of "overtop" tracks that can play at once that don't have overlap set to true. */
    public static int maxOvertop = 1;

    /** How much the background music should fade when music plays over top of it. */
    public static float backgroundFade = 0.4f;

    /** How fast the background music fades */
    public static int fadeStrength = 10;

    /** Tick delay for the menu music */
    public static int menuTickDelayMin = 20;
    public static int menuTickDelayMax = 600;

    /** Tick delay for all ingame music */
    public static int ingameTickDelayMin = 1200;
    public static int ingameTickDelayMax = 3600;

    /** Play vanilla tracks */
    public static boolean playVanilla = true;

    /** Stop tracks that no longer apply */
    public static boolean stopTracks = true;

    /** Distance to stop battle music */
    public static int battleDistance = 16;

    /** if this should play for "non-monster" entities */
    public static boolean battleMonsterOnly = true;

    //------------------//

    //Handles what music should play; based on the MusicTicker class.
    public static MusicChoicesMusicTicker ticker = new MusicChoicesMusicTicker(mc);

    //When an advancement gets set to true in this map,
    //the corresponding advancement music will try to play
    public static Map<Advancement, Boolean> advancementsUnlocked = new HashMap<>();
    public static boolean worldLoaded = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        musicChoices = this;

        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        //Load the debug
        config.load();

        //options

        overrideJsonOptions = config.getBoolean("override json options", "options", false, "Set to true to override the options loaded in through the sounds.json files with these options.");

        maxBackground = config.getInt("maximum background tracks", "options", 3, 1, 10, "The maximum number of background tracks that can play at once (only one will be at full volume at a time).");
        maxOvertop = config.getInt("maximum overtop tracks", "options", 1, 1, 10, "The maximum number of tracks that can play over top of the background music at once.");

        backgroundFade = config.getFloat("background fade", "options", 0.4f, 0.0001f, 1.0f, "How much the background music will fade to when something is playing over top of it. Note this is only when 'overtop' is true; otherwise it will always fade to almost nothing.");
        fadeStrength = config.getInt("fade strength", "options", 5, 0, 100, "How fast the background music fades when it changes volume.");

        menuTickDelayMin = config.getInt("menu music delay minimum", "options", 20, 0, Integer.MAX_VALUE, "Minimum menu music delay.");
        menuTickDelayMax = config.getInt("menu music delay maximum", "options", 600, 0, Integer.MAX_VALUE, "Maximum menu music delay.");

        ingameTickDelayMin = config.getInt("ingame music delay minimum", "options", 1200, 0, Integer.MAX_VALUE, "Minimum in-game music delay.");
        ingameTickDelayMax = config.getInt("ingame music delay maximum", "options", 3600, 0, Integer.MAX_VALUE, "Maximum in-game music delay.");

        playVanilla = config.getBoolean("play vanilla", "options", true, "Play vanilla tracks as default if nothing else is found for the current situation.");

        stopTracks = config.getBoolean("stop tracks", "options", true, "Stop any background tracks that no longer apply (for instance when you move to a biome where it should no longer play) and attempt to play something new.");

        battleDistance = config.getInt("battle max distance", "options", 8, 1, Integer.MAX_VALUE, "The distance an enemy should be away from you for battle music to stop.");
        battleMonsterOnly = config.getBoolean("battle music for only monsters", "options", true, "Only apply blacklisted battle tracks to entities considered monsters.");

        config.save();

        MChRegisterer.registerEventHandlers();
        MChRegisterer.registerTickHandlers();
        MChRegisterer.registerResourceReloadListeners();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //Replace the vanilla music ticker with our custom one

        try {
            for(Field f : mc.getClass().getDeclaredFields()) {
                if(f.getName().equals("mcMusicTicker") || f.getName().equals("field_147126_aw")) {
                    logger.debug("[Music Choices] Found music ticker in Minecraft class.");
                    f.setAccessible(true);
                    f.set(mc, ticker);
                }
            }
        }
        catch(Exception e) {
            throw new ReportedException(new CrashReport("Music Choices couldn't load in it's music ticker! Things won't work. =( Better let Tmtravlr know.", e));
        }
    }

    public static boolean isGamePaused() {
        return mc.isSingleplayer() && mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame() && !(mc.getIntegratedServer() != null && mc.getIntegratedServer().getPublic());
    }
}
