/*
Stirgeman (NLC); ITCG Forging Trader.
by: Alex
for: NobleStory
*/
function start() {
status = -1;
action(1, 0, 0);
}

function action(mode, type, selection) {

if (mode == -1) {
cm.dispose();
} else if (mode == 0) {
cm.dispose();
} else {
if (mode == 1)
status++;
else
status--;
{
if (status == 0) {
cm.sendSimple("Hello. If you have a Stirgeman Equip, I can upgrade it for you. Which type of Stirgeman Equip would you like to upgrade? #e#b\r\n#L20#Stirge Weapon \r\n#L21#Stirgeman Cape \r\n#L22#Stirgeman Pants \r\n#L23#Stirgeman Skirt#k#n");
} else if (status == 1) {
	if (selection == 20) {
cm.sendSimple("Which Stirge weapon would you like to upgrade to? #e#b\r\n#L11##i1302089# Stirge-on-a-Rope \r\n#L20##i1302090# Stirge-o-Whip \r\n#L21##i1302091# Stirge Grappler \r\n#L22##i1302092# Swooping Stirge \r\n#L23##i1302093# Frantic Stirge \r\n#L24##i1302094# Angry Stirge#k#n");
} else if (selection == 21) {
cm.sendSimple("Which Stirgeman Cape would you like to upgrade to? #e#b\r\n#L0##i1102177# Stirgeman Raggedy Cape \r\n#L1##i1102178# Stirgeman Cape Mk II \r\n#L12##i1102179# Stirgeman Cape Mk III \r\n#L13##i1102180# Stirgeman Cape Mk IV \r\n#L14##i1102182# Stirgeman's Cloak of Darkness \r\n#L15##i1102183# Stirgeman's Cloak of Justice \r\n#L16##i1102181# Stirgeman's Cloak of Wiliness#k#n");
} else if (selection == 22) {
cm.sendSimple("Which Stirgeman Pants would you like to upgrade to? #e#b\r\n#L2##i1060128# Stirgeman Utility Pants Mk II \r\n#L3##i1060129# Stirgeman Utility Pants Mk III \r\n#L4##i1060130# Stirgeman Utility Pants Mk IV \r\n#L5##i1060131# Stirgeman Utility Pants Mk V \r\n#L6##i1060132# Stirgeman Power Pants \r\n#L17##i1060133# Stirgeman Power Pants Mk II#k#n");
} else if (selection == 23) {
cm.sendSimple("Which Stirgeman Skirt would you like to upgrade to? #e#b\r\n#L7##i1061150# Stirgeman Utility Skirt Mk II \r\n#L8##i1061151# Stirgeman Utility Skirt Mk III \r\n#L9##i1061152# Stirgeman Utility Skirt Mk IV \r\n#L10##i1061153# Stirgeman Utility Skirt Mk V \r\n#L18##i1061154# Stirgeman Power Skirt \r\n#L19##i1061154# Stirgeman Power Skirt Mk II #k#n");
}
} else if (status == 2){
if (selection == 0) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102176, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102176, -1);
cm.gainItem(1102177, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 1) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102177, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102177, -1);
cm.gainItem(1102178, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 12) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102178, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102178, -1);
cm.gainItem(1102179, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 13) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102179, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102179, -1);
cm.gainItem(1102180, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 14) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102180, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102180, -1);
cm.gainItem(1102182, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 15) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102180, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102180, -1);
cm.gainItem(1102183, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 16) {
if ((cm.haveItem(4032030, 12)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(4032027, 8)) &&
(cm.haveItem(1102180, 1))) {
cm.gainItem(4032030, -12);
cm.gainItem(4032029, -5);
cm.gainItem(4032027, -8);
cm.gainItem(1102180, -1);
cm.gainItem(1102181, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 2) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060127, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060127, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060128, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 3) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060128, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060128, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060129, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 4) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060129, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060129, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060130, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 5) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060130, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060130, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060131, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 6) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060131, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060131, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060132, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 17) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1060132, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1060132, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1060133, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 7) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061149, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061149, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061150, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 8) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061150, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061150, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061151, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 9) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061151, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061151, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061152, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 10) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061152, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061152, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061153, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 18) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061153, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061153, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061154, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 19) {
if ((cm.haveItem(4032030, 8)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(1061154, 1)) &&
(cm.haveItem(4003000, 20)) &&
(cm.haveItem(4000399, 15))) {
cm.gainItem(4032030, -8);
cm.gainItem(1061154, -1);
cm.gainItem(4032028, -8);
cm.gainItem(4003000, -20);
cm.gainItem(4000399, -15);
cm.gainItem(1061155, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 11) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302088, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302088, -1);
cm.gainItem(1302089, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 20) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302089, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302089, -1);
cm.gainItem(1302090, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 21) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302090, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302090, -1);
cm.gainItem(1302091, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 22) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302091, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302091, -1);
cm.gainItem(1302092, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 23) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302092, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302092, -1);
cm.gainItem(1302093, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
else if (selection == 24) {
if ((cm.haveItem(4032027, 15)) &&
(cm.haveItem(4000399, 10)) &&
(cm.haveItem(4032028, 8)) &&
(cm.haveItem(4032029, 5)) &&
(cm.haveItem(1302093, 1))) {
cm.gainItem(4032027, -15);
cm.gainItem(4000399, -10);
cm.gainItem(4032029, -5);
cm.gainItem(4032028, -8);
cm.gainItem(1302093, -1);
cm.gainItem(1302094, 1, true, true);
cm.dispose();
} else {
cm.sendOk("You don't have all of the required items.");
cm.dispose();
}
}
}
}
}
}