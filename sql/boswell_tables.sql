-- make sure to run this file before boswell_data.sql 
-- create the table beforehand
-- CREATE TABLE boswell;

CREATE TYPE boss_type AS ENUM (
    'ZAKUM',
    'HORNTAIL',
    'PINKBEAN',
    'SCARGA',
    'PAPULATUS'
);


CREATE TYPE location_type AS ENUM (
    'FREE_MARKET',
    'WORLDTOUR',
    'FLORINA',
    'INTRO',
    'SUNDAY_MARKET',
    'MIRROR',
    'EVENT',
    'BOSSPQ',
    'HAPPYVILLE',
    'DEVELOPER',
    'MONSTER_CARNIVAL'
);

CREATE TABLE account_emails (
    id bigserial primary key NOT NULL,
    userid bigint,
    email character varying(255),
    token character varying(255),
    type character varying(255),
    inserted_at timestamp(0) without time zone NOT NULL,
    updated_at timestamp(0) without time zone NOT NULL
);

CREATE TABLE accounts (
    id bigserial primary key NOT NULL,
    name character varying(13),
    password character varying(255),
    pin character varying(255),
    pic character varying(255),
    loggedin integer DEFAULT 0,
    lastlogin timestamp(0) without time zone DEFAULT now(),
    birthday date DEFAULT now(),
    banned boolean DEFAULT false,
    banreason character varying(255),
    macs character varying(255),
    nxcredit integer DEFAULT 0,
    maplepoint integer DEFAULT 0,
    nxprepaid integer DEFAULT 0,
    characterslots integer DEFAULT 3,
    gender integer DEFAULT 0,
    tempban timestamp(0) without time zone DEFAULT '1970-01-01 00:00:00'::timestamp without time zone,
    greason integer DEFAULT 0,
    tos boolean DEFAULT false,
    mute boolean DEFAULT false,
    ip character varying(255),
    lastknownip character varying(255),
    hwid character varying(255) DEFAULT ''::character varying,
    gm integer DEFAULT 0,
    last_vote timestamp(0) without time zone DEFAULT '1920-01-01 00:00:00'::timestamp without time zone,
    cheater boolean DEFAULT false,
    inserted_at timestamp(0) without time zone NOT NULL,
    updated_at timestamp(0) without time zone NOT NULL
);

CREATE UNIQUE INDEX accounts_email_index ON accounts USING btree (email);
CREATE UNIQUE INDEX accounts_name_index ON accounts USING btree (name);
CREATE INDEX accounts_ranking1 ON accounts USING btree (id, banned);

CREATE TABLE alliance (
    id bigserial primary key NOT NULL,
    name character varying(255),
    capacity integer DEFAULT 2,
    notice character varying(255) DEFAULT ''::character varying,
    rank1 character varying(255) DEFAULT 'Master'::character varying,
    rank2 character varying(255) DEFAULT 'Jr. Master'::character varying,
    rank3 character varying(255) DEFAULT 'Member'::character varying,
    rank4 character varying(255) DEFAULT 'Member'::character varying,
    rank5 character varying(255) DEFAULT 'Member'::character varying,
    rank6 character varying(255) DEFAULT 'Member'::character varying
);

CREATE INDEX alliance_name_index ON alliance USING btree (name);

CREATE TABLE alliance_guilds (
    id bigserial primary key NOT NULL,
    allianceid integer DEFAULT '-1'::integer,
    guildid integer DEFAULT '-1'::integer
);

CREATE TABLE area_info (
    id bigserial primary key NOT NULL,
    charid integer,
    area integer,
    info character varying(255)
);

CREATE TABLE bbs_replies (
    replyid serial primary key NOT NULL, -- integer
    threadid integer,
    postercid integer,
    "timestamp" bigint,
    content text
);

CREATE TABLE bbs_threads (
    threadid serial primary key NOT NULL, -- integer
    postercid integer,
    name character varying(255) DEFAULT ''::character varying,
    "timestamp" bigint,
    icon integer,
    replycount integer DEFAULT 0,
    startpost text,
    guildid integer,
    localthreadid integer
);

CREATE TABLE boss_logs (
    id bigserial primary key NOT NULL,
    characterid integer,
    bosstype boss_type,
    attempttime timestamp(0) without time zone DEFAULT now()
);

CREATE TABLE boss_quest (
    id integer,
    points integer,
    attemtps integer
);

