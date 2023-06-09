package server.cashshop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import client.inventory.Item;
import database.tables.CashShopTbl;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class CashItemFactory {

    private static Map<Integer, CashItem> items = new HashMap<>();
    private static Map<Integer, List<Integer>> packages = new HashMap<>();
    private static Set<SpecialCashItem> specialCashItems = new HashSet<>();
    private static Set<Integer> blockedCashItems = new HashSet<>();
    private static List<CategoryDiscount> discountedCategories = new ArrayList<>();
    private static Set<LimitedGoods> limitedGoods = new HashSet<>();
    private static Set<LimitedGoods> zeroGoods = new HashSet<>();
    private static Set<ItemStock> stock = new HashSet<>();
    private static Set<BestItems> bestItems = new HashSet<>();
    private static final List<Integer> randomitemids = new ArrayList<>();

    public static void loadCashItems() {
        MapleDataProvider etc = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));
        for (MapleData item : etc.getData("Commodity.img").getChildren()) {
            int sn = MapleDataTool.getIntConvert("SN", item);
            int itemId = MapleDataTool.getIntConvert("ItemId", item);
            int price = MapleDataTool.getIntConvert("Price", item, 0);
            long period = MapleDataTool.getIntConvert("Period", item, 1);
            short count = (short) MapleDataTool.getIntConvert("Count", item, 1);
            short gender = (short) MapleDataTool.getIntConvert("Gender", item, 0);
            boolean onSale = MapleDataTool.getIntConvert("OnSale", item, 0) == 1;
            items.put(sn, new CashItem(sn, itemId, price, period, count, gender, onSale));
        }
        for (MapleData cashPackage : etc.getData("CashPackage.img").getChildren()) {
            List<Integer> cPackage = new ArrayList<>();
            for (MapleData item : cashPackage.getChildByPath("SN").getChildren()) {
                cPackage.add(Integer.parseInt(item.getData().toString()));
            }
            packages.put(Integer.parseInt(cashPackage.getName()), cPackage);
        }
        reloadCashShop();
    }

    public static void reloadCashShop() {
        loadSpecialCashItems();
        loadBlockedItems();
        loadDiscountedCategories();
        loadLimitedGoods();
        loadStock();
        loadZeroGoods();
    }

    public static void loadSpecialCashItems() {
        specialCashItems.clear();
        ArrayList<SpecialCashItem> specialItems = CashShopTbl.loadSpecialCSItems();
        specialCashItems.addAll(specialItems);
    }

    public static void loadBlockedItems() {
        blockedCashItems.clear();
        ArrayList<Integer> blockedItems = CashShopTbl.loadBlockedItems();
        blockedCashItems.addAll(blockedItems);
    }

    public static void loadDiscountedCategories() {
        discountedCategories.clear();
        ArrayList<CategoryDiscount> discounts = CashShopTbl.loadDiscountedCategories();
        discountedCategories.addAll(discounts);
    }

    public static void loadLimitedGoods() {
        limitedGoods.clear();
        ArrayList<LimitedGoods> goods = CashShopTbl.loadGoods("limited");
        limitedGoods.addAll(goods);
    }

    public static void loadZeroGoods() {
        limitedGoods.clear();
        ArrayList<LimitedGoods> goods = CashShopTbl.loadGoods("zero");
        limitedGoods.addAll(goods);
    }

    public static void loadStock() {
        stock.clear();
        ArrayList<ItemStock> itemStock = CashShopTbl.loadStock();
        stock.addAll(itemStock);
    }

    public static CashItem getRandomCashItem() {
        if (randomitemids.isEmpty()) {
            return null;
        }

        int rnd = (int) (Math.random() * randomitemids.size());
        return items.get(randomitemids.get(rnd));
    }

    public static CashItem getItem(int sn) {
        return items.get(sn);
    }

    public static List<Item> getPackage(int itemId) {
        List<Item> cashPackage = new ArrayList<>();
        for (int sn : packages.get(itemId)) {
            cashPackage.add(getItem(sn).toItem());
        }
        return cashPackage;
    }

    public static boolean isPackage(int itemId) {
        return packages.containsKey(itemId);
    }

    public static Set<SpecialCashItem> getSpecialCashItems() {
        return specialCashItems;
    }

    /**
     * This returns a set of blocked cash items
     *
     * @return Set<Integer>
     */
    public static Set<Integer> getBlockedCashItems() {
        return blockedCashItems;
    }

    /**
     * This returns the list of discounted categories in the cashshop
     *
     * @return List<CategoryDiscount>
     */
    public static List<CategoryDiscount> getDiscountedCategories() {
        return discountedCategories;
    }

    /**
     * This returns the list of the limited goods in the cashshop
     *
     * @return Set<LimitedGoods>
     *
     */
    public static Set<LimitedGoods> getLimitedGoods() {
        return limitedGoods;
    }

    /**
     * This returns the list of the limited goods in the cashshop
     *
     * @return Set<LimitedGoods>
     *
     */
    public static Set<LimitedGoods> getZeroGoods() {
        return zeroGoods;
    }

    /**
     * This returns the stock information of a cash item
     *
     * @return Set<ItemStock>
     *
     */
    public static Set<ItemStock> getStock() {
        return stock;
    }

    /**
     * This returns the information for the best items
     *
     * @return Set<BestItems>
     *
     */
    public static Set<BestItems> getBestItems() {
        return bestItems;
    }
}
