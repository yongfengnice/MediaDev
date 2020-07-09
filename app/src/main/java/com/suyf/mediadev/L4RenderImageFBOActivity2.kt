package com.suyf.mediadev

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLFBOImageRender2
import com.suyf.mediadev.utils.GLHelper
import com.suyf.mediadev.utils.PathUtils
import com.suyf.mediadev.view.BlockGLTextureView
import java.io.File

class L4RenderImageFBOActivity2 : AppCompatActivity() {
    private val choosePhotoCode = 0x1001
    private lateinit var seekBar: SeekBar
    private lateinit var blockGLTextureView: BlockGLTextureView
    private lateinit var glFboImageRender2: GLFBOImageRender2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render_image_fbo2)
        seekBar = findViewById<SeekBar>(R.id.seek_bar)
        blockGLTextureView = findViewById<BlockGLTextureView>(R.id.gl_view)
        glFboImageRender2 = GLFBOImageRender2(blockGLTextureView)
        blockGLTextureView.setRenderer(glFboImageRender2)
        initEvent()
    }

    private fun initEvent() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                glFboImageRender2.setColorRatio(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun saveImage(view: View) {
        blockGLTextureView.queueEvent { //run on GLThread
            val filename = Environment.getExternalStorageDirectory().absolutePath + "/fbo_${System.currentTimeMillis()}.png"
            File(filename).createNewFile()
            GLHelper.saveFrame(filename, blockGLTextureView.width, blockGLTextureView.height)
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
            glFboImageRender2.setBitMap(BitmapFactory.decodeFile(url))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blockGLTextureView.destroy()
    }
}