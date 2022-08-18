package server.life;
/**
 * @author Eric
 */
public enum AttackTypes {

    MOB_MAGIC(0),
    MOB_PHYSICAL(-1),
    COUNTER(-2),
    OBSTACLE(-3),
    STAT(-4);

    private int type;

    AttackTypes(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
