package net.usrlib.gobirdie.game;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import net.usrlib.gobirdie.R;
import net.usrlib.gobirdie.actor.Bird;
import net.usrlib.gobirdie.actor.Birdhouse;
import net.usrlib.gobirdie.actor.Eagle;
import net.usrlib.gobirdie.actor.Fruit;
import net.usrlib.gobirdie.actor.Monkey;
import net.usrlib.gobirdie.actor.Snake;
import net.usrlib.gobirdie.util.MediaPlayerTask;
import net.usrlib.gobirdie.util.Preferences;

import java.io.IOException;

public class World {
	public static final String FONT_TYPEWRITER = "font/typewriter.ttf";
	public static final String SOUND_TRACK = "sound/silvadito_96kbps.mp3";
	public static final String SOUND_ALARM = "sound/alarm.mp3";
	public static final String SOUND_TONE_1 = "sound/tone1.mp3";
	public static final String SOUND_TONE_2 = "sound/tone2.mp3";
	public static final String SOUND_PUNCH = "sound/punch.mp3";
	public static final String SOUND_CHIRP = "sound/chirp.mp3";
	public static final String SOUND_SLAP = "sound/slap.mp3";

	//* ========== Game Objects ========== *//

	public static Bird sBird = null;
	public static Birdhouse sBirdhouse = null;
	public static Eagle sEagle = null;
	public static Fruit sFruit = null;
	public static Monkey sMonkey = null;
	public static Snake sSnake = null;

	public static final void loadActors(final Context context) {
		sBird = new Bird(context);
		sBirdhouse = new Birdhouse();
		sEagle = new Eagle(context);
		sFruit = new Fruit();
		sMonkey = new Monkey(context);
		sSnake = new Snake(context);
	}

	public static final void resetActors() {
		sBird.reset();
		sBirdhouse.reset();
		sFruit.reset();
		sMonkey.reset();
		sEagle.reset();
		sSnake.reset();
	}

	public static final void drawActors(final Canvas canvas) {
		sBird.draw(canvas);
		sFruit.draw(canvas);
		sMonkey.draw(canvas);
		sEagle.draw(canvas);
		sSnake.draw(canvas);
	}

	//* ============== Fonts ============= *//

	public static Typeface sTypewriter = null;

	public interface OnFontLoaded {
		void run(Typeface typeface);
	}

	public static final void loadFonts(final Context context, final OnFontLoaded callback) {
		if (sTypewriter == null) {
			sTypewriter = Typeface.createFromAsset(context.getAssets(), FONT_TYPEWRITER);
		}

		callback.run(sTypewriter);
	}

	//* ============= Images ============= *//

	public static Drawable sCherry  = null;
	public static Drawable sGrapes  = null;
	public static Drawable sOrange  = null;
	public static Drawable sPear    = null;
	public static Bitmap sBirdRight = null;
	public static Bitmap sBirdLeft  = null;
	public static Bitmap sBirdDown  = null;

	public static final void loadImages(Context context) {
		final Resources resources = context.getResources();

		// Check if already loaded.
		if (sCherry != null && sBirdLeft != null) {
			return;
		}

		// Use getResources() to support API 10. context.getDrawable requires API 21!
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			sCherry = context.getDrawable(R.drawable.fruit_cherry);
			sGrapes = context.getDrawable(R.drawable.fruit_grapes);
			sOrange = context.getDrawable(R.drawable.fruit_orange);
			sPear = context.getDrawable(R.drawable.fruit_pear);
		} else {
			sCherry = resources.getDrawable(R.drawable.fruit_cherry);
			sGrapes = resources.getDrawable(R.drawable.fruit_grapes);
			sOrange = resources.getDrawable(R.drawable.fruit_orange);
			sPear = resources.getDrawable(R.drawable.fruit_pear);
		}

