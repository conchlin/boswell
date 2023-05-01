package server.cashshop;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import client.inventory.Item;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import database.DatabaseConnection;

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
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_modded_commodity");
            rs = ps.executeQuery();
            while (rs.next()) {
                SpecialCashItem cash = new SpecialCashItem(rs.getInt("sn"), rs.getInt("item_id"));
                cash.setCount(rs.getInt("count"));
                cash.setPriority(rs.getInt("priority"));
                cash.setPeriod(rs.getInt("period"));
                cash.setMaplePoints(rs.getInt("maple_point"));
                cash.setMesos(rs.getInt("mesos"));
                cash.setPremiumUser(rs.getBoolean("premium_user"));
                cash.setRequiredLevel(rs.getInt("required_level"));
                cash.setGender(rs.getInt("gender"));
                cash.setSale(rs.getBoolean("sale"));
                cash.setJob(rs.getInt("class"));
                cash.setLimit(rs.getInt("_limit"));
                cash.setCash(rs.getInt("pb_cash"));
                String[] contents = rs.getString("package_contents").split(",");
                for (String content : contents) {
                    cash.getItems().add(Integer.parseInt(content));
                }
                cash.setPoint(rs.getInt("pb_point"));
                cash.setGift(rs.getInt("pb_gift"));
                specialCashItems.add(cash);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadBlockedItems() {
        blockedCashItems.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_blocked_items");
            rs = ps.executeQuery();
            while (rs.next()) {
                blockedCashItems.add(rs.getInt("sn"));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadDiscountedCategories() {
        discountedCategories.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_discounted_categories");
            rs = ps.executeQuery();
            while (rs.next()) {
                discountedCategories.add(new CategoryDiscount(rs.getInt("category"), rs.getInt("subcategory"), rs.getInt("discount_rate")));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadLimitedGoods() {
        limitedGoods.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_limited_goods");
            rs = ps.executeQuery();
            while (rs.next()) {
                LimitedGoods goods = new LimitedGoods(rs.getInt("start_sn"), rs.getInt("end_sn"));
                goods.setGoodsCount(rs.getInt("count"));
                goods.setEventSN(rs.getInt("event_sn"));
                goods.setExpireDays(rs.getInt("expire"));
                goods.setFlag(rs.getInt("flag"));
                goods.setStartDate(rs.getInt("start_date"));
                goods.setEndDate(rs.getInt("end_date"));
                goods.setStartHour(rs.getInt("start_hour"));
                goods.setEndHour(rs.getInt("end_hour"));
                String[] days = rs.getString("days").split(",");
                int[] daysOfWeek = new int[7];
                for (int i = 0; i < days.length; i++) {
                    daysOfWeek[i] = Integer.parseInt(days[i]);
                }
                goods.setDaysOfWeek(daysOfWeek);

                limitedGoods.add(goods);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadZeroGoods() {
        limitedGoods.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_zero_goods");
            rs = ps.executeQuery();
            while (rs.next()) {
                LimitedGoods goods = new LimitedGoods(rs.getInt("start_sn"), rs.getInt("end_sn"));
                goods.setGoodsCount(rs.getInt("count"));
                goods.setEventSN(rs.getInt("event_sn"));
                goods.setExpireDays(rs.getInt("expire"));
                goods.setFlag(rs.getInt("flag"));
                goods.setStartDate(rs.getInt("start_date"));
                goods.setEndDate(rs.getInt("end_date"));
                goods.setStartHour(rs.getInt("start_hour"));
                goods.setEndHour(rs.getInt("end_hour"));
                String[] days = rs.getString("days").split(",");
                int[] daysOfWeek = new int[7];
                for (int i = 0; i < days.length; i++) {
                    daysOfWeek[i] = Integer.parseInt(days[i]);
                }
                goods.setDaysOfWeek(daysOfWeek);

                limitedGoods.add(goods);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadStock() {
        stock.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_stock");
            rs = ps.executeQuery();
            while (rs.next()) {
                stock.add(new ItemStock(rs.getInt("sn"), rs.getInt("state")));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
