package github.com.desfate.livekit.camera.news;

import android.util.Size;

public class CameraInfo {

    private int cameraFront = 1; //        1: 前置摄像头 2： 后置摄像头

    private int logicCameraId = 1; //      选择的逻辑相机id

    private int physicsCameraId = 0; //    选择的物理相机id

    private Size defaultBufferSize; //     SurfaceTexture 默认的采集大小

    private Size ImageBufferSize; //       ImageReader 需要回调的图片大小

    private int state = 1;//               1: normal 2: dual  (这个是为了适配双摄的情况 ！！！ 当前的解决方案是单逻辑摄像头唤起)



    public CameraInfo() {
        throw new RuntimeException("please user builder model to init");
    }

    private CameraInfo(CameraBuilder builder) {
        cameraFront = builder.cameraFront;
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

        private int cameraFront = 1; //        1: 前置摄像头 2： 后置摄像头

        private int logicCameraId = 1; //      选择的逻辑相机id

        private int physicsCameraId = 0; //    选择的物理相机id

        private Size defaultBufferSize; //     SurfaceTexture 默认的采集大小

        private Size imageBufferSize; //       ImageReader 需要回调的图片大小

        private int state = 1; //              1: normal 2: dual  (这个是为了适配双摄的情况 ！！！ 当前的解决方案是单逻辑摄像头唤起)


        public CameraBuilder setCameraFront(int cameraFront) {
            this.cameraFront = cameraFront;
            return this;
        }


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

        public CameraBuilder setState(int state) {
            this.state = state;
            return this;
        }

        public CameraInfo build() {
            return new CameraInfo(this);}
    }

    public int getCameraFront() {
        return cameraFront;
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

    public int getState() {
        return state;
    }
}
