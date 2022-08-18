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
package client.status;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import server.skills.MobSkill;
import server.skills.PlayerSkill;
import tools.ArrayMap;

public class MonsterStatusEffect {

    private Map<MonsterStatus, Integer> stati;
    private PlayerSkill skill;
    private MobSkill mobskill;
    private boolean monsterSkill;
    private ScheduledFuture<?> cancelTask;
    private ScheduledFuture<?> damageSchedule;
    private long duration;

    public MonsterStatusEffect(Map<MonsterStatus, Integer> stati, PlayerSkill skillId, MobSkill mobskill, boolean monsterSkill, long duration) {
        this.stati = new ArrayMap<>(stati);
        this.skill = skillId;
        this.monsterSkill = monsterSkill;
        this.mobskill = mobskill;
        this.duration = System.currentTimeMillis() + duration;
    }

    public Map<MonsterStatus, Integer> getStati() {
        return stati;
    }

    public Integer setValue(MonsterStatus status, Integer newVal) {
        return stati.put(status, newVal);
    }

    public PlayerSkill getPlayerSkill() {
        return skill;
    }

    public boolean isMonsterSkill() {
        return monsterSkill;
    }

    public final void cancelTask() {
        if (cancelTask != null) {
            cancelTask.cancel(false);
        }
        cancelTask = null;
    }

    public ScheduledFuture<?> getCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(ScheduledFuture<?> cancelTask) {
        this.cancelTask = cancelTask;
    }

    public void removeActiveStatus(MonsterStatus stat) {
        stati.remove(stat);
    }

    public void setDamageSchedule(ScheduledFuture<?> damageSchedule) {
        this.damageSchedule = damageSchedule;
    }

    public void cancelDamageSchedule() {
        if (damageSchedule != null) {
            damageSchedule.cancel(false);
        }
    }

    public MobSkill getMobSkill() {
        return mobskill;
    }

    public boolean isMovementAffectingSkill() {
        return stati.containsKey(MonsterStatus.DOOM) || stati.containsKey(MonsterStatus.STUN) || stati.containsKey(MonsterStatus.SPEED) || stati.containsKey(MonsterStatus.FREEZE) || stati.containsKey(MonsterStatus.RISE_BY_TOSS);
    }

    public long getDuration() {
        return duration;
    }

    public short getDurationAsShort() {
        return (short) ((duration - System.currentTimeMillis()) / 500);
    }

    public void setDuration(long duration) {
        this.duration = System.currentTimeMillis() + duration;
    }
}
