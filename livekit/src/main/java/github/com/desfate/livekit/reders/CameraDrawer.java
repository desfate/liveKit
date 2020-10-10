package github.com.desfate.livekit.reders;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraDrawer {

    // Vertex Shader(顶点着色器) 和 Fragment Shader(片元着色器) 是可编程管线
    // 着色器语言 GLSL

    // 顶点着色器： 功能是把输入的数据进行矩阵变换位置,计算光照公式生成逐顶点颜色,生成/变换纹理坐标.并且把位置和纹理坐标这样的参数发送到片段着色器
    // attribute 限定符 一般用于各个顶点各不相同的量。如顶点颜色、坐标等
    // vec4  四维向量？ vPosition
    // vec2  二维向量   inputTextureCoordinate
    // vec2  二维向量   textureCoordinate
    // varying 表示易变量，一般用于顶点着色器传递到片元着色器的量
    private final String VERTEX_SHADER = "" +
            "attribute vec4 vPosition;" +                                    // 顶点着色器输入变量
            "attribute vec2 inputTextureCoordinate;" +                       // 输入texture纹理
            "varying vec2 textureCoordinate;" +                              // 片段着色器输入变量用arying来声明 这是片元着色器需要接收的数据
            "void main()" +
            "{"+
            "gl_Position = vPosition;"+
            "textureCoordinate = inputTextureCoordinate;" +
            "}";

    // 片元着色器：片元着色器的作用是处理由光栅化阶段生成的每个片元，最终计算出每个像素的最终颜色
    // precision mediump float; 设置默认精度 format：precision <精度> <类型>  精度example：highp， mediump， lowp
    private final String FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n"+                // 使用GL_OES_EGL_image_external扩展处理，来增强GLSL
            "precision mediump float;" +                                       // 设置默认精度
            "varying vec2 textureCoordinate;\n" +                              // 纹理坐标
            "uniform samplerExternalOES s_texture;\n" +                        // 定义扩展的的纹理取样器amplerExternalOES
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +  // 读取纹素(纹理的颜色)放到输出变量gl_FragColor上
            "}";

    // square的面积就是整个手机屏幕  数组的坐标顺序为 左上->左下->右下->右上
    private static final float VERTEXES[] = {
            -1.0f, 1.0f,    // 左上
            -1.0f,-1.0f,    // 左下
            1.0f, -1.0f,    // 右下
            1.0f,  1.0f,    // 右上
    };

//    // 后置摄像头使用的纹理坐标  纹理坐标数组  == 逆时针旋转90度后，上下镜像
//    private static final float TEXTURE_BACK[] = {
//            0.0f, 1.0f,     // 左上
//            1.0f, 1.0f,     // 左下
//            1.0f, 0.0f,     // 右下
//            0.0f, 0.0f,     // 右上
//    };
    // 这里是因为后置观看是横屏观看  竖屏观看使用上面这个纹理坐标数组
    // 后置摄像头使用的纹理坐标  纹理坐标数组  == 逆时针旋转90度后，上下镜像
    private static final float TEXTURE_BACK[] = {
            0.0f, 0.0f,     // 左上
            0.0f, 1.0f,     // 左下
            1.0f, 1.0f,     // 右下
            1.0f, 0.0f,     // 右上
    };

    // 前置摄像头使用的纹理坐标  == 逆时针旋转90度
    private static final float TEXTURE_FRONT[] = {
            1.0f, 1.0f,     // 左上
            0.0f, 1.0f,     // 左下
            0.0f, 0.0f,     // 右下
            1.0f, 0.0f,     // 右上
    };

    //

    // opengl es 在绘制一个多边形时，都是用一个基本的图元即三角形拼凑出来的 =.= 但是这里好像是直接整了个矩形
    private static final byte VERTEX_ORDER[] = { 0, 1, 2, 3 }; // order to draw vertices

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mBackTextureBuffer;
    private FloatBuffer mFrontTextureBuffer;
    private ByteBuffer mDrawListBuffer;
    private int mProgram;
    private int mPositionHandle;  //                      顶点着色器句柄
    private int mTextureHandle;   //                      输入texture纹理句柄
    private final int VERTEX_SIZE = 2;
    private final int VERTEX_STRIDE = VERTEX_SIZE * 4;

    public CameraDrawer() {

        // 分配指定长度的bytebuffer 长度为 8 * 4 ？  使用ByteBuffer中的order()方法即可获取该buffer所使用的字节序
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEXES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(VERTEXES).position(0);

        // init float buffer for texture coordinates
        mBackTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_BACK.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mBackTextureBuffer.put(TEXTURE_BACK).position(0);
        mFrontTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FRONT.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFrontTextureBuffer.put(TEXTURE_FRONT).position(0);

        // init byte buffer for draw list
        mDrawListBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length).order(ByteOrder.nativeOrder());
        mDrawListBuffer.put(VERTEX_ORDER).position(0);

        mProgram = OpenGLUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        // 查询由program指定的先前链接的程序对象，用于name指定的属性变量，并返回绑定到该属性变量的通用顶点属性的索引
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
    }

    public void draw(int texture, boolean isFrontCamera) {
        GLES20.glUseProgram(mProgram); // 指定使用的program  //绘制时使用着色程序
        GLES20.glEnable(GLES20.GL_CULL_FACE); // 启动剔除 根据函数glCullFace要求启用隐藏图形材料的面。
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // 激活当前的texture unit
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture); // 绑定纹理
        // 出于性能考虑，所有顶点着色器的属性（Attribute）变量都是关闭的
        GLES20.glEnableVertexAttribArray(mPositionHandle);  // 激活后可以访问 顶点着色器输入变量
        // 1参数指定从索引index开始取数据，与顶点着色器中layout(location=0)对应
        // 2参数指定顶点属性大小
        // 3参数指定数据类型
        // 4参数定义是否希望数据被标准化（归一化），只表示方向不表示大小
        // 5参数是步长（Stride），指定在连续的顶点属性之间的间隔。
        // 6参数是 mVertexBuffer 可以看成相机取值区域
        GLES20.glVertexAttribPointer(mPositionHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);  // 激活输入texture纹理
        if (isFrontCamera) {
//            GLES20.glViewport(0, 0, 1920, 1080);
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mFrontTextureBuffer);
        } else {
//            GLES20.glViewport(0, 0, 1920, 1080);
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mBackTextureBuffer);
        }
        // 真正绘制的操作
        // 1参数 绘制方式 example:
        // GL_POINTS:将传入的顶点坐标作为单独的点绘制
        // GL_LINES: 将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
        // GL_LINE_STRIP:将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
        // GL_LINE_LOOP: 将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线。
        // GL_TRIANGLES: 将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
        // GL_TRIANGLE_STRIP: 将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形
        // GL_TRIANGLE_FAN: 将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
        // 2参数 mode类型连接的顶点的总数
        // 3参数 type为索引值的类型
        // 4参数 indices 指向索引存贮位置的指针
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, mDrawListBuffer);
        //启用或禁用通用顶点属性数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }

    public void draw(int texture, boolean isFrontCamera, int width, int height) {
        GLES20.glUseProgram(mProgram); // 指定使用的program  //绘制时使用着色程序
        GLES20.glEnable(GLES20.GL_CULL_FACE); // 启动剔除 根据函数glCullFace要求启用隐藏图形材料的面。
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // 激活当前的texture unit
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture); // 绑定纹理
        // 出于性能考虑，所有顶点着色器的属性（Attribute）变量都是关闭的
        GLES20.glEnableVertexAttribArray(mPositionHandle);  // 激活后可以访问 顶点着色器输入变量
        // 1参数指定从索引index开始取数据，与顶点着色器中layout(location=0)对应
        // 2参数指定顶点属性大小
        // 3参数指定数据类型
        // 4参数定义是否希望数据被标准化（归一化），只表示方向不表示大小
        // 5参数是步长（Stride），指定在连续的顶点属性之间的间隔。
        // 6参数是 mVertexBuffer 可以看成相机取值区域
        GLES20.glVertexAttribPointer(mPositionHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);  // 激活输入texture纹理
        if (isFrontCamera) {
            GLES20.glViewport(0, 0, width, height);
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mFrontTextureBuffer);
        } else {
            GLES20.glViewport(0, 0, width, height);
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mBackTextureBuffer);
        }
        // 真正绘制的操作
        // 1参数 绘制方式 example:
        // GL_POINTS:将传入的顶点坐标作为单独的点绘制
        // GL_LINES: 将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
        // GL_LINE_STRIP:将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
        // GL_LINE_LOOP: 将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线。
        // GL_TRIANGLES: 将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
        // GL_TRIANGLE_STRIP: 将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形
        // GL_TRIANGLE_FAN: 将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
        // 2参数 mode类型连接的顶点的总数
        // 3参数 type为索引值的类型
        // 4参数 indices 指向索引存贮位置的指针
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, mDrawListBuffer);
        //启用或禁用通用顶点属性数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }
}
