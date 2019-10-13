package com.tmtravlr.musicchoices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import com.tmtravlr.musicchoices.musicloader.MusicResourceReloadListener;

/**
 * Registers handlers and listeners.
 *
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MChRegisterer {

	public static void registerEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new MusicChoicesEventHandler());
	}
	
	public static void registerTickHandlers() {
		MinecraftForge.EVENT_BUS.register(new MusicChoicesTickHandler());
	}
	
	public static void registerResourceReloadListeners() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		
		if(manager instanceof SimpleReloadableResourceManager) {
			SimpleReloadableResourceManager simpleManager = (SimpleReloadableResourceManager) manager;
			simpleManager.registerReloadListener(new MusicResourceReloadListener());
		}
	}
	
}
