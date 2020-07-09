package com.suyf.mediadev.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.suyf.mediadev.R
import com.suyf.mediadev.utils.AssetUtils
import com.suyf.mediadev.utils.GLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLAdjustImageRender(private val surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    companion object {
        private const val GL_MATRIX_SIZE = 16
    }

    private var context: Context = surfaceView.context
    private var programId = 0
    private var aPositionLocation = 0
    private var aTextureCoordinateLocation = 0
    private var uMVPMatrixLocation = 0
    private var vTextureLocation = 0

    private var mBitmapUpdate = true
    private var mBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.long_width)
    private var mTextureId = GLHelper.NO_TEXTURE
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    private val glVertexArray = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )
    private var glVertexBuffer: FloatBuffer

    private val glTextureArray = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )
    private var glTextureBuffer: FloatBuffer

    private var glMVPMatrixArray = FloatArray(GL_MATRIX_SIZE)

    init {
        //主线程，非GLThread线程
        glVertexBuffer = ByteBuffer.allocateDirect(glVertexArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        glVertexBuffer.put(glVertexArray).position(0)

        glTextureBuffer = ByteBuffer.allocateDirect(glTextureArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        glTextureBuffer.put(glTextureArray).position(0)
    }

    //GLThread线程
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //必须在GLThread线程完成
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        //加载程序，并查找变量位置，必须在GLThread线程完成
        val vertexSrc = AssetUtils.loadShaderSrc(context, "image_adjust_vertex.glsl")
        val fragmentSrc = AssetUtils.loadShaderSrc(context, "image_adjust_frag.glsl")
        programId = GLHelper.loadProgram(vertexSrc, fragmentSrc)
        aPositionLocation = GLES20.glGetAttribLocation(programId, "aPosition")
        aTextureCoordinateLocation = GLES20.glGetAttribLocation(programId, "aTextureCoordinate")
        uMVPMatrixLocation = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        vTextureLocation = GLES20.glGetUniformLocation(programId, "vTexture")
    }

    fun setBitMap(bitmap: Bitmap) {
        mBitmap = bitmap
        mBitmapUpdate = true
        adjustMVPMatrix()
        surfaceView.requestRender()
    }

    //GLThread线程
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        GLES20.glViewport(0, 0, width, height)
        adjustMVPMatrix()
    }

    private fun adjustMVPMatrix() {
        if (surfaceWidth <= 0 || surfaceHeight <= 0 || mBitmap.width <= 0 || mBitmap.height <= 0) {
            return
        }
        val wRatio = mBitmap.width * 1f / surfaceWidth
        val hRatio = mBitmap.height * 1f / surfaceHeight
        GLHelper.printLog("widthRatio:$wRatio,hRatio:$hRatio")

        val xyScale = 1f//可以根据手指触摸事件缩放图片
        Matrix.setIdentityM(glMVPMatrixArray, 0)
        if (wRatio > 1f || hRatio > 1f) {//图片宽度大于屏幕，或者高度大于屏幕
            when {
                wRatio > 1f && hRatio > 1f -> {//大长高图，图片宽度大于View宽度，并且图片高度大于View高度
                    if (wRatio > hRatio) {//宽度比例比高度比例大  3.3 : 1.1
                        Matrix.scaleM(glMVPMatrixArray, 0, 1f * xyScale, hRatio / wRatio * xyScale, 1f)
                    } else {
                        Matrix.scaleM(glMVPMatrixArray, 0, wRatio / hRatio * xyScale, 1f * xyScale, 1f)
                    }
                }
                wRatio > 1f -> {//大长图，图片宽度大于View宽度
                    Matrix.scaleM(glMVPMatrixArray, 0, 1f * xyScale, hRatio / wRatio * xyScale, 1f)
                }
                hRatio > 1f -> {//大高图，图片高度大于View高度
                    Matrix.scaleM(glMVPMatrixArray, 0, wRatio / hRatio * xyScale, 1f * xyScale, 1f)
                }
            }
        } else {//小图，图片宽度小于View宽度，并且图片高度小于View高度
            Matrix.scaleM(glMVPMatrixArray, 0, wRatio * xyScale, hRatio * xyScale, 0f)
        }
    }

    //GLThread线程
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        //1.使用程序
        GLES20.glUseProgram(programId)
        //2.给变量赋值
        glVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, glVertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        glTextureBuffer.position(0)
        GLES20.glVertexAttribPointer(aTextureCoordinateLocation, 2, GLES20.GL_FLOAT, false, 0, glTextureBuffer)
        GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, glMVPMatrixArray, 0)

        //3.激活纹理目标，指定需要绘制的纹理
        if (mBitmapUpdate) {
            mBitmapUpdate = false
            mTextureId = GLHelper.loadTexture(mBitmap, mTextureId, true)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLES20.glUniform1i(vTextureLocation, 1)//这个1要和GLES20.GL_TEXTURE1的1一致

        //4.绘制纹理
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //5.各种解绑操作
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

}