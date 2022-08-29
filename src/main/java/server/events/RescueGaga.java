/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.events;

import client.MapleCharacter;
import server.skills.SkillFactory;

/**
 *
 * @author kevintjuh93
 */
public class RescueGaga extends MapleEvents {
        private byte fallen;
        private int completed;

        public RescueGaga(int completed) {
            super();
            this.completed = completed;
            this.fallen = 0;
        }

        public int fallAndGet() {
            fallen++;
            if (fallen > 3) {
                fallen = 0;
                return 4;
            }
            return fallen;
        }

        public byte getFallen() {
            return fallen;
        }

        public int getCompleted() {
            return completed;
        }

        public void complete() {
            completed++;
        }
        
        public int getInfo() {
            return getCompleted();
        }

        public void giveSkill(MapleCharacter chr) {
            int skillid = switch (chr.getJobType()) {
                case 0 -> 1013;
                case 1, 2 -> 10001014;
                default -> 0;
            };
            long expiration = (System.currentTimeMillis() + (long) (3600 * 24 * 20 * 1000));//20 days
            if (completed < 20) {
                chr.changeSkillLevel(SkillFactory.getSkill(skillid), (byte) 1, 1, expiration);
                chr.changeSkillLevel(SkillFactory.getSkill(skillid + 1), (byte) 1, 1, expiration);
                chr.changeSkillLevel(SkillFactory.getSkill(skillid + 2), (byte) 1, 1, expiration);
            } else {
                chr.changeSkillLevel(SkillFactory.getSkill(skillid), (byte) 2, 2, chr.getSkillExpiration(skillid));
            }
        }
}