CREATE TABLE buddies (
    id bigserial primary key NOT NULL,
    characterid integer,
    buddyid integer,
    pending integer DEFAULT 0,
    "group" character varying(255) DEFAULT '0'::character varying
);


CREATE TABLE characters (
    id bigserial primary key NOT NULL,
    accountid integer DEFAULT 0,
    world integer DEFAULT 0,
    name character varying(255) DEFAULT ''::character varying,
    level integer DEFAULT 1,
    exp integer DEFAULT 0,
    gachaexp integer DEFAULT 0,
    str integer DEFAULT 12,
    dex integer DEFAULT 5,
    luk integer DEFAULT 4,
    "int" integer DEFAULT 4,
    hp integer DEFAULT 50,
    mp integer DEFAULT 5,
    maxhp integer DEFAULT 50,
    maxmp integer DEFAULT 5,
    meso integer DEFAULT 0,
    hpmpused integer DEFAULT 0,
    job integer DEFAULT 0,
    skincolor integer DEFAULT 0,
    gender integer DEFAULT 0,
    fame integer DEFAULT 0,
    fquest integer DEFAULT 0,
    hair integer DEFAULT 0,
    face integer DEFAULT 0,
    ap integer DEFAULT 0,
    sp character varying(255) DEFAULT '0,0,0,0,0,0,0,0,0,0'::character varying,
    map integer DEFAULT 0,
    spawnpoint integer DEFAULT 0,
    gm integer DEFAULT 0,
    party integer DEFAULT 0,
    buddycapacity integer DEFAULT 25,
    createdate timestamp(0) without time zone DEFAULT now(),
    rank integer DEFAULT 1,
    rankmove integer DEFAULT 0,
    jobrank integer DEFAULT 1,
    jobrankmove integer DEFAULT 0,
    playerrank integer DEFAULT 0,
    guildid integer DEFAULT 0,
    guildrank integer DEFAULT 5,
    messengerid integer DEFAULT 0,
    messengerposition integer DEFAULT 4,
    mountlevel integer DEFAULT 1,
    mountexp integer DEFAULT 0,
    mounttiredness integer DEFAULT 0,
    omokwins integer DEFAULT 0,
    omoklosses integer DEFAULT 0,
    omokties integer DEFAULT 0,
    matchcardwins integer DEFAULT 0,
    matchcardlosses integer DEFAULT 0,
    matchcardties integer DEFAULT 0,
    merchantmesos integer DEFAULT 0,
    hasmerchant boolean DEFAULT false,
    equipslots integer DEFAULT 24,
    useslots integer DEFAULT 24,
    setupslots integer DEFAULT 24,
    etcslots integer DEFAULT 24,
    familyid integer DEFAULT '-1'::integer,
    monsterbookcover integer DEFAULT 0,
    alliancerank integer DEFAULT 5,
    vanquisherstage integer DEFAULT 0,
    dojopoints integer DEFAULT 0,
    lastdojostage integer DEFAULT 0,
    finisheddojotutorial boolean DEFAULT false,
    vanquisherkills integer DEFAULT 0,
    summonvalue integer DEFAULT 0,
    partnerid integer DEFAULT 0,
    marriageitemid integer DEFAULT 0,
    reborns integer DEFAULT 0,
    pqpoints integer DEFAULT 0,
    datastring character varying(255) DEFAULT ''::character varying,
    lastlogouttime timestamp(0) without time zone DEFAULT now(),
    pendantexp integer DEFAULT 0,
    jailexpire bigint DEFAULT 0,
    lastexpgaintime timestamp(0) without time zone DEFAULT now(),
    ariantpoints integer DEFAULT 0,
    partysearch boolean DEFAULT false,
    inserted_at timestamp(0) without time zone DEFAULT now(),
    updated_at timestamp(0) without time zone DEFAULT now(),
    clearance integer DEFAULT 0
);

CREATE INDEX characters_ranking1 ON characters USING btree (level, exp);
CREATE INDEX characters_ranking2 ON characters USING btree (gm, job);
CREATE INDEX party ON characters USING btree (party);

CREATE TABLE cooldowns (
    id bigserial primary key NOT NULL,
    charid integer,
    skillid integer,
    length bigint,
    starttime bigint
);

CREATE TABLE cs_blocked_items (
    id serial NOT NULL,
    sn integer NOT NULL
);

CREATE TABLE cs_discounted_categories (
    category integer,
    subcategory integer,
    discount_rate integer
);

