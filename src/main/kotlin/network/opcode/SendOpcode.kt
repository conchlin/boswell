package network.opcode

enum class SendOpcode(val value: Int) {
    //CLogin::OnPacket
    CheckPasswordResult(0),
    GuestIDLoginResult(1),
    AccountInfoResult(2),
    CheckUserLimitResult(3),
    SetAccountResult(4),
    ConfirmEULAResult(5),
    CheckPinCodeResult(6),
    UpdatePinCodeResult(7),
    ViewAllCharResult(8),
    SelectCharacterByVACResult(9),
    WorldInformation(10),
    SelectWorldResult(11),
    SelectCharacterResult(12),
    LoginCheckDuplicatedIDResult(13),
    CreateNewCharacterResult(14),
    DeleteCharacterResult(15),
    ChangeChannel(16),
    Ping(17),
    RelogResponse(22),
    LatestConnectedWorld(26),
    RecommendedWorldMessage(27),
    CheckSPWResult(28),
    /*CWvsContext::OnPacket*/
    InventoryOperation(29),
    InventoryGrow(30),
    STAT_CHANGED(31),
    GIVE_BUFF(32),  //OnTemporaryStatSet
    CANCEL_BUFF(33),  //OnTemporaryStatReset
    FORCED_STAT_SET(34),
    FORCED_STAT_RESET(35),
    UPDATE_SKILLS(36),  //OnChangeSkillRecordResult
    SKILL_USE_RESULT(37),
    FAME_RESPONSE(38),  //OnGivePopularityResult
    SHOW_STATUS_INFO(39),  //OnMessage
    OPEN_FULL_CLIENT_DOWNLOAD_LINK(40),
    MEMO_RESULT(41),
    MAP_TRANSFER_RESULT(42),
    WEDDING_PHOTO(43),  //ANTI_MACRO_RESULT(0x2B),
    CLAIM_RESULT(45),  // unnamed in idb
    CLAIM_AVAILABLE_TIME(46),  // unnamed in idb
    CLAIM_STATUS_CHANGED(47),  // unnamed in idb
    SET_TAMING_MOB_INFO(48),  // unnamed in idb
    QUEST_CLEAR(49),  // unnamed in idb
    ENTRUSTED_SHOP_CHECK_RESULT(50),  //OnEntrustedShopCheckResult
    SKILL_LEARN_ITEM_RESULT(51),  // unnamed in idb
    GATHER_ITEM_RESULT(52),
    SORT_ITEM_RESULT(53),
    SUE_CHARACTER_RESULT(55),
    TRADE_MONEY_LIMIT(57),
    SET_GENDER(58),
    GUILD_BBS_PACKET(59),
    CHAR_INFO(61),
    PartyResult(62),
    BUDDYLIST(63),//OnFriendResult
    GuildResult(65),
    AllianceResult(66),
    SPAWN_PORTAL(67),  //OnTownPortal
    SERVERMESSAGE(68),  //OnBroadcastMsg
    INCUBATOR_RESULT(69),
    SHOP_SCANNER_RESULT(70),
    SHOP_LINK_RESULT(71),

    MARRIAGE_REQUEST(72),
    MARRIAGE_RESULT(73),
    WEDDING_GIFT_RESULT(74),
    NOTIFY_MARRIED_PARTNER_MAP_TRANSFER(75),

    CASH_PET_FOOD_RESULT(76),
    SET_WEEK_EVENT_MESSAGE(77),
    SET_POTION_DISCOUNT_RATE(78),

    BRIDLE_MOB_CATCH_FAIL(79),
    IMITATED_NPC_RESULT(80),
    IMITATED_NPC_DATA(81),  //CNpcPool::OnNpcImitateData
    LIMITED_NPC_DISABLE_INFO(82),  //CNpcPool::OnUpdateLimitedDisableInfo
    MONSTER_BOOK_SET_CARD(83),
    MONSTER_BOOK_SET_COVER(84),
    HOUR_CHANGED(85),
    MINIMAP_ON_OFF(86),
    CONSULT_AUTHKEY_UPDATE(87),
    CLASS_COMPETITION_AUTHKEY_UPDATE(88),
    WEB_BOARD_AUTHKEY_UPDATE(89),
    SESSION_VALUE(90),
    PARTY_VALUE(91),
    FIELD_SET_VARIABLE(92),
    BONUS_EXP_CHANGED(93),  //pendant of spirit etc (guess, not sure about the opcode in v83)

    //Family
    FAMILY_CHART_RESULT(94),
    FAMILY_INFO_RESULT(95),
    FAMILY_RESULT(96),
    FAMILY_JOIN_REQUEST(97),
    FAMILY_JOIN_REQUEST_RESULT(98),
    FAMILY_JOIN_ACCEPTED(99),
    FAMILY_PRIVILEGE_LIST(100),
    FAMILY_FAMOUS_POINT_INC_RESULT(101),
    FAMILY_NOTIFY_LOGIN_OR_LOGOUT(102),  //? is logged in. LOLWUT
    FAMILY_SET_PRIVILEGE(103),
    FAMILY_SUMMON_REQUEST(104),


