package com.cheenx.demo.scankitdemo

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import java.io.IOException


class ScanCodeActivity : AppCompatActivity() {

    companion object {
        const val SCAN_RESULT_CODE = "SCAN_RESULT_CODE"
        const val SCAN_RESULT = "SCAN_RESULT"
        const val SCAN_TYPE = "SCAN_TYPE"
        const val SCAN_PAGE_TITLE = "SCAN_PAGE_TITLE"
        const val SCAN_BEEP_PLAY = "SCAN_BEEP_PLAY"
        const val SCAN_FRAME_SIZE = 240
        const val SCAN_BAR_FRAME_WIDTH = 300
        const val SCAN_BAR_FRAME_HEIGHT = 120
        const val REQUEST_CODE_PHOTO = 0X1113
    }

    private var frameLayout: FrameLayout? = null
    private var remoteView: RemoteView? = null
    private var flushBtn: ImageView? = null
    private var flushDesc: TextView? = null
    private var beepManager: ScanBeep? = null
    private var scanView: ScanView? = null

    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var type = 0 // 0: ALL 1: BARCode 2:QRCode
    private var title = "扫描二维码/条形码"
    private var playBeep = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        type = intent.extras?.getInt(SCAN_TYPE, 1) ?: 1
        title = intent.extras?.getString(SCAN_PAGE_TITLE, title) ?: title
        playBeep = intent.extras?.getBoolean(SCAN_BEEP_PLAY, true) ?: true
        setContentView(R.layout.activity_scan_code)


        beepManager = ScanBeep(this)
        findViewById<TextView>(R.id.scan_code_title).text = title
        // Bind the camera preview screen. 绑定相机预览界面
        frameLayout = findViewById(R.id.rim)
        //1. Obtain the screen density to calculate the viewfinder's rectangle.
        //1. 获取屏幕密度以计算取景器的矩形。
        val dm = getResources().displayMetrics
        val density = dm.density
        //2. Obtain the screen size.
        //2. 获取屏幕尺寸。
        mScreenWidth = getResources().displayMetrics.widthPixels
        mScreenHeight = getResources().displayMetrics.heightPixels

        when (type) {
            0 -> {
                val scanFrameSize = (SCAN_FRAME_SIZE * density)

                //3. Calculate the viewfinder's rectangle, which in the middle of the layout.
                //3. 计算取景器的矩形，它位于布局的中间。
                //Set the scanning area. (Optional. Rect can be null. If no settings are specified, it will be located in the middle of the layout.)
                //设置扫描区域。(可选。Rect 可以为空。如果未指定任何设置，扫描区域将位于布局的中间）。
                val rect = Rect()
                rect.left = (mScreenWidth / 2 - scanFrameSize / 2).toInt()
                rect.right = (mScreenWidth / 2 + scanFrameSize / 2).toInt()
                rect.top = (mScreenHeight / 2 - scanFrameSize / 2).toInt()
                rect.bottom = (mScreenHeight / 2 + scanFrameSize / 2).toInt()

                //Initialize the RemoteView instance, and set callback for the scanning result.
                //初始化 RemoteView 实例，并为扫描结果设置回调。
                remoteView = RemoteView.Builder().setContext(this)
                    .setBoundingBox(rect)
                    .setFormat(HmsScan.ALL_SCAN_TYPE).build()

                scanView = findViewById(R.id.scan_area)

            }

            1 -> {
                val scanFrameWidth = (SCAN_BAR_FRAME_WIDTH * density)
                val scanFrameHeight = (SCAN_BAR_FRAME_HEIGHT * density)

                val rect = Rect()
                rect.left = (mScreenWidth / 2 - scanFrameWidth / 2).toInt()
                rect.right = (mScreenWidth / 2 + scanFrameWidth / 2).toInt()
                rect.top = (mScreenHeight / 2 - scanFrameHeight / 2).toInt()
                rect.bottom = (mScreenHeight / 2 + scanFrameHeight / 2).toInt()

                remoteView = RemoteView.Builder().setContext(this)
                    .setBoundingBox(rect)
                    .setFormat(
                        HmsScan.CODE39_SCAN_TYPE,
                        HmsScan.CODE93_SCAN_TYPE,
                        HmsScan.CODE128_SCAN_TYPE,
                        HmsScan.EAN8_SCAN_TYPE,
                        HmsScan.EAN13_SCAN_TYPE,
                        HmsScan.ITF14_SCAN_TYPE,
                        HmsScan.UPCCODE_A_SCAN_TYPE,
                        HmsScan.UPCCODE_E_SCAN_TYPE,
                        HmsScan.CODABAR_SCAN_TYPE,
                    ).build()

                scanView = findViewById(R.id.scan_bar_area)
            }

            2 -> {
                val scanFrameSize = (SCAN_FRAME_SIZE * density)

                //3. Calculate the viewfinder's rectangle, which in the middle of the layout.
                //3. 计算取景器的矩形，它位于布局的中间。
                //Set the scanning area. (Optional. Rect can be null. If no settings are specified, it will be located in the middle of the layout.)
                //设置扫描区域。(可选。Rect 可以为空。如果未指定任何设置，扫描区域将位于布局的中间）。
                val rect = Rect()
                rect.left = (mScreenWidth / 2 - scanFrameSize / 2).toInt()
                rect.right = (mScreenWidth / 2 + scanFrameSize / 2).toInt()
                rect.top = (mScreenHeight / 2 - scanFrameSize / 2).toInt()
                rect.bottom = (mScreenHeight / 2 + scanFrameSize / 2).toInt()

                //Initialize the RemoteView instance, and set callback for the scanning result.
                //初始化 RemoteView 实例，并为扫描结果设置回调。
                remoteView = RemoteView.Builder().setContext(this)
                    .setBoundingBox(rect)
                    .setFormat(
                        HmsScan.QRCODE_SCAN_TYPE,
                        HmsScan.AZTEC_SCAN_TYPE,
                        HmsScan.DATAMATRIX_SCAN_TYPE,
                        HmsScan.PDF417_SCAN_TYPE
                    ).build()

                scanView = findViewById(R.id.scan_area)
            }
        }

