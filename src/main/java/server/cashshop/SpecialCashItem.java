package server.cashshop;

import java.util.ArrayList;
import java.util.List;

public class SpecialCashItem {

    private int sn;
    private int flag;
    private int itemId;
    private int count;
    private int priority;
    private int price;
    private int period;
    private int maplePoints;
    private int mesos;
    private boolean premiumUser;
    private int gender;
    private boolean sale;
    private int job;
    private int requiredLevel;
    private int cash;
    private int point;
    private int gift;
    private int limit;
    private List<Integer> items;

    public SpecialCashItem(int sn, int flag) {
        this.sn = sn;
        this.flag = flag;
    }

    public int getSN() {
        return sn;
    }

    public int getFlag() {
        return flag;
    }

    /**
     * This sets the itemId
     *
     * @param int
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    /**
     * This returns the itemId
     *
     * @return int
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * This returns the Count
     *
     * @return int
     */
    public int getCount() {
        return count;
    }

    /**
     * This sets the count of the item
     *
     * @param int
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * This returns the Priority
     *
     * @return int
     */
    public int getPriority() {
        return priority;
    }

    /**
     * This sets the priority level
     *
     * @param int
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * This returns the Period
     *
     * @return int
     */
    public int getPeriod() {
        return period;
    }

    /**
     * This sets the period of the item
     *
     * @param int
     */
    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * This returns the Maple Points
     *
     * @return int
     */
    public int getMaplePoints() {
        return maplePoints;
    }

    /**
     * This sets the Maple Point amount
     *
     * @param int
     */
    public void setMaplePoints(int maplePoints) {
        this.maplePoints = maplePoints;
    }

    /**
     * This returns the Mesos
     *
     * @return int
     */
    public int getMesos() {
        return mesos;
    }

    /**
     * This sets the meso amount
     *
     * @param int
     */
    public void setMesos(int mesos) {
        this.mesos = mesos;
    }

    /**
     * This returns if the Package is only for Premium users
     *
     * @return boolean
     */
    public boolean isPremiumUser() {
        return premiumUser;
    }

    /**
     * This sets if the item required can be bought by a premium user
     *
     * @param boolean
     */
    public void setPremiumUser(boolean premiumUser) {
        this.premiumUser = premiumUser;
    }

    /**
     * This returns the Gender
     *
     * @return int
     */
    public int getGender() {
        return gender;
    }

    /**
     * This sets the gender restriction on the item 0 - Male<br\>
     * 1 - Female
     *
     * @param int
     */
    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     * This returns the Sale
     *
     * @return boolean
     */
    public boolean getSale() {
        return sale;
    }

    /**
     * This sets the sale amount of the item
     *
     * @param int
     */
    public void setSale(boolean sale) {
        this.sale = sale;
    }

    /**
     * This returns the job tree
     *
     * @return int
     */
    public int getJob() {
        return job;
    }

    /**
     * This sets the job branch which can buy the item
     *
     * @param int
     */
    public void setJob(int job) {
        this.job = job;
    }

    /**
     * This returns the Required Level
     *
     * @return int
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * This sets the required level of the item
     *
     * @param int
     */
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    /**
     * This returns the Cash
     *
     * @return int
     */
    public int getCash() {
        return cash;
    }

    /**
     * This sets the NX cash amount needed to purchase the item
     *
     * @param int
     */
    public void setCash(int cash) {
        this.cash = cash;
    }

    /**
     * This returns the point
     *
     * @return int
     */
    public int getPoint() {
        return point;
    }

    /**
     * This sets the points needed to purchase the item
     *
     * @param int
     */
    public void setPoint(int point) {
        this.point = point;
    }

    /**
     * This returns the Gift
     *
     * @return int
     */
    public int getGift() {
        return gift;
    }

    /**
     * This sets the gift amount?
     *
     * @param int
     */
    public void setGift(int gift) {
        this.gift = gift;
    }

    /**
     * This returns the limit
     *
     * @return int
     */
    public int getLimit() {
        return limit;
    }

    /**
     * This sets the purchase limit
     *
     * @param int
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * This returns the price
     *
     * @return int
     */
    public int getPrice() {
        return price;
    }

    /**
     * This sets the price of the item
     *
     * @param int
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * This gets the list of item SN inside of a package
     *
     * @return List<Integer>
     */
    public List<Integer> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Integer> items) {
        this.items = items;
    }
}
