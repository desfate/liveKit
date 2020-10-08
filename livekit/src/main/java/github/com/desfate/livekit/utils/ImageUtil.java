package github.com.desfate.livekit.utils;
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
        System.out.println("123");
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
}
