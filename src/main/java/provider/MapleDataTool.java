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

package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;

import provider.wz.MapleDataType;
import tools.ObjectParser;

public class MapleDataTool {

    public static String getString(MapleData data) {
        if (data == null) return null;
        Object d = data.getData();
        if (d == null) return null;
        return ((String) d);
    }

    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((String) data.getData());
        }
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        if (path == null || data == null) return def;
        MapleData d = data.getChildByPath(path);
        if (d == null) return def;
        return getString(d, def);
    }

    public static double getDouble(MapleData data) {
        if (data == null || data.getData() == null) return 0;
        if (data.getType().equals(MapleDataType.STRING)) {
            Double in = ObjectParser.isDouble(getString(data));
            if (in == null) return 0;
            else return in;
        }
        Double in = ((Double) data.getData());
        if (in == null) return 0;
        else return in.doubleValue();
    }

    public static double getDouble(String path, MapleData data) {
        return getDouble(data.getChildByPath(path));
    }

    public static float getFloat(MapleData data) {
        if (data == null || data.getData() == null) return 0;
        if (data.getType().equals(MapleDataType.STRING)) {
            Float in = ObjectParser.isFloat(getString(data));
            if (in == null) return 0;
            else return in;
        } else if (data.getType().equals(MapleDataType.DOUBLE)) return (float) getDouble(data);
        Float in = ((Float) data.getData());
        if (in == null) return 0;
        else return in.floatValue();
    }

    public static float getFloat(String path, MapleData data, float def) {
        if (path == null || data == null) return def;
        MapleData d = data.getChildByPath(path);
        if (d == null) return def;
        return getFloat(d, def);
    }

    public static float getFloat(String path, MapleData data) {
        return getFloat(data.getChildByPath(path));
    }

    public static float getFloat(MapleData data, float def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            try {
                return Float.parseFloat(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            Float in = ((Float) data.getData());
            if (in == null) return def;
            else return in.floatValue();
        }
    }

    public static int getInt(MapleData data) {
        if (data == null || data.getData() == null) return 0;
        if (data.getType().equals(MapleDataType.STRING)) {
            Integer in = ObjectParser.isInt(getString(data));
            if (in == null) return 0;
            else return in;
        }
        Integer in = ((Integer) data.getData());
        if (in == null) return 0;
        else return in.intValue();
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getIntConvert(MapleData data) {
        if (data == null) return 0;
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        } else {
            return getInt(d);
        }
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            try {
                return Integer.parseInt(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else if (data.getType() == MapleDataType.SHORT) {
            return getShort(data, (short) def);
        } else {
            Integer in = ((Integer) data.getData());
            if (in == null) return def;
            else return in.intValue();
        }
    }

    public static int getInt(String path, MapleData data, int def) {
        if (data == null) return def;
        MapleData d = data.getChildByPath(path);
        if (d == null) return def;
        return getInt(d, def);
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) return def;
        MapleData d = data.getChildByPath(path);
        if (d == null) return def;
        return getInt(d, def);
    }

    public static short getShort(MapleData data) {
        return getShort(data, (short) 0);
    }

    public static short getShort(MapleData data, short def) {
        if (data == null || data.getData() == null) return def;
        if (data.getType() == MapleDataType.STRING) {
            try {
                return Short.parseShort(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else if (data.getType() == MapleDataType.SHORT) {
            Short in = ((Short) data.getData());
            if (in == null) return def;
            else return in.shortValue();
        } else if (data.getType() == MapleDataType.INT) {
            Integer in = ((Integer) data.getData());
            if (in == null) return def;
            else return in.shortValue();
        } else {
            System.out.println("Trying to get a short when its: " + data.getType().name());
            return def;
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        if (data == null) return null;
        Object point = data.getData();
        if (point != null) return ((Point) point);
        else return null;
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) return def;
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        StringBuilder path = new StringBuilder();
        MapleDataEntity myData = data;
        while (myData != null) {
            path.insert(0, myData.getName() + "/");
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}