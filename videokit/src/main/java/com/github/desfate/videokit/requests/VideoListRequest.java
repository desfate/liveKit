package com.github.desfate.videokit.requests;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.models.SearchMediaRequest;
import com.tencentcloudapi.vod.v20180717.models.SearchMediaResponse;

public class VideoListRequest extends BaseRequest {

    public SearchMediaResponse getRequest() throws TencentCloudSDKException {
        SearchMediaRequest req = new SearchMediaRequest();
        return getClient().SearchMedia(req);
    }
}
