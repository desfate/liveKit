package github.com.desfate.libbuild

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.tencent.rtmp.ITXLivePushListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePushConfig
import com.tencent.rtmp.TXLivePusher
import github.com.desfate.libbuild.R.id.anchor_push_view
import github.com.desfate.livekit.LiveConfig
import github.com.desfate.livekit.LiveConstant
import github.com.desfate.livekit.controls.MControl
import github.com.desfate.livekit.live.LiveCallBack
import github.com.desfate.livekit.ui.PreviewDualCameraView
import org.w3c.dom.Text

/**
 * 双摄推流测试代码
 */
class DualCameraPushActivity : AppCompatActivity(),
    ITXLivePushListener,
    View.OnClickListener,
    LiveCallBack{

    lateinit var mLivePusher: TXLivePusher
    lateinit var control: MControl
    var isPush = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push)
        findViewById<Button>(R.id.switch_btn).setOnClickListener(this)
        findViewById<Button>(R.id.pushed_btn).setOnClickListener(this)
        initSetting()
        setPushBtnState();
    }

    /**
     * 初始化一些配置  liveConfig 本地采集直播配置   txLiveConfig 腾讯云直播配置
     */
    fun initSetting(){
        val liveConfig = LiveConfig()
        liveConfig.livePushType = LiveConstant.LivePushType.DATA
//        liveConfig.livePushType = LiveConstant.LivePushType.TEXTURE //               推流模式
        liveConfig.liveQuality = LiveConstant.LiveQuality.LIVE_1080P //              直播类型
        liveConfig.pushCameraType = LiveConstant.LiveCameraType.CAMERA_DUAL_BACK  // 后置双摄


        val txLiveConfig = TXLivePushConfig()
        txLiveConfig.setCustomModeType(TXLiveConstants.CUSTOM_MODE_VIDEO_CAPTURE)//  自定义推流
        txLiveConfig.setVideoEncodeGop(5) //                                         缓存帧数
        txLiveConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1080_1920)//  设置推流宽高
        txLiveConfig.setVideoFPS(30)//                                               设置帧数

        mLivePusher = TXLivePusher(this)
        mLivePusher.setPushListener(this)
        mLivePusher.config = txLiveConfig;

        //  初始化预览
        findViewById<PreviewDualCameraView>(anchor_push_view).init(
            liveConfig, this
        )

        //  预览控件
        control = findViewById<PreviewDualCameraView>(anchor_push_view).control
        control.startPreview()
        control.startPush()

        // 这里开始推流
        val resultCode = mLivePusher.startPusher(TestConfig.PUSH_URL);
        if (resultCode == -5) {
            findViewById<TextView>(R.id.sign).text = "license 校验失败";
        }else if(resultCode == 0){
            findViewById<TextView>(R.id.sign).text = "license 校验成功";
        }
    }

    override fun onPushEvent(p0: Int, p1: Bundle?) {

    }

    override fun onNetStatus(p0: Bundle?) {

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.switch_btn -> {
                changeScreenType();
            }
            R.id.pushed_btn -> {
                isPush = !isPush
                setPushBtnState();
            }
        }
    }

    fun setPushBtnState(){
        if(isPush) {
            findViewById<Button>(R.id.pushed_btn).text = "停止推流"
        }else{
            findViewById<Button>(R.id.pushed_btn).text = "开始推流"
        }
    }

    fun changeScreenType(){
        // 这里切换需要进行横竖屏切换
        requestedOrientation = if (control.isCameraFront) {
            control.switchCamera() // switch必须要在setFront前面
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // 切换为横屏
        } else {
            control.switchCamera()
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 切换为竖屏
        }
    }


    override fun startPushByData(buffer: ByteArray?, w: Int, h: Int) {
        if(isPush) mLivePusher.sendCustomVideoData(buffer, TXLivePusher.YUV_420P, w, h)
    }

    override fun startPushByTextureId(textureID: Int, w: Int, h: Int) {
        if(isPush) mLivePusher.sendCustomVideoTexture(textureID, w, h);
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
            val params = findViewById<PreviewDualCameraView>(anchor_push_view).layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "16:9"
            findViewById<PreviewDualCameraView>(anchor_push_view).layoutParams = params
        } else {
            val params = findViewById<PreviewDualCameraView>(anchor_push_view).getLayoutParams() as ConstraintLayout.LayoutParams
            params.dimensionRatio = "9:16"
            findViewById<PreviewDualCameraView>(anchor_push_view).layoutParams = params
        }
    }


}