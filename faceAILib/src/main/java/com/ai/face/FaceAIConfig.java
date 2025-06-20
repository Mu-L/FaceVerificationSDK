package com.ai.face;

import android.content.Context;

import com.ai.face.utils.VoicePlayer;

import java.io.File;

/**
 * 不要直接使用File Api 直接往文件目录插入图片，要使用SDK 提供的APi写入数据，图片还需要向量化，检测质量等操作
 */
public class FaceAIConfig {

    //不要直接使用File Api 直接往文件目录插入图片，要使用SDK 提供的APi写入数据，图片还需要向量化
    public static String CACHE_BASE_FACE_DIR;   //1：1 人脸识别人脸图片存储目录
    public static String CACHE_SEARCH_FACE_DIR; //1：N 人脸识别搜索人脸图片存储目录

    /**
     * 初始化人脸识别 人脸搜索存储目录
     *
     * @param context
     */
    public static void init(Context context) {

        // 人脸图存储在App内部私有空间，其他应用不可以访问，
        // https://developer.android.com/training/data-storage?hl=zh-cn
        // Warming: 目前仅能存储在context.getCacheDir() 或者context.getFilesDir()
        CACHE_BASE_FACE_DIR = context.getCacheDir().getPath() + "/faceAIVerify/";    //1:1 人脸识别目录
        CACHE_SEARCH_FACE_DIR = context.getFilesDir().getPath() + "/faceAISearch/";  //人脸搜索人脸库目录


        VoicePlayer.getInstance().init(context);
        
        


        //文件目录提前创建好，操作移动到SDK 内部
        File file = new File(CACHE_BASE_FACE_DIR);
        if (!file.exists()) file.mkdirs();

        File searchFile = new File(CACHE_SEARCH_FACE_DIR);
        if (!searchFile.exists()) file.mkdirs();
    }


    public static boolean isFaceIDExist(String faceID) {
        File file = new File(CACHE_BASE_FACE_DIR + faceID);
        return file.exists();
    }

}
