package study.rq.com.imagepeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.soundcloud.android.crop.CropTransfer;

public class MainActivity extends AppCompatActivity {
//    <activity android:name="com.soundcloud.android.crop.CropImageActivity" />

    CropTransfer mUtil;
    ImageView imgShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUtil = new CropTransfer(MainActivity.this);
        imgShow = (ImageView) findViewById(R.id.img_photo_show);
        findViewById(R.id.tv_click_picker).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mUtil.startPick();
                    }
                }
        );
        findViewById(R.id.tv_click_take).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        path = Environment.getExternalStorageDirectory().toString() + "/test/";
                        mUtil.startTakePhoto(path, System.currentTimeMillis() + ".jpg");
                        Log.e("MainActivity", "path = " + path);
                    }
                }
        );
    }

    String path;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("MainActivity", "requestCode = " + requestCode + "   resultCode = " + resultCode);
        if (mUtil.onActivityResultCheck(requestCode, resultCode, data,
                new CropTransfer.CropResultListener() {
                    @Override
                    public void onSuccess(Uri resultUri) {
                        Log.e("MainActivity", " CropTransfer resultUri = " + resultUri);
                        imgShow.setImageURI(resultUri);
                        showBigPic(resultUri);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("MainActivity", " CropTransfer errorMessage = " + errorMessage);
                    }
                })) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showBigPic(Uri resultUri) {
//        raoqian156/sketch/src/main/java/me/xiaopan/sketch/SketchImageView
//        SketchImageView sketchImageView = fvb(...);
//        sketchImageView.setShowDownloadProgress(true);
//        sketchImageView.setClickRetryOnError(true);
//        sketchImageView.setSupportLargeImage(true);
//        sketchImageView.setSupportZoom(true);
//        sketchImageView.getImageZoomer().setReadMode(true);
//
//        final ImageLoadingView loadingView = new ImageLoadingView(this);
//        loadingView.setProgress(0f);
//        loadingView.setTargetView(sketchImageView);
//
//        sketchImageView.setDownloadProgressListener(new DownloadProgressListener() {
//            @Override
//            public void onUpdateDownloadProgress(int totalLength, int completedLength) {
//                loadingView.setProgress((float) completedLength / totalLength);
//            }
//        });
//        sketchImageView.setDisplayListener(new DisplayListener() {
//            @Override
//            public void onCompleted(ImageFrom imageFrom, String mimeType) {
////                loadingView.loadCompleted();
//                loadingView.loadCompleted();
//            }
//
//            @Override
//            public void onStarted() {
//
//            }
//
//            @Override
//            public void onError(ErrorCause errorCause) {
//                loadingView.loadFaild();
//                TLog.error(TAG + "errorCause....");
//            }
//
//            @Override
//            public void onCanceled(CancelCause cancelCause) {
//
//            }
//        });
//
//        sketchImageView.displayImage(pic);
    }
}
