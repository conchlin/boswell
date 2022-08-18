 
var status; 
var sel; 

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == -1) { 
        cm.dispose(); 
    } else { 
        if (mode == 0) { 
            cm.dispose(); 
            return; 
        } 
        if (mode == 1) 
            status++; 
        else 
            status--; 
            if (status == 0) { 
            if (cm.getLevel() < 20) { 
                cm.sendDimensionalMirror("#-1# There is no place for you to transport to from here."); 
                cm.dispose(); 
            } else { 
                var selStr = "";
                
                if (cm.getLevel() >= 25) { 
                    selStr += "#1# Mu Lung Dojo"; 
                } 

                if (cm.getLevel() >= 15) { 
                    selStr += "#8# Fishing Lagoon"; 
                }  

            //  if (cm.getLevel() >= 20) { // summer event
            //        selStr += "#7# Aramia's Tree"; 
            //  }
                
                cm.sendDimensionalMirror(selStr); 
            } 
        } else if (status == 1) { 
            cm.getPlayer().saveLocation("MIRROR"); 
            /**
             * 0 ariantpq
             * 1 dojo
             * 2 cpq1
             * 3 cpq2
             * 4 that other pirate pq
             * 5 nett pyramid
             * 6 kerning subway
             * 7 aramia tree (maple hill)
             * 8 fishing
             */
            switch (selection) {  // UI.wz/UIWindow.img/SlideMenu.img
                case 1:
                    cm.warp(925020000, 0); 
                    break; 
                case 7:
                    cm.warp(970010000, 0);
                    break;
                case 8:
                    cm.warp(741000200, 0);
            } 
            cm.dispose(); 
        } 
    } 
}  