CREATE TABLE cs_limited_goods (
    start_sn integer,
    end_sn integer,
    count integer,
    event_sn integer,
    expire integer,
    flag integer,
    start_date integer,
    end_date integer,
    start_hour integer,
    end_hour integer,
    days text
);

CREATE TABLE cs_modded_commodity (
    sn integer,
    item_id integer,
    count integer,
    priority integer,
    maple_point integer,
    mesos integer,
    premium_user boolean DEFAULT false NOT NULL,
    required_level integer,
    gender integer,
    sale boolean DEFAULT false NOT NULL,
    class integer,
    _limit integer,
    pb_cash integer,
    package_contents text,
    pb_point integer,
    pb_gift integer
);

CREATE TABLE cs_stock (
    sn integer,
    state integer
);

CREATE TABLE cs_zero_goods (
    start_sn integer,
    end_sn integer,
    count integer,
    event_sn integer,
    expire integer,
    flag integer,
    start_date integer,
    end_date integer,
    start_hour integer,
    end_hour integer,
    days text
);

CREATE TABLE daily (
    charid integer,
    challenge character varying(12)
);

CREATE TABLE daily_progress (
    charid integer,
    challenge character varying(12),
    num integer
);

CREATE TABLE drop_data (
    id bigserial primary key NOT NULL,
    dropperid integer DEFAULT 0,
    itemid integer DEFAULT 0,
    minimum_quantity integer DEFAULT 1,
    maximum_quantity integer DEFAULT 1,
    questid integer DEFAULT 0,
    chance integer DEFAULT 0,
    monster_level integer,
    boss_tier integer DEFAULT 0,
    item_category integer
);

CREATE INDEX drop_data_dropperid_index ON drop_data USING btree (dropperid);
CREATE UNIQUE INDEX drop_data_dropperid_itemid_index ON drop_data USING btree (dropperid, itemid);
CREATE INDEX drop_data_itemid_index ON drop_data USING btree (itemid);

CREATE TABLE drop_data_global (
    id bigserial primary key NOT NULL,
    continent integer,
    droptype integer DEFAULT 0,
    itemid integer DEFAULT 0,
    minimum_quantity integer DEFAULT 1,
    maximum_quantity integer DEFAULT 1,
    questid integer DEFAULT 0,
    chance integer DEFAULT 0,
    comments character varying(255) DEFAULT ''::character varying
);

CREATE INDEX drop_data_global_continent_index ON drop_data_global USING btree (continent);

CREATE TABLE duey_items (
    id bigserial primary key NOT NULL,
    packageid integer DEFAULT 0,
    itemid integer DEFAULT 0,
    quantity integer DEFAULT 0,
    upgradeslots integer DEFAULT 0,
    level integer DEFAULT 0,
    itemlevel integer DEFAULT 0,
    itemexp integer DEFAULT 0,
    str integer DEFAULT 0,
    dex integer DEFAULT 0,
    "int" integer DEFAULT 0,
    luk integer DEFAULT 0,
    hp integer DEFAULT 0,
    mp integer DEFAULT 0,
    watk integer DEFAULT 0,
    matk integer DEFAULT 0,
    wdef integer DEFAULT 0,
    mdef integer DEFAULT 0,
    acc integer DEFAULT 0,
    avoid integer DEFAULT 0,
    hands integer DEFAULT 0,
    speed integer DEFAULT 0,
    jump integer DEFAULT 0,
    flag integer DEFAULT 0,
    dueyitems_ibfk_1 integer,
    owner character varying(255)
);

CREATE INDEX dueyitems_packageid_index ON duey_items USING btree (packageid);

CREATE TABLE duey_packages (
    packageid serial primary key NOT NULL,
    receiverid integer,
    sendername character varying(255),
    message character varying(255),
    mesos integer DEFAULT 0,
    "timestamp" character varying(255),
    checked boolean DEFAULT true,
    type integer
);

ALTER TABLE ONLY duey_items
    ADD CONSTRAINT dueyitems_dueyitems_ibfk_1_fkey FOREIGN KEY (dueyitems_ibfk_1) REFERENCES duey_packages(packageid) ON DELETE CASCADE;

CREATE TABLE event_stats (
    characterid integer NOT NULL,
    name character varying(255) DEFAULT '0'::character varying,
    info integer
);

