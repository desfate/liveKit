package github.com.desfate.libbuild;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class DualCameraTestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button dual_push_btn, dual_preview_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_camera_test);

        dual_push_btn = findViewById(R.id.dual_push_btn);
        dual_preview_btn = findViewById(R.id.dual_preview_btn);

        dual_push_btn.setOnClickListener(this);
        dual_preview_btn.setOnClickListener(this);

        checkRecordPermission(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.dual_push_btn){
            startActivity(new Intent(DualCameraTestActivity.this, DualCameraPushActivity.class));
        }else if(id == R.id.dual_preview_btn){
            startActivity(new Intent(DualCameraTestActivity.this, DualCameraPreviewActivity.class));
        }
    }

    /**
     * 录制权限检测：存储权限、摄像头权限、录音权限
     *
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
