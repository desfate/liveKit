package com.github.desfate.videokit.requests;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;

public class BaseRequest {

    /**
     * 这个认证会过期 所以需要持续请求
     */
    public VodClient getClient(){
        Credential cred = new Credential("AKIDOHYaQxCZT8bpnbdUecKquJRL3UnqAJrB", "B5D7mBJAmIYnY5LKEsGUUFl00kESWKvH");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vod.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new VodClient(cred, "", clientProfile);
    }
}