CREATE TABLE fame_log (
    famelogid bigserial primary key NOT NULL,
    characterid integer DEFAULT 0 NOT NULL,
    characterid_to integer DEFAULT 0,
    "when" timestamp(0) without time zone DEFAULT now(),
    famelog_ibfk_1 bigint
);

ALTER TABLE ONLY fame_log
    ADD CONSTRAINT famelog_famelog_ibfk_1_fkey FOREIGN KEY (famelog_ibfk_1) REFERENCES characters(id);

CREATE TABLE family_character (
    cid integer NOT NULL,
    familyid integer,
    rank integer,
    reputation integer,
    todaysrep integer,
    totaljuniors integer,
    name character varying(255),
    juniorsadded integer,
    totalreputation integer
);

CREATE INDEX family_character_cid_familyid_index ON family_character USING btree (cid, familyid);

CREATE TABLE fred_storage (
    id bigserial primary key NOT NULL,
    cid integer,
    daynotes integer,
    "timestamp" timestamp(0) without time zone DEFAULT now()
);

CREATE UNIQUE INDEX fredstorage_cid_index ON fred_storage USING btree (cid);

CREATE TABLE gifts (
    id bigserial primary key NOT NULL,
    "to" integer,
    "from" character varying(255),
    message character varying(255),
    sn integer,
    ringid integer
);

CREATE TABLE guilds (
    guildid serial primary key NOT NULL,
    leader integer DEFAULT 0,
    gp integer DEFAULT 0,
    logo integer,
    logocolor integer DEFAULT 0,
    name character varying(255),
    rank1title character varying(255) DEFAULT 'Master'::character varying,
    rank2title character varying(255) DEFAULT 'Jr. Master'::character varying,
    rank3title character varying(255) DEFAULT 'Member'::character varying,
    rank4title character varying(255) DEFAULT 'Member'::character varying,
    rank5title character varying(255) DEFAULT 'Member'::character varying,
    capacity integer DEFAULT 10,
    logobg integer,
    logobgcolor integer DEFAULT 0,
    notice character varying(255),
    signature integer DEFAULT 0,
    allianceid integer DEFAULT 0
);

CREATE INDEX guilds_guildid_name_index ON guilds USING btree (guildid, name);

CREATE TABLE hired_merchant (
    id bigserial primary key NOT NULL,
    ownerid integer DEFAULT 0,
    itemid integer DEFAULT 0,
    quantity integer DEFAULT 0,
    upgradeslots integer DEFAULT 0,
    level integer DEFAULT 0,
    str integer DEFAULT 0,
    dex integer DEFAULT 0,
    "int" integer DEFAULT 0,
    luk integer DEFAULT 0,
    hp integer DEFAULT 0,
    mp integer DEFAULT 0,
    watk integer DEFAULT 0,
    matk integer DEFAULT 0,
    wdef integer DEFAULT 0,
    mdef integer DEFAULT 0,
    acc integer DEFAULT 0,
    avoid integer DEFAULT 0,
    hands integer DEFAULT 0,
    speed integer DEFAULT 0,
    jump integer DEFAULT 0,
    type integer,
    owner character varying(255) DEFAULT ''::character varying
);

CREATE TABLE hwid_accounts (
    accountid integer DEFAULT 0 NOT NULL,
    hwid character varying(255) DEFAULT ''::character varying NOT NULL,
    relevance integer DEFAULT 0,
    expiresat timestamp(0) without time zone DEFAULT now()
);

CREATE TABLE hwid_bans (
    hwidbanid serial primary key NOT NULL,
    hwid character varying(255)
);

CREATE UNIQUE INDEX hwidbans_hwid_index ON hwid_bans USING btree (hwid);

CREATE TABLE inventory_equipment (
    inventoryequipmentid bigserial primary key NOT NULL,
    inventoryitemid bigint DEFAULT 0,
    upgradeslots integer DEFAULT 0,
    level integer DEFAULT 0,
    str integer DEFAULT 0,
    dex integer DEFAULT 0,
    "int" integer DEFAULT 0,
    luk integer DEFAULT 0,
    hp integer DEFAULT 0,
    mp integer DEFAULT 0,
    watk integer DEFAULT 0,
    matk integer DEFAULT 0,
    wdef integer DEFAULT 0,
    mdef integer DEFAULT 0,
    acc integer DEFAULT 0,
    avoid integer DEFAULT 0,
    hands integer DEFAULT 0,
    speed integer DEFAULT 0,
    jump integer DEFAULT 0,
    locked integer DEFAULT 0,
    vicious integer DEFAULT 0,
    itemlevel integer DEFAULT 1,
    itemexp integer DEFAULT 0,
    ringid integer DEFAULT '-1'::integer
);

