package com.cheenx.demo.scankitdemo

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Vibrator
import android.preference.PreferenceManager
import android.util.Log
import java.io.Closeable
import java.io.IOException


/**
 * @auther: Qinjian Xuan
 * @date  : 2024/7/2 .
 * <P>
 * Description:
 * <P>
 */
class ScanBeep(val activity: Activity) : OnCompletionListener, MediaPlayer.OnErrorListener,
    Closeable {

    private var mediaPlayer: MediaPlayer? = null

    init {
        updatePrefs()
    }

    @Synchronized
    fun updatePrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = buildMediaPlayer(activity)
        }
    }

    @Synchronized
    fun playBeepSoundAndVibrate() {
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
        }
        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VIBRATE_DURATION)
    }

    private fun buildMediaPlayer(activity: Context): MediaPlayer? {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        return try {
            val file = activity.resources.openRawResourceFd(R.raw.beep)
            try {
                mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.getLength())
            } finally {
                file.close()
            }
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME)
            mediaPlayer.prepare()
            mediaPlayer
        } catch (ioe: IOException) {
            Log.w("Beep", ioe.message!!)
            mediaPlayer.release()
            null
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        // When the beep has finished playing, rewind to queue up another one.
        mp.seekTo(0)
    }

    @Synchronized
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // we are finished, so put up an appropriate error toast if required
            // and finish
            activity.finish()
        } else {
            // possibly media player error, so release and recreate
            mp.release()
            mediaPlayer = null
            updatePrefs()
        }
        return true
    }

    @Synchronized
    override fun close() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    companion object {
        val TAG = ScanBeep::class.java.getSimpleName()
        const val BEEP_VOLUME = 0.10f
        const val VIBRATE_DURATION = 200L
        
        fun shouldBeep(prefs: SharedPreferences?, activity: Context): Boolean {
            var shouldPlayBeep = true
            if (shouldPlayBeep) {
                // See if sound settings overrides this
                val audioService = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                    shouldPlayBeep = false
                }
            }
            return shouldPlayBeep
        }
    }
}

