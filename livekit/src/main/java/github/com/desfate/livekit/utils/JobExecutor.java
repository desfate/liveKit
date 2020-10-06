package github.com.desfate.livekit.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobExecutor {
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(4);
    private ThreadPoolExecutor threadPoolExecutor;  //线程池处理耗时任务
    private Handler handler;

    public JobExecutor(){
        //初始化
        init();
    }

    public void init(){
        if(threadPoolExecutor == null){
            // 1:corePoolSize: 线程池中核心线程的数量
            // 2:maximumPoolSize: 线程池中最大线程数量
            // 3:keepAliveTime: 非核心线程的超时时长
            // 4:unit: keepAliveTime这个参数的单位
            // 5:workQueue: 线程池中的任务队列
            // 6:threadFactory: 为线程池提供创建新线程的功能
            // 7:handler: 拒绝策略，当线程无法执行新任务时
            threadPoolExecutor = new ThreadPoolExecutor(
                    1,
                    4,
                    10,
                    TimeUnit.SECONDS,
                    workQueue,
                    new ThreadPoolExecutor.DiscardOldestPolicy());
            handler = new Handler(Looper.getMainLooper());  //默认肯定是在主线程返回
        }
    }

    public <T> void execute(final Task<T> task){
        if (threadPoolExecutor != null) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        T res = task.run(); // 前置任务 任务结果会返回给主线程（在子线程中执行）
                        JobExecutor.this.postOnMainThread(task, res); // 主线程任务
                        task.onJobThread(res); // 子线程处理最终任务 任务结果不会返回
                    } catch (Exception e) {
                        e.printStackTrace();
                        task.onError(e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 发送到主线程处理任务
     * @param task
     * @param res
     * @param <T>
     */
    public <T> void postOnMainThread(final Task<T> task, final T res){
        handler.post(new Runnable() {
            @Override
            public void run() {
                task.onMainThread(res);
            }
        });
    }


    public static abstract class Task<T> {
        public T run() {
            return null;
        }
        public void onMainThread(T result) {
            // default no implementation
        }

        public void onJobThread(T result) {
            // default no implementation
        }

        public void onError(String msg) {
            // default no implementation
        }
    }
}
