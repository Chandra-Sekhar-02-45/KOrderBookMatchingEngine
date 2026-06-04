package engine;

import model.Order;
import model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class MatchingEngine implements Runnable {

    private BlockingQueue<Order> orderQueue;

    private volatile boolean marketOpen = true;

    private List<Order> buyOrders = new ArrayList<>();
    private List<Order> sellOrders = new ArrayList<>();

    private List<Trade> matchedTrades = new ArrayList<>();

    public MatchingEngine(BlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    public void closeMarket() {
        marketOpen = false;
    }

    public List<Trade> getMatchedTrades() {
        return matchedTrades;
    }

    @Override
    public void run() {

        while (marketOpen || !orderQueue.isEmpty()) {

            try {

                Order order = orderQueue.poll();

                if (order != null) {

                    processOrder(order);
                }

                Thread.sleep(100);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nMatching Engine Stopped");
    }

    private void processOrder(Order order) {

        if ("BUY".equals(order.getSide())) {

            processBuyOrder(order);

        } else {

            processSellOrder(order);
        }
    }

    private void processBuyOrder(Order buyOrder) {

        for (Order sellOrder : sellOrders) {

            if (buyOrder.getPrice() >= sellOrder.getPrice()) {

                Trade trade =
                        new Trade(
                                buyOrder,
                                sellOrder,
                                sellOrder.getPrice()
                        );

                matchedTrades.add(trade);

                sellOrders.remove(sellOrder);

                System.out.println(
                        "MATCH FOUND -> " + trade
                );

                return;
            }
        }

        buyOrders.add(buyOrder);
    }

    private void processSellOrder(Order sellOrder) {

        for (Order buyOrder : buyOrders) {

            if (buyOrder.getPrice() >= sellOrder.getPrice()) {

                Trade trade =
                        new Trade(
                                buyOrder,
                                sellOrder,
                                sellOrder.getPrice()
                        );

                matchedTrades.add(trade);

                buyOrders.remove(buyOrder);

                System.out.println(
                        "MATCH FOUND -> " + trade
                );

                return;
            }
        }

        sellOrders.add(sellOrder);
    }
}