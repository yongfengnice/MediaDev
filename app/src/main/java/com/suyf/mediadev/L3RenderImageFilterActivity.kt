package com.suyf.mediadev

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLFilerImageRender
import com.suyf.mediadev.utils.PathUtils
import kotlin.concurrent.thread

class L3RenderImageFilterActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var seekBar: SeekBar
    private lateinit var glFilerImageRender: GLFilerImageRender
    private val choosePhotoCode = 0x1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render_image_filter)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        seekBar = findViewById<SeekBar>(R.id.seek_bar)
        glSurfaceView.setEGLContextClientVersion(2)
        glFilerImageRender = GLFilerImageRender(glSurfaceView)
        glSurfaceView.setRenderer(glFilerImageRender)
        initEvent()
    }

    private fun initEvent() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                glFilerImageRender.setColorRatio(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun bigWidth(view: View) {
        glFilerImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_width))
    }

    fun bigHeight(view: View) {
        glFilerImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_height))
    }

    fun bigWidthBigHeight(view: View) {
        glFilerImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_w_long_h))
    }

    fun smallWidthSmallHeight(view: View) {
        glFilerImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.small_w_h))
    }

    fun choosePhoto(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, choosePhotoCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == choosePhotoCode && resultCode == Activity.RESULT_OK) {
            val url = PathUtils.getPath(this, data?.data)
            glFilerImageRender.setBitMap(BitmapFactory.decodeFile(url))
        }
    }

    fun autoPlay(view: View) {
        thread {
            var progress = 0
            while (progress <= 100) {
                glFilerImageRender.setColorRatio(progress / 100f)
                glSurfaceView.requestRender()
                Thread.sleep(16)
                progress++
            }
        }
    }

    fun saveImage(view: View) {
        glFilerImageRender.saveImage()
    }

}