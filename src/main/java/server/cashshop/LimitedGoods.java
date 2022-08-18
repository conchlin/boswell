package server.cashshop;

public class LimitedGoods {

	private int startSN;
	private int endSN;
	private int goodsCount;
	private int eventSN;
	private int expireDays;
	private int flag;
	private int startDate;
	private int endDate;
	private int startHour;
	private int endHour;
	private int[] daysOfWeek = new int[7];
	
	public LimitedGoods(int startSN, int endSN) {
		this.startSN = startSN;
		this.endSN = endSN;
		for (int i = 0; i < daysOfWeek.length; i++) {
			daysOfWeek[i] = 0;
		}
	}
	
	public int getStartSN() {
		return startSN;
	}
	
	public void setStartSN(int startSN) {
		this.startSN = startSN;
	}
	
	public int getEndSN() {
		return endSN;
	}
	
	public void setEndSN(int endSN) {
		this.endSN = endSN;
	}
	
	public int getGoodsCount() {
		return goodsCount;
	}
	
	public void setGoodsCount(int goodsCount) {
		this.goodsCount = goodsCount;
	}
	
	public int getEventSN() {
		return eventSN;
	}
	
	public void setEventSN(int eventSN) {
		this.eventSN = eventSN;
	}
	
	public int getExpireDays() {
		return expireDays;
	}
	
	public void setExpireDays(int expireDays) {
		this.expireDays = expireDays;
	}
	
	public int getFlag() {
		return flag;
	}
	
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public int getStartDate() {
		return startDate;
	}
	
	public void setStartDate(int startDate) {
		this.startDate = startDate;
	}
	
	public int getEndDate() {
		return endDate;
	}
	
	public void setEndDate(int endDate) {
		this.endDate = endDate;
	}
	
	public int getStartHour() {
		return startHour;
	}
	
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	
	public int getEndHour() {
		return endHour;
	}
	
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}
	
	public int[] getDaysOfWeek() {
		return daysOfWeek;
	}
	
	public void setDaysOfWeek(int[] daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}
}
