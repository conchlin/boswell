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
	    NPC Name: 		Big Headward
          Map(s): 		Victoria Road : Henesys Hair Salon (100000104)
	 Description: 		Random haircut

        GMS-like revised by Ronan. Contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
// corrupted list [DO NOT USE] : 32150, 35690, 35700
// unnamed list male:   33230, 36540, 36550, 36870, 36890,
// unnamed list female: 34990, 37180, 42100
// bald male hair [redump]: 40510, 42100
var status = 0;
var beauty = 0;
var fhair = [31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140,
    31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300,
    31310, 31320, 31330, 31340, 31350, 31360, 31370, 31380, 31390, 31400, 31410, 31420, 31440, 31450, 31460, 31470,
    31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640,
    31650, 31660, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31780, 31790, 31800,
    31810, 31820, 31830, 31840, 31850, 31860, 31870, 31880, 31890, 31910, 31920, 31930, 31940, 31950, 31960, 31970,
    31980, 31990, 32050, 32160, 32170, 32190, 32200, 32210, 32230, 32240, 32250, 32260, 32270, 32280, 32290, 32300,
    32310, 32320, 32330, 32340, 32350, 32360, 32430, 32440, 32450, 32460, 32470, 32480, 32490, 32550, 32560, 32570];
var fhair2 = [32580, 32600, 32620, 32650, 32660, 33140, 33160, 33790, 34010, 34020, 34030, 34040, 34050, 34070, 34080,
    34100, 34110, 34120, 34130, 34140, 34150, 34160, 34170, 34180, 34210, 34220, 34230, 34240, 34250, 34260, 34270,
    34280, 34290, 34310, 34320, 34330, 34340, 34350, 34360, 34370, 34380, 34390, 34400, 34410, 34420, 34430, 34440,
    34450, 34470, 34480, 34490, 34510, 34540, 34560, 34580, 34590, 34600, 34610, 34620, 34630, 34640, 34650, 34660,
    34670, 34680, 34690, 34700, 34710, 34720, 34730, 34740, 34750, 34760, 34770, 34780, 34790, 34800, 34810, 34820,
    34830, 34840, 34850, 34860, 34870, 34880, 34890, 34900, 34910, 34940, 34950, 34960, 34970, 34980, 36320, 36610,
    36980, 37000, 37010, 37020, 37030, 37040, 37050, 37060, 37070, 37080, 37090, 37100, 37110, 37120, 37130, 37140,
    37150, 37190, 37200, 37210, 37220, 37230, 37240, 37250, 37260, 37270, 37280, 37290, 37300, 37310, 37320, 37330];
var fhair3 = [37340, 37350, 37370, 37380, 37400, 37420, 37440, 37450, 37460, 37470, 37490, 37500, 37510, 37520, 37530,
    37540, 37550, 37560, 37570, 37580, 37590, 37600, 37610, 37620, 37630, 37640, 37650, 37660, 37670, 37680, 37690,
    37700, 37710, 37720, 37730, 37740, 37750, 37760, 37770, 37780, 37790, 38000, 38010, 38110, 38120, 38130, 38140,
    38150, 38160, 38170, 38180, 38190, 38200, 38210, 38220, 38230, 38240, 38250, 38260, 38270, 38280, 38290, 38300,
    38310, 38320, 38340, 38350, 38380, 38390, 38400, 38410, 38420, 38430, 38440, 38450, 38460, 38470, 38480, 38490,
    38450, 38460, 38470, 38480, 38490, 38500, 38510, 38520, 38540, 38550, 38560, 38570, 38580, 38590, 38600, 38610,
    38620, 38630, 38640, 38650, 38660, 38670, 38680, 38690, 38700, 38710, 38720, 38760, 38770, 38790, 38800, 38810,
    38820, 38830, 38840, 38860, 38880, 38890, 38900, 38910, 38920, 38930, 39050, 39090, 39190, 39200, 39250, 41080];
var fhair4 = [41090, 41100, 41110, 41120, 41150, 41160, 41170, 41180, 41190, 41200, 41220, 41340, 41350, 41360, 41370,
    41380, 41390, 41400, 41410, 41420, 41440, 41460, 41470, 41480, 41490, 41510, 41520, 41560, 41570, 41580, 41590,
    41600, 41610, 41620, 41630, 41640, 41650, 41660, 41670, 41680, 41690, 41670, 41710, 41720, 41730, 41740, 41750,
    41760, 41770, 41780, 41790, 41800, 41810, 41810, 41820, 41830, 41840, 41850, 41860, 41860, 41870, 41900, 41910,
    41920, 41930, 41940, 42070, 42090, 42110, 42120];
