package com.ai.face

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ai.face.FaceAIConfig.CACHE_BASE_FACE_DIR
import com.ai.face.addFaceImage.AddFaceImageActivity
import com.ai.face.databinding.ActivityFaceAiNaviBinding
import com.ai.face.search.SearchNaviActivity
import com.ai.face.utils.SystemUtil
import com.ai.face.utils.VoicePlayer
import com.ai.face.verify.FaceVerificationActivity
import com.ai.face.verify.FaceVerificationActivity.BASE_FACE_DIR_KEY
import com.ai.face.verify.FaceVerificationActivity.USER_FACE_ID_KEY
import com.ai.face.verify.TwoFaceImageVerifyActivity
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks


/**
 * SDK 接入演示Demo，请先熟悉本Demo跑通住流程后再集成到你的主工程验证业务
 *
 */
class FaceAINaviActivity : AppCompatActivity(), PermissionCallbacks {
    private lateinit var viewBinding: ActivityFaceAiNaviBinding

    private var yourUniQueFaceId = "18707611416"  //用户人脸ID，Face ID（unique）eg account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityFaceAiNaviBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        checkNeededPermission()

        //人脸图保存路径初始化
        FaceAIConfig.init(this)
        //语音提示
        VoicePlayer.getInstance().init(this)


        viewBinding.faceVerifyCard.setOnLongClickListener {
            startActivity(
                Intent(this@FaceAINaviActivity, FaceVerificationActivity::class.java)
                    .putExtra(USER_FACE_ID_KEY, yourUniQueFaceId)     //1:1 底片人脸ID
                    .putExtra(BASE_FACE_DIR_KEY, CACHE_BASE_FACE_DIR) //保存目录
            )
            true
        }


        viewBinding.faceVerifyCard.setOnClickListener {
            //1:1 人脸识别先录一张人脸底片
            val baseFacePath=CACHE_BASE_FACE_DIR+yourUniQueFaceId
            if (BitmapFactory.decodeFile(baseFacePath) != null) {
                startActivity(
                    Intent(this@FaceAINaviActivity, FaceVerificationActivity::class.java)
                        .putExtra(USER_FACE_ID_KEY, yourUniQueFaceId)     //1:1 底片人脸ID
                        .putExtra(BASE_FACE_DIR_KEY, CACHE_BASE_FACE_DIR) //保存目录
                )
            } else {
                Toast.makeText(this@FaceAINaviActivity, R.string.add_a_face_image, Toast.LENGTH_LONG).show()
            }
        }


        //添加1：1人脸识别底片
        viewBinding.updateBaseImage.setOnClickListener {
            startActivity(
                Intent(this@FaceAINaviActivity, AddFaceImageActivity::class.java)
                    .putExtra(USER_FACE_ID_KEY, yourUniQueFaceId)
                    .putExtra(BASE_FACE_DIR_KEY, CACHE_BASE_FACE_DIR)
            )
        }



        viewBinding.faceSearch1N.setOnClickListener {
            startActivity(Intent(this@FaceAINaviActivity, SearchNaviActivity::class.java))
        }


        viewBinding.moreAboutMe.setOnClickListener {
            startActivity(Intent(this@FaceAINaviActivity, AboutFaceAppActivity::class.java))
        }


        //两张人脸图 对比
        viewBinding.twoFaceVerify.setOnClickListener {
            startActivity(
                Intent(this@FaceAINaviActivity, TwoFaceImageVerifyActivity::class.java)
            )
        }


        viewBinding.changeCamera.setOnClickListener {
            val sharedPref = getSharedPreferences(
                "faceVerify", Context.MODE_PRIVATE
            )

            if (sharedPref.getInt("cameraFlag", 0) == 1) {
                sharedPref.edit().putInt("cameraFlag", 0).apply()
                Toast.makeText(
                    baseContext,
                    "Front camera",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                sharedPref.edit().putInt("cameraFlag", 1).apply()
                Toast.makeText(
                    baseContext,
                    "Back/USB Camera",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        showSystemParameter()

    }


    /**
     * 统一全局的拦截权限请求，给提示
     *
     */
    private fun checkNeededPermission() {
        //自行管理你的存储 相机权限
        //存储照片在某些目录需要,Manifest.permission.WRITE_EXTERNAL_STORAGE
        val perms = arrayOf(Manifest.permission.CAMERA)

        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                this,
                "SDK Demo 相机和读取相册都仅仅是为了完成人脸识别所必需，请授权！",
                11,
                *perms
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }


    /**
     * 当用户点击了不再提醒的时候的处理方式
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Please Oauth Permission,请授权才能正常演示", Toast.LENGTH_SHORT).show()
    }


    private fun showSystemParameter() {
        val TAG = "系统参数："
        Log.e(TAG, "手机厂商：" + SystemUtil.getDeviceBrand())
        Log.e(TAG, "手机型号：" + SystemUtil.getSystemModel())
        Log.e(TAG, "Android系统版本号：" + SystemUtil.getSystemVersion())
    }

}