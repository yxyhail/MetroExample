/*
 * Copyright 2019 yxyhail
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yxyhail.metroexample.utils

import android.content.Context

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


object FileUtil {

    fun copyAssetsFile(context: Context, fileName: String): File {
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        val file = File(context.filesDir, fileName)
        try {
            inputStream = context.assets.open(fileName)
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int = inputStream!!.read(buffer)
            while (len != -1) {
                outputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream!!.close()
                outputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return file
    }


    @Throws(RuntimeException::class)
    fun unZip(srcFile: File, destDirPath: String) {
        if (!srcFile.exists()) {
            return
        }
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(srcFile)
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.isDirectory) {
                    val dirPath = destDirPath + "/" + entry.name
                    val dir = File(dirPath)
                    dir.mkdirs()
                } else {
                    val targetFile = File(destDirPath + "/" + entry.name)
                    if (!targetFile.parentFile.exists()) {
                        targetFile.parentFile.mkdirs()
                    }
                    targetFile.createNewFile()
                    val inputS = zipFile.getInputStream(entry)
                    val fos = FileOutputStream(targetFile)
                    val buf = ByteArray(2048)
                    var len: Int = inputS.read(buf)
                    while (len != -1) {
                        fos.write(buf, 0, len)
                        len = inputS.read(buf)
                    }
                    fos.close()
                    inputS.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
