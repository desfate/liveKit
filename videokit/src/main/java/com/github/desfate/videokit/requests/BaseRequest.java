package com.github.desfate.videokit.requests;

import com.github.desfate.videokit.VideoConfig;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;

public class BaseRequest {

    /**
     * 这个认证会过期 所以需要持续请求
     */
    public VodClient getClient(){
        Credential cred = new Credential(VideoConfig.secretId, VideoConfig.secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vod.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new VodClient(cred, "", clientProfile);
    }
}
