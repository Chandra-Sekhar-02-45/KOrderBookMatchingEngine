package engine;

import confirmation.TradeConfirmer;
import model.Order;
import model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MatchingEngine implements Runnable {

    private BlockingQueue<Order> orderQueue;

    private volatile boolean marketOpen = true;

    private List<Order> buyOrders = new ArrayList<>();

    private List<Order> sellOrders = new ArrayList<>();

    private List<Trade> matchedTrades =
            new ArrayList<>();

    private List<CompletableFuture<Void>>
            confirmationFutures =
            new ArrayList<>();

    private ReentrantLock orderBookLock =
            new ReentrantLock();

    private AtomicInteger successfulConfirmations =
            new AtomicInteger();

    private AtomicInteger failedConfirmations =
            new AtomicInteger();

    public MatchingEngine(
            BlockingQueue<Order> orderQueue) {

        this.orderQueue = orderQueue;
    }

    public void closeMarket() {

        marketOpen = false;
    }

    public List<Trade> getMatchedTrades() {

        return matchedTrades;
    }

    public List<CompletableFuture<Void>>
    getConfirmationFutures() {

        return confirmationFutures;
    }

    @Override
    public void run() {

        while (marketOpen ||
                !orderQueue.isEmpty()) {

            try {

                Order order =
                        orderQueue.poll();

                if (order != null) {

                    processOrder(order);
                }

                Thread.sleep(100);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        System.out.println(
                "\nMatching Engine Stopped"
        );
    }

    private void processOrder(
            Order order) {

        try {

            if (orderBookLock.tryLock(
                    50,
                    TimeUnit.MILLISECONDS)) {

                try {

                    if ("BUY".equals(
                            order.getSide())) {

                        processBuyOrder(
                                order);

                    } else {

                        processSellOrder(
                                order);
                    }

                } finally {

                    orderBookLock.unlock();
                }

            } else {

                System.out.println(
                        "Could not acquire lock"
                );
            }

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }

    private void processBuyOrder(
            Order buyOrder) {

        for (Order sellOrder :
                sellOrders) {

            if (buyOrder.getPrice()
                    >= sellOrder.getPrice()) {

                Trade trade =
                        new Trade(
                                buyOrder,
                                sellOrder,
                                sellOrder.getPrice()
                        );

                matchedTrades.add(trade);

                sellOrders.remove(
                        sellOrder);

                System.out.println(
                        "MATCH FOUND -> "
                                + trade
                );

               confirmTradeAsync(trade);
                return;
            }
        }

        buyOrders.add(buyOrder);
    }

    private void processSellOrder(
            Order sellOrder) {

        for (Order buyOrder :
                buyOrders) {

            if (buyOrder.getPrice()
                    >= sellOrder.getPrice()) {

                Trade trade =
                        new Trade(
                                buyOrder,
                                sellOrder,
                                sellOrder.getPrice()
                        );

                matchedTrades.add(trade);

                buyOrders.remove(
                        buyOrder);

                System.out.println(
                        "MATCH FOUND -> "
                                + trade
                );

                confirmTradeAsync(trade);

                return;
            }
        }



        sellOrders.add(sellOrder);


    }

    public int getSuccessfulConfirmations() {
        return successfulConfirmations.get();
    }

    public int getFailedConfirmations() {
        return failedConfirmations.get();
    }

    public int getMatchedTradeCount() {
        return matchedTrades.size();
    }


    public int getRemainingBuyOrders() {
        return buyOrders.size();
    }

    public int getRemainingSellOrders() {
        return sellOrders.size();
    }

    private void confirmTradeAsync(Trade trade) {

        CompletableFuture<Void> future =
                CompletableFuture
                        .supplyAsync(() ->
                                TradeConfirmer
                                        .confirmTrade(trade)
                        )
                        .exceptionally(ex -> {

                            failedConfirmations.incrementAndGet();

                            System.out.println(
                                    "CONFIRMATION FAILED -> "
                                            + trade
                            );

                            return false;
                        })
                        .thenAccept(success -> {

                            if (success) {

                                successfulConfirmations.incrementAndGet();

                                System.out.println(
                                        "CONFIRMED -> "
                                                + trade
                                );
                            }
                        });

        confirmationFutures.add(future);
    }
}