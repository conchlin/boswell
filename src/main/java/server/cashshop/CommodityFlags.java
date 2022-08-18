package server.cashshop;

public enum CommodityFlags {

	ITEM_ID(0x1),
	COUNT(0x2),
	PRICE(0x4),
	PRIORITY(0x8),
	PERIOD(0x10),
	MAPLE_POINTS(0x20),
	MESOS(0x40),
	PREMIUM_USER(0x80),
	GENDER(0x100),
	SALE(0x200),
	CLASS(0x400),
	REQUIRED_LEVEL(0x800),
	CASH(0x1000),
	POINT(0x2000),
	GIFT(0x4000),
	PACKAGE_COUNT(0x8000),
	LIMIT(0x10000);
	
	private int flag;
	
	private CommodityFlags(int flag) {
		this.flag = flag;
	}
	
	public int getFlag() {
		return flag;
	}
}