    NOTIFY_LEVELUP(105),
    NOTIFY_MARRIAGE(106),
    NOTIFY_JOB_CHANGE(107),
    //SET_BUY_EQUIP_EXT(0x6C),  //probably extra pendant slot for other versions?
    MAPLE_TV_USE_RES(109),  //It's not blank, It's a popup nibs
    AVATAR_MEGAPHONE_RESULT(110),  //bot useless..
    SET_AVATAR_MEGAPHONE(111),
    CLEAR_AVATAR_MEGAPHONE(112),
    CANCEL_NAME_CHANGE_RESULT(113),
    CANCEL_TRANSFER_WORLD_RESULT(115),
    DESTROY_SHOP_RESULT(115), FAKE_GM_NOTICE(116),  //bad asses
    SUCCESS_IN_USE_GACHAPON_BOX(117),
    NEW_YEAR_CARD_RES(118),
    RANDOM_MORPH_RES(119),
    CANCEL_NAME_CHANGE_BY_OTHER(120),
    SET_EXTRA_PENDANT_SLOT(121),
    SCRIPT_PROGRESS_MESSAGE(122),
    DATA_CRC_CHECK_FAILED(123),
    MACRO_SYS_DATA_INIT(124),

    /*CStage::OnPacket*/
    SET_FIELD(125),
    SET_ITC(126),
    SET_CASH_SHOP(127),  //CMapLoadable::OnPacket
    /*CMapLoadable::OnPacket*/
    SET_BACK_EFFECT(128),  //OnSetBackEffect
    SET_MAP_OBJECT_VISIBLE(129),  //CMapLoadable::OnSetMapObjectVisible O_O
    CLEAR_BACK_EFFECT(130),  //OnClearBackEffect
    /*CField::OnPacket*/
    TransferFieldRequestIgnored(131),
    TransferChannelReqIgnored(132),
    FieldSpecificData(133),
    GroupMessage(134),
    WHISPER(135),
    CoupleMessage(136),
    SummonItemInavailable(137),
    FieldEffect(138),
    FieldObstacleOnOff(139),
    FieldObstacleOnOffStatus(140),
    FieldObstacleOnOffReset(141),
    BlowWeather(142),
    PlayJukebox(143),
    AdminResult(144),
    Quiz(145),
    Desc(146),
    Clock(147),
    //Field_ContiMove::OnPacket
    CONTI_MOVE(148),
    CONTI_STATE(149),
    /*CField::OnPacket*/
    SetQuestClear(150),
    SetQuestTime(151),
    WarnMessage(152),
    SetObjectState(153),
    DestroyClock(154),
    ShowArenaResult(155),
    StalkResult(156),
    //CField_MassacreResult::OnPacket
    MassacreIncGauge(157),
    MassacreResult(158),
    // CUserPool::OnPacket
    UserEnterField(160),
    UserLeaveField(161),
    UserChat(162),
    UserChat2(163),
    ADBoard(164),
    MiniRoomBalloon(165),
    SetConsumeItemEffect(166),
    ShowItemUpgradeEffect(167),
    //CUser::OnPetPacket
    PetActivated(168),
    PetMove(170),
    PetAction(171),
    PetNameChange(172),
    PetLoadExceptionList(173),
    PetActionCommand(174),
    //SummonedPool
    SummonCreated(175),
    SummonRemoved(176),
    SummonMove(177),
    SummonAttack(177),
    SummonDamage(179),
    SummonSkill(180),
    //DragonPacket
    DragonEnterField(181),
    DragonMove(182),
    DragonLeaveField(183),
    //CUserPool::OnUserRemotePacket
    UserMove(185),
    UserMeleeAttack(186),
    UserShootAttack(187),
    UserMagicAttack(188),
    UserBodyAttack(189),
    SkillPrepare(190),
    SkillCancel(191),
    UserHit(192),
    SetEmotion(193),
    SetActiveEffectItem(194),
    SHOW_CHAIR(195),
    AvatarModified(197),
    RemoteUserEffect(198),
    SetTemporaryStat(199),
    ResetTemporaryStat(200),
    ReceiveHp(201),
    GuildNameChanged(202),
    GuildMarkChanged(203),
    ThrowGrenade(204),
    //CUserLocal::OnPacket
    SitResult(205),
    LocalEffect(206),
    Teleport(207),
    MesoGiveSucceeded(209),
    MesoGiveFailed(210),
    QuestResult(211),
    NotifyHpDecByField(212),
    BalloonMsg(214),
    PlayEventSound(215),
    PlayMiniGameSound(216),
    MakerResult(217),
    OpenClassCompetitionPage(219),
    OpenUI(220),
    SetDirectionMode(221),
    DisableUI(222),
    HireTutor(223),
    TutorMsg(224),
    IncComboResponse(225),
    RandomEmotion(226),
    ResignQuestReturn(227),
    PassMateName(228),
    RadioSchedule(229),
    OpenSkillGuide(230),
    NoticeMsg(231),
    ChatMsg(232),
    SayImage(233),
    SkillCooltimeSet(234),
    //CMobPool::OnPacket
    SPAWN_MONSTER(236),  //OnMobEnterField
    KILL_MONSTER(237),  //OnMobLeaveField
    SPAWN_MONSTER_CONTROL(238),  //OnMobChangeController
    //cMobPool::onMobPacket
    MobMove(239),
    CtrlAck(240),
    MobStatSet(242),
    MobStatReset(243),
    MobSuspendReset(244),
    MobAffected(245),
    MobDamaged(246),
    SpecialEffectBySkill(247),
    HPIndicator(250),
    CatchEffect(251),
    EffectByItem(252),
    IncMobChargeCount(254),
    MobAttackedByMob(255),
    //cNpcPool::onPacket
    NpcEnterField(257),
    NpcLeaveField(258),
    NpcChangeController(259),
    NpcAction(260),
    UpdateLimitedInfo(261),
    SetSpecialAction(262),
    SetNpcScript(263),
    //CEmployeePool::OnPacket
    EmployeeEnterField(265),
    EmployeeLeaveField(266),
    EmployeeMiniRoomBalloon(267),
    //CDropPool::OnPacket
    DropEnterField(268),
    DropLeaveField(269),
    //CMessageBoxPool::onPacket
    CreateFailed(270),
    MessageBoxEnterField(271),
    MessageBoxLeaveField(272),
    //CAffectedAreaPool::OnPacket
    AffectedAreaCreated(273),
    AffectedAreaRemoved(274),
    //CTownPortalPool::OnPacket
    TownPortalCreated(275),
    TownPortalRemoved(276),
    //CReactorPool::OnPacket
    ReactorChangeState(277),
    ReactorEnterField(279),
    ReactorLeaveField(280),
    //CField_Snowball
    SnowBallState(281),
    SnowBallHit(282),
    SnowBallMsg(283),
    SnowBallTouch(284),
    //CField_Coconut
    CoconutHit(285),
    CoconutScore(286),
    //CField_GuildBoss
    HealerMove(287),
    PulleyStateChange(288),
    //CField_MonsterCarnival::OnPacket
    CarnivalEnter(289),
    CarnivalPersonalCP(290),
    CarnivalTeamCP(291),
    CarnivalSummon(292),
    CarnivalRequestResult(293),
    CarnivalProcessForDeath(294),
    ShowMemberOutMsg(295),
    CarnivalShowGameResult(296),
    //CField_AriantArena::OnPacket
    ArenaUserScore(297),
    //CField_BattleField::OnPacket
    BattleScoreUpdate(299),
    TeamChanged(300),
    //CField_WitchTower::OnPacket
    TowerScoreUpdate(301),
    HORNTAIL_CAVE(302),  //CField::OnHontailTimer
    ZAKUM_SHRINE(303),  //CField::OnZakumTimer
    ScriptMessage(304),
    OPEN_NPC_SHOP(305),
    CONFIRM_SHOP_TRANSACTION(306),
    ADMIN_SHOP_MESSAGE(307),  //lame :P
    ADMIN_SHOP(308),
    //CTrunk::OnPacket
    Trunk(309),
    FREDRICK_MESSAGE(310),
    FREDRICK(311),
    RPS_GAME(312),
    MESSENGER(313),
    PLAYER_INTERACTION(314),

