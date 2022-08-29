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

public enum MapleStat {

    SKIN(0x1),
    FACE(0x2),
    HAIR(0x4),
    LEVEL(0x10),
    JOB(0x20),
    STR(0x40),
    DEX(0x80),
    INT(0x100),
    LUK(0x200),
    HP(0x400),
    MAXHP(0x800),
    MP(0x1000),
    MAXMP(0x2000),
    AVAILABLEAP(0x4000),
    AVAILABLESP(0x8000),
    EXP(0x10000),
    FAME(0x20000),
    MESO(0x40000),
    PET(0x180008),
    GACHAEXP(0x200000);
    private final int i;

    private MapleStat(int i) {
        this.i = i;
    }

    public int getValue() {
        return i;
    }

    public static MapleStat getByValue(int value) {
        for (MapleStat stat : MapleStat.values()) {
            if (stat.getValue() == value) {
                return stat;
            }
        }
        return null;
    }

    public static MapleStat getBy5ByteEncoding(int encoded) {
        return switch (encoded) {
            case 64 -> STR;
            case 128 -> DEX;
            case 256 -> INT;
            case 512 -> LUK;
            default -> null;
        };
    }

    public static MapleStat getByString(String type) {
        return switch (type) {
            case "SKIN" -> SKIN;
            case "FACE" -> FACE;
            case "HAIR" -> HAIR;
            case "LEVEL" -> LEVEL;
            case "JOB" -> JOB;
            case "STR" -> STR;
            case "DEX" -> DEX;
            case "INT" -> INT;
            case "LUK" -> LUK;
            case "HP" -> HP;
            case "MAXHP" -> MAXHP;
            case "MP" -> MP;
            case "MAXMP" -> MAXMP;
            case "AVAILABLEAP" -> AVAILABLEAP;
            case "AVAILABLESP" -> AVAILABLESP;
            case "EXP" -> EXP;
            case "FAME" -> FAME;
            case "MESO" -> MESO;
            case "PET" -> PET;
            default -> null;
        };
    }
}
