package constants;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConstants {
    // TODO find a new place for all this info

    //Thread Tracker Configuration
    public static final boolean USE_THREAD_TRACKER = false;      //[SEVERE] This deadlock auditing thing will bloat the memory as fast as the time frame one takes to lose track of a raindrop on a tempesting day. Only for debugging purposes.
    
    //Database Configuration
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
    
    //Server Version
    public static short VERSION = 83;

    //Server Rates And Experience
    public static final double EXP_RATE = 20.0;                     // NOTE: World-specific rates within "world.ini" OVERRIDES the default rates from here.
    public static final double MESO_RATE = 6.0;
    public static final double DROP_RATE = 3.0;                     // pre-release only
    public static final double BOSS_DROP_RATE = 3.0;                // NOTE: Boss drop rate OVERRIDES common drop rate, for bosses-only.
    public static final double QUEST_RATE = 20.0;                   // uses this rate instead of normal EXP_RATE when calculating quest exp rewards
    public static final int PARTY_EXPERIENCE_MOD = 1;               // change for event stuff
    public static final double PQ_BONUS_EXP_RATE = 0.0;             // Rate for the PQ exp reward.

    // Boswell Feature Config
    public static final boolean ENABLE_PQ_TOUR_FEATURE = true;      // Allows the Custom PQ Tour Feature
    public static final boolean HTTP_SERVER = false;                // Allows utilization of the boswell HTTP server for web compat

    //Login Configuration
    public static boolean GM_SERVER = false;                     //Allows flexibility for staff to log on before players and open the server to non-GM connections
    public static final int WLDLIST_SIZE = 21;                  //Max possible worlds on the server.
    public static final int CHANNEL_SIZE = 20;                  //Max possible channels per world (which is 20, based on the channel list on login phase).
    public static final int CHANNEL_LOAD = 100;                 //Max players per channel (limit actually used to calculate the World server capacity).
    public static final int CHANNEL_LOCKS = 20;                 //Total number of structure management locks each channel has.
    
    public static final long PURGING_INTERVAL = 5 * 60 * 1000;
    public static final long  COUPON_INTERVAL = 60 * 60 * 1000;	//60 minutes, 3600000.
    public static final long  UPDATE_INTERVAL = 777;            //Dictates the frequency on which the "centralized server time" is updated.
    
    public static final boolean BCRYPT_MIGRATION = false;        //Performs a migration from old SHA-1 and SHA-512 password to bcrypt.
    public static final boolean DETERRED_MULTICLIENT = false;   //Enables multi-client and suspicious remote IP detection on the login system.
    
    //Besides blocking logging in with several client sessions on the same machine, this also blocks suspicious login attempts for players that tries to login on an account using several diferent remote addresses.
    
    //Multiclient Coordinator Configuration
    public static final int MAX_ALLOWED_ACCOUNT_HWID = 4;       //Allows up to N concurrent HWID's for an account. HWID's remains linked to an account longer the more times it's used to login.
    public static final int MAX_ACCOUNT_LOGIN_ATTEMPT = 15;     //After N tries on an account, login on that account gets disabled for a short period.
    public static final int LOGIN_ATTEMPT_DURATION = 120;       //Period in seconds the login attempt remains registered on the system.
    
    //Ip Configuration
    public static String HOST;
    public static boolean LOCALSERVER;

    //Other Configuration
    public static boolean SHUTDOWNHOOK;

    //Debugging Flags
    public static       boolean USE_DEBUG_SHOW_RCVD_PACKET = false; //Prints on the cmd all received packet ids.
    public static       boolean USE_DEBUG_SHOW_RCVD_MVLIFE = false; //Prints on the cmd all received move life content.
    public static final boolean USE_DEBUG_SHOW_PACKET = false;
    

    public static final boolean USE_IP_VALIDATION = true;           //Enables IP checking when logging in.  
    public static final boolean USE_MAXRANGE = true;                //Will send and receive packets from all events on a map, rather than those of only view range.
    public static final boolean USE_MAXRANGE_ECHO_OF_HERO = true;
    public static final boolean USE_DUEY = true;
    public static final boolean USE_AUTOSAVE = true;                //Enables server autosaving feature (saves characters to DB each 1 hour).
    public static final boolean USE_ENFORCE_ADMIN_ACCOUNT = false;  //Forces accounts having GM characters to be treated as a "GM account" by the client (localhost). Some of the GM account perks is the ability to FLY, but unable to TRADE.
    public static final boolean USE_ENFORCE_ITEM_SUGGESTION = false;//Forces the Owl of Minerva and the Cash Shop to always display the defined item array instead of those featured by the players.
    public static final boolean USE_ENFORCE_MERCHANT_SAVE = true;   //Forces automatic DB save on merchant owners, at every item movement on shop.
    public static final boolean USE_SPAWN_LOOT_ON_ANIMATION = false;//Makes loot appear some time after the mob has been killed (following the mob death animation, instead of instantly).
    public static final boolean USE_ERASE_UNTRADEABLE_DROP = true;  //Forces flagged untradeable items to disappear when dropped.
    public static final boolean USE_BANISHABLE_TOWN_SCROLL = true;  //Enables town scrolls to act as if it's a "player banish", rendering the antibanish scroll effect available.
    public static final boolean USE_ENABLE_CHAT_LOG = false;        //Write in-game chat to log
    public static final boolean USE_MAP_OWNERSHIP_SYSTEM = false;    //Flag to enable/disable map ownership system
    public static final boolean USE_NPCS_SCRIPTABLE = true;         //Flag to enable/disable serverside predefined script NPCs.
    
    //Events/PQs Configuration
    public static final boolean USE_OLD_GMS_STYLED_PQ_NPCS = true;  //Enables PQ NPCs with similar behaviour to old GMS style, that skips info about the PQs and immediately tries to register the party in.
    public static final boolean USE_ENABLE_RECALL_EVENT = true;     //Enables a disconnected player to reaccess the last event instance they were in before logging out. Recall only works if the event isn't cleared or disposed yet. Suggestion thanks to Alisson (Goukken).
    
    //Cash Shop Configuration
    public static final boolean USE_JOINT_CASHSHOP_INVENTORY = true;//Enables usage of a same cash shop inventory for explorers, cygnus and legends. Items from exclusive cash shop inventories won't show up on the shared inventory, though.
    public static final boolean USE_CLEAR_OUTDATED_COUPONS = true;  //Enables deletion of older code coupon registry from the DB, freeing so-long irrelevant data.
    
    //Maker Configuration
    public static final boolean USE_MAKER_FEE_HEURISTICS = true;    //Apply compiled values for stimulants and reagents into the Maker fee calculations (max error revolves around 50k mesos). Set false to use basic constant values instead (results are never higher than requested by the client-side).
    
    //Miscellaneous Configuration
    public static final int MAX_AP = 32767;                             //Max AP allotted on the auto-assigner.
    public static final int MAX_EVENT_LEVELS = 8;                       //Event has different levels of rewarding system.
    public static final long BLOCK_NPC_RACE_CONDT = (long)(0.5 * 1000); //Time the player client must wait before reopening a conversation with an NPC.
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000); //Time the pet must wait before trying to pick items up.
    public static final int MOB_REACTOR_REFRESH_TIME = 30 * 1000;       //Overwrites refresh time for those reactors oriented to inflict damage to bosses (Ice Queen, Riche), set 0 for default.
    public static final int PARTY_SEARCH_REENTRY_LIMIT = 10;            //Max amount of times a party leader is allowed to persist on the Party Search before entry expiration (thus needing to manually restart the Party Search to be able to search for members).
    
    //Dangling Items/Locks Configuration
    public static final int ITEM_EXPIRE_TIME  = 3 * 60 * 1000;  //Time before items start disappearing. Recommended to be set up to 3 minutes.
    public static final int KITE_EXPIRE_TIME  = 60 * 60 * 1000; //Time before kites (cash item) disappears.
    public static final int ITEM_MONITOR_TIME = 5 * 60 * 1000;  //Interval between item monitoring tasks on maps, which checks for dangling (null) item objects on the map item history.
    public static final int LOCK_MONITOR_TIME = 30 * 1000;      //Waiting time for a lock to be released. If it reaches timeout, a critical server deadlock has made present.
    
    //Map Monitor Configuration
    public static final int ITEM_EXPIRE_CHECK = 10 * 1000;      //Interval between item expiring tasks on maps, which checks and makes disappear expired items.
    public static final int ITEM_LIMIT_ON_MAP = 200;            //Max number of items allowed on a map.
    public static final int MAP_VISITED_SIZE = 5;               //Max length for last mapids visited by a player. This is used to recover and update drops on these maps accordingly with player actions.
    public static final int MAP_DAMAGE_OVERTIME_INTERVAL = 5000;//Interval in milliseconds between map environment damage (e.g. El Nath and Aqua Road surrondings).
    
    //Channel Mob Disease Monitor Configuration
    public static final int MOB_STATUS_MONITOR_PROC = 200;      //Frequency in milliseconds between each proc on the mob disease monitor schedule.
    public static final int MOB_STATUS_MONITOR_LIFE = 84;       //Idle proc count the mob disease monitor is allowed to be there before closing it due to inactivity.
    
    //Some Gameplay Enhancing Configurations
    //Map-Chair Configuration
    public static final byte CHAIR_EXTRA_HEAL_MULTIPLIER = 10;  //Due to only being able to be send up-to-255 heal values, values being actually updated is the one displayed times this.
    public static final int CHAIR_EXTRA_HEAL_MAX_DELAY = 21;    //Players are expected to recover fully after using this skill for N seconds.
    
    //Player NPC Configuration
    public static final int PLAYERNPC_INITIAL_X = 262;          //Map frame width for putting PlayerNPCs.
    public static final int PLAYERNPC_INITIAL_Y = 262;          //Map frame height for putting PlayerNPCs.
    public static final int PLAYERNPC_AREA_X = 320;             //Initial width gap between PlayerNPCs.
    public static final int PLAYERNPC_AREA_Y = 160;             //Initial height gap between PlayerNPCs.
    public static final int PLAYERNPC_AREA_STEPS = 4;           //Max number of times gap is shortened to comport PlayerNPCs.
    public static final boolean PLAYERNPC_ORGANIZE_AREA = true; //Automatically rearranges PlayerNPCs on the map if there is no space set the new NPC. Current distance gap between NPCs is decreased to solve this issue.
    public static final boolean PLAYERNPC_AUTODEPLOY = true;    //Makes PlayerNPC automatically deployed on the Hall of Fame at the instant one reaches max level. If false, eligible players must talk to 1st job instructor to deploy a NPC.

    //Pet & Mount Configuration
    public static final byte PET_EXHAUST_COUNT = 4;             //Number of proc counts (1 per minute) on the exhaust schedule for fullness.
    public static final byte MOUNT_EXHAUST_COUNT = 1;           //Number of proc counts (1 per minute) on the exhaust schedule for tiredness.
    
    //Event Configuration
    public static final int EVENT_MAX_GUILD_QUEUE = 10;         //Max number of guilds in queue for GPQ.
    public static final long EVENT_LOBBY_DELAY = 10;            //Cooldown duration in seconds before reopening an event lobby.
    
    //Dojo Configuration
    public static final boolean USE_DEADLY_DOJO = false;        //Should bosses really use 1HP,1MP attacks in dojo?
    public static final int DOJO_ENERGY_ATK = 100;              //Dojo energy gain when deal attack
    public static final int DOJO_ENERGY_DMG =  20;              //Dojo energy gain when recv attack
    
    //Wedding Configuration
    public static final int WEDDING_RESERVATION_DELAY = 3;      //Minimum idle slots before processing a wedding reservation.
    public static final int WEDDING_RESERVATION_TIMEOUT = 10;   //Limit time in minutes for the couple to show up before cancelling the wedding reservation.
    public static final int WEDDING_RESERVATION_INTERVAL = 60;  //Time between wedding starts in minutes.
    public static final int WEDDING_GIFT_LIMIT = 1;             //Max number of gifts per person to same wishlist on marriage instances.

    //Buyback Configuration
    public static final boolean USE_BUYBACK_WITH_MESOS = false;  //Enables usage of either mesos or NX for the buyback fee.
    public static final float BUYBACK_FEE = 77.70f;             //Sets the base amount needed to buyback (level 30 or under will use the base value).
    public static final float BUYBACK_LEVEL_STACK_FEE = 85.47f; //Sets the level-stacking portion of the amount needed to buyback (fee will sum up linearly until level 120, when it reaches the peak).
    public static final int BUYBACK_MESO_MULTIPLIER = 1000;     //Sets a multiplier for the fee when using meso as the charge unit.
    public static final int BUYBACK_RETURN_MINUTES = 1;         //Sets the maximum amount of time the player can wait before decide to buyback.
    public static final int BUYBACK_COOLDOWN_MINUTES = 7;       //Sets the time the player must wait before using buyback again.
    
    //Event End Timestamp
    public static final long EVENT_END_TIMESTAMP = 1428897600000L;

    //Debug Variables
    public static int DEBUG_VALUES[] = new int[10];             // Field designed for packet testing purposes
    
    //Properties
    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("./configuration.ini"));

            //Server Host
            ServerConstants.HOST = p.getProperty("HOST");
            ServerConstants.LOCALSERVER = ServerConstants.HOST.startsWith("127.") || ServerConstants.HOST.startsWith("localhost");

            //Sql Database
            ServerConstants.DB_URL = p.getProperty("URL");
            ServerConstants.DB_USER = p.getProperty("DB_USER");
            ServerConstants.DB_PASS = p.getProperty("DB_PASS");

            //Shutdownhook
            ServerConstants.SHUTDOWNHOOK = p.getProperty("SHUTDOWNHOOK").equalsIgnoreCase("true");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load configuration.ini.");
            System.exit(0);
        }
    }
}
