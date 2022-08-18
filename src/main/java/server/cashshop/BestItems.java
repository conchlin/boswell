package server.cashshop;

public class BestItems {
	
	private int category;
	private int gender;
	private int SN;
	
	public BestItems(int category, int gender, int SN) {
		this.category = category;
		this.gender = gender;
		this.SN = SN;
	}
	
	public int getCategory() {
		return category;
	}
	
	public void setCategory(int category) {
		this.category = category;
	}
	
	public int getGender() {
		return gender;
	}
	
	public void setGender(int gender) {
		this.gender = gender;
	}
	
	public int getSN() {
		return SN;
	}
	
	public void setSN(int sN) {
		this.SN = sN;
	}
}