CREATE INDEX inventoryequipment_inventoryitemid_index ON inventory_equipment USING btree (inventoryitemid);

CREATE TABLE inventory_items (
    inventoryitemid bigserial primary key NOT NULL,
    type integer,
    characterid integer,
    accountid integer,
    itemid integer DEFAULT 0,
    inventorytype integer DEFAULT 0,
    "position" integer DEFAULT 0,
    quantity integer DEFAULT 0,
    owner character varying(255),
    petid integer DEFAULT '-1'::integer,
    flag integer,
    expiration bigint DEFAULT '-1'::integer,
    "giftFrom" character varying(255)
);

CREATE INDEX inventoryitems_characterid_index ON inventory_items USING btree (characterid);

CREATE TABLE inventory_merchant (
    inventorymerchantid serial primary key NOT NULL,
    inventoryitemid bigint DEFAULT 0,
    characterid integer,
    bundles integer DEFAULT 0
);

CREATE INDEX inventorymerchant_inventoryitemid_index ON inventory_merchant USING btree (inventoryitemid);

CREATE TABLE ip_bans (
    ipbanid serial primary key NOT NULL,
    ip character varying(255) DEFAULT ''::character varying,
    aid character varying(255)
);

CREATE TABLE keymap (
    id bigserial primary key NOT NULL,
    characterid integer DEFAULT 0,
    key integer DEFAULT 0,
    type integer DEFAULT 0,
    action integer DEFAULT 0
);

CREATE TABLE mac_bans (
    macbanid serial primary key NOT NULL,
    mac character varying(255),
    aid character varying(255)
);

CREATE UNIQUE INDEX macbans_mac_index ON mac_bans USING btree (mac);

CREATE TABLE mac_filters (
    macfilterid serial primary key NOT NULL,
    filter character varying(255)
);

CREATE TABLE maker_create_data (
    id bigserial primary key NOT NULL,
    itemid integer NOT NULL,
    req_level integer,
    req_maker_level integer,
    req_meso integer,
    req_item integer,
    req_equip integer,
    catalyst integer,
    quantity integer,
    tuc integer
);

CREATE TABLE maker_reagent_data (
    itemid integer NOT NULL,
    stat character varying(255),
    value integer
);

CREATE TABLE maker_recipe_data (
    itemid integer NOT NULL,
    req_item integer NOT NULL,
    count integer
);

CREATE TABLE maker_reward_data (
    itemid integer NOT NULL,
    rewardid integer NOT NULL,
    quantity integer,
    prob integer DEFAULT 100
);

CREATE TABLE marriages (
    marriageid serial primary key NOT NULL,
    husbandid integer DEFAULT 0,
    wifeid integer DEFAULT 0
);

CREATE TABLE medal_maps (
    characterid integer,
    queststatusid integer,
    mapid integer
);

CREATE INDEX medalmaps_queststatusid_index ON medal_maps USING btree (queststatusid);

CREATE TABLE monster_book (
    charid integer,
    cardid integer,
    level integer DEFAULT 1
);

CREATE TABLE monster_card_data (
    id bigserial primary key NOT NULL,
    cardid integer DEFAULT 0,
    mobid integer DEFAULT 0
);

CREATE TABLE mts_cart (
    id bigserial primary key NOT NULL,
    cid integer,
    itemid integer
);

CREATE TABLE mts_items (
    id bigserial primary key NOT NULL,
    tab integer DEFAULT 0,
    type integer DEFAULT 0,
    itemid integer DEFAULT 0,
    quantity integer DEFAULT 1,
    seller integer DEFAULT 0,
    price integer DEFAULT 0,
    bid_incre integer DEFAULT 0,
    buy_now integer DEFAULT 0,
    "position" integer DEFAULT 0,
    upgradeslots integer DEFAULT 0,
    level integer DEFAULT 0,
    str integer DEFAULT 0,
    dex integer DEFAULT 0,
    "int" integer DEFAULT 0,
    luk integer DEFAULT 0,
    hp integer DEFAULT 0,
    mp integer DEFAULT 0,
    watk integer DEFAULT 0,
    matk integer DEFAULT 0,
    wdef integer DEFAULT 0,
    mdef integer DEFAULT 0,
    acc integer DEFAULT 0,
    avoid integer DEFAULT 0,
    hands integer DEFAULT 0,
    speed integer DEFAULT 0,
    jump integer DEFAULT 0,
    locked integer DEFAULT 0,
    isequip integer DEFAULT 0,
    owner integer DEFAULT 0,
    sellername character varying(255),
    sell_ends character varying(255),
    transfer integer DEFAULT 0,
    vicious integer DEFAULT 0,
    flag integer DEFAULT 0
);

