package server.stonks;

/**
 *
 * @author Saffron
 */
public class Coin {
    
    public enum Currency {
        BILLION_COIN(0),
        SHELLS(1)
        //LEGO(2),
        //MAGATIA_TECH(3),
        //NLC_GEARS(4),
        //HERBTOWN_SPICES(5)
        ;
        private final int id;

        private Currency(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
    
    public enum Trend {
        STAGNATE(0), BULL(1), BEAR(2);
        private int value = -1;

        private Trend(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    private final String currency;
    private final int price;
    private final int previous;
    private final int dailyBuy;
    private final int dailySell;
    private final int trend;
    
    /**
     * 
     * @param currency - list above in enum
     * @param price - current price
     * @param previous - previous price to calculate percent change
     * @param dailyBuy - amount bought today
     * @param dailySell - amount sold today
     * @param trend - upward, downward, stagnate, etc. probably also something that is 'daily'
    */    
    public Coin(String currency, int price, int previous, int dailyBuy, int dailySell, int trend) {
        this.currency = currency;
        this.price = price;
        this.previous = previous;
        this.dailyBuy = dailyBuy;
        this.dailySell = dailySell;
        this.trend = trend;
    }
    
    /*
    CREATE TABLE exchange_coin (
        currency VARCHAR PRIMARY key,
        price INT,
        previous INT,
        todayBuy INT,
        todaySell INT
        TREND INT
    );
    */
    
    public String getCurrency() {
        return currency;
    }
    
    public int getPrice() {
        return price;
    }
    
    public int getPrevious() {
        return previous;
    }
    
    public int getDailyBuy() {
        return dailyBuy;
    }
    
    public int getDailySell() {
        return dailySell;
    }
    
    public int getMarketTrend() {
        return trend;
    }
}
