package com.example.camerakt

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    //val이 상수, var는 변수
    //1.카메라 사진 촬영 요청코드 정의
    val REQUEST_IMAGE_CAPTURE = 1

    //2.문자열 형태의 사진 경로 값
    lateinit var curPhotoPath : String
    //3.manifest에 카메라 권한 설정->tedPermission gradle에 추가->권한체크 메서드 정의
    //4.manifest에 파일프로파이더선언(takeCapture에서 사용할 파일 프로바이더)
    //5.카메라 사진촬영 메서드 정의(takeCapture)
    //6.이미지 파일 생성 함수 정의(createImageFile)
    //7.미리보기(레이아웃의 iv_profile이미지뷰에 사진 보여줌)(onActivityResult)
    //8.갤러리에 사진 저장(savePhoto)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //권한 체크 메서드
        setPermission()

        btn_camera.setOnClickListener {
            takeCapture()
        }
    }

    private fun takeCapture() {
        //기본 카메라 앱 실행(카메라 앱도 앱이기 때문에 인텐트로 실행)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            run {
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try{
                        createImageFile()
                    }catch (ex : IOException){
                        null
                    }
                    photoFile?.also {
                        val photoURI : Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.camerakt.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI)
                        //카메라도 액티비티이기때문에 startActivityForeResult로 띄워줌
                        startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }
    //이미지 파일 생성 함수
    private fun createImageFile(): File? {
        //사진 파일 이름을 날짜로 하기위한 변수
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //스토리지 지정
        val storageDir:File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //createTempFile로 임시로 파일을 만들어 onActivityResult에서 파일을 보여주고 savePhoto로 갤러리에 저장
        return File.createTempFile("JPEG_${timestamp}",".jpg",storageDir).apply {
            //절대 경로로 지정
            curPhotoPath = absolutePath
        }
    }

    //tedPermission 설정
    private fun setPermission() {
        val permission = object : PermissionListener{
            //권한 허용 됐을 경우
            override fun onPermissionGranted() {
                Toast.makeText(this@MainActivity,"권한이 허용되었습니다",Toast.LENGTH_LONG).show()
            }
            //권한 허용 안됐을 경우
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity,"권한이 거부되었습니다",Toast.LENGTH_LONG).show()
            }
        }
        //permission 구현
        TedPermission.with(this)
            .setPermissionListener(permission)
                //처음 권한 요청할 때의 문구
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.")
            //카메라권한/저장권한 넣음
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }

    //startActivityForResult를 통해 기본 카메라 앱으로부터 가져온 사진 결과 값
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        //이미지를 성공적으로 갖고 왔을 경우
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val bitmap: Bitmap
            //현재 사진이 있는 곳의 경로가 들어감
            val file = File(curPhotoPath)
            //안드로이드 9.0(파이 버전)보다 낮을 경우
            if(Build.VERSION.SDK_INT < 28){
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver,Uri.fromFile(file))
                iv_profile.setImageBitmap(bitmap)
            //안드로이드 9.0이상의 경우
            }else{
                val decode = ImageDecoder.createSource(
                    this.contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
                iv_profile.setImageBitmap(bitmap)
            }
            savePhoto(bitmap)
        }


    }

    //갤러리에 사진 저장하는 함수
    private fun savePhoto(bitmap: Bitmap) {
        //사진 폴더에 저장하기 위한 경로 선언
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/"

        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        val filename = "${timestamp}.jpeg"
        val folder = File(folderPath)
        
        //폴더 없으면 생성
        if (!folder.isDirectory){
            folder.mkdir()
        }
        //실제 저장 처리
        //output으로 최종 save구문
        val out = FileOutputStream(folderPath+ filename)
        //bitmap으로 압축
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        Toast.makeText(this, "사진이 앨범에 저장되었습니다",Toast.LENGTH_LONG).show()
    }
}