package com.suyf.mediadev

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.suyf.mediadev.codec.MediaCodecActivity
import com.suyf.mediadev.codec.MediaCodecESActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), 1
        )
    }

    fun renderImageSimple(view: View) {
        startActivity(Intent(this, L1RenderImageSimpleActivity::class.java))
    }

    fun renderImageAdjust(view: View) {
        startActivity(Intent(this, L2RenderImageAdjustActivity::class.java))
    }

    fun renderImageFilter(view: View) {
        startActivity(Intent(this, L3RenderImageFilterActivity::class.java))
    }

    fun fboImageFilter(view: View) {
        startActivity(Intent(this, L4RenderImageFBOActivity1::class.java))
    }

    fun fboImageFilter2(view: View) {
        startActivity(Intent(this, L4RenderImageFBOActivity2::class.java))
    }

    fun cameraPreview(view: View) {
        startActivity(Intent(this, L5Camera1PreviewActivity::class.java))
    }

    fun camera2Preview(view: View) {
        startActivity(Intent(this, L5Camera2PreviewActivity::class.java))
    }

    fun mediaCodec(view: View) {
        startActivity(Intent(this, MediaCodecActivity::class.java))
    }

    fun mediaCodecES(view: View) {
        startActivity(Intent(this, MediaCodecESActivity::class.java))
    }

}