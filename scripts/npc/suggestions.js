var status = -2;
var suggest = 0;

function start() {
    status = -2
    action(1, 0, 0);
}
                            
function action(mode, type, selection) {
                                
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
                                        
        if(status == -1) { // first window
            var text = "I'm here to provide an easy to use suggestion system. Do you want to suggest a change to our server? Or maybe you'd like" 
                        + "to support what someone else has suggestioned? You can do all that right here! Let's get started, what would you like to do?\r\n\r\n" 
                        + "#L0##bI would like to view current suggestions#k#l\r\n" 
                        + "#L1##bI would like to sumbit a new suggestions#k#l\r\n" 
                        + "#L2##bHow exactly does this system work?#k#l\r\n" ;
            cm.sendSimple(text);
        } else if (status == 0) { // result tree of first wondow (second window)

            if (selection == 0) {
                cm.sendOk("list of suggestions");
                suggest = 2;
                var text = "These are the current suggestions:\r\n\r\n" + cm.getPlayer().getSuggestion().showSuggestions();
                cm.dispose();
            } else if (selection == 1) {
                suggest = 1;
                var text = "What would you like to suggest?";
                cm.sendGetText(text);
            } else if (selection == 2) {
                cm.sendOk("This is a platform for the community to voice their opinions on the server. \r\n\r\n" 
                            + "You can view, submit, and support suggestions that have been made by players such as yourself." 
                            + " You can show your support for a specific suggestion by upvoting it. This will cause that " 
                            + "suggestion to move up on the list. The opposite will happen if you downvote a suggestion \r\n\r\n" +
                            "This will also help the staff organize player input and ideas. Something that will greatly benefit the server.");
                cm.dispose();
            }
        } else if (status == 1) {
            if (suggest == 1) {                
                cm.insertSuggestion(cm.getClient().getAccID(), cm.getText()); 
                cm.sendOk("You have successfully submitted the following suggestion: \r\n\r\n" + cm.getText());
                cm.dispose();
            }
        }
    } 
}