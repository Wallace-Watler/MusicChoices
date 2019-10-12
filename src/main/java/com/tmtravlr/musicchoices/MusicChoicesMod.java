package com.tmtravlr.musicchoices;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = MusicChoicesMod.MODID, name = MusicChoicesMod.NAME, version = MusicChoicesMod.VERSION)
public class MusicChoicesMod
{
    public static final String MODID = "musicchoices";
    public static final String NAME = "Music Choices";
    public static final String VERSION = "1.12.2-2.0.0.0-beta1";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
