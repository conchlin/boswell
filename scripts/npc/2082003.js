/**
----------------------------------------------------------------------------------
	Travel from Leafre to Temple of Time 
	2082003 Corba
----------------------------------------------------------------------------------
**/

function start() {
   cm.sendYesNo("If you had wings, I'm sure you could go there.  But, that alone won't be enough.  If you want to fly though the wind that's sharper than a blade, you'll need tough scales as well.  I'm the only #bHalfling#k left that knows the way back... If you want to go there, I can transform you.  No matter what you are, for this moment, would you like to become a #bdragon#k?");
}

function action(mode, type, selection) {
   if (mode == 1) { // yes
      cm.useItem(2210016);
      cm.warp(200090500, 0);
   } else if (mode == 0) { // no
       cm.sendOk("Okay, talk to me if you change your mind!");
   }
   cm.dispose();
}