package com.suyf.mediadev.utils

import android.content.Context
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object AssetUtils {

    fun loadShaderSrc(context: Context, filename: String): String {
        val input = context.assets.open(filename)
        val output = ByteArrayOutputStream()
        var ch = input.read()
        while (ch != -1) {
            output.write(ch)
            ch = input.read()
        }
        val buffer = output.toByteArray()
        output.close()
        input.close()
        var result = String(buffer, Charset.forName("UTF-8"))
        result = result.replace("\\r\\n", "\n")
        return result
    }
}