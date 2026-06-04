package trader;

import model.Order;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class TraderTask implements Runnable {

    private String traderName;
    private List<Order> orders;
    private BlockingQueue<Order> orderQueue;

    public TraderTask(String traderName,
                      List<Order> orders,
                      BlockingQueue<Order> orderQueue) {

        this.traderName = traderName;
        this.orders = orders;
        this.orderQueue = orderQueue;
    }

    @Override
    public void run() {

        try {

            for (Order order : orders) {

                orderQueue.put(order);

                System.out.println(
                        traderName +
                                " submitted -> " +
                                order
                );

                Thread.sleep(100);
            }

            System.out.println(
                    traderName +
                            " finished submitting orders"
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    traderName +
                            " interrupted"
            );
        }
    }
}