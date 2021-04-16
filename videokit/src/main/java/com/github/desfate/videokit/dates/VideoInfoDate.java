package com.github.desfate.videokit.dates;

/**
 * 视频信息
 */
public class VideoInfoDate {

    private String videoPicUrl;    //  视频预览图
    private String videoPlayUrl;   //  视频播放url
    private String videoName;      //  视频名称
    private String videoAuthor;    //  视频作者

    public String getVideoPicUrl() {
        return videoPicUrl;
    }

    public void setVideoPicUrl(String videoPicUrl) {
        this.videoPicUrl = videoPicUrl;
    }

    public String getVideoPlayUrl() {
        return videoPlayUrl;
    }

    public void setVideoPlayUrl(String videoPlayUrl) {
        this.videoPlayUrl = videoPlayUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoAuthor() {
        return videoAuthor;
    }

    public void setVideoAuthor(String videoAuthor) {
        this.videoAuthor = videoAuthor;
    }
}
