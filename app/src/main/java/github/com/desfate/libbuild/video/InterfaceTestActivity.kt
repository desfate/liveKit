package github.com.desfate.libbuild.video

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.desfate.commonlib.kotlin.FastRecyclerAdapter
import com.github.desfate.commonlib.kotlin.FastViewHolder
import github.com.desfate.libbuild.R

/**
 * 接口测试类
 */
class InterfaceTestActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interfaces)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = FastRecyclerAdapter(items = listOf(
            "VideoList","others"
        ), layoutCallBack = {
            R.layout.views_item
        }, convertCallBack = { mViewHolder: FastViewHolder, s: String, i: Int ->
            mViewHolder.getView<Button>(R.id.item_text)?.text = s
            mViewHolder.getView<Button>(R.id.item_text)?.setOnClickListener {
                if (s == "VideoList") {
                    startActivity(Intent(this, VideoListActivity::class.java))
                }
            }
        })

    }

}

