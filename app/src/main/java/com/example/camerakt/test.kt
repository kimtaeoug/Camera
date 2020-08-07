package com.example.camerakt

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity.apply
import android.widget.Gallery
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat.apply

class test : AppCompatActivity() {
    private val Gallery_code:Int = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        //storage/emulated/0
        Log.e("directory", Environment.getExternalStorageDirectory().absolutePath)
        val path = getRealPathFromURI()
        println("=================================================================")
        println("path is ${path}")
        println("=================================================================")

    }
    //이미지 실제 경로 갖고 오기
    private fun getRealPathFromURI(): String {
        var columnIndex =0
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)!!
        if (cursor.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val imagePath = cursor.getString(columnIndex)
        return imagePath
    }
}