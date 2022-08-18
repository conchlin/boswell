package server;

import client.MapleCharacter;

public class EffectOperation {

    private MapleCharacter chr;
    private int skillId;
    private int petIndex;
    private int itemId;
    private int quantity;
    private int hp;
    private int wheelAmount;
    private int petEffect;
    private int daysRemaining;
    private boolean condition;
    private boolean success;
    private String path;
    private int questEffect = 0;
    private String questString = "";
    private int questOperation = 1;

    public EffectOperation() {}

    public EffectOperation(MapleCharacter chr) {
        this.chr = chr;
    }

    public MapleCharacter getPlayer() {
        return chr;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getSkillLevel() {
        int level = chr.getSkillLevel(skillId);
        return level > 0 ? level : 1;
    }

    public int getPetIndex() {
        return petIndex;
    }

    public void setPetIndex(int petIndex) {
        this.petIndex = petIndex;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getWheelAmount() {
        return wheelAmount;
    }

    public void setWheelAmount(int wheelAmount) {
        this.wheelAmount = wheelAmount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean hasCondition() {
        return condition;
    }

    public void setCondition(boolean condition) {
        this.condition = condition;
    }

    public int getPetEffect() {
        return petEffect;
    }

    public void setPetEffect(int petEffect) {
        this.petEffect = petEffect;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public int getQuestEffect() {
        return questEffect;
    }

    public void setQuestEffect(int questEffect) {
        this.questEffect = questEffect;
    }

    public String getQuestString() {
        return questString;
    }

    public void setQuestString(String questString) {
        this.questString = questString;
    }

    public int questOperation() {
        return questOperation;
    }

    public void setQuestOperation(int questOperation) {
        this.questOperation = questOperation;
    }

}
