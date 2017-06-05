package com.soundcloud.android.crop;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import static android.app.Activity.RESULT_OK;


/**
 * Created by liuyichuanmei on 2017/4/5.
 */

public class CropTransfer {

    private Activity mActivity;
    private Fragment fragment;

    /**
     * @param activity
     */
    public CropTransfer(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * @param fragment
     */
    public CropTransfer(Fragment fragment) {
        this.mActivity = fragment.getActivity();
        this.fragment = fragment;
    }


    public void startPick() {
        Log.e("CropTransfer", "startPick");
        if (this.fragment != null) {
            Crop.pickImage(this.mActivity, this.fragment);
        } else {
            Crop.pickImage(this.mActivity);
        }
    }

    public static final int IMAGES_TAKE_PHOTO_REQUEST_CODE = 0x0123;

    /**
     * @param directoryPath 文件地址
     * @param fileName      文件名
     */
    public void startTakePhoto(String directoryPath, String fileName) {
        File out = new File(directoryPath);
        if (!out.exists()) {
            out.mkdirs();
        }
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        out = new File(directoryPath, fileName);
        setCashPath(directoryPath + fileName);// 该照片的绝对路径
        Uri uri = Uri.fromFile(out);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        if (this.fragment != null) {
            this.fragment.startActivityForResult(imageCaptureIntent, IMAGES_TAKE_PHOTO_REQUEST_CODE);
        } else {
            this.mActivity.startActivityForResult(imageCaptureIntent, IMAGES_TAKE_PHOTO_REQUEST_CODE);
        }
    }

    /**
     * @return 可直接读取的文件地址
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    public String getCashPath() {
        return cashPath;
    }

    public void setCashPath(String cashPath) {
        this.cashPath = cashPath;
    }

    private static String cashPath;

    public boolean onActivityResultCheck(int requestCode, int resultCode, Intent data, CropResultListener listener) {
        Log.e("CropTransfer", "30");
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCropPhotoChoice(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            onCropResult(resultCode, data, listener);
            return true;
        } else if (resultCode == RESULT_OK) {
            if (requestCode == IMAGES_TAKE_PHOTO_REQUEST_CODE) {//拍照成功返回
                beginCropAbsolutePath(getCashPath());
                return true;
            }
        }
        return false;
    }

    public void beginCropPhotoChoice(Uri source) {
        Log.e("CropTransfer", "39");
        Uri destination = Uri.fromFile(new File(this.mActivity.getCacheDir(), "cropped"));
        if (this.fragment != null) {
            Crop.of(source, destination).asSquare().start(this.mActivity, this.fragment);
        } else {
            Crop.of(source, destination).asSquare().start(this.mActivity);
        }
    }

    public void beginCropAbsolutePath(String absolutePath) {
        Log.e("CropTransfer", "45");
        File file = new File(compressImage(absolutePath, absolutePath.replaceAll("\\.jpg", "cropped.jpg"), 200));
        Uri destination = Uri.fromFile(file);
        if (this.fragment != null) {
            Crop.of(Uri.fromFile(file), destination).asSquare().start(this.mActivity, this.fragment);
        } else {
            Crop.of(Uri.fromFile(file), destination).asSquare().start(this.mActivity);
        }
    }

    public void onCropResult(int resultCode, Intent result, CropResultListener listener) {
        Log.e("CropTransfer", "52");
        if (listener != null)
            if (resultCode == RESULT_OK) {
                listener.onSuccess(Crop.getOutput(result));
            } else if (resultCode == Crop.RESULT_ERROR) {
                listener.onError(Crop.getError(result).getMessage());
            }
    }


    /**
     * @param filePath
     * @param targetPath
     * @param targetSize 目标文件大小 单位：kb
     * @return
     */
    private String compressImage(String filePath, String targetPath, int targetSize) {
        Bitmap bm = getSmallBitmap(filePath);//获取一定尺寸的图片
//        int degree = readPictureDegree(filePath);//获取相片拍摄角度
//        if (degree != 0) {//旋转照片角度，防止头像横着显示
//            bm = rotateBitmap(bm, degree);
//        }
        File outputFile = new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
            } else {
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            int quality = 90;
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
            while (outputFile.length() / 8 / 1024 > targetSize) {
                bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
                quality -= 10;
            }
        } catch (Exception e) {
        }
        return outputFile.getPath();
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    private Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public interface CropResultListener {
        void onSuccess(Uri resultUri);

        void onError(String errorMessage);
    }
}
