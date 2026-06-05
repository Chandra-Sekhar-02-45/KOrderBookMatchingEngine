import engine.MatchingEngine;
import model.Order;
import trader.TraderTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {

        BlockingQueue<Order> orderQueue =
                new LinkedBlockingQueue<>();

        MatchingEngine matchingEngine =
                new MatchingEngine(orderQueue);

        Thread engineThread =
                new Thread(matchingEngine);

        engineThread.start();

        List<Order> traderAOrders = Arrays.asList(
                new Order("TRADER_A", "BUY", 102, 10),
                new Order("TRADER_A", "BUY", 105, 5)
        );

        List<Order> traderBOrders = Arrays.asList(
                new Order("TRADER_B", "SELL", 100, 10)
        );

        List<Order> traderCOrders = Arrays.asList(
                new Order("TRADER_C", "BUY", 99, 5)
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(3);

        try {

            Future<?> traderAFuture =
                    executor.submit(
                            new TraderTask(
                                    "TRADER_A",
                                    traderAOrders,
                                    orderQueue
                            )
                    );

            Future<?> traderBFuture =
                    executor.submit(
                            new TraderTask(
                                    "TRADER_B",
                                    traderBOrders,
                                    orderQueue
                            )
                    );

            Future<?> traderCFuture =
                    executor.submit(
                            new TraderTask(
                                    "TRADER_C",
                                    traderCOrders,
                                    orderQueue
                            )
                    );

            traderAFuture.get();
            traderBFuture.get();
            traderCFuture.get();

            System.out.println("\nAll traders finished.");

            matchingEngine.closeMarket();

            engineThread.join();

            CompletableFuture.allOf(
                    matchingEngine
                            .getConfirmationFutures()
                            .toArray(new CompletableFuture[0])
            ).join();

            System.out.println(
                    "\n=========== MARKET SUMMARY ==========="
            );

            System.out.println(
                    "Orders Submitted      : 4"
            );

            System.out.println(
                    "Trades Matched        : "
                            + matchingEngine.getMatchedTradeCount()
            );

            System.out.println(
                    "Trades Confirmed      : "
                            + matchingEngine.getSuccessfulConfirmations()
            );

            System.out.println(
                    "Trades Failed         : "
                            + matchingEngine.getFailedConfirmations()
            );

            System.out.println(
                    "Buy Orders Remaining  : "
                            + matchingEngine.getRemainingBuyOrders()
            );

            System.out.println(
                    "Sell Orders Remaining : "
                            + matchingEngine.getRemainingSellOrders()
            );

            System.out.println(
                    "======================================"
            );

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            executor.shutdown();

            try {

                if (!executor.awaitTermination(
                        5,
                        TimeUnit.SECONDS)) {

                    executor.shutdownNow();
                }

            } catch (InterruptedException e) {

                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}