package github.com.desfate.libbuild;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;

import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.ui.DataLivePushView;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 推流测试类  基于data的推流模式
 *
 * 这个推流模式有个问题
 *
 *
 */
public class DataPushActivity extends AppCompatActivity {

    private final static String TAG = "PushActivity";

    private TXLivePusher mLivePusher;
    private TXLivePushConfig mLivePushConfig;
    private DataLivePushView anchor_push_view;
    private RelativeLayout rootLayout;
    private TextView sign;
    private Button switchBtn;

    int pushSize = 1; // 1: 720P 2: 1080P
    int pushFrame = 1; // 1: 30FPS 2: 60FPS

    private int type;
    LiveConfig liveConfig = new LiveConfig();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);
        anchor_push_view = findViewById(R.id.anchor_push_view);
        sign = findViewById(R.id.sign);
        rootLayout = findViewById(R.id.root);

        switchBtn = findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liveConfig.getPushCameraType() == 1) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 切换为横屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 切换为竖屏
                }
                anchor_push_view.getControl().switchCamera();
            }
        });

        mLivePusher = new TXLivePusher(this);
        mLivePushConfig = new TXLivePushConfig();

        pushSize = getIntent().getIntExtra("pushSize", 0 );
        pushFrame = getIntent().getIntExtra("pushFrame", 0 );

        mLivePushConfig.setVideoEncodeGop(2);

        anchor_push_view.setVisibility(View.VISIBLE);

        // 开启自定义视频采集
        mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
        // 设置视频分辨率，必须和摄像头分辨率一致
        switch(pushFrame){
            case 1:
                mLivePushConfig.setVideoFPS(30);
                break;
            case 2:
                mLivePushConfig.setVideoFPS(60);
                break;
        }
        liveConfig.setLivePushType(LiveConfig.LIVE_PUSH_DATA);  // 采用byte[]推流模式
        switch (pushSize){
            case 1:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_720);
                //  这个参数很关键  ！！！！    这个宽高数据必须和你通过Data上传的每帧数据相同
                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
                break;
            case 2:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_1080);
                //  这个参数很关键  ！！！！    这个宽高数据必须和你通过Data上传的每帧数据相同
                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1920_1080);
                break;
        }

        anchor_push_view.init(liveConfig, rootLayout, new LiveCallBack() {
            @Override
            public void startPushByData(byte[] buffer, int w, int h) {
                int returnCode = mLivePusher.sendCustomVideoData(buffer, TXLivePusher.YUV_420P, w, h);
                if (returnCode != 0) Log.e(TAG, "push error code = " + returnCode);
            }

            @Override
            public void startPushByTextureId(int textureID, int w, int h) {

            }
        });

        mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);
        mLivePusher.setConfig(mLivePushConfig);

        int resultCode = mLivePusher.startPusher(TestConfig.PUSH_URL);
        Log.e(TAG, "startPush: resultCode = " + resultCode);

        if (resultCode == -5) {
            Log.i(TAG, "startRTMPPush: license 校验失败");
            sign.setText("license 校验失败");
        }else if(resultCode == 0){
            sign.setText("license 校验成功");
        }
        anchor_push_view.getControl().startPreview(); // 开启预览
        anchor_push_view.getControl().startPush();  //   开启上传
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int W = mDisplayMetrics.widthPixels;
        int H = mDisplayMetrics.heightPixels;
        if(getResources() == null || getResources().getConfiguration() == null) return;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            anchor_push_view.getLayoutParams().width = W;
            anchor_push_view.getLayoutParams().height = H;
            // land do nothing is ok
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
            anchor_push_view.getLayoutParams().width = W;
            anchor_push_view.getLayoutParams().height = H;
        }
    }
}
