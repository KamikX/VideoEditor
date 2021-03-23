package com.video.trimmer.utils

import android.net.Uri
import android.util.Log
import com.arthenica.mobileffmpeg.*
import com.video.trimmer.interfaces.OnCropVideoListener
import com.video.trimmer.interfaces.OnTrimVideoListener

class VideoOptions {


    /**
     * Trim video
     */
    fun trimVideo(startPosition: String,
                  endPosition: String,
                  inputPath: String,
                  outputPath: String,
                  outputFileUri: Uri,
                  listener: OnTrimVideoListener?) {
        val command = arrayOf("-y", "-i", inputPath, "-ss", startPosition, "-to", endPosition, "-c", "copy", outputPath)

        // Log callback
        Config.enableLogCallback { logMessage ->
            Log.d(TAG, logMessage.text)
        }

        // Statistics callback
        Config.enableStatisticsCallback { newStatistics ->
            Log.d(Config.TAG, String.format("frame: %d, time: %d", newStatistics.videoFrameNumber, newStatistics.time))
            Log.d(TAG, "progress : $newStatistics")
        }
        Log.d(TAG, "Started crop command : ffmpeg " + command.contentToString())
        listener?.onTrimStarted()

        val executionId = FFmpeg.executeAsync(command) { _, returnCode ->

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.d(TAG, "Finished crop command : ffmpeg " + command.contentToString())
                    listener?.getResult(outputFileUri)
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.e(TAG, "Async crop command execution cancelled by user.")
                    listener?.cancelAction()
                }
                else -> {
                    Log.e(TAG, "Async crop command execution failed with returnCode=${returnCode}")
                    listener?.onError("Failed")
                }
            }
        }
        Log.e(TAG, "execFFmpegTrimVideo executionId-$executionId");

    }

    /**
     * Crop video
     */
    fun cropVideo(width: Int,
                  height: Int,
                  x: Int,
                  y: Int,
                  inputPath: String,
                  outputPath: String,
                  outputFileUri: Uri,
                  listener: OnCropVideoListener?, frameCount: Int) {
        val command = arrayOf("-i", inputPath, "-filter:v", "crop=$width:$height:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", outputPath)

        // Log callback
        Config.enableLogCallback { logMessage ->
            Log.d(TAG, logMessage.text)
        }

        // Statistics callback
        Config.enableStatisticsCallback { newStatistics ->
            Log.d(Config.TAG, String.format("frame: %d, time: %d", newStatistics.videoFrameNumber, newStatistics.time))
            Log.d(TAG, "progress : $newStatistics")
            val progress = (newStatistics.videoFrameNumber / frameCount.toFloat()) * 100f
            listener?.onProgress(progress)

        }
        Log.d(TAG, "Started crop command : ffmpeg " + command.contentToString())
        listener?.onCropStarted()

        val executionId = FFmpeg.executeAsync(command) { _, returnCode ->

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.d(TAG, "Finished crop command : ffmpeg " + command.contentToString())
                    listener?.getResult(outputFileUri)
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.e(TAG, "Async crop command execution cancelled by user.")
                    listener?.cancelAction()
                }
                else -> {
                    Log.e(TAG, "Async crop command execution failed with returnCode=${returnCode}")
                    listener?.onError("Failed")
                }
            }
        }
        Log.e(TAG, "execFFmpegCropVideo executionId-$executionId");

    }

    companion object {
        const val TAG = "VideoOptions"
    }

}