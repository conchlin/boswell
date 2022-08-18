package server.cashshop;

public enum CashType {
	
	CREDIT(1), AVECOIN(2), PREPAID(3), PREPAID2(4);
	
	private int type;
	
	private CashType(int type) {
		this.type = type;
	}
	
	public int getValue() {
		return type;
	}
	
	public static CashType getByType(int type) {
		for (CashType cash : CashType.values())
			if (cash.getValue() == type)
				return cash;
		return CREDIT;
	}
}