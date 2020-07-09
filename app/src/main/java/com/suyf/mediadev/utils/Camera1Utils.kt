package com.suyf.mediadev.utils

import android.hardware.Camera
import android.hardware.Camera.CameraInfo

@Suppress("DEPRECATION")
object Camera1Utils {
    var cameraId = 0
    var cameraInstance: Camera? = null
    private var cameraFacing: Int = CameraInfo.CAMERA_FACING_BACK

    private var mWidth = 1280
    private var mHeight = 960

    private fun getCurrentCameraId(): Int {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = CameraInfo()
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraFacing) {
                return i
            }
        }
        return 0
    }

    fun setupCamera(): Camera? {
        cameraId = getCurrentCameraId()
        cameraInstance = Camera.open(cameraId)
        val camera = cameraInstance ?: return null
        setupParameters(camera)
        return camera
    }

    fun releaseCamera() {
        cameraInstance?.stopPreview()
        cameraInstance?.release()
        cameraInstance = null
    }

    private fun setupParameters(camera: Camera) {
        val parameters = camera.parameters
        val closestSize = findClosestSize(mWidth, mHeight, parameters)
        mWidth = closestSize[0]
        mHeight = closestSize[1]
        parameters.setPreviewSize(mWidth, mHeight)
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        camera.parameters = parameters
    }

    private fun findClosestSize(width: Int, height: Int, parameters: Camera.Parameters): IntArray {
        val previewSizes = parameters.supportedPreviewSizes
        var closestWidth = -1
        var closestHeight = -1
        var smallestWidth = previewSizes[0].width
        var smallestHeight = previewSizes[0].height
        for (size in previewSizes) {
            if (size.width <= width && size.height <= height && size.width >= closestWidth && size.height >= closestHeight) {
                closestWidth = size.width
                closestHeight = size.height
            }
            if (size.width < smallestWidth && size.height < smallestHeight) {
                smallestWidth = size.width
                smallestHeight = size.height
            }
        }
        if (closestWidth == -1) {
            closestWidth = smallestWidth
            closestHeight = smallestHeight
        }
        return intArrayOf(closestWidth, closestHeight)
    }

}