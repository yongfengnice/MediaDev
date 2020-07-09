package com.suyf.mediadev

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLSimpleImageRender

class L1RenderImageSimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_surface_view)
        val glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(GLSimpleImageRender(glSurfaceView))
    }

}