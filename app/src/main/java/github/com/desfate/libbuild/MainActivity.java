package github.com.desfate.libbuild;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import github.com.desfate.livekit.LivePushView;
import github.com.desfate.livekit.live.LiveConfig;
import github.com.desfate.livekit.live.LiveManager;
import github.com.desfate.livekit.utils.LiveSupportUtils;

/**
 * 关于腾讯云直播  自定义推流播放的demo
 */
public class MainActivity extends AppCompatActivity {

    Button play;
    Button push;
    Button pushTexture;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        push = findViewById(R.id.push_btn);
        play = findViewById(R.id.play_btn);
        pushTexture = findViewById(R.id.push_texture_btn);
        relativeLayout = findViewById(R.id.root);

        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRecordPermission(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, PushActivity.class);
                    intent.putExtra("type", PushActivity.DATA_TYPE);
                    startActivity(intent);
                }
            }
        });

        pushTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PushActivity.class);
                intent.putExtra("type", PushActivity.TEXTURE_TYPE);
                startActivity(intent);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRecordPermission(MainActivity.this)) {
                    startActivity(new Intent(MainActivity.this, PlayActivity.class));
                }
            }
        });
    }

    /**
     * 录制权限检测：存储权限、摄像头权限、录音权限
     * @param activity
     * @return
     */
    public static boolean checkRecordPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(activity,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }

        return true;
    }
}