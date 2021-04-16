package github.com.desfate.libbuild.video

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.desfate.commonlib.kotlin.FastRecyclerAdapter
import com.github.desfate.commonlib.kotlin.FastViewHolder
import com.github.desfate.commonlib.kotlin.SpacesItemDecoration
import com.github.desfate.videokit.controls.VideoRequestControls
import com.github.desfate.videokit.dates.VideoInfoDate
import github.com.desfate.libbuild.R
import java.util.ArrayList

class VideoListActivity : AppCompatActivity() , VideoRequestControls.VideoListResponse {

    lateinit var controls: VideoRequestControls
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FastRecyclerAdapter<VideoInfoDate>

    var dataList = MutableLiveData<List<VideoInfoDate>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = FastRecyclerAdapter(
            items = arrayListOf(),
            layoutCallBack = { R.layout.video_list },
            convertCallBack = { mViewHolder: FastViewHolder, video: VideoInfoDate, i: Int ->
                mViewHolder.getView<ImageView>(R.id.video_img)?.setBackgroundResource(R.drawable.ic_launcher_background)
                mViewHolder.getView<TextView>(R.id.video_name)?.text = video.videoName
                mViewHolder.getView<ImageView>(R.id.video_img)?.setOnClickListener {
                    val intent = Intent(this, VideoPlayActivity::class.java)
                    intent.putExtra("play_url", video.videoPlayUrl)
                    startActivity(intent)
                }
        })
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpacesItemDecoration(10))

        controls = VideoRequestControls()
        controls.getVideoList(this)
        dataList.observe(this, Observer {
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })
    }

    override fun callBack(result: ArrayList<VideoInfoDate>?) {
        dataList.postValue(result)
    }
}