CREATE TABLE new_year (
    id bigserial primary key NOT NULL,
    senderid integer DEFAULT '-1'::integer,
    receiverid integer DEFAULT '-1'::integer,
    senderdiscard integer DEFAULT 0,
    receiverdiscard integer DEFAULT 0,
    received integer DEFAULT 0,
    timesent bigint,
    timereceived bigint,
    sendername character varying(255) DEFAULT ''::character varying,
    receivername character varying(255) DEFAULT ''::character varying,
    message character varying(255) DEFAULT ''::character varying
);

CREATE TABLE notes (
    id bigserial primary key NOT NULL,
    "to" character varying(255) DEFAULT ''::character varying,
    "from" character varying(255) DEFAULT ''::character varying,
    message character varying(255),
    "timestamp" bigint,
    fame integer DEFAULT 0,
    deleted integer DEFAULT 0
);

CREATE TABLE nxcode (
    id bigserial primary key NOT NULL,
    code character varying(255),
    retriever character varying(255),
    expiration bigint DEFAULT 0
);

CREATE UNIQUE INDEX nxcode_code_index ON nxcode USING btree (code);

CREATE TABLE nxcode_items (
    id bigserial primary key NOT NULL,
    codeid integer,
    type integer DEFAULT 5,
    item integer DEFAULT 4000000,
    quantity integer DEFAULT 1
);

CREATE TABLE nxcoupons (
    id bigserial primary key NOT NULL,
    couponid integer DEFAULT 0,
    rate integer DEFAULT 0,
    activeday integer DEFAULT 0,
    starthour integer DEFAULT 0,
    endhour integer DEFAULT 0
);

CREATE TABLE pet_ignores (
    id bigserial primary key NOT NULL,
    petid integer,
    itemid integer
);

CREATE TABLE pets (
    petid serial primary key NOT NULL,
    name character varying(255),
    level integer,
    closeness integer,
    fullness integer,
    summoned boolean DEFAULT false,
    flag integer
);

CREATE TABLE player_diseases (
    id bigserial primary key NOT NULL,
    charid integer,
    disease integer,
    mobskillid integer,
    mobskilllv integer,
    length integer DEFAULT 1
);

CREATE TABLE playernpcs (
    id bigserial primary key NOT NULL,
    name character varying(255),
    hair integer DEFAULT 0,
    face integer DEFAULT 0,
    skin integer DEFAULT 0,
    gender integer DEFAULT 0,
    x integer DEFAULT 0,
    cy integer DEFAULT 0,
    world integer DEFAULT 0,
    map integer DEFAULT 0,
    dir integer DEFAULT 0,
    scriptid integer DEFAULT 0,
    fh integer DEFAULT 0,
    rx0 integer DEFAULT 0,
    rx1 integer DEFAULT 0,
    worldrank integer DEFAULT 0,
    overallrank integer DEFAULT 0,
    worldjobrank integer DEFAULT 0,
    job integer DEFAULT 0
);

CREATE TABLE playernpcs_equip (
    id bigserial primary key NOT NULL,
    npcid integer DEFAULT 0,
    equipid integer,
    type integer DEFAULT 0,
    equippos integer
);

CREATE TABLE playernpcs_field (
    id bigserial primary key NOT NULL,
    world integer,
    map integer,
    step integer DEFAULT 0,
    podium integer DEFAULT 0
);

CREATE TABLE plife (
    id bigserial primary key NOT NULL,
    world integer DEFAULT '-1'::integer,
    map integer DEFAULT 0,
    life integer DEFAULT 0,
    type character varying(255) DEFAULT 'n'::character varying,
    cy integer DEFAULT 0,
    f integer DEFAULT 0,
    fh integer DEFAULT 0,
    rx0 integer DEFAULT 0,
    rx1 integer DEFAULT 0,
    x integer DEFAULT 0,
    y integer DEFAULT 0,
    hide integer DEFAULT 0,
    mobtime integer DEFAULT 0,
    team integer DEFAULT 0
);

