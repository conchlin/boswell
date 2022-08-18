/*

John Barricade
by: Lex-Exile
Cleaned up by: Saffron
for: NobleStory

 */
importPackage(Packages.client.inventory);
 
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

	    if (status == 0) {
		    cm.sendSimple("Hi I'm John Barricade. What would you like to do? #e#b\r\n#L0#Create Forging Manuals"
			    + "\r\n#L11#Forge Equipment#k#n");
		} else if (status == 1) {
		    if (selection == 0) {
		        cm.sendSimple("Which forging manual would you like to create? #e#b\r\n#L1##i4031823# Antellion "
		            + "Miter Forging Manual \r\n#L2##i4031912# Crystal Ilbi Forging Manual \r\n#L3##i4031825# Crystal "
		            + "Leaf Earrings Forging Manual \r\n#L4##i4031911# Facestompers Forging Manual \r\n#L5##i4031910# "
		            + "Glitter Gloves Forging Manual \r\n#L6##i4031822# Infinity Circlet Forging Manual "
		            + "\r\n#L7##i4031908# Neva Forging Manual \r\n#L8##i4031824# Stormcaster Gloves Forging Manual "
		            + "\r\n#L9##i4031907# Tiger's Fang Forging Manual \r\n#L10##i4031909# Winkel Forging Manual#n");
		    } else if (selection == 11) {
		        cm.sendSimple("Which iTCG equip would you like to forge? #e#b\r\n#L12##i1002675# Antellion Miter "
		            + "\r\n#L13##i1052148# Bosshunter Armor \r\n#L14##i1072343# Bosshunter Boots \r\n#L15##i1002740# "
		            + "Bosshunter Faceguard \r\n#L16##i1052149# Bosshunter Gi \r\n#L17##i1072342# Bosshunter Greaves "
		            + "\r\n#L18##i1002739# Bosshunter Helm \r\n#L19##i2070016# Crystal Ilbi Throwing-Stars "
		            + "\r\n#L20##i1032048# Crystal Leaf Earrings \r\n#L21##i1072344# Facestompers "
		            + "\r\n#L22##i1082230# Glitter Gloves \r\n#L23##i1002676# Infinity Circlet \r\n#L24##i1472064# Neva "
		            + "\r\n#L25##i1082223# Stormcaster Gloves \r\n#L26##i1402045# Tiger's Fang \r\n#L27##i1452053# Winkel#n");
			}
		} else if (status == 2) {
		    if (selection == 1) {
			    if (cm.haveItem(4031757, 1) && cm.haveItem(4031756, 1)) {
			        if (cm.canHold(4031823)) {
				        cm.gainItem(4031823, 1);
					    cm.gainItem(4031756, -1);
					    cm.gainItem(4031757, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Antellion Relic and 1 Mystic Astrolabe#k#n.");
					cm.dispose();
				}
			} else if (selection == 2) {
			    if (cm.haveItem(4031917, 1) && cm.haveItem(4031758, 1)) {
			        if (cm.canHold(4031912)) {
					    cm.gainItem(4031912, 1);
					    cm.gainItem(4031917, -1); // crystal shard
					    cm.gainItem(4031758, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Crystal Shard and 1 Naricain Jewel#k#n.");
					cm.dispose();
				}
			} else if (selection == 3) {
				if (cm.haveItem(4031755, 1) && cm.haveItem(4031756, 1) && cm.haveItem(4031758, 1)) {
				    if (cm.canHold(4031825)) {
					    cm.gainItem(4031825, 1);
					    cm.gainItem(4031756, -1); //mystic astrolabe
					    cm.gainItem(4031755, -1); // taru totem
					    cm.gainItem(4031758, -1); //naricain jewel
				    } else {
				        cm.sendOk("Please make some room in your inventory.");
				    }
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Taru Totem, 1 Naricain Jewel and 1 Mystic Astrolabe#k#n.");
					cm.dispose();
				}
			} else if (selection == 4) {
				if (cm.haveItem(4031755, 1) && cm.haveItem(4031913, 1)) {
				    if (cm.canHold(4031911)) {
					    cm.gainItem(4031911, 1);
					    cm.gainItem(4031755, -1);
					    cm.gainItem(4031913, -1); //stone tiger head
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Taru Totem and 1 Stone Tiger Head#k#n.");
					cm.dispose();
				}
			} else if (selection == 5) {
				if (cm.haveItem(4031915, 1) && cm.haveItem(4031916, 1)) {
				    if (cm.canHold(4031910)) {
				        cm.gainItem(4031910, 1);
					    cm.gainItem(4031915, -1); // lefay jewel
					    cm.gainItem(4031916, -1); //wrappings
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 LeFay Jewel and 1 Pharoah's Wrappings#k#n.");
					cm.dispose();
				}
			} else if (selection == 6) {
				if (cm.haveItem(94031759, 1) && cm.haveItem(4031758, 1)) {
				    if (cm.canHold(4031822)) {
					    cm.gainItem(4031822, 1);
					    cm.gainItem(4031759, -1);
					    cm.gainItem(4031758, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Naricain Jewel and 1 Subani Ankh#k#n.");
					cm.dispose();
				}
			} else if (selection == 7) {
				if (cm.haveItem(4031916, 1) && cm.haveItem(4031914, 1)) {
				    if (cm.canHold(4031908)) {
					    cm.gainItem(4031908, 1);
					    cm.gainItem(4031916, -1);
					    cm.gainItem(4031914, -1); //typhon crest
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Pharoah's Wrappings and 1 Typhon Crest#k#n.");
					cm.dispose();
				}
			} else if (selection == 8) {
				if (cm.haveItem(4031755, 1) &&cm.haveItem(4031757, 1) && cm.haveItem(4031759, 1)) {
				    if (cm.canHold(4031824)) {
					    cm.gainItem(4031824, 1);
					    cm.gainItem(4031757, -1); //antellion relic
					    cm.gainItem(4031755, -1); // taru totem
					    cm.gainItem(4031759, -1); // subani ankh
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Antellion Relic, 1 Subani Ankh and 1 Taru Totem#k#n.");
					cm.dispose();
				}
			} else if (selection == 9) {
				if (cm.haveItem(4031917, 1) && cm.haveItem(4031913, 1)) {
				    if (cm.canHold(4031907)) {
					    cm.gainItem(4031907, 1);
					    cm.gainItem(4031917, -1);
					    cm.gainItem(4031913, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Stone Tiger Head and 1 Crystal Shard#k#n.");
					cm.dispose();
				}
			} else if (selection == 10) {
				if (cm.haveItem(4031915, 1) && cm.haveItem(4031914, 1)) {
				    if (cm.canHold(4031909)) {
					    cm.gainItem(4031909, 1);
					    cm.gainItem(4031915, -1);
					    cm.gainItem(4031914, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 LeFay Jewel and 1 Typhon Crest#k#n.");
					cm.dispose();
				}
			} else if (selection == 12) {
				if (cm.haveItem(4031823, 1) && cm.haveItem(4011006, 5) && cm.haveItem(4011001, 5)
				    && cm.haveItem(4003000, 20)) {
					if (cm.canHold(1002675)) {
					    cm.gainItem(1002675, 1);
					    cm.gainItem(4031823, -1);
					    cm.gainItem(4011006, -5);
					    cm.gainItem(4011001, -5);
					    cm.gainItem(4003000, -20);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Antellion Miter Forging Manual, 5 Gold Plate, 5 Steel Plate, and 20 Screw#k#n.");
					cm.dispose();
				}
			} else if (selection == 13) {
				if (cm.haveItem(4031900, 1) &&cm.haveItem(4031905, 1) && cm.haveItem(4011898, 1)
				    && cm.haveItem(4031901, 1)) {
				    if (cm.canHold(1052148)) {
					    cm.gainItem(1052148, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4011898, -1);
					    cm.gainItem(4031901, -1);
					    cm.gainItem(4031905, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Armor, 1 Papulatus Curl, and 1 Pianus Scale#k#n.");
					cm.dispose();
				}
			} else if (selection == 14) {
				if (cm.haveItem(4031900, 1) && cm.haveItem(4031905, 1) && cm.haveItem(4031920, 1)
				    && cm.haveItem(4031904, 1)) {
				    if (cm.canHold(1072343)) {
					    cm.gainItem(1072343, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4031920, -1);
					    cm.gainItem(4031904, -1);
					    cm.gainItem(4031905, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Boots, 1 Ergoth's Jawbone, and 1 Pianus Scale#k#n.");
						cm.dispose();
				}
			} else if (selection == 15) {
				if (cm.haveItem(4031900, 1) && cm.haveItem(4031902, 1) && cm.haveItem(4031918, 1)
				    && cm.haveItem(4031903, 1)) {
				    if (cm.canHold(1002740)) {
					    cm.gainItem(1002740, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4031918, -1);
					    cm.gainItem(4031902, -1);
					    cm.gainItem(4031903, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Faceguard, 1 Tengu Nose, and 1 Jack O'Lantern#k#n.");
					cm.dispose();
				}
			} else if (selection == 16) {
				if (cm.haveItem(4031900, 1) && cm.haveItem(4031901, 1) && cm.haveItem(4031919, 1)
				    && cm.haveItem(4031906, 1)) {
				    if (cm.canHold(1052149)) {
					    cm.gainItem(1052149, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4031919, -1);
					    cm.gainItem(4031901, -1);
					    cm.gainItem(4031906, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Gi, 1 Papulatus Curl, and 1 Balrog Claw#k#n.");
					cm.dispose();
				}
			} else if (selection == 17) {
				if (cm.haveItem(4031900, 1) && cm.haveItem(4031903, 1) && cm.haveItem(4031899, 1)
				    && cm.haveItem(4031906, 1)) {
				    if (cm.canHold(1072342)) {
					    cm.gainItem(1072342, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4031899, -1);
					    cm.gainItem(4031903, -1);
					    cm.gainItem(4031906, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Greaves, 1 Balrog Claw, and 1 Jack O'Lantern#k#n.");
					cm.dispose();
				}
			} else if (selection == 18) {
				if (cm.haveItem(4031900, 1) && cm.haveItem(4031904, 1) && cm.haveItem(4031897, 1)
				    && cm.haveItem(4031902, 1)) {
				    if (cm.canHold(1002739)) {
					    cm.gainItem(1002739, 1);
					    cm.gainItem(4031900, -1);
					    cm.gainItem(4031897, -1);
					    cm.gainItem(4031904, -1);
					    cm.gainItem(4031902, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Ridley's Book of Rituals, 1 Ancient Greaves, 1 Ergoth's Jawbone, and 1 Tengu Nose#k#n.");
					cm.dispose();
				}
			} else if (selection == 19) {
				if (cm.haveItem(4031912, 1) && cm.haveItem(2070006, 1) && cm.haveItem(4005003, 7)
				    && cm.haveItem(4005004, 1)) {
				    if (cm.canHold(2070016)) {
				        var ilbi = cm.getChar().getInventory(MapleInventoryType.USE).listById(2070006).iterator();
                    	var ilbinum = ilbi.next().getQuantity();

					    cm.gainItem(2070016, 1);
					    cm.gainItem(4031912, -1);
					    cm.gainItem(2070006, -ilbinum);
					    cm.gainItem(4005003, -7);
					    cm.gainItem(4005004, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Crystal Ilbi Forging Manual, 1 Ilbi Throwing-Star, 7 LUK Crystal, and 1 Dark Crystal#k#n.");
					cm.dispose();
				}
			} else if (selection == 20) {
				if (cm.haveItem(4031825, 1) && cm.haveItem(4021007, 2)) {
				    if (cm.canHold(1032048)) {
					    cm.gainItem(1032048, 1);
					    cm.gainItem(4031825, -1);
					    cm.gainItem(4021007, -2);
					} else {
                        cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Crystal Leaf Earrings Forging Manual and 2 Diamond#k#n.");
					cm.dispose();
				}
			} else if (selection == 21) {
				if (cm.haveItem(4031911, 1) && cm.haveItem(4011001, 50) && cm.haveItem(4000021, 25)
				    && cm.haveItem(4000030, 50) && cm.haveItem(4003000, 25)) {
				    if (cm.canHold(1072344)) {
					    cm.gainItem(1072344, 1);
					    cm.gainItem(4031911, -1);
					    cm.gainItem(4011001, -50);
					    cm.gainItem(4000021, -25);
					    cm.gainItem(4000030, -50);
					    cm.gainItem(4003000, -25);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Facestompers Forging Manual, 50 Steel Plate, 25 Leather, 50 Dragon Skin, and 25 Screw#k#n.");
					cm.dispose();
				}
			} else if (selection == 22) {
				if (cm.haveItem(4031910, 1) && cm.haveItem(1082002, 1) && cm.haveItem(4021007, 6)
				    && cm.haveItem(4005001, 6)) {
				    if (cm.canHold(1082230)) {
					    cm.gainItem(1082230, 1);
					    cm.gainItem(4031910, -1);
					    cm.gainItem(1082002, -1);
					    cm.gainItem(4021007, -6);
					    cm.gainItem(4005001, -6);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Glitter Gloves Forging Manual, Work Gloves, 6 Diamond, and 6 Wisdom Crystal#k#n.");
					cm.dispose();
				}
			} else if (selection == 23) {
				if (cm.haveItem(4031822, 1) && cm.haveItem(4011004, 5) && cm.haveItem(4005000, 1)
				    && cm.haveItem(4005001, 1)) {
				    if (cm.canHold(1002676)) {
					    cm.gainItem(1002676, 1);
					    cm.gainItem(4031822, -1);
					    cm.gainItem(4011004, -5);
					    cm.gainItem(4005000, -1);
					    cm.gainItem(4005001, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
				} else {
					cm.sendOk ("You do not have at least #e#r1 Infinity Circlet Forging Manual, 5 Silver Plate, 1 Power "
					    + " Crystal, and 1 Wisdom Crystal#k#n.");
					cm.dispose();
				}
			} else if (selection == 24) {
				if (cm.haveItem(4031908, 1) && cm.haveItem(4021008, 5) && cm.haveItem(4005003, 1)) {
				    if (cm.canHold(1472064)) {
					    cm.gainItem(1472064, 1);
					    cm.gainItem(4031908, -1);
					    cm.gainItem(4021008, -5);
					    cm.gainItem(4005003, -1);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
				} else {
					cm.sendOk ("You do not have at least #e#r1 Neva Forging Manual, 6 Black Crystal, and 6 LUK Crystal#k#n.");
					cm.dispose();
				}
			} else if (selection == 25) {
				if (cm.haveItem(4031824, 1) && cm.haveItem(4000021, 15) && cm.haveItem(4005000, 2)) {
				    if (cm.canHold(1082223)) {
					    cm.gainItem(1082223, 1);
					    cm.gainItem(4031824, -1);
					    cm.gainItem(4000021, -15);
					    cm.gainItem(4005000, -2);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
				} else {
					cm.sendOk ("You do not have at least #e#r1 Stormcasters Forging Manual, 15 Leather, and 2 Power Crystal#k#n.");
					cm.dispose();
				}
			} else if (selection == 26) {
				if (cm.haveItem(4031907, 1) && cm.haveItem(4005000, 6) && cm.haveItem(4005004, 6)) {
				    if (cm.canHold(1402045)) {
					    cm.gainItem(1402045, 1);
					    cm.gainItem(4031907, -1);
					    cm.gainItem(4005000, -6);
					    cm.gainItem(4005004, -6);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
				} else {
					cm.sendOk ("You do not have at least #e#r1 Tiger's Fang Forging Manual, 6 Power Crystal, and 6 Black Crystal#k#n.");
					cm.dispose();
				}
			}  else if (selection == 27) {
				if (cm.haveItem(4031909, 1) && cm.haveItem(4131010, 1) && cm.haveItem(4003000, 50) &&
					    cm.haveItem(4000030, 6) && cm.haveItem(4005004, 6)) {
					if (cm.canHold(1452053)) {
					    cm.gainItem(1452053, 1);
					    cm.gainItem(4031909, -1);
					    cm.gainItem(4131010, -1);
					    cm.gainItem(4003000, -50);
					    cm.gainItem(4000030, -6);
					    cm.gainItem(4005004, -6);
					} else {
					    cm.sendOk("Please make some room in your inventory.");
					}
					cm.dispose();
				} else {
					cm.sendOk("You do not have at least #e#r1 Winkel Forging Manual, Bow Production Manual, 50 Screw, 6"
					    + " DEX Crystal, and 6 Black Crystal#k#n.");
					cm.dispose();
				}
			}
		}
    }
}
                                        
                        
