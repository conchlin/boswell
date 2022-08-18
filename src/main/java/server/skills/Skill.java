package server.skills;

import server.life.Element;
import tools.data.output.MaplePacketLittleEndianWriter;

public interface Skill {
	
	public void setId(int id);
	public int getId();
	public void setLevel(int level);
	public int getLevel();
	public Element getElement();
	public int getAnimationTime();
	public void writeSkillInfo(MaplePacketLittleEndianWriter mplew);
	public void setDelay(int delay);
	public int getDelay();
}
