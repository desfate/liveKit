package github.com.desfate.libbuild;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 基于腾讯云 texturid 推流模式
 */
public class TexturePushActivity extends AppCompatActivity {

    private final static String TAG = "TexturePushActivity";
    private TXCloudVideoView txCloudVideoView;  //                   实现本地预览的ui
    private TXLivePusher mLivePusher;   //                           腾讯推送
    private TXLivePushConfig mLivePushConfig;//                      推送配置

    private LivePushControl control;
    LiveConfig liveConfig = new LiveConfig();

    private TextView sign;
    private Button switchBtn;

    int pushSize = 1; // 1: 720P 2: 1080P
    int pushFrame = 1; // 1: 30FPS 2: 60FPS

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_texture);
        txCloudVideoView = findViewById(R.id.tx_cloud_view);
        txCloudVideoView.setVisibility(View.VISIBLE);
        sign = findViewById(R.id.sign);
        switchBtn = findViewById(R.id.switch_btn);
        mLivePusher = new TXLivePusher(this);//                   初始化腾讯推流对象
        mLivePushConfig = new TXLivePushConfig();//                       推流设置

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liveConfig.getPushCameraType() == 1) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 切换为横屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 切换为竖屏
                }
                control.switchCamera();
            }
        });

        pushSize = getIntent().getIntExtra("pushSize", 0 );
        pushFrame = getIntent().getIntExtra("pushFrame", 0 );

        // 开启自定义视频采集
        mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
        mLivePushConfig.setVideoEncodeGop(5);
        liveConfig.setLivePushType(LiveConstant.LIVE_PUSH_TEXTURE);//       texture 模式

        switch (pushSize){
            case 1:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_720);
                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280);
                break;
            case 2:
                liveConfig.setLiveQuality(LiveSupportUtils.LIVE_SIZE_1080);
                mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1080_1920);
                break;
        }
        // 设置视频分辨率，必须和摄像头分辨率一致
        switch(pushFrame){
            case 1:
                mLivePushConfig.setVideoFPS(30);
                break;
            case 2:
                mLivePushConfig.setVideoFPS(60);
                break;
        }

        mLivePusher.setPushListener(new ITXLivePushListener() {  //       推流状态回调
            @Override
            public void onPushEvent(int i, Bundle bundle) {

            }

            @Override
            public void onNetStatus(Bundle bundle) {

            }
        });

        mLivePusher.setConfig(mLivePushConfig);

        TextureView textureView = new TextureView(this);

        control = new LivePushControl.LivePushControlBuilder()
                .setContext(this)
                .setLiveConfig(liveConfig)
                .setSurfaceTexture(textureView.getSurfaceTexture())
                .setTextureView(textureView)
                .setLiveCallBack(new LiveCallBack() {
                    @Override
                    public void startPushByData(byte[] buffer, int w, int h) {

                    }

                    @Override
                    public void startPushByTextureId(int textureID, int w, int h) {
                        mLivePusher.sendCustomVideoTexture(textureID, w, h);
                    }
                }).build();

        txCloudVideoView.addVideoView(textureView);//        绑定本地预览UI

        int resultCode = mLivePusher.startPusher(TestConfig.PUSH_URL);
        if (resultCode == -5) {
            Log.i(TAG, "startRTMPPush: license 校验失败");
            sign.setText("license 校验失败");
        }else if(resultCode == 0){
            sign.setText("license 校验成功");
        }

        control.startPreview();
        control.startPush();
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
            txCloudVideoView.getLayoutParams().width = H * 1920 / 1080;
            txCloudVideoView.getLayoutParams().height = H;
            // land do nothing is ok
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
            txCloudVideoView.getLayoutParams().width = W;
            txCloudVideoView.getLayoutParams().height = W * 1920 / 1080;
        }
    }
}
