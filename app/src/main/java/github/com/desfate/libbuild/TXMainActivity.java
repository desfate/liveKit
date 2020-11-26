package github.com.desfate.libbuild;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
public class TXMainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView licence_url;
    TextView licence_key;
    Button push_type, push_size, push_frame, submit, play;

    int pushType = 1; // 1: data 2: texture
    int pushSize = 1; // 1: 720P 2: 1080P
    int pushFrame = 1; // 1: 30FPS 2: 60FPS

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        licence_url = findViewById(R.id.licence_url);
        licence_key = findViewById(R.id.licence_key);

        push_type = findViewById(R.id.push_type);
        push_size = findViewById(R.id.push_size);
        push_frame = findViewById(R.id.push_frame);
        submit = findViewById(R.id.submit);
        play = findViewById(R.id.play);

        checkRecordPermission(this);

        push_type.setOnClickListener(this);
        push_size.setOnClickListener(this);
        push_frame.setOnClickListener(this);
        submit.setOnClickListener(this);
        play.setOnClickListener(this);

        licence_url.setText("LICENCE_URL = " + TestConfig.LICENCE_URL);
        licence_key.setText("LICENCE_KEY = " + TestConfig.LICENCE_KEY);

        refresh();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.push_type:
                changeType();
                break;
            case R.id.push_size:
                changeSize();
                break;
            case R.id.push_frame:
                changeFPS();
                break;
            case R.id.submit:
                Intent intent;
                if(pushType == 1){
                    intent = new Intent(TXMainActivity.this, DataPushActivity.class);
                }else{
                    intent = new Intent(TXMainActivity.this, TexturePushActivity.class);
                }
                intent.putExtra("pushType", pushType);
                intent.putExtra("pushSize", pushSize);
                intent.putExtra("pushFrame", pushFrame);
                startActivity(intent);
                break;

            case R.id.play:
                intent = new Intent(TXMainActivity.this, PlayActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void changeType(){
        pushType = (pushType == 1 ? 2 : 1);
        refresh();
    }

    private void changeSize(){
        pushSize = (pushSize == 1 ? 2 : 1);
        refresh();
    }

    private void changeFPS(){
        pushFrame = (pushFrame == 1 ? 2 : 1);
        refresh();
    }

    public void refresh(){
        if(pushType == 1){
            push_type.setText("推流类型：  Customer By Data");
        }else{
            push_type.setText("推流类型：  Customer By Texture");
        }
        if(pushSize == 1){
            push_size.setText("推流大小：  720P");
        }else{
            push_size.setText("推流大小：  1080P");
        }
        if(pushFrame == 1){
            push_frame.setText("推流帧率：  30FPS");
        }else{
            push_frame.setText("推流帧率：  60FPS");
        }
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