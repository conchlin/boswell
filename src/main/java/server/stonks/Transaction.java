
package server.stonks;

/**
 *
 * @author Saffron
 */
public class Transaction {
    private final int id;
    private final int charid;
    private String action; // buy/sell
    private final int amount;
    private final int profit;
    private final long timestamp;
    
    // purely used for record keeping
            
    public Transaction(int id, int charid, String action, int amount, int profit, long timestamp) {
        this.id = id;
        this.charid = charid;
        this.action = action;
        this.amount = amount;
        this.profit = profit;
        this.timestamp = timestamp;
    }
    
    /*
    CREATE TABLE exchange_transactions(
        id SERIAL PRIMARY key,
        charid int,
        action VARCHAR,
        amount int,
        profit int,
        timestamp TIMESTAMP
    );
    */
    
    // i think coin would be the best place to calculate profit and call this method
    /*public void logTransaction(int cid, boolean purchase, int amount, int profit) {
        String actionStr = purchase ? "BUY" : "SELL";
         try {
            try (Connection con = DatabaseConnection.getConnection()) {
                Statements.Insert.into("exchange_transactions")
                .add("charid", cid)
                .add("action", actionStr)
                .add("amount", amount)
                .add("profit", profit)
                    .execute(con);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }*/
}
