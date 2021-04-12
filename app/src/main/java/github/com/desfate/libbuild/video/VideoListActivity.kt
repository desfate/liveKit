package github.com.desfate.libbuild.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.desfate.videokit.controls.VideoRequestControls
import github.com.desfate.libbuild.R

class VideoListActivity : AppCompatActivity() , VideoRequestControls.VideoListResponse {

    lateinit var controls: VideoRequestControls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list)
        controls = VideoRequestControls()
        controls.getVideoList(this)
    }

    override fun callBack(result: String) {

    }
}