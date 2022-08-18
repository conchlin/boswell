package server.life;

public enum MobSpawnType {
    EFFECT(0),
    NORMAL(-1),
    REGEN(-2),
    REVIVED(-3),
    SUSPENDED(-4), //invisible & not targetable
    DELAY(-5);

    private int type;

    private MobSpawnType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}

