package client.listeners;

import java.util.EventListener;

public interface MobKilledListener extends EventListener {

    void update(MobKilledEvent event);

}
