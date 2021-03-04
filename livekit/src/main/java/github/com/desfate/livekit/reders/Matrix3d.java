package github.com.desfate.livekit.reders;


import android.opengl.Matrix;

/**
 * Created by bai on 2019/5/21.
 */

public class Matrix3d {
    String TAG = "DualCamera";

    public float[] matrix = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    public Matrix3d() {
        setIdentity();
    }

    public void setIdentity() {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i == j) {
                    matrix[i * 4 + j] = 1;
                }
                else {
                    matrix[i * 4 + j] = 0;
                }
            }
        }
    }

    public void setMatrix(Matrix3d m) {
        for(int i = 0; i < 16; i++) {
            matrix[i] = m.matrix[i];
        }
    }

    public void multply(Matrix3d m) {
        Matrix.multiplyMM(matrix, 0, matrix, 0, m.matrix, 0);
    }

    public void multply(Matrix3d lm, Matrix3d rm) {
        Matrix.multiplyMM(matrix, 0, lm.matrix, 0, rm.matrix, 0);
    }

    public void createMirrorMatrix() {
        Matrix.scaleM(matrix,0, 1, -1, 1);
    }

    public void logMatrix() {
//        Log.e(TAG, "matrix = \n"+matrix[0] + " "+matrix[1] + " "+matrix[2] + " "+matrix[3] +
//                "    \n"+matrix[4] + " "+matrix[5] + " "+matrix[6] + " "+matrix[7] +
//                "    \n"+matrix[8] + " "+matrix[9] + " "+matrix[10] + " "+matrix[11] +
//                "    \n"+matrix[12] + " "+matrix[13] + " "+matrix[14] + " "+matrix[15] );
    }

    public void translate(float x,float y,float z) {//设置沿xyz轴移
        Matrix.translateM(matrix, 0, x, y, z);
    }

    public void rotate(float angle,float x,float y,float z) {//设置绕xyz轴移
        Matrix.rotateM(matrix,0, angle, x, y, z);
    }

    //设置透视投影参数
    public void setProjectFrustum
    (
            float left,		//near面的left
            float right,    //near面的right
            float bottom,   //near面的bottom
            float top,      //near面的top
            float near,		//near面距
            float far       //far面距
    )  {
        Matrix.frustumM(matrix, 0, left, right, bottom, top, near, far);
    }
    //设置正交投影参数
    public void setProjectOrtho (
            float left,		//near面的left
            float right,    //near面的right
            float bottom,   //near面的bottom
            float top,      //near面的top
            float near,		//near面距
            float far       //far面距
    )  {
        Matrix.orthoM(matrix, 0, left, right, bottom, top, near, far);
    }

    //设置摄像
    public void setCamera
    (
            float cx,	//摄像机位置x
            float cy,   //摄像机位置y
            float cz,   //摄像机位置z
            float tx,   //摄像机目标点x
            float ty,   //摄像机目标点y
            float tz,   //摄像机目标点z
            float upx,  //摄像机UP向量X分量
            float upy,  //摄像机UP向量Y分量
            float upz   //摄像机UP向量Z分量
    ) {
        Matrix.setLookAtM (matrix,   0,   cx, cy, cz, 	  tx, ty, tz,   upx, upy, upz);
    }

}
