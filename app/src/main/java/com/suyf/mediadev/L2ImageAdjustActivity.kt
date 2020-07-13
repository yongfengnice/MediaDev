package com.suyf.mediadev

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLAdjustImageRender

class L2ImageAdjustActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glAdjustImageRender: GLAdjustImageRender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render_image_adjust)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        glSurfaceView.setEGLContextClientVersion(2)
        glAdjustImageRender = GLAdjustImageRender(glSurfaceView)
        glSurfaceView.setRenderer(glAdjustImageRender)
    }

    fun bigWidth(view: View) {
        glAdjustImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_width))
    }

    fun bigHeight(view: View) {
        glAdjustImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_height))
    }

    fun bigWidthBigHeight(view: View) {
        glAdjustImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.long_w_long_h))
    }

    fun smallWidthSmallHeight(view: View) {
        glAdjustImageRender.setBitMap(BitmapFactory.decodeResource(resources, R.drawable.small_w_h))
    }

}