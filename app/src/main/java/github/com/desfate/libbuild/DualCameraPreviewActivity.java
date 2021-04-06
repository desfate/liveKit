package github.com.desfate.libbuild;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;

import github.com.desfate.livekit.CameraConstant;
import github.com.desfate.livekit.LiveConstant;
import github.com.desfate.livekit.CameraSetting;
import github.com.desfate.livekit.controls.MControl;
import github.com.desfate.livekit.dual.M3dConfig;
import github.com.desfate.livekit.dual.MPreviewControl;
import github.com.desfate.livekit.live.LiveCallBack;
import github.com.desfate.livekit.LiveConfig;
import github.com.desfate.livekit.live.LivePushControl;
import github.com.desfate.livekit.live.MLiveControl;
import github.com.desfate.livekit.ui.PreviewDualCameraView;

/**
 * 双摄预览测试  前后摄像头预览切换
 */
public class DualCameraPreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private Button switch_btn;  // 切换前后置
    private PreviewDualCameraView preview_view;
    private MControl control;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_preview);
        switch_btn = findViewById(R.id.switch_btn);
        preview_view = findViewById(R.id.preview_view);
        switch_btn.setOnClickListener(this);
        settingTXConfig();
        settingCustomerConfig();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.switch_btn){
            // 这里切换需要进行横竖屏切换
            if(control.isCameraFront()){
                control.switchCamera();  // switch必须要在setFront前面
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  // 切换为横屏
            }else{
                control.switchCamera();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 切换为竖屏
            }

        }
    }


    private TXLivePusher mLivePusher;   //                           腾讯推送
    private TXLivePushConfig mLivePushConfig;//                      推送配置

    public void settingTXConfig(){
        mLivePusher = new TXLivePusher(this);//                   初始化腾讯推流对象
        mLivePushConfig = new TXLivePushConfig();//                       推流设置

        // 开启自定义视频采集
        mLivePushConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE);
        mLivePushConfig.setVideoEncodeGop(5);
        mLivePusher.setPushListener(new ITXLivePushListener() {  //       推流状态回调
            @Override
            public void onPushEvent(int i, Bundle bundle) {

            }

            @Override
            public void onNetStatus(Bundle bundle) {

            }
        });
        mLivePusher.setConfig(mLivePushConfig);
    }
    LiveConfig liveConfig;
    public void settingCustomerConfig(){
        liveConfig = new LiveConfig();
        liveConfig.setLivePushType(LiveConstant.LivePushType.TEXTURE);
        liveConfig.setPushCameraType(LiveConstant.LiveCameraType.CAMERA_DUAL_BACK);//
        mLivePushConfig.setVideoFPS(30);
        preview_view.init(
                liveConfig,
                new LiveCallBack() {
                    @Override
                    public void startPushByData(byte[] buffer, int w, int h) {

                    }

                    @Override
                    public void startPushByTextureId(int textureID, int w, int h) {

                    }
                }
        );
        control = preview_view.getControl();
        control.startPreview();
        control.startPush();
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
