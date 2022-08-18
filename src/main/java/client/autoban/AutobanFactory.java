/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package client.autoban;

import client.MapleCharacter;
import net.server.Server;
import tools.FilePrinter;
import tools.MapleLogger;
import tools.MaplePacketCreator;

/**
 * @author kevintjuh93
 */
public enum AutobanFactory {
    MOB_COUNT,
    GENERAL,
    FIX_DAMAGE,
    DAMAGE_HACK(15, 60 * 1000),
    DISTANCE_HACK(10, 120 * 1000),
    PORTAL_DISTANCE(10, 30000),
    PACKET_EDIT,
    ACC_HACK,
    CREATION_GENERATOR,
    HIGH_HP_HEALING,
    FAST_HP_HEALING(10, 5000),
    FAST_MP_HEALING(10, 5000),
    GACHA_EXP,
    TUBI(20, 15000),
    SHORT_ITEM_VAC,
    ITEM_VAC(20, 15000),
    MOB_VAC,
    FAST_ITEM_PICKUP(5, 30000),
    FAST_ATTACK(10, 30000),
    MPCON(25, 30000),
    SUMMON_DAMAGE(10, 100000),
    ILLEGAL_HP_HEALING(9),
    SKILL_PACKET_EDIT(20), //they get all the points
    ILLEGAL_MP_HEALING(7),
    ATTACKING_WITHOUT_TAKING_DMG, // also known as godmode
    WZ_EDIT;;

    private int points;
    private long expiretime;

    private AutobanFactory() {
        this(1, -1);
    }

    private AutobanFactory(int points) {
        this.points = points;
        this.expiretime = -1;
    }

    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public long getExpire() {
        return expiretime;
    }

    public void addPoint(AutobanManager ban, String reason) {
        ban.addPoint(this, reason);
    }

    public void alert(MapleCharacter chr, String reason) {
        if (chr != null && MapleLogger.ignored.contains(chr.getName())) {
            return;
        }
		if (!chr.isCheater()) {
            Server.getInstance().broadcastGMMessage((chr != null ? chr.getWorld() : 0), MaplePacketCreator.earnTitleMessage((chr != null ? MapleCharacter.makeMapleReadable(chr.getName()) : "") + " caused " + this.name() + " " + reason));
            FilePrinter.print(FilePrinter.AUTOBAN_WARNING, (chr != null ? MapleCharacter.makeMapleReadable(chr.getName()) : "") + " caused " + this.name() + " " + reason);
        }
    }

    public void autoban(MapleCharacter chr, String value) {
        if (!chr.isGM()) {
            chr.autoban(" (" + this.name() + ": " + value + ")", this);
        }
    }
}
