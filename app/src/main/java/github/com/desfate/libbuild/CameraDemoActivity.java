package github.com.desfate.libbuild;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import github.com.desfate.livekit.camera.interfaces.CameraErrorCallBack;
import github.com.desfate.livekit.camera.news.CameraClient;
import github.com.desfate.livekit.camera.news.CameraErrorCode;
import github.com.desfate.livekit.camera.news.CameraInfo;
import github.com.desfate.livekit.camera.news.CameraUtils;

/**
 * 相机相关功能测试demo
 */
public class CameraDemoActivity extends AppCompatActivity {

    private EditText camera_info;
    private Button open_btn, add_btn, switch_btn;
    private Spinner camera_select;
    private TextureView texture, texture2, texture3, texture4;
    private TextView camera_tv;


    private List<Boolean> cameraList = new ArrayList<>();

    private List<TextureView> textureList = new ArrayList<>();
    private ArrayList<CameraClient> clientList = new ArrayList<>();



    private String selectedCameraId = "0";
    private String nowShowCameraId = "0";

    int nowCamera = 0;



    CameraErrorCallBack cameraErrorCallBack = new CameraErrorCallBack() {
        @Override
        public void onCameraOpenSuccess(CameraInfo info) {
            camera_tv.post(new Runnable() {
                @Override
                public void run() {
                    camera_tv.setText("相机开启成功");
                }
            });
        }

        @Override
        public void onCameraOpenError(CameraInfo info, final int error) {
            CameraDemoActivity.this.nowCamera -- ;
            camera_tv.post(new Runnable() {
                @Override
                public void run() {
                    camera_tv.setText("相机开启错误信息 = "+ CameraErrorCode.errorCamera(error) +  "--------------------当前camera " + CameraDemoActivity.this.nowCamera);
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_demo);

        add_btn = findViewById(R.id.add_btn);
        switch_btn = findViewById(R.id.switch_btn);
        camera_info = findViewById(R.id.camera_info);
        open_btn = findViewById(R.id.open_btn);
        camera_select = findViewById(R.id.camera_select);
        camera_tv = findViewById(R.id.camera_tv);

        texture = findViewById(R.id.texture);
        texture2 = findViewById(R.id.texture2);
        texture3 = findViewById(R.id.texture3);
        texture4 = findViewById(R.id.texture4);

        textureList.add(texture);
        textureList.add(texture2);
        textureList.add(texture3);
        textureList.add(texture4);

        clientList.add(null);
        clientList.add(null);
        clientList.add(null);
        clientList.add(null);

        checkRecordPermission(this);

        try {
            camera_info.setText(getCameraInformation());
            final String[] logicCameraIds = CameraUtils.getCameraLogicId(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, logicCameraIds);
            camera_select.setAdapter(adapter);
            camera_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCameraId = logicCameraIds[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        switch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraInfo cameraInfo = new CameraInfo.CameraBuilder()
                        .setCameraFront(cameraList.get(nowCamera) ? 1 : 2)
                        .setLogicCameraId(Integer.parseInt(selectedCameraId))
                        .setDefaultBufferSize(new Size(1920, 1080))
                        .setImageBufferSize(new Size(1920, 1080))
                        .build();
                nowShowCameraId = selectedCameraId;
                if(clientList.get(nowCamera).getCamera() != null) {
                    clientList.get(nowCamera).getCamera().switchCamera(cameraInfo);
                }
            }
        });

        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 CameraClient cameraClient = new CameraClient.CameraClientBuilder()
                        .setContext(CameraDemoActivity.this)
                        .setmOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader imageReader) {
                                Image image = imageReader.acquireNextImage();
                                image.close();
                            }
                        })
                        .setmFocusStateCallback(null)
                        .setSurfaceTexture(textureList.get(nowCamera).getSurfaceTexture())
                        .setCameraErrorCallBack(null)
                        .build();

