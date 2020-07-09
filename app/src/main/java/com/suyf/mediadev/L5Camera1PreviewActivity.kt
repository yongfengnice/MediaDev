package com.suyf.mediadev

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.suyf.mediadev.render.GLSimpleCameraRender
import com.suyf.mediadev.utils.Camera1Utils
import com.suyf.mediadev.utils.GLHelper
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Suppress("DEPRECATION")
class L5Camera1PreviewActivity : AppCompatActivity() {
    private lateinit var glSimpleCameraRender: GLSimpleCameraRender
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var surfaceTexture: SurfaceTexture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_camera_preview)
        glSurfaceView = findViewById<GLSurfaceView>(R.id.gl_view)
        glSurfaceView.setEGLContextClientVersion(2)
        glSimpleCameraRender = GLSimpleCameraRender(glSurfaceView)
        glSurfaceView.setRenderer(glSimpleCameraRender)
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
        val camera = Camera1Utils.setupCamera()
        camera?.setPreviewTexture(surfaceTexture)
        camera?.startPreview()
    }

    override fun onResume() {
        super.onResume()
        Camera1Utils.cameraInstance?.reconnect()
        Camera1Utils.cameraInstance?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        Camera1Utils.cameraInstance?.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        Camera1Utils.releaseCamera()
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