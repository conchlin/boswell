package server.cashshop;

public class CategoryDiscount {

	private int category;
	private int subCategory;
	private int discontRate;
	
	public CategoryDiscount(int category, int subCategory, int discountRate) {
		this.category = category;
		this.subCategory = subCategory;
		this.discontRate = discountRate;
	}
	
	/**
	 * @return the category
	 */
	public int getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(int category) {
		this.category = category;
	}
	/**
	 * @return the subCategory
	 */
	public int getSubCategory() {
		return subCategory;
	}
	/**
	 * @param subCategory the subCategory to set
	 */
	public void setSubCategory(int subCategory) {
		this.subCategory = subCategory;
	}
	/**
	 * @return the discontRate
	 */
	public int getDiscontRate() {
		return discontRate;
	}
	/**
	 * @param discontRate the discontRate to set
	 */
	public void setDiscontRate(int discontRate) {
		this.discontRate = discontRate;
	}
	
}
