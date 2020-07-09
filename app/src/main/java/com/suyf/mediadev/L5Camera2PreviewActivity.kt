package com.suyf.mediadev

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLFilterCameraRender2
import com.suyf.mediadev.utils.Camera2Utils
import com.suyf.mediadev.utils.GLHelper
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class L5Camera2PreviewActivity : AppCompatActivity() {
    private lateinit var glSimpleCameraRender: GLFilterCameraRender2
    private lateinit var seekBar: SeekBar
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var surfaceTexture: SurfaceTexture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_camera_preview2)
        seekBar = findViewById<SeekBar>(R.id.seek_bar)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        glSurfaceView.setEGLContextClientVersion(2)
        glSimpleCameraRender = GLFilterCameraRender2(glSurfaceView)
        glSurfaceView.setRenderer(glSimpleCameraRender)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                glSimpleCameraRender.setColorRatio(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        glSimpleCameraRender.renderCallback = object : GLSurfaceView.Renderer {
            override fun onDrawFrame(gl: GL10?) {
                surfaceTexture.updateTexImage()
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                surfaceTexture.setDefaultBufferSize(width, height)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                initCamera()
            }
        }
    }

    private fun initCamera() {
        surfaceTexture = SurfaceTexture(glSimpleCameraRender.mTextureId)
        surfaceTexture.setOnFrameAvailableListener {
            GLHelper.printLog("setOnFrameAvailableListener:${System.currentTimeMillis()}")
            glSurfaceView.requestRender()
        }
        glSurfaceView.post {
            val cameraDevice = Camera2Utils.setupCamera(this, surfaceTexture)
        }
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

}