var mhair = [30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150,
    30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310,
    30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510,
    30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670,
    30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30830,
    30840, 30860, 30870, 30880, 30890, 30900, 30910, 30920, 30930, 30940, 30950, 30990, 32180, 32220, 32380, 32390,
    32400, 32410, 32420, 32430, 32440, 32450, 32460, 32470, 32480, 32490, 32510, 32590, 32610, 32630, 32640, 33000,
    33030, 33040, 33050, 33100, 33110, 33120, 33130, 33730, 33150, 33170, 33180, 33190, 33220, 33240, 33250, 33260];
var mhair2 = [33270, 33280, 33290, 33740, 33750, 33760, 33770, 33780, 33790, 33800, 33810, 33820, 33830, 33930, 33940,
    33950, 33960, 33990, 33310, 33320, 33330, 33350, 33360, 33370, 33380, 33390, 33400, 33410, 33420, 33430, 33440,
    33450, 33460, 33460, 33470, 33480, 33500, 33510, 33520, 33530, 33540, 33550, 33580, 33590, 33600, 33610, 33620,
    33630, 33640, 33650, 33660, 33670, 33680, 33690, 33700, 33710, 33720, 33730, 33740, 33750, 33760, 33770, 33780,
    33800, 33810, 33820, 33830, 33930, 33940, 33950, 33960, 33990, 35000, 35010, 35020, 35030, 35040, 35060, 35070,
    35080, 35090, 35100, 35110, 35120, 35130, 35140, 35150, 35160, 35170, 35180, 35190, 35200, 35210, 35220, 35240,
    35250, 35260, 35270, 35290, 35300, 35310, 35330, 35340, 35350, 35360, 35420, 35430, 35440, 35450, 35460, 35470,
    35480, 35490, 35500, 35510, 35520, 35530, 35540, 35550, 35560, 35570, 35580, 35590, 35600, 35620, 35630, 35640];
var mhair3 = [35650, 35680, 35710, 35720, 35760, 35770, 35780, 35790, 35820, 35830, 35950, 35960, 36000,
    36010, 36020, 36030, 36040, 36050, 36060, 36070, 36080, 36090, 36100, 36110, 36120, 36130, 36140, 36150, 36150,
    36160, 36170, 36180, 36190, 36200, 36210, 36220, 36230, 36240, 36250, 36260, 36270, 36280, 36290, 36300, 36310,
    36321, 36330, 36340, 36350, 36380, 36390, 36400, 36410, 36420, 36430, 36440, 36450, 36460, 36470, 36480, 36490,
    36500, 36510, 36520, 36530, 36560, 36570, 36580, 36590, 36600, 36610, 36620, 36630, 36640, 36650, 36670, 36680,
    37160, 36680, 36690, 36700, 36710, 36720, 36730, 36740, 36750, 36760, 36770, 36780, 36790, 36800, 36810, 36820,
    36830, 36840, 36850, 36860, 36880, 36900, 36910, 36920, 36930, 36940, 36950, 36980, 36990, 37170, 39000, 39060,
    39070, 39080, 39260, 40000, 40010, 40020, 40030, 40040, 40050, 40060, 40070, 40080, 40090, 40100, 40110, 40120];
var mhair4 = [40250, 40260, 40270, 40280, 40290, 40300, 40310, 40320, 40330, 40350, 40360, 40370, 40390, 40400, 40410,
    40420, 40440, 40450, 40460, 40470, 40480, 40490, 40500, 40520, 40530, 40540, 40550, 40560, 40570, 40580,
    40590, 40600, 40610, 40620, 40630, 40640, 40650, 40680, 40700, 40710, 41060, 41070, 42060, 42080, 42100];
var mface = [20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014,
    20015, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024, 20025, 20026, 20027, 20028, 20029, 20031,
    20032, 20035, 20036, 20037, 20038, 20040, 20043, 20044, 20045, 20046, 20047, 20048, 20049, 20050, 20052, 20053,
    20054, 20055, 20056, 20057, 20058, 20059, 20060, 20061, 20062, 20063, 20064, 20065, 20066, 20067, 20068, 20069,
    20070, 20071, 20072, 20073, 20074, 20075, 20076, 20077, 20078, 20079, 20080, 20081, 20082, 20083, 20084, 20085,
    20086, 20087, 20088, 20091, 20092, 20093];
var fface = [21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014,
    21016, 21017, 21018, 21019, 21020, 21021, 21022, 21023, 21024, 21025, 21026, 21027, 21029, 21030, 21082, 21100,
    21101, 21031, 21032, 21033, 21034, 21035, 21036, 21037, 21038, 21041, 21042, 21043, 21044, 21045, 21046, 21047,
    21048, 21049, 21052, 21053, 21054, 21055, 21056, 21057, 21058, 21059, 21060, 21061, 21062, 21063, 21064, 21065,
    21066, 21067, 21068, 21069, 21070, 21071, 21072, 21073, 21074, 21075, 21076, 21077, 21078, 21080, 21081, 21082,
    21083, 21084, 21085, 21087, 21088];
