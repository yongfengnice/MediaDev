package com.suyf.mediadev.codec

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.view.Surface
import com.suyf.mediadev.R
import com.suyf.mediadev.utils.GLHelper
import java.nio.ByteBuffer
import kotlin.math.max

//这里使用同步API，并且在主线程进行，实际需要开线程处理，并且最好使用异步API
@Suppress("DEPRECATION")
class MediaDecoder {
    private val mediaExtractor by lazy { MediaExtractor() }//流提取器
    private var mediaFormat: MediaFormat? = null
    private var videoDecoder: MediaCodec? = null
    private lateinit var inputBuffers: Array<out ByteBuffer>
    private lateinit var outputBuffers: Array<out ByteBuffer>
    private val inputBufferIndices: MutableList<Int> = mutableListOf()
    private val outputBufferIndices: MutableList<Int> = mutableListOf()

    //查找解码器
    fun findVideoDecoder(context: Context): Boolean {
        val videoUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.video)
        mediaExtractor.setDataSource(context, videoUri, null)
        val trackCount = mediaExtractor.trackCount
        for (index in 0 until trackCount) {
            mediaExtractor.unselectTrack(index)
        }
        for (index in 0 until trackCount) {
            mediaFormat = mediaExtractor.getTrackFormat(index)
            val codecType: String? = mediaFormat?.getString(MediaFormat.KEY_MIME)
            if (codecType?.contains("video/", true) == true) {
                mediaExtractor.selectTrack(index)
                videoDecoder = MediaCodec.createDecoderByType(codecType)
                return videoDecoder != null
            }
        }
        return false
    }

    //配置解码器
    fun config(surface: Surface) {
        val dec = videoDecoder ?: return
        dec.configure(mediaFormat, surface, null, 0)
        dec.start()
        inputBuffers = dec.inputBuffers
        outputBuffers = dec.outputBuffers
    }

    //从VideoExtractor读取数据包，送往codec解码
    fun writeSampleData(): Boolean {
        var result = false
        val dec = videoDecoder ?: return result
        //将可用的空inputBuffer出队列
        var indices = dec.dequeueInputBuffer(0)
        while (indices != MediaCodec.INFO_TRY_AGAIN_LATER) {
            inputBufferIndices.add(indices)
            indices = dec.dequeueInputBuffer(0)
        }
        //取出对应的空inputBuffer然后填充数据
        if (inputBufferIndices.isNotEmpty()) {
            val index = inputBufferIndices.removeAt(0)
            val byteBuffer = inputBuffers[index]
            val size = mediaExtractor.readSampleData(byteBuffer, 0)
            val flag = if (size == -1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else mediaExtractor.sampleFlags
            dec.queueInputBuffer(index, 0, max(size, 0), mediaExtractor.sampleTime, flag)//填充数据后送往解码器
            result = size != -1
        }
        if (result) {
            mediaExtractor.advance()
        }
        return result
    }

    private fun updateOutputBuffer() {
        val dec = videoDecoder ?: return
        val info = MediaCodec.BufferInfo()
        var indices = dec.dequeueOutputBuffer(info, 0)//将解码好的数据出队列
        while (indices != MediaCodec.INFO_TRY_AGAIN_LATER) {
            when (indices) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    GLHelper.printLog("updateOutputBuffer INFO_OUTPUT_FORMAT_CHANGED...")
                }
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    GLHelper.printLog("updateOutputBuffer INFO_OUTPUT_BUFFERS_CHANGED...")
                    outputBuffers = dec.outputBuffers
                    outputBufferIndices.clear()
                }
                else -> {
                    if (indices >= 0) {
                        outputBufferIndices.add(indices)
                    } else {
                        GLHelper.printLog("updateOutputBuffer error...")
                    }
                }
            }
            indices = dec.dequeueOutputBuffer(info, 0)
        }
    }

    fun popOutputBufferData(): Boolean {
        updateOutputBuffer()
        val dec = videoDecoder ?: return false
        if (outputBufferIndices.isNotEmpty()) {
            val index = outputBufferIndices.removeAt(0)
            dec.releaseOutputBuffer(index, true)//使用后释放buffer还给解码器
            return true
        }
        return false
    }

    fun isEndOfFile(): Boolean {
        return (mediaExtractor.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM
    }

    fun stopAndRelease() {
        val dec = videoDecoder ?: return
        dec.stop()
        dec.release()
        mediaExtractor.release()
        videoDecoder = null
    }
}