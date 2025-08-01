package com.faceAI.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.faceAI.demo.databinding.ActivityAboutFaceAppBinding


/**
 * 关于我们
 *
 */
class AboutFaceAppActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityAboutFaceAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAboutFaceAppBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.moreAboutMe.setOnClickListener {
            val uri = Uri.parse("https://mp.weixin.qq.com/s/_ro9zBfzAmkpazL-QAPi9w")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = uri
            startActivity(intent)
        }

        viewBinding.back.setOnClickListener {
            this.finish()
        }

        viewBinding.newAppCheck.setOnClickListener {
            val uri = Uri.parse("https://www.pgyer.com/faceVerify")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = uri
            startActivity(intent)
        }

        viewBinding.newAppCheck.setText("当前版本：${getVersionName(this)}  查看版本列表")

        viewBinding.whatapp.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("WhatApp", "+8618452365423234235")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        viewBinding.wechat.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("wechat", "FaceAISDK")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        viewBinding.email.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Creates a new text clip to put on the clipboard
            val clip: ClipData = ClipData.newPlainText("email", "FaceAISDK.Service@gmail.com")

            // Set the clipboard's primary clip. 复制
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()

            return@setOnLongClickListener true
        }

    }


    private fun getVersionName(context: Context): String? {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.versionName
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

}