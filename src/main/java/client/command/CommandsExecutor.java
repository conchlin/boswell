/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2018 RonanLana

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
   @Author: Arthur L - Refactored command content into modules
   @Modified: Saffron - further refactoring
*/
package client.command;

import client.command.commands.donor.*;
import client.command.commands.player.*;
import client.command.commands.admin.*;
import client.command.commands.staff.*;

import client.MapleClient;

import tools.FilePrinter;
import tools.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class CommandsExecutor {

    public static CommandsExecutor instance = new CommandsExecutor();

    public static CommandsExecutor getInstance() {
        return instance;
    }

    private static final char USER_HEADING = '@';
    private static final char DONOR_HEADING = '~';
    private static final char GM_HEADING = '!';

    public static boolean isCommand(MapleClient client, String content){
        char heading = content.charAt(0);
        if (client.getPlayer().isGM()){
            return heading == USER_HEADING || heading == GM_HEADING;
        } else if (client.getPlayer().isDonor()) {
            return heading == USER_HEADING || heading == DONOR_HEADING;
        }
        return heading == USER_HEADING;
    }

    private HashMap<String, Command> registeredCommands = new HashMap<>();
    private Pair<List<String>, List<String>> levelCommandsCursor;
    private List<Pair<List<String>, List<String>>> commandsNameDesc = new ArrayList<>();

    private CommandsExecutor(){
        registerPlayerCommands();
        registerDonorCommands();
        registerInternCommands();
        registerStaffCommands();
        registerAdminCommands();
    }

    public List<Pair<List<String>, List<String>>> getGmCommands() {
        return commandsNameDesc;
    }

    public void handle(MapleClient client, String message){
        if (client.tryacquireClient()) {
            try {
                handleInternal(client, message);
            } finally {
                client.releaseClient();
            }
        } else {
            client.getPlayer().dropMessage(5, "Try again in a while... Latest commands are currently being processed.");
        }
    }

    private void handleInternal(MapleClient client, String message){
        final String splitRegex = "[ ]";
        String[] splitMessage = message.substring(1).split(splitRegex, 2);
        if (splitMessage.length < 2) {
            splitMessage = new String[]{splitMessage[0], ""};
        }

        client.getPlayer().setLastCommandMessage(splitMessage[1]);    // thanks Tochi & Nulliphite for noticing string messages being marshalled lowercase
        final String commandName = splitMessage[0].toLowerCase();
        final String[] lowercaseParams = splitMessage[1].toLowerCase().split(splitRegex);

        final Command command = registeredCommands.get(commandName);
        if (command == null){
            client.getPlayer().yellowMessage("Command '" + commandName + "' is not available. See @commands for a list of available commands.");
            return;
        }
        if (client.getPlayer().gmLevel() < command.getRank()){
            client.getPlayer().yellowMessage("You do not have permission to use this command.");
            return;
        }
        String[] params;
        if (lowercaseParams.length > 0 && !lowercaseParams[0].isEmpty()) {
            params = Arrays.copyOfRange(lowercaseParams, 0, lowercaseParams.length);
        } else {
            params = new String[]{};
        }
        command.execute(client, params);
        writeLog(client, message);
    }

    private void writeLog(MapleClient client, String command){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        FilePrinter.print(FilePrinter.USED_COMMANDS, client.getPlayer().getName() + " used: " + command + " on "
                + sdf.format(Calendar.getInstance().getTime()));
    }

    private void addCommandInfo(String name, Class<? extends Command> commandClass) {
        try {
            levelCommandsCursor.getRight().add(commandClass.newInstance().getDescription());
            levelCommandsCursor.getLeft().add(name);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addCommand(String[] syntaxs, Class<? extends Command> commandClass){
        for (String syntax : syntaxs){
            addCommand(syntax, 0, commandClass);
        }
    }
    private void addCommand(String syntax, Class<? extends Command> commandClass){
        //for (String syntax : syntaxs){
        addCommand(syntax, 0, commandClass);
        //}
    }

    private void addCommand(String[] surtaxes, int rank, Class<? extends Command> commandClass){
        for (String syntax : surtaxes){
            addCommand(syntax, rank, commandClass);
        }
    }

    private void addCommand(String syntax, int rank,  Class<? extends Command> commandClass){
        if (registeredCommands.containsKey(syntax.toLowerCase())){
            System.out.println("Error on register command with name: " + syntax + ". Already exists.");
            return;
        }

        String commandName = syntax.toLowerCase();
        addCommandInfo(commandName, commandClass);

        try {
            Command commandInstance = commandClass.newInstance();     // thanks Halcyon for noticing commands getting reinstanced every call
            commandInstance.setRank(rank);

            registeredCommands.put(commandName, commandInstance);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void registerPlayerCommands(){ // gmLevel 0
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());

        addCommand(new String[]{"help", "commands"}, HelpCommand.class);
        addCommand("time", TimeCommand.class);
        addCommand("uptime", UptimeCommand.class);
        addCommand("dispose", DisposeCommand.class);
        addCommand(new String[]{"showrates", "rates"}, RatesCommand.class);
        addCommand("online", OnlineCommand.class);
        addCommand("gm", GmCommand.class);
        addCommand("reportbug", ReportBugCommand.class);
        addCommand("joinevent", JoinEventCommand.class);
        addCommand("leaveevent", LeaveEventCommand.class);
        //addCommand("boot", BootCommand.class);
        addCommand("gachapon", GachaCommand.class);
        addCommand("staff", StaffCommand.class);
        addCommand("str", StatStrCommand.class);
        addCommand("dex", StatDexCommand.class);
        addCommand("int", StatIntCommand.class);
        addCommand("luk", StatLukCommand.class);
        addCommand("bosshp", BossHpCommand.class);
        addCommand("whatdropsfrom", WhatDropsFromCommand.class);
        addCommand("whodrops", WhoDropsCommand.class);
        addCommand("features", FeaturesCommand.class);
        addCommand("roll", RollCommand.class);
        addCommand("pqtour", PqTourCommand.class);
        addCommand(new String[]{"trophy"}, TrophyCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }
    
    private void registerDonorCommands() { // gmLevel 1
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("henesys", 1, HenesysCommand.class);
        addCommand("shop", 1, ShopCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }
    
    private void registerInternCommands() { // gmLevel 2
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("whereami", 2, WhereaMiCommand.class);
        addCommand("hide", 2, HideCommand.class);
        addCommand("unhide", 2, UnHideCommand.class);
        addCommand("dc", 2, DcCommand.class);
        addCommand("warp", 2, WarpCommand.class);
        addCommand(new String[]{"warphere", "summon"}, 2, SummonCommand.class);
        addCommand(new String[]{"warpto", "reach", "follow"}, 2, ReachCommand.class);
        addCommand("level", 2, LevelCommand.class);
        addCommand("item", 2, ItemCommand.class);
        addCommand("drop", 2, ItemDropCommand.class);
        addCommand("search", 2, SearchCommand.class);
        addCommand("jail", 2, JailCommand.class);
        addCommand("unjail", 2, UnJailCommand.class);
        addCommand("job", 2, JobCommand.class);
        addCommand("checkdmg", 2, CheckDmgCommand.class);
        addCommand("notice", 2, NoticeCommand.class);
        addCommand("noticeanon", 2, NoticeAnonCommand.class);
        addCommand("ban", 2, BanCommand.class);
        addCommand("unban", 2, UnBanCommand.class);
        addCommand(new String[]{"cheater", "flag", "fuckoff"}, 2, CheaterCommand.class);
        addCommand(new String[]{"noncheater", "unflag", "uncheater"}, 2, NonCheaterCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }


    private void registerStaffCommands(){ // gmLevel 3
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());

        addCommand("recharge", 3, RechargeCommand.class);
        addCommand("sp", 3, SpCommand.class);
        addCommand("ap", 3, ApCommand.class);
        addCommand("empowerme", 3, EmpowerMeCommand.class);
        addCommand("buffmap", 3, BuffMapCommand.class);
        addCommand("buff", 3, BuffCommand.class);
        addCommand("bomb", 3, BombCommand.class);
        addCommand("cleardrops", 3, ClearDropsCommand.class);
        addCommand("clearslot", 3, ClearSlotCommand.class);
        addCommand("clearsavelocs", 3, ClearSavedLocationsCommand.class);
        addCommand("gmshop", 3, GmShopCommand.class);
        addCommand("heal", 3, HealCommand.class);
        addCommand("levelpro", 3, LevelProCommand.class);
        addCommand("setstat", 3, SetStatCommand.class);
        addCommand("maxstat", 3, MaxStatCommand.class);
        addCommand("maxskill", 3, MaxSkillCommand.class);
        addCommand("resetskill", 3, ResetSkillCommand.class);
        addCommand("unbug", 3, UnBugCommand.class);
        addCommand("id", 3, IdCommand.class);
        addCommand("debuff", 3, DebuffCommand.class);
        addCommand("fly", 3, FlyCommand.class);
        addCommand("spawn", 3, SpawnCommand.class);
        addCommand("mutemap", 3, MuteMapCommand.class);
        addCommand("inmap", 3, InMapCommand.class);
        addCommand("hpmp", 3, HpMpCommand.class);
        addCommand("maxhpmp", 3, MaxHpMpCommand.class);
        addCommand("music", 3, MusicCommand.class);
        addCommand("monitor", 3, MonitorCommand.class);
        addCommand("monitors", 3, MonitorsCommand.class);
        addCommand("ignore", 3,  IgnoreCommand.class);
        addCommand("ignored", 3, IgnoredCommand.class);
        addCommand("pos", 3, PosCommand.class);
        addCommand("togglecoupon", 3, ToggleCouponCommand.class);
        addCommand("togglewhitechat", 3, ChatCommand.class);
        addCommand("fame", 3, FameCommand.class);
        addCommand("givenx", 3, GiveNxCommand.class);
        addCommand("givems", 3, GiveMesosCommand.class);
        addCommand("expeds", 3, ExpedsCommand.class);
        addCommand("kill", 3, KillCommand.class);
        addCommand("seed", 3, SeedCommand.class);
        addCommand("maxenergy", 3, MaxEnergyCommand.class);
        addCommand("killall", 3, KillAllCommand.class);
        addCommand("rip", 3, RipCommand.class);
        addCommand("openportal", 3, OpenPortalCommand.class);
        addCommand("closeportal", 3, ClosePortalCommand.class);
        addCommand("pe", 3, PeCommand.class);
        addCommand("startevent", 3, StartEventCommand.class);
        addCommand("endevent", 3, EndEventCommand.class);
        addCommand("startmapevent", 3, StartMapEventCommand.class);
        addCommand("stopmapevent", 3, StopMapEventCommand.class);
        addCommand("online2", 3, OnlineTwoCommand.class);
        addCommand("healmap", 3, HealMapCommand.class);
        addCommand("healperson", 3, HealPersonCommand.class);
        addCommand("hurt", 3, HurtCommand.class);
        addCommand("killmap", 3, KillMapCommand.class);
        addCommand("night", 3, NightCommand.class);
        addCommand("npc", 3, NpcCommand.class);
        addCommand("face", 3, FaceCommand.class);
        addCommand("hair", 3, HairCommand.class);
        addCommand("startquest", 3, QuestStartCommand.class);
        addCommand("completequest", 3, QuestCompleteCommand.class);
        addCommand("resetquest", 3, QuestResetCommand.class);
        addCommand("timer", 3, TimerCommand.class);
        addCommand("timermap", 3, TimerMapCommand.class);
        addCommand("timerall", 3, TimerAllCommand.class);
        addCommand("warpmap", 3, WarpMapCommand.class);
        addCommand("warparea", 3, WarpAreaCommand.class);
        addCommand("itemvac", 3, ItemVacCommand.class);
        addCommand("forcevac", 3, ForceVacCommand.class);
        addCommand("zakum", 3, ZakumCommand.class);
        addCommand("horntail", 3, HorntailCommand.class);
        addCommand("pinkbean", 3, PinkbeanCommand.class);
        addCommand("pap", 3, PapCommand.class);
        addCommand("pianus", 3, PianusCommand.class);
        addCommand("cake", 3, CakeCommand.class);
        addCommand("proitem", 3, ProItemCommand.class);
        addCommand("dpstest", 3, DpsTestCommand.class);
        addCommand("dpsbuff", 3, OptimalBuffCommand.class);
        addCommand("server", 3, GmServerCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerAdminCommands(){ // gmLevel 4
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());

        addCommand("servermessage", 4, ServerMessageCommand.class);
        addCommand("seteqstat", 4, SetEqStatCommand.class);
        addCommand("exprate", 4, ExpRateCommand.class);
        addCommand("mesorate", 4, MesoRateCommand.class);
        addCommand("droprate", 4, DropRateCommand.class);
        addCommand("bossdroprate", 4, BossDropRateCommand.class);
        addCommand("questrate", 4, QuestRateCommand.class);
        addCommand("playernpc", 4, PlayerNpcCommand.class);
        addCommand("playernpcremove", 4, PlayerNpcRemoveCommand.class);
        addCommand("pnpc", 4, PnpcCommand.class);
        addCommand("pnpcremove", 4, PnpcRemoveCommand.class);
        addCommand("pmob", 4, PmobCommand.class);
        addCommand("pmobremove", 4, PmobRemoveCommand.class);
        addCommand("debug", 4, DebugCommand.class);
        addCommand("set", 4, SetCommand.class);
        addCommand("showpackets", 4, ShowPacketsCommand.class);
        addCommand("showmovelife", 4, ShowMoveLifeCommand.class);
        addCommand("showsessions", 4, ShowSessionsCommand.class);
        addCommand("iplist", 4, IpListCommand.class);
        addCommand("setgmlevel", 4, SetGmLevelCommand.class);
        addCommand("warpworld", 4, WarpWorldCommand.class);
        addCommand("saveall", 4, SaveAllCommand.class);
        addCommand("dcall", 4, DCAllCommand.class);
        addCommand("mapplayers", 4, MapPlayersCommand.class);
        addCommand("getacc", 4, GetAccCommand.class);
        addCommand("shutdown", 4, ShutdownCommand.class);
        addCommand("clearquestcache", 4, ClearQuestCacheCommand.class);
        addCommand("clearquest", 4, ClearQuestCommand.class);
        addCommand("spawnallpnpcs", 4, SpawnAllPNpcsCommand.class);
        addCommand("eraseallpnpcs", 4, EraseAllPNpcsCommand.class);
        addCommand("addchannel", 4, ServerAddChannelCommand.class);
        addCommand("addworld", 4, ServerAddWorldCommand.class);
        addCommand("removechannel", 4, ServerRemoveChannelCommand.class);
        addCommand("removeworld", 4, ServerRemoveWorldCommand.class);
        addCommand("reloadevents", 4, ReloadEventsCommand.class);
        addCommand("reloaddrops", 4, ReloadDropsCommand.class);
        addCommand("reloadportals", 4, ReloadPortalsCommand.class);
        addCommand("reloadmap", 4, ReloadMapCommand.class);
        addCommand("reloadshops", 4, ReloadShopsCommand.class);
        addCommand("reloadcashshop", 4, ReloadCashShopCommand.class);


        commandsNameDesc.add(levelCommandsCursor);
    }
}
