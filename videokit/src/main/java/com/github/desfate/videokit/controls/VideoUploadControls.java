package com.github.desfate.videokit.controls;

import static com.github.desfate.videokit.VideoUploadConstant.CUSTOMER_KEY;

import android.content.Context;

import com.github.desfate.videokit.videoupload.Signature;
import com.github.desfate.videokit.videoupload.TXUGCPublish;
import com.github.desfate.videokit.videoupload.TXUGCPublishTypeDef;

import java.util.Random;

/**
 *  这不是最终版本  很多东西不能交于移动端完成 这只是测试版本的解决方案
 * 控制移动端视频上传的控制器
 */
public class VideoUploadControls implements
        TXUGCPublishTypeDef.ITXVideoPublishListener{

    Context context;
    TXUGCPublish mPublish;
    ProgressCallBack callBack;

    public VideoUploadControls(Context context,  ProgressCallBack callBack){
        this.context = context;
        this.callBack = callBack;
        mPublish = new TXUGCPublish(context, CUSTOMER_KEY);
        mPublish.setListener(this);
    }

    /**
     * 开始视频上传
     * @param mVideoPath 视频地址
     */
    public void beginUpload(String mVideoPath){
        TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
        param.signature = getSignature();
        param.videoPath = mVideoPath;
        int publishCode = mPublish.publishVideo(param); // 开始上传
        if (publishCode != 0) {
//            mResultMsg.setText("发布失败，错误码：" + publishCode);
        }
    }

    /**
     * 暂停视频上传
     */
    public void pauseUpload() {
        if (mPublish != null) {
            mPublish.canclePublish();
        }
    }

    /**
     * 恢复上传
     * @param mVideoPath 视频地址
     */
    public void resumeUpload(String mVideoPath) {
        if (mPublish != null) {
            TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
            param.signature = getSignature();
            param.videoPath = mVideoPath;
            int publishCode = mPublish.publishVideo(param);
            if (publishCode != 0) {
//                mResultMsg.setText("发布失败，错误码：" + publishCode);
            }
        }
    }

    /**
     * 获取签名
     * @return 签名
     */
    private String getSignature(){
        // signature计算规则可参考 https://www.qcloud.com/document/product/266/9221
        Signature sign = new Signature();
        // 设置 App 的云 API 密钥
        sign.setSecretId("AKIDOHYaQxCZT8bpnbdUecKquJRL3UnqAJrB");
        sign.setSecretKey("B5D7mBJAmIYnY5LKEsGUUFl00kESWKvH");
        sign.setCurrentTime(System.currentTimeMillis() / 1000);
        sign.setRandom(new Random().nextInt(java.lang.Integer.MAX_VALUE));
        sign.setSignValidDuration(3600 * 24 * 2); // 签名有效期：2天
        String signature = "";
        try {
            signature = sign.getUploadSignature();
            System.out.println("signature : " + signature);
        } catch (Exception e) {
            System.out.print("获取签名失败");
            e.printStackTrace();
        }
        return signature;
    }

    /**
     * 进度返回
     * @param uploadBytes 当前进度
     * @param totalBytes 总进度
     */
    @Override
    public void onPublishProgress(long uploadBytes, long totalBytes) {
        if(callBack != null) callBack.onProgress(uploadBytes, totalBytes);
    }

    /**
     * 上传完成返回
     * @param result 上传结果
     */
    @Override
    public void onPublishComplete(TXUGCPublishTypeDef.TXPublishResult result) {
        if(callBack != null) callBack.success(result);
    }

    public interface ProgressCallBack {
        void onProgress(long upload, long total);

        void success(TXUGCPublishTypeDef.TXPublishResult result);
    }
}
