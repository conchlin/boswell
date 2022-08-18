// tru

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == -1 || (mode == 0 && status == 0)){
		cm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;

	if(status == 0){ 
		if (cm.getPlayer().getMap().countMonsters() > 0) {
			cm.sendOk("They ambushed me... *Sniff sniff* Don't worry about me just destroy him!");
			cm.dispose();
		} else {
			cm.sendNext("Whoa, were you able to defeat them? I wouldn't expect anything less from a hero. Ugh, let's clean this place up.");
			mode++;
		}
	} else if(mode == 1) {
		cm.warp(104000004);
		//cm.dispose();
		cm.openNpc(1002104);
		//return;
	}
}


//function start() {
//	if (cm.getPlayer().getMap().countMonsters() > 0) {
//    	cm.sendOk("They ambushed me... *Sniff sniff* Don't worry about me just destroy him!");
//		cm.dispose();
//	} else {
//		cm.sendOk("Whoa, were you able to defeat them? I wouldn't expect anything less from a hero. Ugh, let's clean this place up.");
//		cm.warp(104000004);
//		cm.dispose();
//	}
//}