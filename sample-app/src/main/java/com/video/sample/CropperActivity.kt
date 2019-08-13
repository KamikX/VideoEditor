package com.video.sample

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import kotlinx.android.synthetic.main.activity_cropper.*
import kotlinx.android.synthetic.main.activity_cropper.back
import kotlinx.android.synthetic.main.activity_cropper.save
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File

class CropperActivity : AppCompatActivity(), OnTrimVideoListener, OnVideoListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)

        setupPermissions {
            val extraIntent = intent
            var path = ""
            if (extraIntent != null) path = extraIntent.getStringExtra(MainActivity.EXTRA_VIDEO_PATH)
            videoCropper.setTextTimeSelectionTypeface(FontsHelper[this, FontsConstants.SEMIBOLD])
                    .setTextTimeTypeface(FontsHelper[this, FontsConstants.SEMIBOLD])
                    .setOnTrimVideoListener(this)
                    .setOnVideoListener(this)
                    .setVideoURI(Uri.parse(path))
                    .setVideoInformationVisibility(true)
                    .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "Zoho Social" + File.separator + "Videos" + File.separator)
        }

        back.setOnClickListener {
            videoCropper.onCancelClicked()
        }

        save.setOnClickListener {
            videoCropper.onSaveClicked()
        }
    }

    override fun onTrimStarted() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Started Trimming", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getResult(uri: Uri) {
        RunOnUiThread(this).safely {
            RunOnUiThread(this).safely {
                Toast.makeText(this, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
            }
//            val id = ContentUris.parseId(getImageContentUri(File(uri.path)))
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, uri.path)
            values.put(MediaStore.Video.VideoColumns.DURATION, 2000)
            val id = ContentUris.parseId(contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values))
            Log.e("VIDEO ID", id.toString())
        }
    }

    override fun cancelAction() {
        RunOnUiThread(this).safely {
            videoTrimmer.destroy()
            finish()
        }
    }

    override fun onError(message: String) {
        Log.e("ERROR", message)
    }

    override fun onVideoPrepared() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }
    }

    lateinit var dothis: () -> Unit
    private fun setupPermissions(doSomething: () -> Unit) {
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        dothis = doSomething
        if (writePermission != PackageManager.PERMISSION_GRANTED && readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        } else dothis()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    PermissionsDialog(this@CropperActivity, "To continue, give Zoho Social access to your Photos.").show()
                } else dothis()
            }
        }
    }
}