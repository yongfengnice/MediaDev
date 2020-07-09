package com.suyf.mediadev

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLFBOImageRender
import com.suyf.mediadev.utils.GLHelper
import com.suyf.mediadev.utils.PathUtils
import java.io.File

class L4RenderImageFBOActivity1 : AppCompatActivity() {
    private val choosePhotoCode = 0x1001
    private lateinit var seekBar: SeekBar
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glFboImageRender: GLFBOImageRender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render_image_fbo)
        seekBar = findViewById<SeekBar>(R.id.seek_bar)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.debugFlags = GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS
        glFboImageRender = GLFBOImageRender(glSurfaceView)
        glSurfaceView.setRenderer(glFboImageRender)
        initEvent()
    }

    private fun initEvent() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                glFboImageRender.setColorRatio(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun saveImage(view: View) {
        glSurfaceView.queueEvent { //run on GLThread
            val filename = Environment.getExternalStorageDirectory().absolutePath + "/fbo_${System.currentTimeMillis()}.png"
            File(filename).createNewFile()
            GLHelper.saveFrame(filename, glSurfaceView.width, glSurfaceView.height)
            runOnUiThread {//run on UIThread
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            }
        }
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
            glFboImageRender.setBitMap(BitmapFactory.decodeFile(url))
        }
    }

}