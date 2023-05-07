package client.command.commands.donor;

import client.MapleClient;
import client.command.Command;
import server.MapleShopFactory;

public class ShopCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleShopFactory.getInstance().getShop(69420).openShopDlg(c);
    }
}

