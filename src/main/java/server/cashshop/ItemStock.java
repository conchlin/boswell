package server.cashshop;

public class ItemStock {

	private int SN;
	private int stockState;
	
	public ItemStock(int SN, int stockState) {
		this.SN = SN;
		this.stockState = stockState;
	}
	
	public int getSN() {
		return SN;
	}
	
	public int getStockState() {
		return stockState;
	}
	
}
