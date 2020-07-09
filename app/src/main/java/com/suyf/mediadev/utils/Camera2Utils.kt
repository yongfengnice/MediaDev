package com.suyf.mediadev.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object Camera2Utils {
    private var imageReader: ImageReader? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK

    fun setupCamera(context: Context, surfaceTexture: SurfaceTexture): CameraDevice? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        cameraManager.openCamera(getCameraId(cameraManager, cameraFacing) ?: "", object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startCaptureSession(surfaceTexture)
            }

            override fun onDisconnected(camera: CameraDevice) {
                GLHelper.printLog("onDisconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                GLHelper.printLog("CameraDevice onError")
                camera.close()
            }
        }, null)
        return cameraDevice
    }

    private fun startCaptureSession(surfaceTexture: SurfaceTexture) {
        //大小写死，可以传递进来
        imageReader = ImageReader.newInstance(1080, 1440, ImageFormat.YUV_420_888, 2).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader?.acquireNextImage() ?: return@setOnImageAvailableListener
                val planes = image.planes
                val yBuffer = planes[0].buffer
                val uBuffer = planes[1].buffer
                val vBuffer = planes[2].buffer
                image.close()
            }, null)
        }
        val surface = Surface(surfaceTexture)//用于预览
        cameraDevice?.createCaptureSession(listOf(imageReader!!.surface, surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                session.stopRepeating()
            }

            override fun onConfigured(session: CameraCaptureSession) {
                val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)!!
                builder.addTarget(imageReader!!.surface)
                builder.addTarget(surface)
                sendRepeatingRequest(session, builder.build())
            }
        }, null)

    }

    private fun sendRepeatingRequest(session: CameraCaptureSession, request: CaptureRequest) {
        session.setRepeatingRequest(request, object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                result
            }
        }, null)

//        session.capture()

    }

    private fun getCameraId(cameraManager: CameraManager?, facing: Int): String? {
        return cameraManager?.cameraIdList?.find { id ->
            cameraManager?.getCameraCharacteristics(id)?.get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

}