package com.github.desfate.videokit.controls;

import com.github.desfate.commonlib.tools.JobExecutor;
import com.github.desfate.videokit.dates.VideoInfoDate;
import com.github.desfate.videokit.requests.VideoListRequest;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.models.MediaInfo;
import com.tencentcloudapi.vod.v20180717.models.SearchMediaResponse;

import java.util.ArrayList;

/**
 * fixme 这不是最终版本  很多东西不能交于移动端完成 这只是测试版本的解决方案
 * 控制移动端请求的控制器
 * 这些接口本来需要服务端对接的  这里直接用客户端请求
 *
 * 需要保证token的安全性
 */
public class VideoRequestControls {
    JobExecutor jobExecutor;
    public VideoRequestControls(){
        jobExecutor = new JobExecutor();
    }
    public void getVideoList(VideoListResponse response){
        jobExecutor.execute(new JobExecutor.Task<SearchMediaResponse>() {
            @Override
            public SearchMediaResponse run() {
                try {
                    return new VideoListRequest().getRequest();
                } catch (TencentCloudSDKException e) {
                    e.printStackTrace();
                }
                return super.run();
            }
            @Override
            public void onMainThread(SearchMediaResponse result) {
                super.onMainThread(result);
                ArrayList<VideoInfoDate> mediaList = new ArrayList<>();
                if(result != null){
                    for(MediaInfo info :result.getMediaInfoSet()){
                        VideoInfoDate data = new VideoInfoDate();
                        if(info.getBasicInfo() != null){
                            data.setVideoPicUrl(info.getBasicInfo().getCoverUrl());
                            data.setVideoName(info.getBasicInfo().getName());
                            data.setVideoPlayUrl(info.getBasicInfo().getMediaUrl());
                        }
                        mediaList.add(data);
                    }
                }
                if(response != null) response.callBack(mediaList);
            }
        });
    }

    public interface VideoListResponse{
        void callBack(ArrayList<VideoInfoDate> result);
    }
}