                CameraInfo cameraInfo = new CameraInfo.CameraBuilder()
                        .setCameraFront(1)
                        .setLogicCameraId(Integer.parseInt(selectedCameraId))
                        .setDefaultBufferSize(new Size(1920, 1080))
                        .setImageBufferSize(new Size(1920, 1080))
                        .build();
                nowShowCameraId = selectedCameraId;
                cameraClient.getCamera().openCamera(cameraInfo);
                clientList.remove(nowCamera);
                clientList.add(nowCamera, cameraClient);
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowShowCameraId.equals(selectedCameraId)){
                    camera_tv.post(new Runnable() {
                        @Override
                        public void run() {
                            camera_tv.setText("             请选择不同的相机 ");
                        }
                    });
                    return;
                }
                nowCamera ++ ;
                if(nowCamera > 3){
                    nowCamera = 0;
                }
                CameraClient cameraClient = new CameraClient.CameraClientBuilder()
                        .setContext(CameraDemoActivity.this)
                        .setmOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader imageReader) {
                                Image image = imageReader.acquireNextImage();
                                image.close();
                            }
                        })
                        .setmFocusStateCallback(null)
                        .setSurfaceTexture(textureList.get(nowCamera).getSurfaceTexture())
                        .setCameraErrorCallBack(cameraErrorCallBack)
                        .build();

                CameraInfo cameraInfo = new CameraInfo.CameraBuilder()
                        .setCameraFront(1)
                        .setLogicCameraId(Integer.parseInt(selectedCameraId))
                        .setDefaultBufferSize(new Size(1920, 1080))
                        .setImageBufferSize(new Size(1920, 1080))
                        .build();

                nowShowCameraId = selectedCameraId;
                cameraClient.getCamera().openCamera(cameraInfo);
                clientList.remove(nowCamera);
                clientList.add(nowCamera, cameraClient);
            }
        });

    }

    /**
     * 获取相机相关信息并打印
     */
    private String getCameraInformation() throws CameraAccessException {

        StringBuffer stringBuffer = new StringBuffer();
        String[] logicCameraIds = CameraUtils.getCameraLogicId(this);
        stringBuffer.append("\n================逻辑相机列表==================");
        stringBuffer.append(MessageFormat.format("\n 获取逻辑相机列表 ：  logicList = {0}", Arrays.asList(logicCameraIds)));
        /**
         *      * LEGACY    设备在较旧的Android设备上以向后兼容模式运行，并且功能非常有限
         *      * LIMITED   设备代表基准功能集，并且还可能包含作为的子集的其他功能FULL
         *      * FULL      设备还支持对传感器，闪光灯，镜头和后处理设置进行逐帧手动控制，以及高速率的图像捕获
         *      * LEVEL_3   设备还支持YUV重新处理和RAW图像捕获，以及其他输出流配置
         *      * EXTERNAL  设备类似于LIMITED设备，但有一些例外，例如未报告某些传感器或镜头信息或帧速率较不稳定
         */
        stringBuffer.append("\n================逻辑相机级别==================");
        for (String id : logicCameraIds) {
            int level = CameraUtils.checkHardwareSupport(CameraUtils.getLogicCameraCharacteristics(this, id));
            stringBuffer.append("\n 获取逻辑相机支持的功能级别 ：相机id = " + id + " *** 对应的级别 " + getLevelString(level));
        }

        stringBuffer.append("\n=================物理相机id==================");
        HashMap<String, List<Integer>> physicalIds = CameraUtils.getPhysicalId(this);
        if (physicalIds.isEmpty()) stringBuffer.append("");
        stringBuffer.append("\n 无法获取支持的物理相机");
        for (HashMap.Entry<String, List<Integer>> entry : physicalIds.entrySet()) {
            stringBuffer.append("\n 逻辑相机id = " + entry.getKey() + " 逻辑相机下的物理相机ids =" + entry.getValue());
        }
        stringBuffer.append("\n=================逻辑相机支持的分辨率==================");

        for (String id : logicCameraIds) {
            int front = CameraUtils.getCameraFront(CameraUtils.getLogicCameraCharacteristics(this, id));
            stringBuffer.append("\n 获取逻辑相机相关信息 ：相机id = " + id + " *** 摄像头方向 " + getFrontString(front));
            List<Size> sizeList = CameraUtils.getCameraSizeSupport(CameraUtils.getLogicCameraCharacteristics(this, id), ImageFormat.JPEG);
            stringBuffer.append("\n 获取逻辑相机相关信息 ：相机id = " + id + " *** 摄像头支持输出分辨率 " + sizeList);
            cameraList.add(front == 0);
        }
        return stringBuffer.toString();
    }

    private String getFrontString(int front) {
        switch (front) {
            case CameraCharacteristics.LENS_FACING_BACK:
                return "摄像头设备面对与设备屏幕相反的方向 (后置摄像头)";
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                return "摄像头设备是外部摄像头，相对于设备的屏幕没有固定的朝向 （外部摄像头）";
            case CameraCharacteristics.LENS_FACING_FRONT:
                return "摄像头设备与设备的屏幕朝向相同的方向 （前置摄像头）";
        }
        return "";
    }

    private String getLevelString(int level) {
        switch (level) {
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                return "LEGACY / 设备在较旧的Android设备上以向后兼容模式运行，并且功能非常有限";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                return "LIMITED / 设备代表基准功能集，并且还可能包含作为的子集的其他功能FULL";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                return "FULL / 设备还支持对传感器，闪光灯，镜头和后处理设置进行逐帧手动控制，以及高速率的图像捕获";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                return "LEVEL_3 / 设备还支持YUV重新处理和RAW图像捕获，以及其他输出流配置";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                return "EXTERNAL / 设备类似于LIMITED设备，但有一些例外，例如未报告某些传感器或镜头信息或帧速率较不稳定";
        }
        return "";
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
