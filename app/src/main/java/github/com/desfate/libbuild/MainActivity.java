package github.com.desfate.libbuild;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 关于腾讯云直播  自定义推流的demo
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button camera_btn, live_btn, dual_btn, test_btn, dual_texture_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        camera_btn = findViewById(R.id.camera_btn);
        live_btn = findViewById(R.id.live_btn);
        dual_btn = findViewById(R.id.dual_btn);
        test_btn = findViewById(R.id.test_btn);
//        dual_texture_btn = findViewById(R.id.dual_texture_btn);
        camera_btn.setOnClickListener(this);
        live_btn.setOnClickListener(this);
        dual_btn.setOnClickListener(this);
        test_btn.setOnClickListener(this);
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
//            case R.id.dual_texture_btn:
//                startActivity(new Intent(MainActivity.this, DualCameraTextureActivity.class));
//                break;
        }
    }
}