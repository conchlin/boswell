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
package server.skills;

import java.util.ArrayList;
import java.util.List;

import server.MapleStatEffect;
import server.life.Element;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PlayerSkill implements Skill {
    private int id;
    private int level;
    protected List<MapleStatEffect> effects = new ArrayList<>();
    protected Element element;
    protected int animationTime, skillType, delay;
    private int job;
    protected boolean invisible = false;
    protected boolean action;

    public PlayerSkill(int id) {
        this.id = id;
        this.job = id / 10000;
    }

    public int getId() {
        return id;
    }

    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    public int getMaxLevel() {
        return effects.size();
    }

    public boolean isFourthJob() {
        if (job >= 2200 && job <= 2218) {
            return job > 2216;
        }
        return job % 10 == 2;
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }
	
	public int getDelay() {
		return delay;
	}
	
	public int getSkillType() {
		return skillType;
	}

    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }

    public boolean getAction() {
        return action;
    }
    
    public boolean isInvisible() {
    	return invisible;
    }
    
    public boolean isActiveSkill() {
    	return (id % 10000) / 1000 == 1;
    }

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void writeSkillInfo(MaplePacketLittleEndianWriter mplew) {
		mplew.writeInt(id);
	}

	@Override
	public void setDelay(int delay) {
		this.delay = delay;
		
	}
    
}