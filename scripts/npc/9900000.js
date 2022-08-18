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
/*
 * @Name         NimaKIN
 * @Author:      Signalize
 * @Author:      MainlandHero - repurposed to be the style setter in-game for mesos
 * @NPC:         9900000
 * @Purpose:     Hair/Face/Eye Changer
 * @Map:         180000000
 */
var status = 0;
var beauty = 0;
var haircolor = Array();
var skin = [0, 1, 2, 3, 4, 5, 9, 10];
var fhair = [34098, 31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190,
	31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31360, 31370, 31380, 31390, 
	31400, 31410, 31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 
	31600, 31610, 31620, 31630, 31640, 31650, 31670, 31660, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31780, 31790, 
	31800, 31810, 31820, 31830, 31840, 31850, 31860, 31870, 31880, 31890, 31910, 31920, 31930, 31940, 31950, 31960, 31970, 31980, 31990, 
	32050, 32160, 32340, 32350, 32360, 34010, 34020, 34030, 34050, 34110,
	37790, 37780, 37770, 37760,
	37640, 37600,
	37590, 37530, 37520, 37510, 37500,
	37490]; 
var fhair2 = [37470, 37460, 37450, 37440, 37420, 37400,
	37380, 37370, 37350, 37340, 37330, 37320, 37310, 37300,
	37270, 37260, 37250, 37240, 37230, 37220, 37210, 37200,
	37150, 37130, 37120, 37110, 37100,
	37090, 37080, 37070, 37060, 37040, 37030, 37010, 37000,
	36980, 36610,
	34980, 34970, 34960, 34950, 34940, 34910, 34900,
	34890, 34880, 34870, 34860, 34850, 34840, 34830, 34820, 34810, 34800,
	34790, 34780, 34770, 34760, 34750, 34740, 34730, 34720, 34710, 34700,
	34690, 34680, 34670, 34660, 34650, 34640, 34630, 34620, 34610, 34600,
	34590, 34580, 34560, 34540, 34510,
	34490, 34480, 34470, 34450, 34440, 34430, 34420, 34410, 34400,
	34380, 34370, 34360, 34350, 34340, 34330, 34320, 34310,
	34270, 34260, 34250, 34240, 34230, 34220, 34210,
	34180, 34170, 34160, 34150, 34140, 34130, 34120, 34110, 34100,
	34080, 34070,
	38000, 38010,
	38110, 38120, 38130, 38160];
var hair = [30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 
	30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 
	30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 
	30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 
	30800, 30810, 30820, 30830, 30840, 30860, 30870, 30880, 30890, 30900, 30910, 30920, 30930, 30940, 30950, 30990, 
	33000, 33040, 33100,
	33990, 33960, 33950, 33940, 33930,
	33830, 33820, 33810, 33800,
	33790, 33780, 33770, 33760, 33750, 33740, 33730]; 
var hair2 = [33720, 33710, 33700,
	33690, 33680, 33670, 33660, 33640, 33630, 33620, 33610, 33600,
	33590, 33580, 33550, 33540, 33530, 33520, 33510, 33500,
	33480, 33470, 33460, 33450, 33440, 33430, 33410, 33400,
	33390, 33380, 33370, 33360, 33350, 33330, 33320, 33310,
	36150,
	36680,
	36530, 36520, 36510, 36500,
	36490, 36480, 36470, 36460, 36420, 36410, 36400,
	36390, 36380, 36350, 36340, 36330,
	36280, 36270, 36260, 36250, 36240, 36230, 36220, 36210, 36200,
	36190, 36180, 36170, 36160, 36150, 36140, 36130, 36110, 36100,
	36090, 36080, 36070, 36060, 36050, 36040, 36030, 36020, 36010,
	37170, 37160];
