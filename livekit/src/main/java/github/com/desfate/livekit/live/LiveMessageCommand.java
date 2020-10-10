package github.com.desfate.livekit.live;
/**
 * 加密命令 发在公屏的对话 但是不显示 被当作命令
 */
public class LiveMessageCommand {

    public final static String key = "&futureMessageCommand&";

    public final static String SWITCH_CAMERA_FRONT = "switchCameraFront";
    public final static String SWITCH_CAMERA_BACK = "switchCameraBack";

    /**
     * 命令加密
     * @param command
     * @return
     */
    public static String addCommand(String command){
        return key + command;
    }

    /**
     * 命令解析
     * @param command
     * @return 如果为空 则不是命令
     */
    public static String resolveCommand(String command){
        if(command.contains(key))
            return command.replace(key, "");
        else
            return "";
    }
}