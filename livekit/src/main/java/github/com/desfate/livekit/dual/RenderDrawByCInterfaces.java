package github.com.desfate.livekit.dual;

public interface RenderDrawByCInterfaces {

    public int drawRender(int vertexPos, int texcoordPos);

    public int drawRender2D(int vertexPos, int texcoordPos);

    public int drawRender2DR(int vertexPos, int texcoordPos);

    public int drawRender2DTop(int vertexPos, int texcoordPos);

    public int drawRender2DBottom(int vertexPos, int texcoordPos);
}
