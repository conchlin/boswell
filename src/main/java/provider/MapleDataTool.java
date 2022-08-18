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

public class MapleDataTool {

    public static String getString(MapleData data) {
        return (data.getData() + "");
    }

    public static String getString(MapleData data, String def) {
        return (data == null || data.getData() == null) ? def : (data.getData() + "");
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        return getString(data.getChildByPath(path), def);
    }

    public static double getDouble(MapleData data) {
        return ((Double) data.getData());
    }

    public static float getFloat(MapleData data) {
        return ((Float) data.getData());
    }
	
	public static float getFloat(MapleData data, float def) {
		try {
			if(data == null)
				return def;
			
			return (data.getType() == MapleDataType.STRING) ? Float.parseFloat(getString(data)) : getFloat(data);
		} catch(NumberFormatException ex) {
			return def;
		}
	}

    public static int getInt(MapleData data) { //0 = def
        return (data == null || data.getData() == null) ? 0 : ((Integer) data.getData());
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

	public static int getIntConvert(MapleData data) {
		return (data.getType() == MapleDataType.STRING) ? Integer.parseInt(getString(data)) : getInt(data);
	}
	
    public static int getIntConvert(MapleData data, int def) {
        try {
			if(data == null)
				return def;
			
			return (data.getType() == MapleDataType.STRING) ? Integer.parseInt(getString(data)) : getInt(data);
		} catch(NumberFormatException ex) {
			return def;
		}
	}

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        return (d.getType() == MapleDataType.STRING) ? Integer.parseInt(getString(d)) : getInt(d);
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        }
        return (data.getType() == MapleDataType.STRING)
                ? Integer.parseInt(getString(data)) : ((Integer) data.getData());
    }

    public static int getInt(String path, MapleData data, int def) {
        return getInt(data.getChildByPath(path), def);
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            try {
                return Integer.parseInt(getString(d));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def);
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        return ((Point) data.getData());
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        return (pointData == null) ? def : getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        StringBuilder path = new StringBuilder();
        MapleDataEntity myData = data;
        while (myData != null) {
            path.append(myData.getName() + "/" + path);
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}
