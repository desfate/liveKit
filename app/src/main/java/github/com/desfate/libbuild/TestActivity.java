package github.com.desfate.libbuild;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import github.com.desfate.libbuild.test.CameraToMpegTest;

public class TestActivity extends AppCompatActivity {
    private Button mCameraRecord = null;
    CameraToMpegTest mCameraMpeg = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (TestActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    TestActivity.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                } else {
                    //这里就是权限打开之后自己要操作的逻辑
                }
            }
        }


        mCameraRecord = (Button)findViewById(R.id.record_camera);
        mCameraRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCameraMpeg != null) {

                            try {
                                mCameraMpeg.testEncodeCameraToMp4();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }

        });
        mCameraMpeg = new CameraToMpegTest();
    }

}