		sBirdRight = BitmapFactory.decodeResource(resources, R.drawable.bird_right);
		sBirdLeft = BitmapFactory.decodeResource(resources, R.drawable.bird_left);
		sBirdDown = BitmapFactory.decodeResource(resources, R.drawable.bird_down);
	}

	//* ========== Music Player ========== *//

	private static AssetFileDescriptor sFileTrack   = null;
	private static MediaPlayerTask sMediaPlayerTask = null;

	public static final void loadMusic(Context context) {
		// Check if already loaded.
		if (sFileTrack != null && sMediaPlayerTask != null) {
			Log.d("World", "loadMusic already loaded.");
			return;
		}

		Log.d("World", "loadMusic first time.");
		try {
			final AssetManager assetManager = context.getAssets();
			sFileTrack = assetManager.openFd(SOUND_TRACK);
			sMediaPlayerTask = new MediaPlayerTask(sFileTrack);

			if (Preferences.isMusicEnabled(context)) {
				startMediaPlayer(context);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final void startMediaPlayer(Context context) {
		if (sMediaPlayerTask == null) {
			return;
		}

		if (!sMediaPlayerTask.isRunning() && Preferences.isMusicEnabled(context)) {
			new Thread(sMediaPlayerTask).start();
		}
	}

	public static final void resumeMediaPlayer(Context context) {
		if (sMediaPlayerTask == null) {
			return;
		}

		if (!sMediaPlayerTask.isRunning() && Preferences.isMusicEnabled(context)) {
			startMediaPlayer(context);
		}

		if (Preferences.isMusicEnabled(context)) {
			sMediaPlayerTask.start();
		}
	}

	public static final void pauseMediaPlayer() {
		if (sMediaPlayerTask == null) {
			return;
		}

		sMediaPlayerTask.pause();
	}

	//* ============= Sound ============== *//

	private static final int MAX_STREAMS = 5;
	private static final int MAX_PLAY_COUNT = 5;

	private static AssetFileDescriptor sFileAlarm;
	private static AssetFileDescriptor sFileTone1;
	private static AssetFileDescriptor sFileTone2;
	private static AssetFileDescriptor sFilePunch;
	private static AssetFileDescriptor sFileChirp;
	private static AssetFileDescriptor sFileSlap;

	private static SoundPool sSoundPool = null;

	private static int sAlarmId;
	private static int sTone1Id;
	private static int sTone2Id;
	private static int sPunchId;
	private static int sChirpId;
	private static int sSlapId;
	private static int sSoundCount;

	private static boolean sIsReady;

	public static final void loadSounds(Context context) {
		// Check if already loaded.
		if (sSoundPool != null) {
			return;
		}

		try {

			final AssetManager assetManager = context.getAssets();

			sFileAlarm = assetManager.openFd(SOUND_ALARM);
			sFileTone1 = assetManager.openFd(SOUND_TONE_1);
			sFileTone2 = assetManager.openFd(SOUND_TONE_2);
			sFilePunch = assetManager.openFd(SOUND_PUNCH);
			sFileChirp = assetManager.openFd(SOUND_CHIRP);
			sFileSlap = assetManager.openFd(SOUND_SLAP);

			// Use SoundPool to support API's < 23 as required by SoundPool.Builder
			sSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
			sSoundPool.setOnLoadCompleteListener((SoundPool soundPool, int sampleId, int status) -> {
				sIsReady = true;
			});

			sAlarmId = sSoundPool.load(sFileAlarm, 1);
			sTone1Id = sSoundPool.load(sFileTone1, 1);
			sTone2Id = sSoundPool.load(sFileTone2, 1);
			sPunchId = sSoundPool.load(sFilePunch, 1);
			sChirpId = sSoundPool.load(sFileChirp, 1);
			sSlapId = sSoundPool.load(sFileSlap, 1);

		} catch (IOException e) {

			sIsReady = false;
			//GameEvent.SoundLoaded.notifyError(e.getMessage());
			e.printStackTrace();

		}
	}

	public static final void playAlarm(Context context) {
		sSoundCount++;

		// Avoid jittery playback
		if (sSoundCount >= MAX_PLAY_COUNT) {
			playSound(context, sAlarmId);
			sSoundCount = 0;
		}
	}

	public static final void playTone1(Context context) {
		playSound(context, sTone1Id);
	}

	public static final void playTone2(Context context) {
		playSound(context, sTone2Id);
	}

	public static final void playPunch(Context context) {
		playSound(context, sPunchId);
	}

	public static final void playChirp(Context context) {
		playSound(context, sChirpId);
	}

	public static final void playSlap(Context context) {
		sSoundCount++;

		// Avoid jittery playback
		if (sSoundCount >= MAX_PLAY_COUNT) {
			playSound(context, sSlapId);
			sSoundCount = 0;
		}
	}

	public static final void releaseSoundPool() {
		if (sSoundPool == null) {
			return;
		}

		sSoundPool.release();
		sSoundPool = null;
		sIsReady = false;
		sSoundCount = 0;
	}

	private static final void playSound(final Context context, final int soundId) {
		if (sSoundPool == null || !sIsReady || !Preferences.isSoundEnabled(context)) {
			return;
		}

		new Thread(() -> {
			sSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
		}).start();
	}

	//* ============= Stage ============== *//

	public static final String SCORE_FORMAT = "%s\n%06d";

	public static Paint sPaint = new Paint();
	private static GameTask sGameTask;

	private static int sScore = 0;
	private static int sWidth;
	private static int sHeight;

	public static final void initWithSurfaceView(final Surface view) {
		sScore = 0;
		sWidth = view.getWidth();
		sHeight = view.getHeight();

		sPaint.setDither(true);
		sPaint.setFilterBitmap(true);

		sGameTask = new GameTask(view);

		Log.i("STAGE", "initWithSurfaceView " + String.valueOf(getWidth()) + "x" + String.valueOf(getHeight()));
	}

	public static final int getWidth() {
		return sWidth;
	}

	public static final int getHeight() {
		return sHeight;
	}

	public static boolean isGameLoopRunning() {
		return sGameTask.isRunning();
	}

	public static final void startGameLoop() {
		if (sGameTask == null || sGameTask.isRunning()) {
			return;
		}

		sGameTask.init();

		new Thread(sGameTask).start();
	}

	public static final void stopGameLoop() {
		sGameTask.stopTask();
		releaseSoundPool();
	}

	public static final void updateScore() {
		sScore++;
	}

	public static final int getScore() {
		return sScore;
	}

	public static final void saveScoreToPreferences(final Context context) {
		Preferences.setScore(context, sScore);
	}
}
