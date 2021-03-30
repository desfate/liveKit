package github.com.desfate.livekit.camera.news;

import android.util.Size;

import github.com.desfate.livekit.CameraConstant;

public class CameraInfo {

    private int logicCameraId = 1; //      选择的逻辑相机id

    private int physicsCameraId = 0; //    选择的物理相机id

    private Size defaultBufferSize; //     SurfaceTexture 默认的采集大小

    private Size ImageBufferSize; //       ImageReader 需要回调的图片大小

    // 这个状态位包含了摄像头前后信息以及是否为双摄 （其实也包含了相机的逻辑id信息  但是这里还是分开处理）
    private CameraConstant.CameraState state = CameraConstant.CameraState.CAMERA_DUAL_FRONT;

    /**
     * 当前是否是前置
     * @return true 是前置 false 不是前置
     */
    public boolean isFront(){
        return state ==  CameraConstant.CameraState.CAMERA_FRONT || state == CameraConstant.CameraState.CAMERA_DUAL_FRONT;
    }


    public CameraInfo() {
        throw new RuntimeException("please user builder model to init");
    }

    private CameraInfo(CameraBuilder builder) {
        logicCameraId = builder.logicCameraId;
        physicsCameraId = builder.physicsCameraId;
        defaultBufferSize = builder.defaultBufferSize;
        ImageBufferSize = builder.imageBufferSize;
        state = builder.state;
    }


    /**
     * 相机开启时需要的一系列数据
     *
     * 采用Builder模式
     *
     */
    public static final class CameraBuilder {

        private int logicCameraId = 1; //      选择的逻辑相机id

        private int physicsCameraId = 0; //    选择的物理相机id

        private Size defaultBufferSize; //     SurfaceTexture 默认的采集大小

        private Size imageBufferSize; //       ImageReader 需要回调的图片大小

        private CameraConstant.CameraState state = CameraConstant.CameraState.CAMERA_DUAL_FRONT;


        public CameraBuilder setLogicCameraId(int logicCameraId) {
            this.logicCameraId = logicCameraId;
            return this;
        }


        public CameraBuilder setPhysicsCameraId(int physicsCameraId) {
            this.physicsCameraId = physicsCameraId;
            return this;
        }

        public CameraBuilder setDefaultBufferSize(Size defaultBufferSize) {
            this.defaultBufferSize = defaultBufferSize;
            return this;
        }

        public CameraBuilder setImageBufferSize(Size imageBufferSize) {
            this.imageBufferSize = imageBufferSize;
            return this;
        }

        public CameraBuilder setState(CameraConstant.CameraState state) {
            this.state = state;
            return this;
        }

        public CameraInfo build() {
            return new CameraInfo(this);}
    }

    public int getLogicCameraId() {
        return logicCameraId;
    }

    public int getPhysicsCameraId() {
        return physicsCameraId;
    }

    public Size getDefaultBufferSize() {
        return defaultBufferSize;
    }

    public Size getImageBufferSize() {
        return ImageBufferSize;
    }

    public CameraConstant.CameraState getState() {
        return state;
    }
}
