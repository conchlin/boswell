/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.command.commands.admin;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import server.cashshop.CashItemFactory;

public class ReloadCashShopCommand extends Command {
    {
        setDescription("reload cash shop");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        CashItemFactory.reloadCashShop();
        player.dropMessage(5, "CashShop reloaded.");
    }
}