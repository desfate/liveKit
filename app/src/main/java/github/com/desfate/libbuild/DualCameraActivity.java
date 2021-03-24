package github.com.desfate.libbuild;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import github.com.desfate.libbuild.test.CameraToMpegTest;
import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.ui.PreviewDualCameraView;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 双摄功能测试
 */
public class DualCameraActivity extends AppCompatActivity {

    PreviewDualCameraView dual_preview_view;
//    PreviewConfig mConfig;

    String TAG = "DualCameraActivity";


//    private TXLivePusher mLivePusher;   //                           腾讯推送
//    private TXLivePushConfig mLivePushConfig;//                      推送配置

    //    private LivePushControl control;
    LiveConfig liveConfig = new LiveConfig();

    private TextView sign;
    private Button switchBtn, record_btn;

    int pushSize = 1; // 1: 720P 2: 1080P
    int pushFrame = 1; // 1: 30FPS 2: 60FPS

    private boolean original = true;
    CameraToMpegTest mCameraMpeg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        dual_preview_view = findViewById(R.id.dual_preview_view);
        sign = findViewById(R.id.sign);
        mCameraMpeg = new CameraToMpegTest();
        record_btn = findViewById(R.id.record_btn);

//        m3d_btn = findViewById(R.id.m3d_btn);
//        rootLayout = findViewById(R.id.root);

        switchBtn = findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liveConfig.getPushCameraType() == 1) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 切换为横屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 切换为竖屏
                }
                dual_preview_view.getControl().switchCamera();
            }
        });

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCameraMpeg != null) {

                            try {
                                mCameraMpeg.testEncodeCameraToMp4();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }
        });
//        m3d_btn.setOnClickListener(new View.OnClickListener() {
//                                       @Override
//                                       public void onClick(View view) {
//                                           original = !original;
//                                           dual_preview_view.setOriginal(original);
//                                       }
//                                   }
//        );

//        mLivePusher = new TXLivePusher(this);
//        mLivePushConfig = new TXLivePushConfig();

        pushSize = getIntent().getIntExtra("pushSize", 1);
        pushFrame = getIntent().getIntExtra("pushFrame", 1);

//        mLivePushConfig.setVideoEncodeGop(2);
//
//        // 开启自定义视频采集
//        mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
//        // 设置视频分辨率，必须和摄像头分辨率一致
//        switch(pushFrame){
//            case 1:
//                mLivePushConfig.setVideoFPS(30);
//                break;
//            case 2:
//                mLivePushConfig.setVideoFPS(60);
//                break;
//        }
        liveConfig.setLivePushType(LiveConstant.LIVE_PUSH_TEXTURE);  // 采用byte[]推流模式
        liveConfig.setPushCameraType(LiveConstant.LIVE_CAMERA_DUAL);  // 设置拍摄模式
        switch (pushSize) {
            case 1:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_720);
                //  这个参数很关键  ！！！！    这个宽高数据必须和你通过Data上传的每帧数据相同
//                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
                break;
            case 2:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_1080);
                //  这个参数很关键  ！！！！    这个宽高数据必须和你通过Data上传的每帧数据相同
//                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1920_1080);
                break;
        }

//        mConfig = new PreviewConfig();
//        mConfig.setIsCameraFront(1);  // 後置
//        mConfig.setQuality_type(PreviewConfig.Preview_Quality.DUAL);
//        mConfig.setState(1);



        dual_preview_view.init(liveConfig, new LiveCallBack() {
            @Override
            public void startPushByData(byte[] buffer, int w, int h) {
//                int returnCode = mLivePusher.sendCustomVideoData(buffer, TXLivePusher.YUV_420P, w, h);
//                if (returnCode != 0) Log.e(TAG, "push error code = " + returnCode);
                System.out.println("123");
            }

            @Override
            public void startPushByTextureId(int textureID, int w, int h) {

            }
        });
        dual_preview_view.setOriginal(false); //  不交织
//        mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);
//        mLivePusher.setConfig(mLivePushConfig);
//
//        int resultCode = mLivePusher.startPusher(TestConfig.PUSH_URL);
//        Log.e(TAG, "startPush: resultCode = " + resultCode);
//
//        if (resultCode == -5) {
//            Log.i(TAG, "startRTMPPush: license 校验失败");
//            sign.setText("license 校验失败");
//        }else if(resultCode == 0){
//            sign.setText("license 校验成功");
//        }

        dual_preview_view.getControl().startPreview(); // 开启预览
//        dual_preview_view.getControl().startPush();  //   开启上传
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int W = mDisplayMetrics.widthPixels;
        int H = mDisplayMetrics.heightPixels;
        if (getResources() == null || getResources().getConfiguration() == null) return;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dual_preview_view.getLayoutParams().width = W;
            dual_preview_view.getLayoutParams().height = H;
            // land do nothing is ok
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
            dual_preview_view.getLayoutParams().width = W;
            dual_preview_view.getLayoutParams().height = H;
        }
    }


}
