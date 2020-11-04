package com.data.studysensor.timetracker;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.data.studysensor.androidoreoforegroundtest.R;

import java.io.Serializable;


//import com.issist.activity.R;

/**
 * Alternative soundpool.<br/>
 * Play small wav files located in the raw folder like the ticking sound and click sound.
 * @author ISSIST
 *
 */
public class SafeSoundPool extends SoundPool implements Serializable, OnLoadCompleteListener{
	/**
	 * Current context.
	 */
	static Context context;
	/**
	 * Singleton
	 */
	static SafeSoundPool SINGLETON;
	/**
	 * Loop int
	 */
	final static int LOOP = -1;
	/**
	 * No loop int
	 */
	final static int NO_LOOP = 0;
	/**
	 * Number of audio streams
	 */
	final static int NUM_OF_STREAMS = 3;
	private static final String TAG = "SafeSoundPool";

	/**
	 * Sound loaded
	 */
	public boolean isSoundLoaded = false;

	/**
	 * Sound ids
	 */
	public int[] soundIDs;
	/**
	 * Stream ids
	 */
	public int[] streamIDs;
	
	/**
	 * Constructor.
	 * @param context
	 * @param maxStreams
	 * @param streamType
	 * @param srcQuality
	 */
	private SafeSoundPool(Context context, int maxStreams, int streamType, int srcQuality) {
		super(maxStreams, streamType, srcQuality);
		this.context = context;
		this.setOnLoadCompleteListener(this);
		
		soundIDs = new int[3];
		streamIDs = new int[4];
		
		loadSounds();

	}
	/**
	 * Get singleton
	 * @param context
	 * @return
	 */
	public static SafeSoundPool getSoundPool( Context context )
	{
		if( SINGLETON == null)
		{
			SINGLETON = new SafeSoundPool(
					context,
					NUM_OF_STREAMS, 
					AudioManager.STREAM_MUSIC, 
					0
					);
		}
		return SINGLETON;
	}
	
	/**
	 * Set isSoundLoaded to true once sounds are finished loading.
	 */
	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
	{
		this.isSoundLoaded = true;
	}
	
	/**
	 * Load the sound from an asset file descriptor.
	 */
	private void loadSounds()
	{
		isSoundLoaded = false;

		soundIDs[0] = this.load(context, R.raw.beepbeep, 1);
		soundIDs[1] = this.load(context, R.raw.sleep, 1);
		soundIDs[2] = this.load(context, R.raw.shutdown_1, 1);
	}

	/**
	 * Play ticking sound.
	 * @return non-zero number if play was successful.
	 */
	public int playTickingSound()
	{
		Log.e(TAG, "Play ticking sound");
		int result = 0;
		if( isSoundLoaded )
		{
			result = this.play(soundIDs[0] , 1, 1, 0, NO_LOOP, 1);
			if( result == 0 )
			{
				Log.e("","Failed.");
			}
			else
			{
				streamIDs[0] = result;
			}
		}
		return result;
	}
	
	/**
	 * Play ticking sound.
	 * @return non-zero number if play was successful.
	 */
	public int playSleepSound()
	{
//		Log.e(TAG, "Play Sleep sound");
		int result = 0;
		if( isSoundLoaded )
		{
			result = this.play(soundIDs[1] , 1, 1, 0, NO_LOOP, 1);
			if( result == 0 )
			{
				Log.e("","Failed.");
			}
			else
			{
				streamIDs[1] = result;
			}
		}
		return result;
	}
	
	/**
	 * Play ticking sound.
	 * @return non-zero number if play was successful.
	 */
	public int playShutdownSound()
	{
		Log.e(TAG, "Play Sleep sound");
		int result = 0;
		if( isSoundLoaded )
		{
			result = this.play(soundIDs[2] , 1, 1, 0, NO_LOOP, 1);
			if( result == 0 )
			{
				Log.e("","Failed.");
			}
			else
			{
				streamIDs[1] = result;
			}
		}
		return result;
	}
	
	
	/**
	 * Unload sound from a sound ID. 
	 */
	public void unloadSounds()
	{
		this.unload(soundIDs[0]);
		this.unload(soundIDs[1]);
		this.unload(soundIDs[2]);
	}

}
