package com.cheenx.demo.scankitdemo

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import com.cheenx.demo.scankitdemo.ScanCodeActivity.Companion.SCAN_PAGE_TITLE
import com.cheenx.demo.scankitdemo.ScanCodeActivity.Companion.SCAN_TYPE
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan

class MainActivity : AppCompatActivity() {

    val CAMERA_BAR_CODE = 1
    val CAMERA_QR_CODE = 2


    val REQUEST_CODE_SCAN = 0x012

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.scan_bar).setOnClickListener {
            checkCameraPermission(CAMERA_BAR_CODE)
        }

        findViewById<Button>(R.id.scan_qrcode).setOnClickListener {
            checkCameraPermission(CAMERA_QR_CODE)
        }

        findViewById<Button>(R.id.gene_qrcode).setOnClickListener {
            geneQrCode()
        }
    }

    private fun geneQrCode() {
        val width = 400
        val height = 400
        val options = HmsBuildBitmapOption.Creator()
            .setBitmapColor(Color.BLACK)
            .setQRErrorCorrection(HmsBuildBitmapOption.ErrorCorrectionLevel.Q)
            .setQRLogoBitmap(resources.getDrawable(R.mipmap.ic_launcher).toBitmapOrNull()).create()
        try {
            // 如果未设置HmsBuildBitmapOption对象，生成二维码参数options置null
            val qrBitmap = ScanUtil.buildBitmap(
                "resources.getDrawable(R.mipmap.ic_launcher).toBitmapOrNull()",
                HmsScan.QRCODE_SCAN_TYPE,
                width,
                height,
                options
            )
            findViewById<ImageView>(R.id.qrcode_iv).setImageBitmap(qrBitmap)
        } catch (e: Exception) {

        }
    }

    private fun checkCameraPermission(code: Int) {
        // CAMERA_REQ_CODE为用户自定义，用于接收权限校验结果的请求码
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), code)
    }

    // 实现“onRequestPermissionsResult”函数接收校验权限结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 判断“requestCode”是否为申请权限时设置请求码CAMERA_REQ_CODE，然后校验权限开启状态
        if (requestCode == CAMERA_BAR_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 调用扫码接口，构建扫码能力 条形码
            startScanBar()
        } else if (requestCode == CAMERA_QR_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 调用扫码接口，构建扫码能力 条形码
            startScanQrcode()
        }
    }

    private fun startScanQrcode() {
        val intent = Intent(this, ScanCodeActivity::class.java)
        intent.putExtra(SCAN_TYPE, 2)
        intent.putExtra(SCAN_PAGE_TITLE, "扫描二维码")
        this.startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    private fun startScanBar() {
        val intent = Intent(this, ScanCodeActivity::class.java)
        intent.putExtra(SCAN_TYPE, 1)
        intent.putExtra(SCAN_PAGE_TITLE, "扫描条形码")
        this.startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SCAN && data != null) {
            val errorCode: Int = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS)
            if (errorCode == ScanUtil.ERROR_NO_READ_PERMISSION) {
                checkCameraPermission(CAMERA_BAR_CODE)
            } else if (errorCode == ScanUtil.SUCCESS) {
                val hmsScan: HmsScan? = data.getParcelableExtra(ScanUtil.RESULT)
                if (hmsScan?.getOriginalValue()?.isNotEmpty() == true) {
                    findViewById<TextView>(R.id.scan_text).text = hmsScan.getOriginalValue()
                }
            }
        }
    }
}