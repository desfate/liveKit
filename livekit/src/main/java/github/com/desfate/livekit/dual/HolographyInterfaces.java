package github.com.desfate.livekit.dual;

public interface HolographyInterfaces {

    public int getViewpos();

    public int HolographyInit(int x,int y);

    public int HolographyInit(String model,int width, int height);

    public void update(int a, int b);

    public int getx();

    public int gety();

    public void deinitHolography();

}
