package client.inventory;

import java.util.HashMap;
import java.util.Map;

public class LevelUpInformation {

    private Map<String, Integer> stats = new HashMap<>();

    public LevelUpInformation() {}

    public Map<String, Integer> getStats() {
            return stats;
    }

}