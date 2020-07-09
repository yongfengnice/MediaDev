package com.suyf.mediadev.codec

import android.animation.TimeAnimator
import android.app.Activity
import android.media.MediaCodec
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.suyf.mediadev.R
import com.suyf.mediadev.utils.GLHelper

class MediaCodecActivity : Activity() {
    private lateinit var textureView: TextureView
    private val mediaDecoder by lazy { MediaDecoder() }//流提取器
    private val timeAnimator by lazy { TimeAnimator() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_media_codec)
        textureView = findViewById(R.id.texture_view)
    }

    fun play(view: View) {
        view.visibility = View.GONE
        startPlayVideo()
    }

    private fun startPlayVideo() {
        if (!mediaDecoder.findVideoDecoder(this)) {
            GLHelper.printLog("findVideoDecoder fail...")
            return
        }
        mediaDecoder.config(Surface(textureView.surfaceTexture))
        timeAnimator.setTimeListener { animation, totalTime, deltaTime ->
            if (!mediaDecoder.isEndOfFile()) {
                val result = mediaDecoder.writeSampleData()
                GLHelper.printLog("writeSampleData:$result")
                mediaDecoder.popOutputBufferData()
            } else {
                timeAnimator.end()
                mediaDecoder.stopAndRelease()
            }
        }
        timeAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timeAnimator.isRunning) {
            timeAnimator.end()
            mediaDecoder.stopAndRelease()
        }
    }

}