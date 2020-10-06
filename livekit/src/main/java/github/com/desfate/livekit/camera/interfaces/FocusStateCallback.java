package github.com.desfate.livekit.camera.interfaces;

public interface FocusStateCallback {
    /**
     * 对焦方式出现变化
     * @param state
     */
    void focusChanged(int state);
}