        remoteView?.setOnResultCallback { result ->
            if (result != null && result.isNotEmpty() && result[0] != null && !result[0].originalValue.isNullOrBlank()) {
                if (playBeep) {
                    //扫描成功播放声音滴一下，可根据需要自行确定什么时候播
                    beepManager?.playBeepSoundAndVibrate()
                }

                val intent = Intent()
                intent.putExtra(SCAN_RESULT_CODE, 0)
                intent.putExtra(SCAN_RESULT, result[0])
                setResult(RESULT_OK, intent)
                this@ScanCodeActivity.finish()
            }
        }

        remoteView?.setOnErrorCallback { errorCode ->
            val intent = Intent()
            intent.putExtra(SCAN_RESULT_CODE, errorCode)
            setResult(RESULT_OK, intent)
            this@ScanCodeActivity.finish()
        }

        flushBtn = findViewById(R.id.scan_flash)
        flushDesc = findViewById(R.id.scan_flash_desc)
        remoteView?.setOnLightVisibleCallback { visible ->
            flushBtn?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            flushDesc?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
        flushBtn?.setOnClickListener {
            remoteView?.switchLight()
            if (remoteView?.lightStatus == true) {
                flushBtn?.setImageResource(R.drawable.scan_code_light_on)
                flushDesc?.text = "轻点关闭"
            } else {
                flushBtn?.setImageResource(R.drawable.scan_code_light_off)
                flushDesc?.text = "轻点照亮"
            }
        }
        findViewById<ImageView>(R.id.scan_back).setOnClickListener {
            this.finish()
        }
        findViewById<ImageView>(R.id.scan_code_gallery).setOnClickListener {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            this.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
        }
        // Load the customized view to the activity.
        remoteView?.onCreate(savedInstanceState)
        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        frameLayout?.addView(remoteView, params)
        scanView?.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        remoteView?.onResume()
        scanView?.startScan()
    }

    override fun onPause() {
        super.onPause()
        remoteView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteView?.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        remoteView?.onStop()
        scanView?.stop()
        beepManager?.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
                val hmsScans = ScanUtil.decodeWithBitmap(
                    this,
                    bitmap,
                    HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
                )
                if (hmsScans != null && hmsScans.isNotEmpty() && hmsScans[0] != null && !TextUtils.isEmpty(
                        hmsScans[0]!!.getOriginalValue()
                    )
                ) {
                    val intent = Intent()
                    intent.putExtra(SCAN_RESULT_CODE, 0)
                    intent.putExtra(SCAN_RESULT, hmsScans[0])
                    setResult(RESULT_OK, intent)
                    this.finish()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}