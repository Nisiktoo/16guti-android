package com.nisiktoo.guti16.featuregame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.nisiktoo.guti16.featuregame.R
import com.nisiktoo.guti16.core.gameengine.model.MoveSound

class GameSoundPlayer(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Note: Ensure move.mp3 and capture.mp3 exist in feature-game/src/main/res/raw/
    private val moveSoundId = soundPool.load(context, R.raw.move, 1)
    private val captureSoundId = soundPool.load(context, R.raw.capture, 1)

    fun playSound(type: MoveSound) {
        when (type) {
            MoveSound.MOVE -> soundPool.play(moveSoundId, 1f, 1f, 1, 0, 1f)
            MoveSound.CAPTURE -> soundPool.play(captureSoundId, 1f, 1f, 1, 0, 1f)
            MoveSound.NONE -> { /* Do nothing */ }
        }
    }

    fun release() {
        soundPool.release()
    }
}
