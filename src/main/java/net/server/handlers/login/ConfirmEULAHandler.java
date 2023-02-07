package net.server.handlers.login;

import client.MapleClient;
import enums.LoginResultType;
import net.AbstractMaplePacketHandler;
import network.packet.CLogin;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public final class ConfirmEULAHandler extends AbstractMaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() == 0 || slea.readByte() != 1 || c.acceptToS()) {
            c.disconnect(false, false);//Client dc's but just because I am cool I do this (:
            return;
        }
        if (c.finishLogin() == 0) {
            c.announce(CLogin.Packet.getAuthSuccess(c));
        } else {
            c.announce(CLogin.Packet.getLoginFailed(LoginResultType.SystemError2.getReason()));//shouldn't happen XD
        }
    }
}
