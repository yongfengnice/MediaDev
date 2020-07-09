package com.suyf.mediadev.codec

import android.animation.TimeAnimator
import android.app.Activity
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.SeekBar
import com.suyf.mediadev.R
import com.suyf.mediadev.render.GLFilterCameraRender2
import com.suyf.mediadev.render.GLMediaCodecEsRender
import com.suyf.mediadev.utils.GLHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MediaCodecESActivity : Activity() {
    private lateinit var seekBar: SeekBar
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glMediaCodecEsRender: GLMediaCodecEsRender
    private val mediaDecoder by lazy { MediaDecoder() }//流提取器
    private val timeAnimator by lazy { TimeAnimator() }
    private var surfaceTexture: SurfaceTexture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_media_codec_es)
        glSurfaceView = findViewById(R.id.gl_view)
        seekBar = findViewById(R.id.seek_bar)

        glSurfaceView.setEGLContextClientVersion(2)
        glMediaCodecEsRender = GLMediaCodecEsRender(glSurfaceView)
        glSurfaceView.setRenderer(glMediaCodecEsRender)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //这里只改变绿色通道作为滤镜试验，其他滤镜、缩放、旋转等等都可以通过OpenGL的shape处理的
                glMediaCodecEsRender.setColorRatio(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        glMediaCodecEsRender.renderCallback = object : GLSurfaceView.Renderer {
            override fun onDrawFrame(gl: GL10?) {
                surfaceTexture?.updateTexImage()
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                surfaceTexture?.setDefaultBufferSize(width, height)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                surfaceTexture = SurfaceTexture(glMediaCodecEsRender.mTextureId)
            }
        }
    }

    fun play(view: View) {
        if (surfaceTexture == null) {
            GLHelper.printLog("GLSurfaceView 未初始化...")
            return
        }
        view.visibility = View.GONE
        startPlayVideo()
    }

    private fun startPlayVideo() {
        if (!mediaDecoder.findVideoDecoder(this)) {
            GLHelper.printLog("findVideoDecoder fail...")
            return
        }
        mediaDecoder.config(Surface(surfaceTexture))
        timeAnimator.setTimeListener { animation, totalTime, deltaTime ->
            if (!mediaDecoder.isEndOfFile()) {
                val result = mediaDecoder.writeSampleData()
                GLHelper.printLog("writeSampleData:$result,totalTime:$totalTime,deltaTime:$deltaTime")
                if (mediaDecoder.popOutputBufferData() && totalTime > 170) {
                    glSurfaceView.requestRender()//有输出并且延迟一下才刷新，不然一开始会闪烁一下，还不知道原因
                }
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