package server.stonks;

import client.MapleCharacter;
import java.sql.Connection;
import java.sql.SQLException;
import server.Statements;
import tools.DatabaseConnection;

/**
 *
 * @author Saffron
 */
public class Wallet {
    private final int charId;
    private final String currency;
    private final int price; // price purchased
    private int amount;
    public int walletId;
    
    Coin coin;
    
    
    public Wallet(int id, int charId, String currency, int price, int amount) {
        this.walletId = id;
        this.charId = charId;
        this.currency = currency;
        this.price = price;
        this.amount = amount;
    }
    
    /*
    CREATE TABLE exchange_wallet (
        id int SERIAL PRIMARY KEY,
        charid INT,
        currency VARCHAR,
        price INT,
        amount INT
    );
    */
    
    public int getWalletId() {
        return walletId;
    }
    
    /*  
    private int getCharId() {
        return charId;
    }
    
    private String getCurrency() {
        return currency;
    }
    
    private int getPrice() {
        return price;
    }
    */
    
    private int getAmount() {
        return amount;
    }
    
    /**
     * create a new purchase record for the wallet. 
     * It can be tracked through it's unique wallet id 
     * to be called from an npc doing a coin exchange
     * @param investor - char doing transaction
     */
    /*public void executeCoinPurchase(MapleCharacter investor) {
        try {
          try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Insert.into("exchange_wallet")
                    .add("charid", investor.getId())
                    .add("currency", coin.getCurrency())
                    .add("price", coin.getPrice())
                    .add("amount", getAmount())
                    .execute(con);
          }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }*/
}
