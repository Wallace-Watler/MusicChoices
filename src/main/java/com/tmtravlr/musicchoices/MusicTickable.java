package com.tmtravlr.musicchoices;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

/**
 * A tickable sound which has support for "fades"; created for background music.
 * @author Rebeca Rey, Wallace Watler
 * @Date October 2019
 * @version 1.12.2-2.0.0.0-beta1
 */
public class MusicTickable extends PositionedSound implements ITickableSound {

	//Used for fades
	public boolean primary;
	public float fadeVolume;
	
	public MusicTickable(ResourceLocation location) {
		this(location, false);
	}
	
	public MusicTickable(ResourceLocation location, boolean repeat) {
		this(location, 1.0F, 1.0F, repeat, 0, AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
	}
	
	public MusicTickable(ResourceLocation location, float volume, float pitch, boolean repeat, int repeatDelay, AttenuationType attenuation, float posX, float posY, float posZ) {
		super(location, SoundCategory.MUSIC);
		
		this.volume = volume;
		this.primary = true;
		this.fadeVolume = volume;
		this.pitch = pitch;
		this.repeat = repeat;
		this.repeatDelay = repeatDelay;
		this.attenuationType = attenuation;
		this.xPosF = posX;
		this.yPosF = posY;
		this.zPosF = posZ;
	}

	@Override
	public void update() {
		fadeVolume = MathHelper.clamp(fadeVolume, 0.0F, 1.0F);
		float primaryVolume = primary ? 1.0f : 0.0001f;
		MusicChoicesMod.logger.debug("Volume: " + volume + ", Fade volume: " + fadeVolume + ", Primary volume: " + primaryVolume);
		
		if(Math.abs(volume - Math.min(fadeVolume, primaryVolume)) >= 0.0001f) {
			volume = (volume*MusicChoicesMod.fadeStrength + Math.min(primaryVolume, fadeVolume)) / (MusicChoicesMod.fadeStrength + 1);
		}
		
	}

	@Override
	public boolean isDonePlaying() {
		return volume < 0.0001f || !MChHelper.isSoundTracked(this);
	}

}
