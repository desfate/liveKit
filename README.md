# liveKit

### 自定义采集，预览，直播推流，观众观看

###### 1）使用Camera2进行的视频数据采集，所以最低版本为24

###### 2）ImageReader获取数据，由于腾讯小直播需要的视频格式为YUV_420P，ImageReader采集的格式为YUV_420_888， 本地支持相关转换（不同手机采样率不同，还需要适配更多机型）

###### 3）OpenGl本地绘制预览，以及本地绘制观众观看的在线视频流

