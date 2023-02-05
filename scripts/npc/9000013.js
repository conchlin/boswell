/* Tony
	Event NPC
	located in Ludibrium
*/

importPackage(Packages.tools);
importPackage(Packages.network.packet.context);
importPackage(Packages.enums);

var status = 0;
var minPlayers = 1;
var maxPlayers = 6;
var prize;
var chosen;
var minLevel = 1;
var maxLevel = 200;

function numberFormat(nStr,prefix){
	var prefix = prefix || '';
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) x1 = x1.replace(rgx, '$1' + ',' + '$2');
	return prefix + x1 + x2;
}

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			status--;
		}
		if (mode == 1) { status++; } else {	status--; }
		if (status == 0) {
			cm.sendNext("Hi! I'm #e#bTony#k#n. Welcome to the #e#bBoss Quest - Hell Mode#k#n!");
		} else if (status == 1) {
			chosen = 0;
			cm.sendSimple("What would you like to do? #e#b\r\n#L0#Begin the Boss Quest - Hell#l\r\n#L1#Trade your Boss Quest Points#l \r\n#L2#Check my Boss Quest Runs#k#n");
		} else if (status == 2) {
			if (selection == 0) {
				if (cm.getParty() == null) {
					cm.sendOk("To begin the Boss Quest you must be in a party.");
					cm.dispose();
					return;
				} if (!cm.isLeader()) {
					cm.sendOk("You are not the leader of your party! If you want to begin the Boss Quest, please tell the leader to talk to me.");
					cm.dispose();
					return;
                } if (cm.getParty().getMembers().size() < minPlayers || cm.getParty().getMembers().size() > maxPlayers) { // check for min and max number of players
						cm.sendOk("You must have at least #e#b" + minPlayers + "#n#k players in your party in order to participate in this party quest. A maximum of #e#b" + maxPlayers + "#k#n players are allowed to participate.");
						cm.dispose();
						return;
					}
					if (cm.partyMembersInMap() != cm.getParty().getMembers().size()) { // check for all players are present
						cm.sendOk("Not all members of your party are present. Please make sure that everyone is here in order to begin the Boss Quest.");
						cm.dispose();
						return;
					} else {
					var em = cm.getEventManager("BossQuestHell");
					if (em == null) {
						cm.sendOk("The Boss Party Quest is currently unavailable.");
					} else {
						if (cm.getChar().getBossQuestRepeats() < 3 && cm.getLevel() >= minLevel) {
   							var party = cm.getParty().getMembers();
                            var inMap = cm.partyMembersInMap();
							var mapId = cm.getChar().getMapId();
							var inMap = true;
							var canRepeat = true;
							var badRepeat;
							var PartyRange = true;
							var maxLevelRange = 0;
							var minLevelRange = 200;
							var it = party.iterator();
							for (var i = 0; i < party.size(); i++) {
							    if (party.get(i).getPlayer().getBossQuestRepeats() == 3) {
							        canrepeat = false;
							    }
							    if (party.get(i).getPlayer().getMapId() != 220000000) { // ludi
                                    inMap = false;
                                }
							    if (party.get(i).getPlayer().getLevel() > maxLevelRange) {
							        maxLevelRange = party.get(i).getPlayer().getLevel();
							    }
							    if (party.get(i).getPlayer().getLevel() <= minLevelRange) {
							        minLevelRange = party.get(i).getPlayer().getLevel();
							    }
							    if (maxLevelRange - minLevelRange > 50) {
                                    PartyRange = false;
                                }
							}
							/*while (it.hasNext()) {
								var cPlayer = it.next();
								if (cPlayer.getMapid() != mapId) {
									inMap = false;
								}
								if (cPlayer.getPlayer().getBossQuestRepeats() == 3) {
									canRepeat = false;
									//badRepeat += ", " + cPlayer().getPlayer().getName();
								}
								if (cPlayer.getPlayer().getLevel() > maxLevelRange) {
								maxLevelRange = cPlayer.getPlayer().getLevel();
							}	if (cPlayer.getPlayer().getLevel() <= minLevelRange) {
								minLevelRange = cPlayer.getPlayer().getLevel();
							}
							if (maxLevelRange - minLevelRange > 50) {
								PartyRange = false;
							}}*/
							if (PartyRange == false) {
								cm.sendOk("Please make sure that all members of your party are within 50 levels of one another!");
								cm.dispose();
							}
							else if (inMap == false) {
								cm.sendOk("Please make sure all the members of your party are in the map!");
								cm.dispose();
							} else if (canRepeat == false) {
								cm.sendOk("One or more members of your party have already participated in the Boss Quest three times today!");
								cm.dispose();
							} else {
							    for (var i = 0; i < party.size(); i++) {
							        party.get(i).getPlayer().getBossQuest().addAttempts(cm.getPlayer().getId(), +1);
							        em.startInstance(cm.getParty(), cm.getPlayer().getMap().getId(), true);
							    }
								/*var iterator = party.iterator();
								while (iterator.hasNext()) {
									var member = iterator.next();
									member.getPlayer().setBossQuestRepeats(member.getPlayer().getBossQuestRepeats() + 1);
								}
								em.startInstance(cm.getParty(), cm.getChar().getMap(), true);*/
							}
						} else {
							cm.sendOk("You have already participated in the Boss Quest three times today!");
							cm.dispose();
						}
					}
					cm.dispose();
				}
			} else if (selection == 1) {
				cm.sendYesNo("You currently have #b" + numberFormat(cm.getChar().getBossPoints()) + "#k Boss Quest Points! \r\nWould you like to trade your points for prizes?");
			} else if (selection == 2) {
				cm.sendOk("You have completed #b#e" +  cm.getChar().getBossQuestRepeats() + "#k#n Boss PQ Runs within the last 24 hours. Remember that you may enter Boss PQ a maximum of 3 times per day. Boss PQ Runs reset every day at 6:00am Server Time, so please ensure that you are logged off exactly at 6:00am PST for your runs to reset.");
				cm.dispose();
			}
		} else if (status == 3) {
			if (cm.getChar().isGM()) { cm.getChar().setBossPoints(100000000); } // this is used for testing... should be removed when done - denny
			cm.sendSimple("How many points would you like to trade?#b\r\n#L0#1,000 Boss Quest Points#l\r\n#L1#2,500 Boss Quest Points#l\r\n#L2#5,000 Boss Quest Points#l \r\n#L4#10,000 Boss Quest Points \r\n#L6#15,000 Boss Quest Points#l\r\n#L7#20,000 Boss Quest Points#l\r\n#L8#30,000 Boss Quest Points#l \r\n#L9#40,000 Boss Quest Points \r\n#L224#50,000 Boss Quest Points#k");
		} else if (status == 4) {
			switch (selection) {
				case 0:
					cm.sendSimple("#L10##eSpend 1,000 Points on:#l #n\r\n\r\n#i4031751# Vorticular Gyro \r\n#i4031753# Zeta Residue \r\n#i4031754# Black Versal Materia \r\n#i4031752# Blinking Dingbat \r\n#i4031750# Dark Matter \r\n#i4031936# Taru Spirit Feather \r\n#i4032134# Stone Denari");
					break;
				case 1:
					cm.sendSimple("#L11##eSpend 2,500 Points on:#l #n\r\n\r\n#i4031757# Antellion Relic \r\n#i4031756# Mystic Astrolabe \r\n#i4031900# Ridley's Book of Rituals \r\n#i4031898# Ancient Armor \r\n#i4031920# Ancient Boots \r\n#i4031918# Ancient Faceguard \r\n#i4031919# Ancient Gi \r\n#i4031899# Ancient Greaves \r\n#i4031897# Ancient Helm \r\n#i4032026# Ridley's Stone");
					break;
				case 2:
					cm.sendSimple("#L12##eSpend 5,000 Points on:#l #n\r\n\r\n#i1452054# Akha (Bow) \r\n#i1462047# Xaru (Crossbow) \r\n#i1472065# Kuma (Claw) \r\n#i1332067# Maku (LUK) (Dagger) \r\n#i1332070# Maku (STR) (Dagger) \r\n#i1382054# Umaru (Staff) \r\n#i1302088# Stirge-on-a-String \r\n#i1102176# Stirgeman Cape \r\n#i1060127# Stirgeman Utility Pants (Male) \r\n#i1061149# Stirgeman Utility Skirt (Female) \r\n#i4031760# White Versal Materia \r\n#i4031915# LeFay Jewel \r\n#i4031914# Typhon Crest \r\n#i4031916# Pharaoh's Wrappings \r\n#i4031759# Subani Ankh \r\n#i4031917# Crystal Shard \r\n#i4031758# Naricain Jewel \r\n#i4031755# Taru Totem \r\n#i4031913# Stone Tiger Head");
					break;
				case 231:
					cm.sendSimple("#L232##eSpend 7,500 Points on: #n\r\n\r\n#s1003# Skill Mastery 10");
					break;
				case 3:
					cm.sendSimple("#L13##eSpend 10,000 Points on:#l #n\r\n\r\n#i2290002# Mastery Book 20");
					break;
				case 4:
					cm.sendSimple("#L14##eSpend 10,000 Points on:#l #n\r\n\r\n#i1102193# Cloak of Corruption \r\n#i1302106# Crystal Blade \r\n#i1102191# El Nathian Cape \r\n#i1002856# Miner's Hat \r\n#i1092061# Crossheider \r\n#i1092050# Khanjar");
					break;
				case 5:
					cm.sendSimple("#L15##eSpend 15,000 Points on:#l #n\r\n\r\n#i2290003# Mastery Book 30");
					break;
				case 6:
					cm.sendSimple("#L16##eSpend 15,000 Points on:#l #n\r\n\r\n#i1372035# Elemental Wand 1 \r\n#i1372036# Elemental Wand 2 \r\n#i1372037# Elemental Wand 3 \r\n#i1372038# Elemental Wand 4 \r\n#i1382045# Elemental Staff 1 \r\n#i1382046# Elemental Staff 2 \r\n#i1382047# Elemental Staff 3 \r\n#i1382048# Elemental Staff 4");
					break;
				case 7:
					cm.sendSimple("#L17##eSpend 20,000 Points on:#l #n\r\n\r\n#i1302081# Timeless Executioners (level 120 1H Sword) \r\n#i1402046# Timeless Nibleheim (level 120 2H Sword) \r\n#i1322060# Timeless Allargando (level 120 1H Mace) \r\n#i1422037# Timeless Bellocce (level 120 2H Mace) \r\n#i1312037# Timeless Bardiche (level 120 2H Axe) \r\n#i1412033# Timeless Tabarzin (level 120 1H Axe) \r\n#i1442063# Timeless Diestra (level 120 Pole Arm) \r\n#i1432047# Timeless Alchupiz (level 120 Spear) \r\n#i1452057# Timeless Engaw (level 120 Bow) \r\n#i1462050# Timeless Black Beauty (level 120 Crossbow) \r\n#i1372044# Timeless Enreal Tear (level 120 Wand) \r\n#i1382057# Timeless Aeas Hand (level 120 Staff) \r\n#i1472068# Timeless Lampion (level 120 Claw) \r\n#i1332073# Timeless Pescas (level 120 Dagger) \r\n#i1332074# Timeless Killic (level 120 Dagger) \r\n#i1482023# Timeless Equinox (level 120 Knuckle) \r\n#i1492023# Timeless Blindness (level 120 Gun) \r\n#i1092049# Dragon Khanjar (level 120 Thief Shield) \r\n#i1092058# Timeless Kite Shield \r\n#i1092057# Timeless Prelude \r\n#i1092059# Timeless List \r\n#i1052075# Blue Dragon Set (level 110 Warrior) \r\n#i1052071# Red Hunter Set (level 110 Bowman) \r\n#i1052076# Blue Elemental Set (level 110 Magician) \r\n#i1052072# Black Garina Set (level 110 Thief) \r\n#i1052134# Canopus Set (level 110 Pirate)");
					break;
				case 8:
					cm.sendSimple("#L18##eSpend 30,000 Points on:#l #n\r\n\r\n#i1372039# Elemental Wand 5 \r\n#i1372040# Elemental Wand 6 \r\n#i1372041# Elemental Wand 7 \r\n#i1372042# Elemental Wand 8 \r\n#i1382049# Elemental Staff 5 \r\n#i1382050# Elemental Staff 6 \r\n#i1382051# Elemental Staff 7 \r\n#i1382052# Elemental Staff 8");
					break;
				case 9:
					cm.sendSimple("#L19##eSpend 40,000 Points on:#l #n\r\n\r\n#i2290096# Maple Warrior 20 \r\n#i2290017# Enrage 30 \r\n#i2290021# Heaven's Hammer 30 \r\n#i2290023# Berserk 30 \r\n#i2290041# Meteor Shower 30 \r\n#i2290047# Blizzard 30 \r\n#i2290049# Genesis 30 \r\n#i2290085# Triple Throw 30 \r\n#i2290111# Time Leap 30 \r\n#i2290116# Aerial Strike 30 \r\n#i2290075# Snipe 30");
					break;
				case 224:
					cm.sendSimple("#L225##eSpend 50,000 Points on: #n\r\n\r\n#s1005# Echo of Hero");
			}
		} else if (status == 5) {
			switch (selection) {
				case 10:
					if (cm.getChar().getBossPoints() > 1000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L29#Vorticular Gyro \r\n#L30#Zeta Residue \r\n#L31#Black Versal Materia \r\n#L32#Blinking Dingbat \r\n#L33#Dark Matter \r\n#L34#Taru Spirit Feather \r\n#L218#Stone Denari#k");
						} else { notenoughpoints(1000); }
					break;
				case 11:
					if (cm.getChar().getBossPoints() > 2500) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L38# Antellion Relic \r\n#L39#Mystic Astrolabe \r\n#L40#Ridley's Book of Rituals \r\n#L41#Ancient Armor \r\n#L42#Ancient Boots \r\n#L43#Ancient Faceguard \r\n#L44#Ancient Gi \r\n#L45#Ancient Greaves \r\n#L46#Ancient Helm \r\n#L217#Ridley's Stone#k");
						} else { notenoughpoints(2500); }
					break;
				case 12:
					if (cm.getChar().getBossPoints() > 5000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L20#Akha (Bow)#l \r\n#L21#Xaru (Crossbow) \r\n#L22#Kuma (Claw) \r\n#L23#Maku (LUK) (Dagger) \r\n#L24#Maku (STR) (Dagger) \r\n#L25#Umaru (Staff) \r\n#L219#Stirge-on-a-String \r\n#L220#Stirgeman Cape \r\n#L221#Stirgeman Utility Pants (Male) \r\n#L222#Stirgeman Utility Skirt (Female) \r\n#L27#White Versal Materia \r\n#L47#LeFay Jewel \r\n#L48#Typhon Crest \r\n#L49#Pharaoh's Wrappings \r\n#L50#Subani Ankh \r\n#L51#Crystal Shard \r\n#L52#Naricain Jewel \r\n#L53#Taru Totem \r\n#L54#Stone Tiger Head#k");
						} else { notenoughpoints(5000); }
					break;
				case 13:
					if (cm.getChar().getBossPoints() > 10000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L94#Achilles 20 \r\n#L95#Guardian 20 \r\n#L96#Advanced Combo Attack 20 \r\n#L97#Monster Magnet 20 \r\n#L98#Power Stance 20 \r\n#L99#Rush 20 \r\n#L100#Brandish 20 \r\n#L101#Enrage 20 \r\n#L102#Blast 20 \r\n#L103#Holy Charge 20 \r\n#L104#Divine Charge 20 \r\n#L105#Heaven's Hammer \r\n#L106#Berserk 20 \r\n#L107#Big Bang 20 \r\n#L108#Mana Reflection 20 \r\n#L109#Infinity 20 \r\n#L110#Fire Demon 20 \r\n#L111#Elquines 20 \r\n#L112#Paralyze 20 \r\n#L113#Meteor Shower 20 \r\n#L114#Ice Demon 20 \r\n#L115#Ifrit 20 \r\n#L116#Chain Lightning 20 \r\n#L117#Blizzard 20 \r\n#L118#Angel Ray 20 \r\n#L119#Genesis 20 \r\n#L120#Holy Shield 20 \r\n#L121#Sharp Eyes 20 \r\n#L122#Dragon's Breath 20 \r\n#L123#Hurricane 20 \r\n#L124#Hamstring Shot \r\n#L125#Phoenix 20 \r\n#L126#Bow Expert 20 \r\n#L127#Concentrate 20 \r\n#L128#Marksman Boost 20 \r\n#L129#Piercing Arrow 20 \r\n#L130#Blind 20 \r\n#L131#Frostprey 20 \r\n#L132#Snipe 20 \r\n#L133#Taunt 20 \r\n#L134#Triple Throw 20 \r\n#L135#Shadow Stars 20 \r\n#L136#Shadow Shifter 20 \r\n#L137#Venomous Star/Stab 20 \r\n#L138#Ninja Abush 20 \r\n#L139#Ninja Storm 20 \r\n#L140#Assassinate 20 \r\n#L141#Boomerang Step 20 \r\n#L142#Smokescreen 20 \r\n#L143#Dragon Strike 20 \r\n#L144#Barrage 20 \r\n#L145#Energy Orb 20 \r\n#L146#Super Transformation 20 \r\n#L147#Demolition 20 \r\n#L148#Snatch 20 \r\n#L149#Speed Infusion 20 \r\n#L150#Time Leap 20 \r\n#L151#Elemental Boost \r\n#L152#Bullseye 20 \r\n#L153#Wrath of the Octopi 20 \r\n#L154#Aerial Strike 20 \r\n#L155#Rapid Fire 20 \r\n#L156#Hypnotize 20 \r\n#L157#Battleship Cannon 20 \r\n#L158#Battleship Torpedo 20#k");
						} else { notenoughpoints(10000); }
					break;
				case 14:
					if (cm.getChar().getBossPoints() > 10000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L35#Cloak of Corruption \r\n#L36#Crystal Blade \r\n#L37#El Nathian Cape \r\n#L26#Miner's Hat \r\n#L1005#Crossheider \r\n#L62#Khanjar#k");
						} else { notenoughpoints(10000); }
					break;
				case 15:
					if (cm.getChar().getBossPoints() > 15000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L159#Achilles 30 \r\n#L160#Guardian 30 \r\n#L161#Advanced Combo Attack 30 \r\n#L162#Monster Magnet \r\n#L163#Power Stance 30 \r\n#L164#Rush 30 \r\n#L165#Brandish 30 \r\n#L166#Enrage 30 \r\n#L167#Blast 30 \r\n#L168#Heaven's Hammer 30 \r\n#L169#Berserk 30 \r\n#L170#Big Bang 30 \r\n#L171#Mana Reflection 30 \r\n#L172#Infinity 30 \r\n#L173#Fire Demon 30 \r\n#L174#Elquines 30 \r\n#L175#Paralyze 30 \r\n#L176#Meteor Shower 30 \r\n#L178#Ice Demon 30 \r\n#L179#Ifrit 30 \r\n#L180#Chain Lightning 30 \r\n#L181#Blizzard 30 \r\n#L182#Angel Ray 30 \r\n#L183#Genesis 30 \r\n#L177#Holy Shield 30 \r\n#L184#Sharp Eyes 30 \r\n#L185#Dragon's Breath 30 \r\n#L186#Hurricane 30 \r\n#L187#Hamstring Shot 30 \r\n#L188#Phoenix 30 \r\n#L189#Bow Expert 30 \r\n#L190#Concentrate 30 \r\n#L191#Marksman Boost 30 \r\n#L192#Piercing Arrow 30 \r\n#L193#Blind 30 \r\n#L194#Frostyprey 30 \r\n#L195#Snipe 30 \r\n#L196#Taunt 30 \r\n#L197#Triple Throw 30 \r\n#L198#Shadow Stars 30 \r\n#L199#Shadow Shifter 30 \r\n#L200#Venomous Star/Stab 30 \r\n#L201#Ninja Ambush 30 \r\n#L202#Ninja Storm 30 \r\n#L203#Assassinate 30 \r\n#L204#Boomerange Step 30 \r\n#L205#Smokescreen 30 \r\n#L206#Dragon Strike 30 \r\n#L207#Barrage 30 \r\n#L208#Energy Orb 30 \r\n#L209#Demolition 30 \r\n#L210#Snatch 30 \r\n#L211#Time Leap 30 \r\n#L212#Elemental Boost 30 \r\n#L213#Aerial Strike 30 \r\n#L214#Rapid Fire 30 \r\n#L215#Battleship Cannon 30 \r\n#L216#Battleship Torpedo 30#k");
						} else { notenoughpoints(15000); }
					break;
				case 16:
					if (cm.getChar().getBossPoints() > 15000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L58#Elemental Wand 1 \r\n#L59#Elemental Wand 2 \r\n#L60#Elemental Wand 3 \r\n#L61#Elemental Wand 4 \r\n#L66#Elemental Staff 1 \r\n#L67#Elemental Staff 2 \r\n#L68#Elemental Staff 3 \r\n#L69#Elemental Staff 4#k");
						} else { notenoughpoints(15000); }
					break;
				case 17:
					if (cm.getChar().getBossPoints() > 20000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L75#Timeless Executioners \r\n#L76#Timeless Nibleheim \r\n#L77#Timeless Allargando \r\n#L78#Timeless Bellocce \r\n#L79#Timeless Bardiche \r\n#L80#Timeless Tabarzin \r\n#L250#Timeless Diesra \r\n#L251#Timeless Alchupiz \r\n#L252#Timeless Engaw \r\n#L253#Timeless Black Beauty \r\n#L254#Timeless Enreal Tear \r\n#L255#Timeless Aeas Hand \r\n#L256#Timeless Lampion \r\n#L257#Timeless Pescas \r\n#L258#Timeless Killic \r\n#L81#Timeless Equinox \r\n#L259#Timeless Blindness \r\n#L92#Dragon Khanjar#l \r\n#L930#Timeless Kite Shield \r\n#L931#Timeless Prelude \r\n#L932#Timeless List \r\n#L226#Blue Dragon Set \r\n#L227#Red Hunter Set \r\n#L228#Blue Elemental Set \r\n#L229#Black Garina Set \r\n#L230#Canopus Set#k");
						} else { notenoughpoints(20000); }
					break;
				case 18:
					if (cm.getChar().getBossPoints() > 30000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L84#Elemental Wand 5 \r\n#L85#Elemental Wand 6 \r\n#L86#Elemental Wand 7 \r\n#L87#Elemental Wand 8 \r\n#L88#Elemental Staff 5 \r\n#L89#Elemental Staff 6 \r\n#L90#Elemental Staff 7 \r\n#L91#Elemental Staff 8#k");
						} else { notenoughpoints(30000); }
					break;
				case 19:
					if (cm.getChar().getBossPoints() > 40000) {
						cm.sendSimple("Which item would you like to obtain? #b\r\n#L400#Maple Warrior 20 \r\n#L401#Enrage 30 \r\n#L402#Heaven's Hammer 30 \r\n#L403#Berserk 30 \r\n#L404#Meteor Shower 30 \r\n#L405#Blizzard 30 \r\n#L406#Genesis 30 \r\n#L407#Triple Throw 30 \r\n#L408#Time Leap 30 \r\n#L409#Aerial Strike 30 \r\n#L410#Snipe 30#k");
						} else { notenoughpoints(40000); }
					break;
				case 225:
					if (cm.getChar().getBossPoints() > 50000) {
					cm.teachSkill(1005, 1, 1);
					cm.getPlayer().getClient().getSession().write(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.PinkText.getType(), "You have learned Echo of Hero with level 1 and with max level 1."));
					cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 50000);}
					else { notenoughpoints(50000); }
					break;
				case 232:
					if (cm.getChar().getBossPoints() > 7500) {
					cm.sendSimple("Which job's skill would you like to master? #b\r\n#L233#Warrior \r\n#L234#Magician \r\n#L235#Bowman \r\n#L236#Thief \r\n#L237#Pirate");
					}
					break;
			}
		} else if (status == 6) {
			if (cm.getChar().getBossPoints() < 1000) { notenoughpoints(1000); }
			switch (selection) {
				case 29: giveitem(1000, 4031751, 1, false); break;
				case 30: giveitem(1000, 4031753, 1, false); break;
				case 31: giveitem(1000, 4031754, 1, false); break;
				case 32: giveitem(1000, 4031752, 1, false); break;
				case 33: giveitem(1000, 4031750, 1, false); break;
				case 34: giveitem(1000, 4031936, 1, false); break;
				case 218: giveitem(1000, 4032134, 1, false); break;
			if (cm.getChar().getBossPoints() < 2500) { notenoughpoints(2500); }
				case 38: giveitem(2500, 4031757, 1, false); break;
				case 39: giveitem(2500, 4031756, 1, false); break;
				case 40: giveitem(2500, 4031900, 1, false); break;
				case 41: giveitem(2500, 4031898, 1, false); break;
				case 42: giveitem(2500, 4031920, 1, false); break;
				case 43: giveitem(2500, 4031918, 1, false); break;
				case 44: giveitem(2500, 4031919, 1, false); break;
				case 45: giveitem(2500, 4031899, 1, false); break;
				case 46: giveitem(2500, 4031897, 1, false); break;
				case 217: giveitem(2500, 4032026, 1, false); break;
			if (cm.getChar().getBossPoints() < 5000) { notenoughpoints(5000); }
				case 20: giveitem(5000, 1452054, 1, true); break;
				case 21: giveitem(5000, 1462047, 1, true); break;
				case 22: giveitem(5000, 1472065, 1, true); break;
				case 23: giveitem(5000, 1332067, 1, true); break;
				case 24: giveitem(5000, 1332070, 1, true); break;
				case 25: giveitem(5000, 1382054, 1, true); break;
				case 219: giveitem(5000, 1302088, 1, true); break;
				case 220: giveitem(5000, 1102176, 1, true); break;
				case 221: giveitem(5000, 1060127, 1, true); break;
				case 222: giveitem(5000, 1061149, 1, true); break;
				case 27: giveitem(5000, 4031760, 1, false); break;
				case 47: giveitem(5000, 4031915, 1, false); break;
				case 48: giveitem(5000, 4031914, 1, false); break;
				case 49: giveitem(5000, 4031916, 1, false); break;
				case 50: giveitem(5000, 4031759, 1, false); break;
				case 51: giveitem(5000, 4031917, 1, false); break;
				case 52: giveitem(5000, 4031758, 1, false); break;
				case 53: giveitem(5000, 4031755, 1, false); break;
				case 54: giveitem(5000, 4031913, 1, false); break;
			if (cm.getChar().getBossPoints() < 7500) {notenoughpoints(7500); }
				case 233:
					cm.sendSimple("Which warrior job's skill would you like to master? #b\r\n#L238#Hero \r\n#L239#Paladin \r\n#L240#Dark Knight");
					break;
				case 234:
					cm.sendSimple("Which magician job's skill would you like to master? #b\r\n#L241#F/P Arch Mage \r\n#L242#I/L Arch Mage \r\n#L243#Bishop");
					break;
				case 235:
					cm.sendSimple("Which bowman job's skill would you like to master? #b\r\n#L244#Bow Master \r\n#L245#Marksman");
					break;
				case 236:
					cm.sendSimple("Which thief job's skill would you like to master? #b\r\n#L246#Night Lord \r\n#L247#Shadower");
					break;
				case 237:
					cm.sendSimple("Which pirate job's skill would you like to master? #b\r\n#L248#Buccaneer \r\n#L249#Corsair");
					break;
			if (cm.getChar().getBossPoints() < 10000) { notenoughpoints(10000); }
				case 94: giveitem(10000, 2290002, 1, false); break;
				case 95: giveitem(10000, 2290014, 1, false); break;
				case 96: giveitem(10000, 2290008, 1, false); break;
				case 97: giveitem(10000, 2290000, 1, false); break;
				case 98: giveitem(10000, 2290006, 1, false); break;
				case 99: giveitem(10000, 2290004, 1, false); break;
				case 100: giveitem(10000, 2290010, 1, false); break;
				case 101: giveitem(10000, 2290016, 1, false); break;
				case 102: giveitem(10000, 2290012, 1, false); break;
				case 103: giveitem(10000, 2290018, 1, false); break;
				case 104: giveitem(10000, 2290019, 1, false); break;
				case 105: giveitem(10000, 2290020, 1, false); break;
				case 106: giveitem(10000, 2290022, 1, false); break;
				case 107: giveitem(10000, 2290026, 1, false); break;
				case 108: giveitem(10000, 2290024, 1, false); break;
				case 109: giveitem(10000, 2290028, 1, false); break;
				case 110: giveitem(10000, 2290036, 1, false); break;
				case 111: giveitem(10000, 2290038, 1, false); break;
				case 112: giveitem(10000, 2290030, 1, false); break;
				case 113: giveitem(10000, 2290040, 1, false); break;
				case 114: giveitem(10000, 2290042, 1, false); break;
				case 114: giveitem(10000, 2290042, 1, false); break;
				case 115: giveitem(10000, 2290044, 1, false); break;
				case 116: giveitem(10000, 2290032, 1, false); break;
				case 117: giveitem(10000, 2290046, 1, false); break;
				case 118: giveitem(10000, 2290050, 1, false); break;
				case 119: giveitem(10000, 2290048, 1, false); break;
				case 120: giveitem(10000, 2290034, 1, false); break;
				case 121: giveitem(10000, 2290052, 1, false); break;
				case 122: giveitem(10000, 2290054, 1, false); break;
				case 123: giveitem(10000, 2290060, 1, false); break;
				case 124: giveitem(10000, 2290058, 1, false); break;
				case 125: giveitem(10000, 2290062, 1, false); break;
				case 126: giveitem(10000, 2290056, 1, false); break;
				case 127: giveitem(10000, 2290064, 1, false); break;
				case 128: giveitem(10000, 2290066, 1, false); break;
				case 129: giveitem(10000, 2290070, 1, false); break;
				case 130: giveitem(10000, 2290068, 1, false); break;
				case 131: giveitem(10000, 2290072, 1, false); break;
				case 132: giveitem(10000, 2290074, 1, false); break;
				case 133: giveitem(10000, 2290080, 1, false); break;
				case 134: giveitem(10000, 2290084, 1, false); break;
				case 135: giveitem(10000, 2290088, 1, false); break;
				case 136: giveitem(10000, 2290076, 1, false); break;
				case 137: giveitem(10000, 2290078, 1, false); break;
				case 138: giveitem(10000, 2290082, 1, false); break;
				case 139: giveitem(10000, 2290086, 1, false); break;
				case 140: giveitem(10000, 2290092, 1, false); break;
				case 141: giveitem(10000, 2290090, 1, false); break;
				case 142: giveitem(10000, 2290094, 1, false); break;
				case 143: giveitem(10000, 2290097, 1, false); break;
				case 144: giveitem(10000, 2290106, 1, false); break;
				case 145: giveitem(10000, 2290099, 1, false); break;
				case 146: giveitem(10000, 2290101, 1, false); break;
				case 147: giveitem(10000, 2290102, 1, false); break;
				case 148: giveitem(10000, 2290104, 1, false); break;
				case 149: giveitem(10000, 2290108, 1, false); break;
				case 150: giveitem(10000, 2290110, 1, false); break;
				case 151: giveitem(10000, 2290112, 1, false); break;
				case 152: giveitem(10000, 2290124, 1, false); break;
				case 153: giveitem(10000, 2290114, 1, false); break;
				case 154: giveitem(10000, 2290115, 1, false); break;
				case 155: giveitem(10000, 2290117, 1, false); break;
				case 156: giveitem(10000, 2290123, 1, false); break;
				case 157: giveitem(10000, 2290119, 1, false); break;
				case 158: giveitem(10000, 2290121, 1, false); break;
			if (cm.getChar().getBossPoints() < 10000) { notenoughpoints(10000); }
				case 35: giveitem(10000, 1102193, 1, true); break;
				case 36: giveitem(10000, 1302106, 1, true); break;
				case 37: giveitem(10000, 1102191, 1, true); break;
				case 26: giveitem(10000, 1002856, 1, true); break;
				case 62: giveitem(10000, 1092050, 1, true); break;
				case 63: giveitem(10000, 1472072, 1, true); break;
				case 64: giveitem(10000, 1462052, 1, true); break;
				case 65: giveitem(10000, 1402048, 1, true); break;
				case 1005: giveitem(10000, 1092061, 1, true); break;
			if (cm.getChar().getBossPoints() < 15000) { notenoughpoints(15000); }
				case 159: giveitem(15000, 2290003, 1, false); break;
				case 160: giveitem(15000, 2290015, 1, false); break;
				case 161: giveitem(15000, 2290009, 1, false); break;
				case 162: giveitem(15000, 2290001, 1, false); break;
				case 163: giveitem(15000, 2290007, 1, false); break;
				case 164: giveitem(15000, 2290005, 1, false); break;
				case 165: giveitem(15000, 2290011, 1, false); break;
				case 166: giveitem(15000, 2290017, 1, false); break;
				case 167: giveitem(15000, 2290013, 1, false); break;
				case 168: giveitem(15000, 2290021, 1, false); break;
				case 169: giveitem(15000, 2290023, 1, false); break;
				case 170: giveitem(15000, 2290027, 1, false); break;
				case 171: giveitem(15000, 2290025, 1, false); break;
				case 172: giveitem(15000, 2290029, 1, false); break;
				case 173: giveitem(15000, 2290037, 1, false); break;
				case 174: giveitem(15000, 2290039, 1, false); break;
				case 175: giveitem(15000, 2290031, 1, false); break;
				case 176: giveitem(15000, 2290041, 1, false); break;
				case 177: giveitem(15000, 2290035, 1, false); break;
				case 178: giveitem(15000, 2290043, 1, false); break;
				case 179: giveitem(15000, 2290045, 1, false); break;
				case 180: giveitem(15000, 2290033, 1, false); break;
				case 181: giveitem(15000, 2290047, 1, false); break;
				case 182: giveitem(15000, 2290051, 1, false); break;
				case 183: giveitem(15000, 2290049, 1, false); break;
				case 184: giveitem(15000, 2290053, 1, false); break;
				case 185: giveitem(15000, 2290055, 1, false); break;
				case 186: giveitem(15000, 2290061, 1, false); break;
				case 187: giveitem(15000, 2290059, 1, false); break;
				case 188: giveitem(15000, 2290063, 1, false); break;
				case 189: giveitem(15000, 2290057, 1, false); break;
				case 190: giveitem(15000, 2290065, 1, false); break;
				case 191: giveitem(15000, 2290067, 1, false); break;
				case 192: giveitem(15000, 2290071, 1, false); break;
				case 193: giveitem(15000, 2290069, 1, false); break;
				case 194: giveitem(15000, 2290073, 1, false); break;
				case 195: giveitem(15000, 2290075, 1, false); break;
				case 196: giveitem(15000, 2290081, 1, false); break;
				case 197: giveitem(15000, 2290085, 1, false); break;
				case 198: giveitem(15000, 2290089, 1, false); break;
				case 199: giveitem(15000, 2290077, 1, false); break;
				case 200: giveitem(15000, 2290079, 1, false); break;
				case 201: giveitem(15000, 2290083, 1, false); break;
				case 202: giveitem(15000, 2290087, 1, false); break;
				case 203: giveitem(15000, 2290093, 1, false); break;
				case 204: giveitem(15000, 2290091, 1, false); break;
				case 205: giveitem(15000, 2290095, 1, false); break;
				case 206: giveitem(15000, 2290098, 1, false); break;
				case 207: giveitem(15000, 2290107, 1, false); break;
				case 208: giveitem(15000, 2290100, 1, false); break;
				case 209: giveitem(15000, 2290103, 1, false); break;
				case 210: giveitem(15000, 2290105, 1, false); break;
				case 211: giveitem(15000, 2290111, 1, false); break;
				case 212: giveitem(15000, 2290113, 1, false); break;
				case 213: giveitem(15000, 2290116, 1, false); break;
				case 214: giveitem(15000, 2290118, 1, false); break;
				case 215: giveitem(15000, 2290120, 1, false); break;
				case 216: giveitem(15000, 2290122, 1, false); break;
			if (cm.getChar().getBossPoints() < 15000) { notenoughpoints(15000); }
				case 58: giveitem(15000, 1372035, 1, true); break;
				case 59: giveitem(15000, 1372036, 1, true); break;
				case 60: giveitem(15000, 1372037, 1, true); break;
				case 61: giveitem(15000, 1372038, 1, true); break;
				case 66: giveitem(15000, 1382045, 1, true); break;
				case 67: giveitem(15000, 1382046, 1, true); break;
				case 68: giveitem(15000, 1382047, 1, true); break;
				case 69: giveitem(15000, 1382048, 1, true); break;
			if (cm.getChar().getBossPoints() < 20000) { notenoughpoints(20000); }
				case 226: {
				cm.gainItem(1052075, 1, true, true);
				cm.gainItem(1072273, 1, true, true);
				cm.gainItem(1002551, 1, true, true);
				cm.gainItem(1082168, 1, true, true);
				cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 20000);} break;
				case 227: {
				cm.gainItem(1072269, 1, true, true);
				cm.gainItem(1082163, 1, true, true);
				cm.gainItem(1002547, 1, true, true);
				cm.gainItem(1052071, 1, true, true);
				cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 20000);} break;
				case 228: {
				cm.gainItem(1072268, 1, true, true);
				cm.gainItem(1082164, 1, true, true);
				cm.gainItem(1002773, 1, true, true);
				cm.gainItem(1052076, 1, true, true);
				cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 20000);} break;
				case 229: {
				cm.gainItem(1072272, 1, true, true);
				cm.gainItem(1002550, 1, true, true);
				cm.gainItem(1082167, 1, true, true);
				cm.gainItem(1052072, 1, true, true);
				cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 20000);} break;
				case 230: {
				cm.gainItem(1052134, 1, true, true);
				cm.gainItem(1072321, 1, true, true);
				cm.gainItem(1002649, 1, true, true);
				cm.gainItem(1082216, 1, true, true);
				cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 20000);} break;
				case 75: giveitem(20000, 1302081, 1, true); break;
				case 76: giveitem(20000, 1402046, 1, true); break;
				case 77: giveitem(20000, 1322060, 1, true); break;
				case 78: giveitem(20000, 1422037, 1, true); break;
				case 79: giveitem(20000, 1312037, 1, true); break;
				case 80: giveitem(20000, 1412033, 1, true); break;
				case 81: giveitem(20000, 1482023, 1, true); break;
				case 92: giveitem(20000, 1092049, 1, true); break;
				case 930: giveitem(20000, 1092058, 1, true); break;
				case 931: giveitem(20000, 1092057, 1, true); break;
				case 932: giveitem(20000, 1092059, 1, true); break;
				case 250: giveitem(20000,1442063, 1, true); break;
				case 251: giveitem(20000,1432047, 1, true); break;
				case 252: giveitem(20000,1452057, 1, true); break;
				case 253: giveitem(20000,1462050, 1, true); break;
				case 254: giveitem(20000,1372044, 1, true); break;
				case 255: giveitem(20000,1382057, 1, true); break;
				case 256: giveitem(20000,1472068, 1, true); break;
				case 257: giveitem(20000,1332073, 1, true); break;
				case 258: giveitem(20000,1332074, 1, true); break;
				case 259: giveitem(20000,1492023, 1, true); break;
			if (cm.getChar().getBossPoints() < 30000) { notenoughpoints(30000); }
				case 84: giveitem(30000, 1372039, 1, true); break;
				case 85: giveitem(30000, 1372040, 1, true); break;
				case 86: giveitem(30000, 1372041, 1, true); break;
				case 87: giveitem(30000, 1372042, 1, true); break;
				case 88: giveitem(30000, 1382049, 1, true); break;
				case 89: giveitem(30000, 1382050, 1, true); break;
				case 90: giveitem(30000, 1382051, 1, true); break;
				case 91: giveitem(30000, 1382052, 1, true); break;
			if (cm.getChar().getBossPoints() < 40000) { notenoughpoints(40000); }
				case 400: giveitem(40000, 2290096, 1); break;
				case 401: giveitem(40000, 2290017, 1); break;
				case 402: giveitem(40000, 2290021, 1); break;
				case 403: giveitem(40000, 2290023, 1); break;
				case 404: giveitem(40000, 2290041, 1); break;
				case 405: giveitem(40000, 2290047, 1); break;
				case 406: giveitem(40000, 2290049, 1); break;
				case 407: giveitem(40000, 2290085, 1); break;
				case 408: giveitem(40000, 2290111, 1); break;
				case 409: giveitem(40000, 2290116, 1); break;
				case 410: giveitem(40000, 2290075, 1); break;
			}
		} else if (status == 7) {
		switch (selection) {
			case 238:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L260##s1120005# Guardian \r\n#L261##s1120003# Advanced Combo Attack \r\n#L262##s1121002# Power Stance \r\n#L263##s1121006# Rush \r\n#L264##s1121010# Enrage#k");
				break;
			case 239:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L265##s1220005# Guardian \r\n#L266##s1221002# Power Stance \r\n#L267##s1221007# Rush \r\n#L268##s1221003# Holy Charge: Sword \r\n#L269##s1221004# Divine Charge: BW \r\n#L270##s1220010# Advanced Charge \r\n#L271##s1221011# Heaven's Hammer#k");
				break;
			case 240:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L272##s1321003# Rush \r\n#L273##s1321002# Power Stance \r\n#L274##s1320006# Berserk#k");
				break;
			case 241:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L275##s2121004# Infinity \r\n#L276##s2121003# Fire Demon \r\n#L278##s2121005# Elquines \r\n#L279##s2121007# Meteor Shower");
				break;
			case 242:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L280##s2221004# Infinity \r\n#L281##s2221003# Ice Demon \r\n#L282##s2221005# Ifrit \r\n#L283##s2221007# Blizzard");
				break;
			case 243:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L284##s2321004# Infinity \r\n#L285##s2321007# Angel Ray \r\n#L286##s2321006# Resurrection \r\n#L287##s2321008# Genesis");
				break;
			case 244:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L288##s3121003# Dragon's Breath \r\n#L289##s3121004# Hurricane \r\n#L290##s3121006# Phoenix \r\n#L291##s3121008# Concentrate");
				break;
			case 245:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L292##s3221003# Dragon's Breath \r\n#L293##s3221001# Piercing Arrow \r\n#L294##s3221005# Frostprey \r\n#L295##s3221007# Snipe");
				break;
			case 246:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L296##s4121003# Taunt \r\n#L297##s4121007# Triple Throw \r\n#L298##s4121004# Ninja Ambush \r\n#L299##s4121008# Ninja Storm");
				break;
			case 247:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L300##s4221003# Taunt \r\n#L301##s4221001# Assassinate \r\n#L302##s4221004# Ninja Ambush \r\n#L303##s4221006# Smokescreen");
				break;
			case 248:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L304##s5121003# Super Transformation \r\n#L305##s5121004# Demolition \r\n#L306##s5121005# Snatch \r\n#L307##s5121010# Time Leap");
				break;
			case 249:
				cm.sendSimple("Which skill would you like to master? #b\r\n#L308##s5221003# Aerial Strike \r\n#L309##s5221009# Hypnotize \r\n#L310##s5221006# Battleship \r\n#L311##s5221007# Battleship Cannon \r\n#L312##s5221008# Battleship Torpedo");
				break;
		}
		} else if (status==8) {
		switch (selection) {
		case 260:
			cm.teachSkill(1120005, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 261:
			cm.teachSkill(1120003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 262:
			cm.teachSkill(1121002, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 263:
		cm.teachSkill(1121006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 264:
		cm.teachSkill(1121010, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 265:
			cm.teachSkill(1220006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 266:
			cm.teachSkill(1221002, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 267:
			cm.teachSkill(1221007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 268:
			cm.teachSkill(1221003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 269:
			cm.teachSkill(1221004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 270:
			cm.teachSkill(1220010, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 271:
			cm.teachSkill(1221011, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 272:
			cm.teachSkill(1321003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 273:
			cm.teachSkill(1321002, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 274:
			cm.teachSkill(1320006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 275:
			cm.teachSkill(2121004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 276:
			cm.teachSkill(2121003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 278:
			cm.teachSkill(2121005, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 279:
			cm.teachSkill(2121007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 280:
			cm.teachSkill(2221004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 281:
			cm.teachSkill(2221003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 282:
			cm.teachSkill(2221005, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 283:
			cm.teachSkill(2221007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 284:
			cm.teachSkill(2321004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 285:
			cm.teachSkill(2321007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 286:
			cm.teachSkill(2321006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 287:
			cm.teachSkill(2321008, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 288:
			cm.teachSkill(3121003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 289:
			cm.teachSkill(3121004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 290:
			cm.teachSkill(3121006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 291:
			cm.teachSkill(3121008, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 292:
			cm.teachSkill(3221003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 293:
			cm.teachSkill(3221001, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 294:
			cm.teachSkill(3221005, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 295:
			cm.teachSkill(3221007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 296:
			cm.teachSkill(4121003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 297:
			cm.teachSkill(4121007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 298:
			cm.teachSkill(4121004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 299:
			cm.teachSkill(4121008, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 300:
			cm.teachSkill(4221003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 301:
			cm.teachSkill(4221001, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 302:
			cm.teachSkill(4221004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 303:
			cm.teachSkill(4221006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 304:
			cm.teachSkill(5121003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 305:
			cm.teachSkill(5121004, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 306:
			cm.teachSkill(5121005, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 307:
			cm.teachSkill(5121010, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 308:
			cm.teachSkill(5221003, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 309:
			cm.teachSkill(5221009, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 310:
			cm.teachSkill(5221006, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 311:
			cm.teachSkill(5221007, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		case 312:
			cm.teachSkill(5221008, 0, 10);
			cm.getChar().setBossPoints(cm.getChar().getBossPoints() - 7500);
			break;
		}
		}
	}
}

function giveitem(points, itemid, quantity, israndom) {
	if (israndom) { cm.gainItem(itemid, quantity, true, true); } else { cm.gainItem(itemid, quantity); }
	cm.getChar().setBossPoints(cm.getChar().getBossPoints() - points);
	cm.dispose();
}

function notenoughpoints(points) {
	cm.sendOk("You do not have #b " + points + " #k Boss Quest Points. Sorry!");
	cm.dispose();
}