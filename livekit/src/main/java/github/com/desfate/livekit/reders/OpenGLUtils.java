package github.com.desfate.livekit.reders;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLUtils {

    private static final String TAG = "OpenGLUtils";

    public static int getExternalOESTextureID() {
        int[] texture = new int[1];
        texture[0] = new Random().nextInt(10000);
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    // 指定要创建的着色器的类型 返回一个可以引用的非零值（shader ID）
    // GL_VERTEX_SHADER:在可编程顶点处理器上运行的着色器
    // GL_FRAGMENT_SHADER:可编程片段处理器上运行
    public static int loadShader(int type, String source) {
        // 1. create shader
        int shader = GLES20.glCreateShader(type);
        // 如果创建着色器对象时发生错误，则此函数返回0。
        if (shader == GLES20.GL_NONE) {
            Log.e(TAG, "create shared failed! type: " + type);
            return GLES20.GL_NONE;
        }
        // 2. load shader source
        // 替换着色器对象中的源代码
        // shader 要被替换源代码的着色器对象的句柄（ID）
        // source 指定指向包含要加载到着色器的源代码的字符串的指针数组
        GLES20.glShaderSource(shader, source);
        // 3. compile shared source
        // 编译一个着色器对象
        // 如果着色器编译时没有错误并且可以使用，则此值将设置为GL_TRUE，否则将设置为GL_FALSE
        GLES20.glCompileShader(shader);
        // 4. check compile status
        // 查询编译结果
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GLES20.GL_FALSE) { // compile failed
            Log.e(TAG, "Error compiling shader. type: " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader); // delete shader
            shader = GLES20.GL_NONE;
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        // 1. load shader
        // 生成一个顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == GLES20.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ");
            return GLES20.GL_NONE;
        }
        // 生成一个片段着色器
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == GLES20.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ");
            return GLES20.GL_NONE;
        }
        // 2. create gl program
        int program = GLES20.glCreateProgram();
        if (program == GLES20.GL_NONE) {
            Log.e(TAG, "create program failed! ");
            return GLES20.GL_NONE;
        }
        // 3. attach shader
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // we can delete shader after attach
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        // 4. link program
        GLES20.glLinkProgram(program);
        // 5. check link status
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES20.GL_FALSE) { // link failed
            Log.e(TAG, "Error link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program); // delete program
            return GLES20.GL_NONE;
        }
        return program;
    }

    public static String loadFromAssets(String fileName, Resources resources) {
        String result = null;
        try {
            InputStream is = resources.getAssets().open(fileName);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            is.close();
            result = new String(data, "UTF-8");
            result = result.replace("\\r\\n", "\\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
