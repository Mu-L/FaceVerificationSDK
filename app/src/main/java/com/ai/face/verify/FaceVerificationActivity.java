package com.ai.face.verify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ai.face.MyFaceApplication;
import com.ai.face.R;
import com.ai.face.base.view.CameraXFragment;
import com.ai.face.base.view.FaceCoverView;
import com.ai.face.faceVerify.graphic.FaceTipsOverlay;
import com.ai.face.faceVerify.verify.FaceProcessBuilder;
import com.ai.face.faceVerify.verify.FaceVerifyUtils;
import com.ai.face.faceVerify.verify.ProcessCallBack;
import com.ai.face.faceVerify.verify.VerifyStatus.*;
import com.ai.face.faceVerify.verify.liveness.LivenessDetectionMode;
import com.ai.face.faceVerify.verify.liveness.LivenessType;
import com.ai.face.utils.VoicePlayer;
import java.io.File;

/**
 * 1：1 的人脸识别 + 动作活体检测 SDK 接入演示Demo 代码
 * <p>
 * 人脸图要求：
 * 1.尽量使用较高配置设备和摄像头，光线不好带上补光灯
 * 2.录入高质量的人脸图，人脸清晰，背景纯色（证件照输入目前优化中）
 * 3.光线环境好，检测的人脸无遮挡，化浓妆或佩戴墨镜口罩帽子等
 * 4.人脸照片要求300*300 裁剪好的仅含人脸的正方形照片，背景纯色
 *
 */
public class FaceVerificationActivity extends AppCompatActivity {
    private TextView tipsTextView, secondTipsTextView, scoreText;
    private FaceTipsOverlay faceTipsOverlay;
    private FaceCoverView faceCoverView;
    private final FaceVerifyUtils faceVerifyUtils = new FaceVerifyUtils();

    //静默活体检测要求 RGB 镜头 720p， 固定 30 帧，无拖影，RGB 镜头建议是宽动态
    private final float silentLivenessPassScore = 0.92f; //静默活体分数通过的阈值


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);
        setTitle("1:1 face verify");
        scoreText = findViewById(R.id.silent_Score);
        tipsTextView = findViewById(R.id.tips_view);
        secondTipsTextView = findViewById(R.id.second_tips_view);
        faceCoverView = findViewById(R.id.face_cover);
        faceTipsOverlay = findViewById(R.id.faceTips);
        findViewById(R.id.back).setOnClickListener(v -> {
            FaceVerificationActivity.this.finish();
        });


        int cameraLensFacing = getSharedPreferences("faceVerify", Context.MODE_PRIVATE)
                .getInt("cameraFlag", 0);

        /*
         * 1. Camera 的初始化。
         * 第一个参数0/1 指定前后摄像头；
         * 第二个参数linearZoom [0.001f,1.0f] 指定焦距，参考{@link CameraControl#setLinearZoom(float)}
         * 焦距拉远一点，人才会靠近屏幕，才会减轻杂乱背景的影响。定制设备的摄像头自行调教此参数
         */
        CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLensFacing, 0.005f);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_camerax, cameraXFragment).commit();


        //1:1 人脸对比，摄像头实时采集的人脸和预留的人脸底片对比。（动作活体人脸检测完成后开始1:1比对）
        //人脸底图要经过BaseImageDispose saveBaseImage处理，不是随便一张图能当底图！！！
        String yourUniQueFaceId = getIntent().getStringExtra(MyFaceApplication.USER_ID_KEY);

        File file = new File(MyFaceApplication.CACHE_BASE_FACE_DIR, yourUniQueFaceId);
        //baseBitmap 就是你要进行1:1 人脸识别对比的底图
        Bitmap baseBitmap = BitmapFactory.decodeFile(file.getPath());


        //这是注册人脸的方式
