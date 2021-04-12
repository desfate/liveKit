package github.com.desfate.libbuild.video

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import github.com.desfate.libbuild.R

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        findViewById<Button>(R.id.upload_btn).setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        findViewById<Button>(R.id.interface_btn).setOnClickListener {
            startActivity(Intent(this, InterfaceTestActivity::class.java))
        }
    }
}