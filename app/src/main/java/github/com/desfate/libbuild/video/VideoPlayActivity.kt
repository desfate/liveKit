package github.com.desfate.libbuild.video

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.desfate.videokit.ui.VideoPlayView
import github.com.desfate.libbuild.R
import github.com.desfate.livekit.ui.DualLivePlayView
import github.com.desfate.livekit.ui.PreviewDualCameraView

class VideoPlayActivity : AppCompatActivity(){

    lateinit var previewView: DualLivePlayView
    lateinit var videoPlayView: VideoPlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        previewView = findViewById(R.id.anchor_play_view)
        videoPlayView = findViewById(R.id.video_play_view)

        videoPlayView.init(intent.getStringExtra("play_url"))
        videoPlayView.setPreviewSurface(previewView.surface)  // 绑定播放器和本地绘制部分
        videoPlayView.setPreviewSurfaceHolder(previewView.holder)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
            val params = previewView.layoutParams as ConstraintLayout.LayoutParams
            val params2 = videoPlayView.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "16:9"
            params2.dimensionRatio = "16:9"
            videoPlayView.layoutParams = params
            previewView.layoutParams = params
        } else {
            val params = previewView.layoutParams as ConstraintLayout.LayoutParams
            val params2 = videoPlayView.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "9:16"
            params2.dimensionRatio = "16:9"
            videoPlayView.layoutParams = params
            previewView.layoutParams = params
        }
    }

}