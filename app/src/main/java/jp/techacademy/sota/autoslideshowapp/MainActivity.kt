package jp.techacademy.sota.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.util.Log

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    private var mHandler = Handler()

    private var mCursor: Cursor? = null

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("ANDROID", "ACCESS DENIED")
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*パーミッションの許可状態を確認し、許可されていればギャラリーの情報を取得し、最初の画像を表示する*/
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getFirstContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getFirstContentsInfo()
        }

        /*「進む」ボタンが押されたら、次の画像を表示する*/
        next_button.setOnClickListener {
            getNextContentsInfo()
        }

        /*「戻る」ボタンが押されたら、前の画像を表示する*/
        back_button.setOnClickListener {
            getBackContentsInfo()
        }

        /* 自動再生モード */
        auto_button.setOnClickListener {
            //「再生」ボタンを押した場合、自動再生が開始される
            if (mTimer == null){
                auto_button.text = "停止"
                next_button.isClickable = false
                back_button.isClickable = false
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            getNextContentsInfo()
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定
            }else{
                //「停止」ボタンを押した場合、自動再生が開始される
                auto_button.text = "再生"
                next_button.isClickable = true
                back_button.isClickable = true
                mTimer!!.cancel()
                mTimer = null
            }
        }
    }

    private fun getImage() {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = mCursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = mCursor!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    }

    /*ギャラリー情報を取得し、最初の画像を表示する*/
    private fun getFirstContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        mCursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        //最初の画像をURIを取得
        if (mCursor!!.moveToFirst()) {
            getImage()
        }
    }

    /*次の画像を表示する*/
    private fun getNextContentsInfo() {
        // 次の画像の情報を取得する
        if (mCursor!!.moveToNext()) {
            getImage()
        } else {
            // 次の画像が存在しない時は、最初の画像の情報を取得する
            mCursor!!.moveToFirst()
            getImage()
        }
    }

    /*前の画像を表示する*/
    private fun getBackContentsInfo(){
            // 前の画像の情報を取得する
        if (mCursor!!.moveToPrevious()) {
            getImage()
        } else {
            // 前の画像が存在しない時は、最後の画像の情報を取得する
            mCursor!!.moveToLast()
            getImage()
        }
    }

}

