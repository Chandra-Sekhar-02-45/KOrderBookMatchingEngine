public class TestThread extends Thread {
    public static void main(String[] args) throws InterruptedException {
        TestThread t1 = new TestThread();
        TestThread t2 = new TestThread();
        TestThread t3 = new TestThread();

        t1.start();
        t1.join();
        t2.start();
        t3.start();
    }

    @Override
    public void run() {
        System.out.println("Thread : " + Thread.currentThread().getName());
    }

}
