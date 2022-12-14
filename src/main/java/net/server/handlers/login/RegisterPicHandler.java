package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;

import enums.LoginResultType;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import network.packet.CLogin;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import net.server.coordinator.MapleSessionCoordinator;
import net.server.coordinator.MapleSessionCoordinator.AntiMulticlientResult;
import org.apache.mina.core.session.IoSession;

public final class RegisterPicHandler extends AbstractMaplePacketHandler {

    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        return switch (res) {
            case REMOTE_PROCESSING -> 10;
            case REMOTE_LOGGEDIN -> 7;
            case REMOTE_NO_MATCH -> 17;
            case COORDINATOR_ERROR -> 8;
            default -> 9;
        };
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
        
        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();
        
        if (!hwid.matches("[0-9A-F]{12}_[0-9A-F]{8}")) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.WrongGateway.getReason()));
            return;
        }
        
        c.updateMacs(macs);
        c.updateHWID(hwid);
        
        IoSession session = c.getSession();
        AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptGameSession(session, c.getAccID(), hwid);
        if (res != AntiMulticlientResult.SUCCESS) {
            c.announce(CLogin.Packet.onSelectCharacterByVACResult(parseAntiMulticlientError(res)));
            return;
        }
        
        if (c.hasBannedMac() || c.hasBannedHWID()) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
            return;
        }
		
        String pic = slea.readMapleAsciiString();
        if (c.getPic() == null || c.getPic().equals("")) {
            c.setPic(pic);
            
            c.setWorld(server.getCharacterWorld(charId));
            World wserv = c.getWorldServer();
            if(wserv == null || wserv.isWorldCapacityFull()) {
                c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
                return;
            }
            
            String[] socket = server.getInetSocket(c.getWorld(), c.getChannel());
            if(socket == null) {
                c.announce(CLogin.Packet.onSelectCharacterByVACResult(LoginResultType.TooManyConnections.getReason()));
                return;
            }
            
            server.unregisterLoginState(c);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            server.setCharacteridInTransition(session, charId);
            
            try {
                c.announce(CLogin.Packet.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            MapleSessionCoordinator.getInstance().closeSession(c.getSession(), true);
        }
    }
}