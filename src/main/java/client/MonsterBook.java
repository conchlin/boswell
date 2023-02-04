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
package client;

import enums.UserEffectType;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.database.Statements;
import network.packet.UserLocal;
import network.packet.UserRemote;
import network.packet.context.WvsContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public final class MonsterBook {
    private static final Semaphore semaphore = new Semaphore(10);

    private MapleCharacter owner;

    private int specialCard = 0;
    private int normalCard = 0;
    private int bookLevel = 1;
    private Map<Integer, Integer> cards = new LinkedHashMap<>();
    private Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.BOOK);

    public MonsterBook(MapleCharacter character) {
        owner = character;
    }

    private Set<Entry<Integer, Integer>> getCardSet() {
        lock.lock();
        try {
            return Collections.unmodifiableSet(cards.entrySet());
        } finally {
            lock.unlock();
        }
    }

    public void addCard(final MapleClient c, final int cardid) {
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
                UserRemote.Packet.onRemoteUserEffect(c.getPlayer().getId(), UserEffectType.MONSTERBOOK_PICKUP.getEffect()), false);

        Integer qty;
        lock.lock();
        try {
            qty = cards.get(cardid);

            if (qty != null) {
                if (qty < 5) {
                    cards.put(cardid, qty + 1);
                }
            } else {
                cards.put(cardid, 1);
                qty = 0;

                if (cardid / 1000 >= 2388) {
                    specialCard++;
                } else {
                    normalCard++;
                }
            }
        } finally {
            lock.unlock();
        }

        if (qty < 5) {
            calculateLevel();   // current leveling system only accounts unique cards...

            c.announce(WvsContext.Packet.onSetCard(false, cardid, qty + 1));
            c.announce(UserLocal.Packet.onEffect(UserEffectType.MONSTERBOOK_PICKUP.getEffect(), ""));
        } else {
            c.announce(WvsContext.Packet.onSetCard(true, cardid, 5));
        }
    }

    private void calculateLevel() {
        lock.lock();
        try {
            bookLevel = (int) Math.max(1, Math.sqrt((normalCard + specialCard) / 5));
        } finally {
            lock.unlock();
        }
    }

    public int getBookLevel() {
        lock.lock();
        try {
            return bookLevel;
        } finally {
            lock.unlock();
        }
    }

    public Map<Integer, Integer> getCards() {
        lock.lock();
        try {
            return Collections.unmodifiableMap(cards);
        } finally {
            lock.unlock();
        }
    }

    public int getTotalCards() {
        lock.lock();
        try {
            return specialCard + normalCard;
        } finally {
            lock.unlock();
        }
    }

    public int getNormalCard() {
        lock.lock();
        try {
            return normalCard;
        } finally {
            lock.unlock();
        }
    }

    public int getSpecialCard() {
        lock.lock();
        try {
            return specialCard;
        } finally {
            lock.unlock();
        }
    }

    public void load(Connection con) throws SQLException {
        lock.lock();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT cardid, level FROM monster_book WHERE charid = ? ORDER BY cardid ASC")) {
                ps.setInt(1, owner.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    int cardid, level;
                    while (rs.next()) {
                        cardid = rs.getInt("cardid");
                        level = rs.getInt("level");
                        if (cardid / 1000 >= 2388) {
                            specialCard++;
                        } else {
                            normalCard++;
                        }
                        cards.put(cardid, level);
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        calculateLevel();
    }

    public void saveCards(Connection con) throws SQLException {
        Set<Entry<Integer, Integer>> cardSet = getCardSet();

        if (cardSet.isEmpty()) {
            return;
        }

        Statements.Delete.from("monster_book").where("charid", owner.getId());

        Statements.BatchInsert statement = new Statements.BatchInsert("monster_book");
        for (Entry<Integer, Integer> all : cardSet) {
            statement.add("charid", owner.getId());
            statement.add("cardid", all.getKey());
            statement.add("level", all.getValue());
        }
        statement.execute(con);
    }
}
