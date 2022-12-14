package enums

enum class CashItemResultType(val result: Int) {
    CouponRedeem(73),
    LoadInventory(75),
    LoadGifts(77),
    LoadWishList(79),
    UpdateWishlist(85),
    ItemPurchase(87),
    ItemGift(94),
    SlotInventoryPurchase(96),
    SlotStoragePurchase(98),
    SlotCharacterPurchase(100),
    ItemRetrieve(104),
    ItemDeposit(106),
    ItemPackage(137), // bundle
    ItemQuest(141)
}