package github.com.desfate.livekit.utils;

import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;

public class ImageUtil {
    /**
     * 将Y:U:V == 4:2:2的数据转换为nv21
     *
     * @param y      Y 数据
     * @param u      U 数据
     * @param v      V 数据
     * @param nv21   生成的nv21，需要预先分配内存
     * @param stride 步长
     * @param height 图像高度
     */
    public static void yuv422ToYuv420sp(byte[] y, byte[] u, byte[] v, byte[] nv21, int stride, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        // 注意，若length值为 y.length * 3 / 2 会有数组越界的风险，需使用真实数据长度计算
        int length = y.length + u.length / 2 + v.length / 2;
        int uIndex = 0, vIndex = 0;
        for (int i = stride * height; i < length; i += 2) {
            nv21[i] = v[vIndex];
            nv21[i + 1] = u[uIndex];
            vIndex += 2;
            uIndex += 2;
        }
    }

    /**
     * 将Y:U:V == 4:1:1的数据转换为nv21
     *
     * @param y      Y 数据
     * @param u      U 数据
     * @param v      V 数据
     * @param nv21   生成的nv21，需要预先分配内存
     * @param stride 步长
     * @param height 图像高度
     */
    public static void yuv420ToYuv420sp(byte[] y, byte[] u, byte[] v, byte[] nv21, int stride, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        // 注意，若length值为 y.length * 3 / 2 会有数组越界的风险，需使用真实数据长度计算
        int length = y.length + u.length + v.length;
        int uIndex = 0, vIndex = 0;
        for (int i = stride * height; i < length; i++) {
            nv21[i] = v[vIndex++];
            nv21[i + 1] = u[uIndex++];
        }
    }

    /**
     * 将Y:U:V == 4：2：2的数据转换为 4:2:0 yu12 （I420） YU12
     * @param y        Y 数据
     * @param u        U 数据
     * @param v        V 数据
     * @param yu12     生成的 yu12（I420），需要预先分配内存
     * @param stride   步长
     * @param height   图像高度
     */
    public static void yuv422ToYuv420p(byte[] y, byte[] u, byte[] v, byte[] yu12, int stride, int height) {
        if(stride * height == y.length) {  // 防止越界
            System.arraycopy(y, 0, yu12, 0, y.length);  // 先整Y
            if(u.length > y.length / 2) return;  // 数据异常  不处理
            byte[] u2 = new byte[u.length / 2];
            byte[] v2 = new byte[v.length / 2];
            // U 和 V 隔一个取一个
            for(int i = 0; i < u.length - 1; i++){
                if(i % 2 == 0){
                    u2[i / 2] = u[i];
                    v2[i / 2] = v[i];
                }
            }
            System.arraycopy(u2, 0, yu12, y.length, u2.length);
            System.arraycopy(v2, 0, yu12, y.length + (y.length / 4), v2.length);
        }
    }

    /**
     * 宽度减半 转换
     * @param y
     * @param u
     * @param v
     * @param yu12
     * @param stride
     * @param height
     */
    public static void yuv411ToYuv420pHalfWidth(byte[] y, byte[] u, byte[] v, byte[] yu12, int stride, int height){

    }


    /**
     * 将Y:U:V == 4:1:1的数据转换为 yu12（I420）  YU12：亮度(行×列) + U(行×列/4) + V(行×列/4)
     * @param y        Y 数据
     * @param u        U 数据
     * @param v        V 数据
     * @param yu12     生成的 yu12（I420），需要预先分配内存
     * @param stride   步长
     * @param height   图像高度
     */
    public static void yuv420ToYuv420p(byte[] y, byte[] u, byte[] v, byte[] yu12, int stride, int height) {
        if(stride * height == y.length) {
            System.arraycopy(y, 0, yu12, 0, y.length);
            System.arraycopy(u, 0, yu12, y.length, u.length);
            System.arraycopy(v, 0, yu12, y.length + u.length, v.length);
        }
    }


    /***
     * YUV420 转化成 RGB
     */
    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height)
    {
        final int frameSize = width * height;
        int rgb[] = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }


    public static final int YUV420P = 0;
    public static final int YUV420SP = 1;
    public static final int NV21 = 2;
    private static final String TAG = "ImageUtil";
    /***
     * 此方法内注释以640*480为例
     * 未考虑CropRect的
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] getBytesFromImageAsType(Image image, int type) {
        try {
            //获取源数据，如果是YUV格式的数据planes.length = 3
            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }

            //   image.close();

            //根据要求的结果类型进行填充
            switch (type) {
                case YUV420P:
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
                    break;
                case YUV420SP:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = uBytes[i];
                        yuvBytes[dstIndex++] = vBytes[i];
                    }
                    break;
                case NV21:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = vBytes[i];
                        yuvBytes[dstIndex++] = uBytes[i];
                    }
                    break;
            }
            return yuvBytes;
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
        }
        return null;
    }
}
