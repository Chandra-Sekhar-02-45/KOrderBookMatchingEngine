package model;

public class Trade {

    private Order buyOrder;
    private Order sellOrder;
    private int matchedPrice;

    public Trade(Order buyOrder,
                 Order sellOrder,
                 int matchedPrice) {

        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.matchedPrice = matchedPrice;
    }

    public Order getBuyOrder() {
        return buyOrder;
    }

    public Order getSellOrder() {
        return sellOrder;
    }

    public int getMatchedPrice() {
        return matchedPrice;
    }

    @Override
    public String toString() {

        return "Trade {" +
                buyOrder.getTraderName() +
                " <-> " +
                sellOrder.getTraderName() +
                ", Price=" +
                matchedPrice +
                "}";
    }
}