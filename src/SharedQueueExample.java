import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedQueueExample {

    static BlockingQueue<String> queue =
            new LinkedBlockingQueue<>();

    public static void main(String[] args) {

        Thread producer = new Thread(() -> {
            try {
                queue.put("Task 1");
                queue.put("Task 2");
                queue.put("Task 3");

                System.out.println("Producer sleeping...");
                Thread.sleep(5000);

                queue.put("Task 4");
                System.out.println("Task 4 added");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                int count = 4;

                while (count-- > 0) {
                    String task = queue.take();

                    System.out.println(
                            Thread.currentThread().getName()
                                    + " processing " + task);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        producer.start();
        consumer.start();
    }
}