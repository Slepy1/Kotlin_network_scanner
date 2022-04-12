package uk.ac.abertay.cmp309.networkscanner

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class FileHelper {//this class is used for saving and reading data from internal memory

    var parentContext: Context? = null
    fun saveToInternalStorage(filename: String?, data: String) {
        try {
            val fos = parentContext!!.openFileOutput(filename, Context.MODE_PRIVATE)
            fos.write(data.toByteArray())
            fos.close()
            Log.d("debug", "file saved")
        } catch (e: Exception) {
            Log.e("error", "failed trying to save - $e")
        }
    }

    fun loadFromInternalStorage(filename: String?): String? {
        var result = ""
        var fis: FileInputStream? = null
        try {
            fis = parentContext!!.openFileInput(filename)
            val reader = BufferedReader(InputStreamReader(fis))
            val builder = StringBuilder()
            var line: String? = null
            line = reader.readLine()
            while (line != null) {
                builder.append(line).append("\n")
                line = reader.readLine()
            }
            result = builder.toString()
            reader.close()
            fis.close()
            Log.d("debug", "file loaded")
            return result
        } catch (e: Exception) {
            Log.e("error", "failed trying to read from file - $e")
        }
        return "2000"
    }

    companion object {
        private var instance: FileHelper? = null
        fun getInstance(context: Context?): FileHelper? {
            if (instance == null) {
                instance = FileHelper()
            }
            instance!!.parentContext = context
            return instance
        }
    }

}