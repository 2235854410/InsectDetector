package com.tsw.insectdetector;
/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Objects;

public class MediaHandler {
    private final String TAG = getClass().getName();
    public static final int TAKE_CAMERA = 101;
    public static final int OPEN_ALBUM = 102;
    private Uri imageUri;

    public void takeCamera(Activity activity, Context context) {
        File outputImage = new File(Objects.requireNonNull(context).getExternalCacheDir(), "output_image.jpg");
               /* 从Android 6.0系统开始，读写SD卡被列为了危险权限，如果将图片存放在SD卡的任何其他目录，
                  都要进行运行时权限处理才行，而使用应用关联 目录则可以跳过这一步
                */
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
                /*
                   7.0系统开始，直接使用本地真实路径的Uri被认为是不安全的，会抛 出一个FileUriExposedException异常。
                   而FileProvider则是一种特殊的内容提供器，它使用了和内 容提供器类似的机制来对数据进行保护，
                   可以选择性地将封装过的Uri共享给外部，从而提高了 应用的安全性
                 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //大于等于版本24（7.0）的场合
            imageUri = FileProvider.getUriForFile(context, "com.tsw.insectdetector.fileprovider", outputImage);
        } else {
            //小于android 版本7.0（24）的场合
            imageUri = Uri.fromFile(outputImage);
        }

        //启动相机程序
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, TAKE_CAMERA);
    }

    public void openAlbum(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(context),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(activity), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {
            //打开相册
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            activity.startActivityForResult(intent, OPEN_ALBUM); // 打开相册
        }
    }

    public void startUCrop(Activity activity, Context context, Uri uri) {
        UCrop.Options options = new UCrop.Options();
        //裁剪后图片保存在文件夹中
        String storePath = Objects.requireNonNull(context).getExternalCacheDir()
                .getAbsolutePath() + File.separator + "UCropImg";
        File UCropDir = new File(storePath);
        if(!UCropDir.exists()) {
            UCropDir.mkdir();
            Log.d(TAG, "startUCrop: ");
        }
        Uri destinationUri = Uri.fromFile(new File(storePath,"original.jpg"));
        UCrop uCrop = UCrop.of(uri, destinationUri);//第一个参数是裁剪前的uri,第二个参数是裁剪后的uri
        options.setAllowedGestures(com.yalantis.ucrop.UCropActivity.SCALE, com.yalantis.ucrop.UCropActivity.ROTATE, com.yalantis.ucrop.UCropActivity.ALL);
        options.setToolbarTitle("编辑图片");
        //设置隐藏底部容器，默认显示
        //options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(context, R.color.colorMiddleGrey));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(context, R.color.colorMiddleGrey));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        uCrop.withOptions(options);
        uCrop.start(activity);
    }

    public Uri getImageUri() {
        return imageUri;
    }

}