//        new BaseImageDispose(this).saveBaseImage(baseBitmap,FaceApplication.CACHE_BASE_FACE_DIR, yourUniQueFaceId,400);

        //1.初始化引擎，各种参数配置
        initFaceVerify(baseBitmap);

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            //防止在识别过程中关闭页面导致Crash
            if (!isDestroyed() && !isFinishing() && faceVerifyUtils != null) {
                //2.第二个参数是指圆形人脸框到屏幕边距，可加快裁剪图像和指定识别区域，设太大会裁剪掉人脸区域
                faceVerifyUtils.goVerify(imageProxy, faceCoverView.getMargin());
            }
        });
    }


    /**
     * 初始化认证引擎
     * <p>
     * 活体检测的使用需要你发送邮件申请，简要描述App名称，包名和功能简介到 anylife.zlb@gmail.com
     *
     * @param baseBitmap 1:1 人脸识别对比的底片，如果仅仅需要活体检测，可以把App logo Bitmap 当参数传入并忽略对比结果
     */
    private void initFaceVerify(Bitmap baseBitmap) {

        FaceProcessBuilder faceProcessBuilder = new FaceProcessBuilder.Builder(this)
                .setThreshold(0.88f)                    //阈值设置，范围限 [0.8 , 0.95] 识别可信度，也是识别灵敏度
                .setBaseBitmap(baseBitmap)              //1:1 人脸识别对比的底片，仅仅需要SDK活体检测可以忽略比对结果
                .setLivenessType(LivenessType.SILENT_MOTION)  //活体检测可以有静默活体，动作活体或者组合也可以不需要活体NONE
                .setLivenessDetectionMode(LivenessDetectionMode.FAST)//硬件配置低用FAST动作活体模式，否则用精确模式
                .setSilentLivenessThreshold(silentLivenessPassScore)     //静默活体阈值 [0.88,0.99]
                .setMotionLivenessStepSize(2)         //随机动作活体的步骤个数[1-2]，SILENT_MOTION和MOTION 才有效
                .setVerifyTimeOut(16)                 //活体检测支持设置超时时间 [9,22] 秒
                .setGraphicOverlay(faceTipsOverlay)   //正式环境请去除设置
                .setProcessCallBack(new ProcessCallBack() {

                    /**
                     * 1:1 人脸识别 活体检测 对比结束
                     *
                     * @param isMatched   true匹配成功（大于setThreshold）； false 与底片不是同一人
                     * @param similarity  与底片匹配的相似度值
                     * @param vipBitmap   识别完成的时候人脸实时图，仅授权用户会返回。可以拿这张图和你的服务器再次严格匹配
                     */
                    @Override
                    public void onVerifyMatched(boolean isMatched, float similarity, float silentLivenessScore, Bitmap vipBitmap) {
                        showVerifyResult(isMatched, similarity, silentLivenessScore);
                    }

                    //人脸识别，活体检测过程中的各种提示
                    @Override
                    public void onProcessTips(int i) {
                        showFaceVerifyTips(i);
                    }

                    //动作活体检测时间限制倒计时百分比
                    @Override
                    public void onTimeCountDown(float percent) {
                        faceCoverView.startCountDown(percent);
                    }


                    @Override
                    public void onFailed(int i) {
                        //预留
                    }

                }).create();

        faceVerifyUtils.setDetectorParams(faceProcessBuilder);
    }

    /**
     * 检测1:1 人脸识别是否通过
     *
     * 动作活体要有动作配合，必须先动作匹配通过再1：1 匹配
     * 静默活体不需要人配合，如果不需要静默活体检测，分数直接会被赋值 1.0
     */
    private void showVerifyResult(boolean isVerifyMatched, float similarity, float silentLivenessScore) {
        runOnUiThread(() -> {
            scoreText.setText("SilentLivenessScore:"+silentLivenessScore);

            if (silentLivenessScore < silentLivenessPassScore) {
                tipsTextView.setText(R.string.silent_anti_spoofing_error);
                new AlertDialog.Builder(FaceVerificationActivity.this)
                        .setMessage(R.string.silent_anti_spoofing_error)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, (dialogInterface, i) -> finish())
                        .show();
            } else if (isVerifyMatched) {
                tipsTextView.setText("Successful,similarity= " + similarity);
                VoicePlayer.getInstance().addPayList(R.raw.verify_success);

                //关闭页面时间业务自己根据实际情况定
                new Handler(Looper.getMainLooper()).postDelayed(FaceVerificationActivity.this::finish, 1000);
            } else {
                tipsTextView.setText("Failed ！ similarity=" + similarity);
                VoicePlayer.getInstance().addPayList(R.raw.verify_failed);
                new AlertDialog.Builder(FaceVerificationActivity.this)
                        .setMessage(R.string.face_verify_failed)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm, (dialogInterface, i) -> finish())
                        .setNegativeButton(R.string.retry, (dialog, which) -> {
                            faceVerifyUtils.retryVerify();
                        })
                        .show();

            }
        });
    }


    /**
     * 根据业务和设计师UI交互修改你的 UI，Demo 仅供参考
     * <p>
     * 添加声音提示和动画提示定制也在这里根据返回码进行定制
     */
    private void showFaceVerifyTips(int actionCode) {
        if (!isDestroyed() && !isFinishing()) {
            runOnUiThread(() -> {
                switch (actionCode) {

                    //5次相比阈值太低就判断为非同一人
                    case VERIFY_DETECT_TIPS_ENUM.ACTION_PROCESS:
                        tipsTextView.setText(R.string.face_verifying);
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.ACTION_NO_FACE:
                        tipsTextView.setText(R.string.no_face_detected_tips);
                        break;


                    case VERIFY_DETECT_TIPS_ENUM.ACTION_FAILED:
                        tipsTextView.setText(R.string.motion_liveness_detection_failed);
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.ACTION_OK:
                        VoicePlayer.getInstance().play(R.raw.face_camera);
                        tipsTextView.setText(R.string.keep_face_visible);
                        break;

                    case ALIVE_DETECT_TYPE_ENUM.OPEN_MOUSE:
                        VoicePlayer.getInstance().play(R.raw.open_mouse);
                        tipsTextView.setText(R.string.repeat_open_close_mouse);
                        break;

                    case ALIVE_DETECT_TYPE_ENUM.SMILE: {
                        tipsTextView.setText(R.string.motion_smile);
                        VoicePlayer.getInstance().play(R.raw.smile);
                    }
                    break;

                    case ALIVE_DETECT_TYPE_ENUM.BLINK: {
                        VoicePlayer.getInstance().play(R.raw.blink);
                        tipsTextView.setText(R.string.motion_blink_eye);
                    }
                    break;

                    case ALIVE_DETECT_TYPE_ENUM.SHAKE_HEAD:
                        VoicePlayer.getInstance().play(R.raw.shake_head);
                        tipsTextView.setText(R.string.motion_shake_head);
                        break;

                    case ALIVE_DETECT_TYPE_ENUM.NOD_HEAD:
                        VoicePlayer.getInstance().play(R.raw.nod_head);
                        tipsTextView.setText(R.string.motion_node_head);
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.ACTION_TIME_OUT:
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.motion_liveness_detection_time_out)
                                .setCancelable(false)
                                .setPositiveButton(R.string.retry, (dialogInterface, i) -> {
                                            faceVerifyUtils.retryVerify();
                                        }
                                ).show();
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.NO_FACE_REPEATEDLY:
                        tipsTextView.setText(R.string.no_face_or_repeat_switch_screen);
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.stop_verify_tips)
                                .setCancelable(false)
                                .setPositiveButton(R.string.confirm, (dialogInterface, i) -> finish())
                                .show();

                        break;

                    // 单独使用一个textview 提示，防止上一个提示被覆盖。
                    // 也可以自行记住上个状态，FACE_SIZE_FIT 中恢复上一个提示
                    case VERIFY_DETECT_TIPS_ENUM.FACE_TOO_LARGE:
                        secondTipsTextView.setText(R.string.far_away_tips);
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.FACE_TOO_SMALL:
                        secondTipsTextView.setText(R.string.come_closer_tips);
                        break;

                    case VERIFY_DETECT_TIPS_ENUM.FACE_SIZE_FIT:
                        secondTipsTextView.setText("");
                        break;

                }
            });
        }
    }


    /**
     * 资源释放
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceVerifyUtils.destroyProcess();
    }


    /**
     * 暂停识别，防止切屏识别，如果你需要退后台不能识别的话
     */
    protected void onPause() {
        super.onPause();
        faceVerifyUtils.pauseProcess();
    }


}
