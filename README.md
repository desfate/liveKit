# liveKit

### 自定义采集，预览，直播推流，观众观看

###### 1）使用Camera2进行的视频数据采集，所以最低版本为24

###### 2）ImageReader获取数据，由于腾讯小直播需要的视频格式为YUV_420P，ImageReader采集的格式为YUV_420_888， 本地支持相关转换（不同手机采样率不同，还需要适配更多机型）

###### 3）OpenGl本地绘制预览，以及本地绘制观众观看的在线视频流



###### 导入方式：

```
implementation 'com.github.desfate:liveKit:0.76'
```

### 推流 LivePushView  extend  GLSurfaceView

```
mLivePushView.setParentLayout(mRootLay); // 设置父布局
LiveConfig config = new LiveConfig();  // 设置直播配置
config.setLivePushType(LiveConfig.LIVE_PUSH_DATA);  // 采用byte[]推流模式
mLivePushView.setLiveConfig(config);  // 添加直播配置
mLivePushView.setLivePushListener(new LiveManager() {
                @Override
                public void startPushByData(byte[] buffer, int w, int h) {
					// 通过buffer推流
                }

                @Override
                public void startPushByTextureId(int textureID, int w, int h) {
					// 通过textureId推流
                }
            });
mLivePushView.startPush();  // 开始推流 （只是个开关）
```