    TOURNAMENT(315),
    TOURNAMENT_MATCH_TABLE(316),
    TOURNAMENT_SET_PRIZE(317),
    TOURNAMENT_UEW(318),
    TOURNAMENT_CHARACTERS(319),  //they never coded this :|

    //CField_Wedding
    WEDDING_PROGRESS(320),  //byte step, int groomid, int brideid
    WEDDING_CEREMONY_END(321),

    PARCEL(322),
    //CCashShop:onPacket
    CashShopChargeParamResult(323),
    QueryCashResult(324),
    CashItemResult(325),
    PurchaseExpChange(326),
    GiftMateInfoResult(327),
    CashShopCheckDuplicatedIDResult(328),
    CheckNameChangePossibleResult(329),
    CheckTransferWorldPossibleResult(331),
    CashShopGachaponStampResult(332),
    CashItemGachaponResult(333),
    //OneADay(395), //v95
    //NoticeFreeCashItem(396), //v95
    //CFuncKeyMappedMan:OnPacket
    FuncKeyMappedItemInit(335),
    PetConsumeItemInit(336),
    PetConsumeMPItemInit(337),
    //CMapleTVMan:OnPacket
    SetMessage(341),
    ClearMessage(342),
    SendMessageResult(343),

    MTS_OPERATION2(347),
    MTS_OPERATION(348),
    MAPLELIFE_RESULT(349),
    MAPLELIFE_ERROR(350),
    VICIOUS_HAMMER(354),
    VEGA_SCROLL(358);
}