CREATE TABLE quest_actions (
    questactionid serial primary key NOT NULL,
    questid integer DEFAULT 0,
    status integer DEFAULT 0,
    data bytea
);

CREATE TABLE quest_progress (
    id bigserial primary key NOT NULL,
    characterid integer,
    queststatusid integer DEFAULT 0,
    progressid integer DEFAULT 0,
    progress character varying(255) DEFAULT ''::character varying
);

CREATE TABLE quest_requirements (
    questrequirementid serial primary key NOT NULL,
    questid integer DEFAULT 0,
    status integer DEFAULT 0,
    data bytea
);

CREATE TABLE quest_status (
    queststatusid serial primary key NOT NULL,
    characterid integer DEFAULT 0,
    quest integer DEFAULT 0,
    status integer DEFAULT 0,
    "time" integer DEFAULT 0,
    expires bigint DEFAULT 0,
    forfeited integer DEFAULT 0,
    completed integer DEFAULT 0,
    info integer DEFAULT 0
);

CREATE TABLE reactor_drops (
    reactordropid serial primary key NOT NULL,
    reactorid integer,
    itemid integer,
    chance integer,
    questid integer DEFAULT '-1'::integer
);

CREATE INDEX reactordrops_reactorid_index ON reactor_drops USING btree (reactorid);

CREATE TABLE reports (
    id bigserial primary key NOT NULL,
    reporttime timestamp(0) without time zone DEFAULT now(),
    reporterid integer,
    victimid integer,
    reason integer,
    chatlog character varying(255),
    status character varying(255)
);

CREATE TABLE responses (
    id bigserial primary key NOT NULL,
    chat character varying(255),
    response character varying(255)
);

CREATE TABLE rings (
    id bigserial primary key NOT NULL,
    partnerringid integer DEFAULT 0,
    partnerchrid integer DEFAULT 0,
    itemid integer DEFAULT 0,
    partnername character varying(255)
);

CREATE TABLE saved_locations (
    id bigserial primary key NOT NULL,
    characterid integer,
    locationtype location_type,
    map integer,
    portal integer
);

CREATE TABLE server_queue (
    id bigserial primary key NOT NULL,
    accountid integer DEFAULT 0,
    characterid integer DEFAULT 0,
    type integer DEFAULT 0,
    value integer DEFAULT 0,
    message character varying(255),
    createtime timestamp(0) without time zone DEFAULT now()
);

CREATE TABLE shop_items (
    shopitemid serial primary key NOT NULL,
    shopid integer,
    itemid integer,
    price integer,
    pitch integer DEFAULT 0,
    "position" integer
);

CREATE TABLE shops (
    shopid serial primary key NOT NULL,
    npcid integer DEFAULT 0
);

CREATE TABLE skill_macros (
    id bigserial primary key NOT NULL,
    characterid integer DEFAULT 0,
    "position" integer DEFAULT 0,
    skill1 integer DEFAULT 0,
    skill2 integer DEFAULT 0,
    skill3 integer DEFAULT 0,
    name character varying(255),
    shout integer DEFAULT 0
);

CREATE TABLE skills (
    id bigserial primary key NOT NULL,
    skillid integer DEFAULT 0,
    characterid integer DEFAULT 0,
    skilllevel integer DEFAULT 0,
    masterlevel integer DEFAULT 0,
    expiration bigint DEFAULT '-1'::integer
);

CREATE TABLE special_cash_items (
    id bigserial primary key NOT NULL,
    sn integer,
    modifier integer,
    info integer
);

CREATE TABLE storages (
    storageid serial primary key NOT NULL,
    accountid integer DEFAULT 0,
    world integer,
    slots integer DEFAULT 0,
    meso integer DEFAULT 0
);

CREATE TABLE trock_locations (
    trockid serial primary key NOT NULL,
    characterid integer,
    mapid integer,
    vip integer
);

CREATE TABLE wish_lists (
    id bigserial primary key NOT NULL,
    charid integer,
    sn integer
);

CREATE TABLE world_tour (
    worldtourid character varying(255),
    charid integer DEFAULT 0,
    accountid integer
);

CREATE UNIQUE INDEX worldtour_worldtourid_charid_index ON world_tour USING btree (worldtourid, charid);