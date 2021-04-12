package github.com.desfate.libbuild.video

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.github.desfate.commonlib.tools.FileUtils
import com.github.desfate.commonlib.views.progress.WaterWaveProgress

import com.github.desfate.videokit.controls.VideoUploadControls
import com.github.desfate.videokit.controls.VideoUploadControls.ProgressCallBack
import com.github.desfate.videokit.videoupload.TXUGCPublishTypeDef
import github.com.desfate.libbuild.R

class UploadActivity : AppCompatActivity(), View.OnClickListener , ProgressCallBack{

    lateinit var upload_btn: Button
    lateinit var stop_btn: Button
    lateinit var resume_btn: Button
    lateinit var select_btn: Button
    lateinit var progressView: WaterWaveProgress

    private val FILE_RESULT_CODE = 10086

    lateinit var control: VideoUploadControls
    var uri = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        upload_btn = findViewById(R.id.upload_btn)
        stop_btn = findViewById(R.id.stop_btn)
        resume_btn = findViewById(R.id.resume_btn)
        select_btn = findViewById(R.id.select_btn)
        progressView =findViewById(R.id.progress_view)

        upload_btn.setOnClickListener(this)
        stop_btn.setOnClickListener(this)
        resume_btn.setOnClickListener(this)
        select_btn.setOnClickListener(this)

        control = VideoUploadControls(this,this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.upload_btn -> control.beginUpload(uri.value)
            R.id.stop_btn -> control.pauseUpload()
            R.id.resume_btn -> control.resumeUpload(uri.value)
            R.id.select_btn -> chooseVideo()
        }
    }

    fun chooseVideo(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*" //设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val wrapIntent = Intent.createChooser(intent, null)
        startActivityForResult(wrapIntent, FILE_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FILE_RESULT_CODE && resultCode == RESULT_OK){
            uri.postValue( FileUtils.getRealPathFromUri(this, data?.data))
        }
    }

    override fun onProgress(upload: Long, total: Long) {
        progressView.progress = upload.toInt() * 100 / total.toInt()
    }

    override fun success(result: TXUGCPublishTypeDef.TXPublishResult?) {
        // 提交成功后的返回
    }

}
