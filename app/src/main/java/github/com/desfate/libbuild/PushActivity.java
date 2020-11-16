package github.com.desfate.libbuild;

import android.opengl.EGLContext;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import github.com.desfate.libbuild.tc.TestRenderVideoFrame;
import github.com.desfate.libbuild.tc.TestSendCustomCameraData;
import github.com.desfate.livekit.LivePushView;
import github.com.desfate.livekit.camera.interfaces.OnFrameAvailable;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LiveManager;

/**
 * 推流测试类
 */
public class PushActivity extends AppCompatActivity {

    private final static String TAG = "PushActivity";

    public final static int DATA_TYPE = 1000;
    public final static int TEXTURE_TYPE = DATA_TYPE + 1;

    private TestSendCustomCameraData mCustomCameraCapture;      // 外部采集
    private TestRenderVideoFrame mCustomRender;              // 外部渲染

    private TXLivePusher mLivePusher;
    private TXLivePushConfig mLivePushConfig;
    private LivePushView anchor_push_view;
    private RelativeLayout rootLayout;
    private TXCloudVideoView txCloudVideoView;

    private int type;
    LiveConfig liveConfig = new LiveConfig();
    private String pushUrl = "rtmp://117780.livepush.myqcloud.com/live/test?txSecret=955fdc1f31b8cd27ad067b6f1dddbd02&txTime=5FB33D5B";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);
        anchor_push_view = findViewById(R.id.anchor_push_view);
        txCloudVideoView = findViewById(R.id.tx_cloud_view);
        rootLayout = findViewById(R.id.root);
        mLivePusher = new TXLivePusher(this);
        mLivePushConfig = new TXLivePushConfig();

        type = getIntent().getIntExtra("type", 0);
        if(type == DATA_TYPE){
            mLivePushConfig.setVideoEncodeGop(2);
            liveConfig.setLivePushType(LiveConfig.LIVE_PUSH_DATA);  // 采用byte[]推流模式
            anchor_push_view.setVisibility(View.VISIBLE);
            txCloudVideoView.setVisibility(View.GONE);
        }else{
            mLivePushConfig.setVideoEncodeGop(5);
            liveConfig.setLivePushType(LiveConfig.LIVE_PUSH_TEXTURE);  // 采用byte[]推流模式
            mCustomRender = new TestRenderVideoFrame();
            mCustomCameraCapture = new TestSendCustomCameraData(this);
            mCustomCameraCapture.setRenderListener(mCustomRender);
            // 开启自定义视频采集
            mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
            // 设置视频分辨率，必须和摄像头分辨率一致
            mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_720_1280);
            mLivePusher.setPushListener(mCustomRender);
            mLivePusher.setConfig(mLivePushConfig);
            mCustomCameraCapture.setLivePusher(mLivePusher);
            anchor_push_view.setVisibility(View.GONE);
            txCloudVideoView.setVisibility(View.VISIBLE);
            // 开始自定义采集
            mCustomCameraCapture.start();
            // 创建渲染的View
            TextureView textureView = new TextureView(this);
            // 添加到父布局
            txCloudVideoView.addVideoView(textureView);
            // 自定义渲染器与渲染的 View 绑定
            mCustomRender.start(textureView);
        }
//        // 开启自定义视频采集
//        mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
//        // 设置视频分辨率，必须和摄像头分辨率一致
//        mLivePushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1920_1080);
//        mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, false, false);
//        mLivePusher.setConfig(mLivePushConfig);

        int resultCode = mLivePusher.startPusher(pushUrl);
        Log.e(TAG, "startPush: resultCode = " + resultCode);
        if (resultCode == -5) {
            Log.i(TAG, "startRTMPPush: license 校验失败");
        }
//        anchor_push_view.setParentLayout(rootLayout);
//        anchor_push_view.setLiveConfig(liveConfig);
//        anchor_push_view.setLivePushListener(new LiveManager() {
//            @Override
//            public void startPushByData(byte[] buffer, int w, int h) {
//                /*
//                  returnCode
//                  0：发送成功；
//                  1：视频分辨率非法；
//                  2：YUV 数据长度与设置的视频分辨率所要求的长度不一致；
//                  3：视频格式非法；
//                  4：视频图像长宽不符合要求，画面比要求的小了；
//                  1000：SDK 内部错误。
//                 */
//                int returnCode = mLivePusher.sendCustomVideoData(buffer, TXLivePusher.YUV_420P, w, h);
//                if (returnCode != 0) Log.e(TAG, "push error code = " + returnCode);
//            }
//
//            @Override
//            public void startPushByTextureId(int textureID, int w, int h) {
//                /*
//                 * returnCode
//                 * 0：发送成功；
//                 * 1：视频分辨率非法；
//                 * 3：视频格式非法；
//                 * 4：视频图像长宽不符合要求，画面比要求的小了；
//                 * 1000：SDK 内部错误。
//                 */
//                int returnCode = mLivePusher.sendCustomVideoTexture(textureID, w, h);
//                if (returnCode != 0) Log.e(TAG, "push error code = " + returnCode);
//            }
//        });
//
//
//        anchor_push_view.startPush();


    }
}