var hairnew = Array();
var facenew = Array();
var colors = Array();

function pushIfItemExists(array, itemId) {
    if ((itemId = cm.getCosmeticItem(itemId)) !== -1 && !cm.isCosmeticEquipped(itemId)) {
        array.push(itemId);
    }
}

function start() {

    if (cm.getPlayer().isMale()) {
        cm.sendSimple("Hi, I'm #p1012117#, the most charming and stylish stylist around. If you're looking for the" +
            " best looking hairdos around, look no further!" +
            "\r\n#L0#Male Hairs#l" +
            "\r\n#L2#Male Hairs cont.#l" +
            "\r\n#L4#Male Hairs cont 2.#l" +
            "\r\n#L6#Male Hairs cont 3.#l" +
            "\r\n#L8#Male Eyes#l");
    } else {
        cm.sendSimple("Hi, I'm #p1012117#, the most charming and stylish stylist around. If you're looking for the" +
            " best looking hairdos around, look no further!" +
            "\r\n#L1#Female Hairs#l" +
            "\r\n#L3#Female Hairs cont.#l" +
            "\r\n#L5#Female Hairs cont 2.#l" +
            "\r\n#L7#Female Hairs cont 3.#l" +
            "\r\n#L9#Female Eyes#l");
    }
}

function action(mode, type, selection) {
    status++;
    if (mode !== 1) {
        cm.dispose();
        return;
    }

    if (status === 1) {
        beauty = selection + 1;
        if (selection === 0 || selection === 1 || selection === 2 || selection === 3 || selection === 4 ||
            selection === 5 || selection === 6 || selection === 7 ) {
            if (selection === 0) { // HAIR #1
                for (var i = 0; i < mhair.length; i++)
                    pushIfItemExists(hairnew, mhair[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 1) {
                for (var i = 0; i < fhair.length; i++)
                    pushIfItemExists(hairnew, fhair[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 2) { // HAIR #2
                for (var i = 0; i < mhair2.length; i++)
                    pushIfItemExists(hairnew, mhair2[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 3) {
                for (var i = 0; i < fhair2.length; i++)
                    pushIfItemExists(hairnew, fhair2[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 4) { // HAIR #3
                for (var i = 0; i < mhair3.length; i++)
                    pushIfItemExists(hairnew, mhair3[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 5) {
                for (var i = 0; i < fhair3.length; i++)
                    pushIfItemExists(hairnew, fhair3[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 6) { // HAIR #4
                for (var i = 0; i < mhair4.length; i++)
                    pushIfItemExists(hairnew, mhair4[i] + parseInt(cm.getPlayer().getHair() % 10));

            } else if (selection === 7) {
                for (var i = 0; i < fhair4.length; i++)
                    pushIfItemExists(hairnew, fhair4[i] + parseInt(cm.getPlayer().getHair() % 10));
            }
            cm.sendStyle("Using the SPECIAL coupon you can choose the style your hair will become. Pick the " +
                "style that best provides you delight...", hairnew);

        } else if (selection === 8 || selection === 9) {
            if (selection === 8) { // FACE
                for (var i = 0; i < mface.length; i++)
                    pushIfItemExists(facenew, mface[i] + cm.getPlayer().getFace() % 1000 -
                        (cm.getPlayer().getFace() % 100));

            } else if (selection === 9) {
                for (var i = 0; i < mface.length; i++)
                    pushIfItemExists(facenew, fface[i] + cm.getPlayer().getFace() % 1000 -
                        (cm.getPlayer().getFace() % 100));
            }
            cm.sendStyle("Using the SPECIAL coupon you can choose the style your hair will become. Pick the " +
                "style that best provides you delight...", facenew);
        }

    } else if (status === 2){
        if (cm.haveItem(5150044) === true) {
            if (beauty === 1 || beauty === 2 || beauty === 3 || beauty === 4 || beauty === 5 || beauty === 6 ||
                beauty === 7 || beauty === 8) {
                print(beauty);
                cm.setHair(hairnew[selection]);
                cm.gainItem(5150044, -1);
                cm.sendOk("Thanks for supporting Boswell!");
                cm.dispose();
            }
        }

        if (cm.haveItem(5150041) === true) {
            if (beauty === 9 || beauty === 10){
                print(beauty);
                cm.setFace(facenew[selection]);
                cm.gainItem(5150041, -1);
                cm.sendOk("Thanks for supporting Boswell!");
                cm.dispose();
            }
        } else {
            cm.sendOk("Hmmm... it looks like you don't have our designated coupon...I'm afraid I can't " +
                "give you a makeover without it. I'm sorry...");
        }
        cm.dispose();
    }
}