package github.com.desfate.libbuild;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import github.com.desfate.libbuild.video.VideoActivity;

/**
 * 关于腾讯云直播  自定义推流的demo
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button camera_btn, live_btn, dual_btn, test_btn, video_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        camera_btn = findViewById(R.id.camera_btn);
        live_btn = findViewById(R.id.live_btn);
        dual_btn = findViewById(R.id.dual_btn);
        test_btn = findViewById(R.id.test_btn);
        video_btn = findViewById(R.id.video_btn);
//        dual_texture_btn = findViewById(R.id.dual_texture_btn);
        camera_btn.setOnClickListener(this);
        live_btn.setOnClickListener(this);
        dual_btn.setOnClickListener(this);
        test_btn.setOnClickListener(this);
        video_btn.setOnClickListener(this);
//        dual_texture_btn.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.camera_btn:
                startActivity(new Intent(MainActivity.this, CameraDemoActivity.class));
                break;
            case R.id.live_btn:
                startActivity(new Intent(MainActivity.this, TXMainActivity.class));
                break;
            case R.id.dual_btn:
                startActivity(new Intent(MainActivity.this, DualCameraTestActivity.class));
                break;
            case R.id.test_btn:
                startActivity(new Intent(MainActivity.this, TestActivity.class));
                break;
            case R.id.video_btn:
                startActivity(new Intent(MainActivity.this, VideoActivity.class));
//            case R.id.dual_texture_btn:
//                startActivity(new Intent(MainActivity.this, DualCameraTextureActivity.class));
//                break;
        }
    }
}