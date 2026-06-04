package confirmation;

import model.Trade;

import java.util.Random;

public class TradeConfirmer {

    private static final Random random =
            new Random();

    public static boolean confirmTrade(
            Trade trade) {

        try {

            Thread.sleep(500);

            if (random.nextInt(10) == 0) {

                throw new RuntimeException(
                        "Confirmation Failed"
                );
            }

            return true;

        } catch (Exception e) {

            throw new RuntimeException(
                    e.getMessage()
            );
        }
    }
}