var hairnew = Array();
var face = [20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20015, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024, 20025, 20026, 20027, 20028, 20029, 20031, 20032, 20035, 20036, 20037, 20038, 20040, 20043, 20044, 20045, 20046, 20047, 20048, 20049, 20050, 20052, 20053, 20054, 20055, 20056, 20057, 20058, 20059, 20060, 20061, 20062, 20063, 20064, 20065, 20066, 20067, 20068, 20069, 20070, 20071, 20072, 20073, 20074, 20075, 20076, 20077, 20078, 20079, 20080, 20081, 20082, 20083, 20084, 20085, 20086, 20087, 20088, 20091, 20092, 20093];
var fface = [21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21021, 21022, 21023, 21024, 21025, 21026, 21027, 21029, 21030, 21082, 21100, 21101, 211031, 21032, 21033, 21034, 21035, 21036, 21037, 21038, 21041, 21042, 21043, 21044, 21045, 21046, 21047, 21048, 21049, 21052, 21053, 21054, 21055, 21056, 21057, 21058, 21059, 21060, 21061, 21062, 21063, 21064, 21065, 21066, 21067, 21068, 21069, 21070, 21071, 21072, 21073, 21074, 21075, 21076, 21077, 21078, 21080, 21081, 21082, 21083, 21084, 21085, 21087,, 21088];
var facenew = Array();
var colors = Array();
var price = 0;

function start() {
    if(cm.getPlayer().gmLevel() < 1) {
        cm.sendOk("Hey wassup?");
        cm.dispose();
        return;
    }
    
	if(cm.getPlayer().isMale()){
		cm.sendSimple("Hey there, you can change your look for " + price + " mesos. What would you like to change?\r\n#L0#Skin#l\r\n#L1#Male Hairs 1#l\r\n#L8#Male Hairs 2#l\r\n#L2#Hair Color#l\r\n#L3#Male Regular Eyes#l\r\n#L4#Eye Color#l");
	}else{
		cm.sendSimple("Hey there, you can change your look for " + price + " mesos. What would you like to change?\r\n#L0#Skin#l\r\n#L5#Female Hairs 1#l\r\n#L7#Female Hairs 2#l\r\n#L2#Hair Color#l\r\n#L6#Female Eyes#l\r\n#L4#Eye Color#l");
	}
}

function action(mode, type, selection) {
    status++;
    if (mode != 1 || cm.getPlayer().gmLevel() < 1){
        cm.dispose();
        return;
    }
    if (status == 1) {
        beauty = selection + 1;
		if(cm.getMeso() > price){
			if (selection == 0)
				cm.sendStyle("Pick one?", skin);
			else if (selection == 1 || selection == 5) {
				for each(var i in selection == 1 ? hair : fhair)
					hairnew.push(i);
				cm.sendStyle("Pick one?", hairnew);
			}else if (selection == 7 || selection ==8) {
				for each(var i in selection == 8 ? hair2 : fhair2)
					hairnew.push(i);
				cm.sendStyle("Pick one?", hairnew);
			} else if (selection == 2) {
				var baseHair = parseInt(cm.getPlayer().getHair() / 10) * 10;
				for(var k = 0; k < 8; k++)
					haircolor.push(baseHair + k);
				cm.sendStyle("Pick one?", haircolor);
			} else if (selection == 3 || selection == 6) {
				for each(var j in selection == 3 ? face : fface)
					facenew.push(j);
				cm.sendStyle("Pick one?", facenew);
			} else if (selection == 4) {
				var baseFace = parseInt(cm.getPlayer().getFace() / 1000) * 1000 + parseInt(cm.getPlayer().getFace() % 100);
				for(var i = 0; i < 9; i++)
					colors.push(baseFace + (i*100));
				cm.sendStyle("Pick one?", colors);
			}
		} else {
			cm.sendNext("You don't have enough mesos. Sorry to say this, but without " + price + " mesos, you won't be able to change your look!");
            cm.dispose();
		}
        
    } else if (status == 2){
        if (beauty == 1){
            cm.setSkin(skin[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 2 || beauty == 6 || beauty == 8 || beauty == 9){
            cm.setHair(hairnew[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 3){
            cm.setHair(haircolor[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 4 || beauty == 7){
            cm.setFace(facenew[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 5){
            cm.setFace(colors[selection]);
			cm.gainMeso(-price);
		}
        cm.dispose();
    }
}