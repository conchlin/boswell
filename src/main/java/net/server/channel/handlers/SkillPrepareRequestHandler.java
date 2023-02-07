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
package net.server.channel.handlers;

import client.MapleClient;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.ChiefBandit;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.Gunslinger;
import constants.skills.Hero;
import constants.skills.ILArchMage;
import constants.skills.Marksman;
import constants.skills.NightWalker;
import constants.skills.Paladin;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import net.AbstractMaplePacketHandler;
import network.packet.UserRemote;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SkillPrepareRequestHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int skillId = slea.readInt();
        int level = slea.readByte();
        byte flags = slea.readByte();
        int speed = slea.readByte();
        byte aids = slea.readByte();//Mmmk
        switch (skillId) {
            case FPMage.EXPLOSION, FPArchMage.BIG_BANG, ILArchMage.BIG_BANG,
                    Bishop.BIG_BANG, Bowmaster.HURRICANE, Marksman.PIERCING_ARROW,
                    ChiefBandit.CHAKRA, Brawler.CORKSCREW_BLOW, Gunslinger.GRENADE,
                    Corsair.RAPID_FIRE, WindArcher.HURRICANE, NightWalker.POISON_BOMB,
                    ThunderBreaker.CORKSCREW_BLOW, Paladin.MONSTER_MAGNET,
                    DarkKnight.MONSTER_MAGNET, Hero.MONSTER_MAGNET, Evan.FIRE_BREATH, Evan.ICE_BREATH
                    -> c.getPlayer().getMap().broadcastMessage(c.getPlayer(), UserRemote.Packet.onSkillPrepare(c.getPlayer(), skillId, level, flags, speed, aids), false);
            default -> System.out.println(c.getPlayer() + " entered SkillEffectHandler without being handled using " + skillId + ".");
        }
    }
}