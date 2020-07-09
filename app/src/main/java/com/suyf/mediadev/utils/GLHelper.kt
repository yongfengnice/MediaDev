package com.suyf.mediadev.utils

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10


object GLHelper {
    const val GL_MATRIX_SIZE = 16
    private const val TAG = "OpenGLUtils"
    const val NO_TEXTURE = -1

    private fun loadShader(vertexSrc: String, type: Int): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, vertexSrc)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.d(TAG, "Load Shader Failed:${GLES20.glGetShaderInfoLog(shader)}")
            return 0
        }
        return shader
    }

    fun loadProgram(vertexSrc: String, fragmentSrc: String): Int {
        val vertexShader = loadShader(vertexSrc, GLES20.GL_VERTEX_SHADER)
        if (vertexShader == 0) {
            Log.d(TAG, "Vertex Shader Failed")
            return 0
        }
        val fragmentShader = loadShader(fragmentSrc, GLES20.GL_FRAGMENT_SHADER)
        if (fragmentShader == 0) {
            Log.d(TAG, "Fragment Shader Failed")
            return 0
        }
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val link = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d(TAG, "Load Program Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        return program
    }

    fun loadTexture(bitmap: Bitmap, usedTextureId: Int, isRecycled: Boolean): Int {
        if (bitmap.isRecycled) {
            return usedTextureId
        }
        val textures = IntArray(1)
        if (usedTextureId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        } else {
            textures[0] = usedTextureId
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        if (isRecycled && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        printLog("create texture:id-->${textures[0]}")
        return textures[0]
    }

    fun createCameraTexture(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return texture[0]
    }

    fun saveFrame(filename: String, width: Int, height: Int) {
        val startTime = System.currentTimeMillis()
        //1.glReadPixels返回的是大端的RGBA Byte组数，我们使用小端Buffer接收得到ABGR Byte组数
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        buffer.rewind()//reset position
        val pixelCount = width * height
        val colors = IntArray(pixelCount)
        buffer.asIntBuffer().get(colors)
        for (i in 0 until pixelCount) {
            val c = colors[i]   //2.每个int类型的c是接收到的ABGR，但bitmap需要ARGB格式，所以需要交换B和R的位置
            colors[i] = c and -0xff0100 or (c and 0x00ff0000 shr 16) or (c and 0x000000ff shl 16) //交换B和R，得到ARGB
        }
        //上下翻转
        for (y in 0 until height / 2) {
            for (x in 0 until width) {
                val temp: Int = colors[(height - y - 1) * width + x]
                colors[(height - y - 1) * width + x] = colors[y * width + x]
                colors[y * width + x] = temp
            }
        }
        //写入文件
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(filename)
            val bmp = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos)
            bmp.recycle()
        } catch (ioe: IOException) {
            throw RuntimeException("Failed to write file $filename", ioe)
        } finally {
            try {
                fos?.close()
            } catch (ioe2: IOException) {
                throw RuntimeException("Failed to close file $filename", ioe2)
            }
        }
        val endTime = System.currentTimeMillis()
        Log.d(TAG, "Saved duration:" + (endTime - startTime) + "ms -> " + width + "x" + height + " frame as '" + filename + "'")
    }

    fun printLog(str: String) {
        Log.d(TAG, str)
    }

}