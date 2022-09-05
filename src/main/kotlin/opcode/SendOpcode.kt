package opcode

enum class SendOpcode(val value: Int) {
    // the names taken from the client are commented if they are different from the enum name

    //CLogin::OnPacket
    LOGIN_STATUS(0),  //OnCheckPasswordResult
    GUEST_ID_LOGIN(1),  //OnGuestIDLoginResult
    ACCOUNT_INFO(2),  //OnAccountInfoResult
    SERVERSTATUS(3),  //OnCheckUserLimitResult
    GENDER_DONE(4),  //OnSetAccountResult
    CONFIRM_EULA_RESULT(5),  //OnConfirmEULAResult
    CHECK_PINCODE(6),  //OnCheckPinCodeResult
    UPDATE_PINCODE(7),  //OnUpdatePinCodeResult
    VIEW_ALL_CHAR(8),  //OnViewAllCharResult
    SELECT_CHARACTER_BY_VAC(9),  //OnSelectCharacterByVACResult
    SERVERLIST(10),  //OnWorldInformation
    CHARLIST(11),  //OnSelectWorldResult
    SERVER_IP(12),  //OnSelectCharacterResult
    CHAR_NAME_RESPONSE(13),  //OnCheckDuplicatedIDResult
    ADD_NEW_CHAR_ENTRY(14),  //OnCreateNewCharacterResult
    DELETE_CHAR_RESPONSE(15),  //OnDeleteCharacterResult
    CHANGE_CHANNEL(16),
    PING(17),
    CHANNEL_SELECTED(20),
    HACKSHIELD_REQUEST(21),  //maybe this is RELOG_RESPONSE, can't care less
    RELOG_RESPONSE(22),
    CHECK_CRC_RESULT(23),
    LAST_CONNECTED_WORLD(26),
    RECOMMENDED_WORLD_MESSAGE(27),
    CHECK_SPW_RESULT(28),

    /*CWvsContext::OnPacket*/
    INVENTORY_OPERATION(29),
    INVENTORY_GROW(30),
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
    PARTY_OPERATION(62),  //OnPartyResult
    BUDDYLIST(63),//OnFriendResult
    GUILD_OPERATION(65),  //OnGuildResult
    ALLIANCE_OPERATION(66),  //OnAllianceResult
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

    /*CField::OnPacket*/
    SET_BACK_EFFECT(128),  //OnSetBackEffect
    SET_MAP_OBJECT_VISIBLE(129),  //CMapLoadable::OnSetMapObjectVisible O_O
    CLEAR_BACK_EFFECT(130),  //OnClearBackEffect
    BLOCKED_MAP(131),  //TransferFieldRequestIgnored
    BLOCKED_SERVER(132),  //OnTransferChannelReqIgnored
    FORCED_MAP_EQUIP(133),  //OnFieldSpecificData
    MULTICHAT(134),  //OnGroupMessage
    WHISPER(135),
    SPOUSE_CHAT(136),
    SUMMON_ITEM_INAVAILABLE(137),  //OnSummonItemInavailable

    FIELD_EFFECT(138),
    FIELD_OBSTACLE_ONOFF(139),  //OnFieldObstacleOnOff
    FIELD_OBSTACLE_ONOFF_LIST(140),  //OnFieldObstacleOnOffStatus
    FIELD_OBSTACLE_ALL_RESET(141),  //OnFieldObstacleOnOffReset
    BLOW_WEATHER(142),
    PLAY_JUKEBOX(143),

    ADMIN_RESULT(144),
    OX_QUIZ(145),  //QUIZ
    GMEVENT_INSTRUCTIONS(146),  //onDesc
    CLOCK(147),

    //Field_ContiMove::OnPacket
    CONTI_MOVE(148),
    CONTI_STATE(149),


    SET_QUEST_CLEAR(150),
    SET_QUEST_TIME(151),
    WARN_MESSAGE(152),  // onWarnMessage
    SET_OBJECT_STATE(153),
    STOP_CLOCK(154),
    ARIANT_ARENA_SHOW_RESULT(155),
    STALK_RESULT(156),  //CField_Massacre

    //CField_MassacreResult::OnPacket
    PYRAMID_GAUGE(157),  //OnMassacreIncGauge
    PYRAMID_SCORE(158),  //OnMassacreResult


    // CUserPool::OnPacket
    SPAWN_PLAYER(160),  //OnUserEnterField
    REMOVE_PLAYER_FROM_MAP(161),  //OnUserLeaveField
    CHATTEXT(162),  //CUser::OnChat 0
    CHATTEXT1(163),  //CUser::OnChat 1
    CHALKBOARD(164),  //CUser::OnADBoard
    UPDATE_CHAR_BOX(165),  //OnMiniRoomBalloon
    SHOW_CONSUME_EFFECT(166),  //CUser::SetConsumeItemEffect
    SHOW_SCROLL_EFFECT(167),  //ShowItemUpgradeEffect

    //CUser::OnPetPacket
    SPAWN_PET(168),  //OnPetActivated
    MOVE_PET(170),  //OnMove
    PET_CHAT(171),  //OnAction
    PET_NAMECHANGE(172),
    PET_EXCEPTION_LIST(173),
    PET_COMMAND(174),  //OnActionCommand
    SPAWN_SPECIAL_MAPOBJECT(175),
    REMOVE_SPECIAL_MAPOBJECT(176),
    MOVE_SUMMON(177),
    SUMMON_ATTACK(177),
    DAMAGE_SUMMON(179),
    SUMMON_SKILL(180),
    SPAWN_DRAGON(181),
    MOVE_DRAGON(182),
    REMOVE_DRAGON(183),
    DRAGON_LEAVE_FIELD(184),
    MOVE_PLAYER(185),  //OnMove

    //CUserPool::OnUserRemotePacket
    CLOSE_RANGE_ATTACK(186),  //CUserRemote::OnAttack
    RANGED_ATTACK(187),  //CUserRemote::OnAttack
    MAGIC_ATTACK(188),  //CUserRemote::OnAttack
    ENERGY_ATTACK(189),  //CUserRemote::OnAttack
    SKILL_EFFECT(190),
    CANCEL_SKILL_EFFECT(191),
    DAMAGE_PLAYER(192),  //CUserRemote::OnHit
    FACIAL_EXPRESSION(193),  //CAvatar::SetEmotion
    SHOW_ITEM_EFFECT(194),  //CUser::SetActiveEffectItem
    SHOW_CHAIR(195),
    UPDATE_CHAR_LOOK(197),  //CUserRemote::OnAvatarModified
    SHOW_FOREIGN_EFFECT(198),
    GIVE_FOREIGN_BUFF(199),
    CANCEL_FOREIGN_BUFF(200),
    UPDATE_PARTYMEMBER_HP(201),  //CUserRemote::OnReceiveHP
    GUILD_NAME_CHANGED(202),
    GUILD_MARK_CHANGED(203),
    THROW_GRENADE(204),  //CUserLocal::OnPacket

    //CUserLocal::OnPacket
    SIT_RESULT(205),  //OnSitResult
    USER_LOCAL_EFFECT(206),  //onEffect
    TELEPORT(207),  //OnTeleport
    MESO_GIVE_SUCCEED(209),  //OnMesoGive_Succeeded
    MESO_GIVE_FAIL(210),  //OnMesoGive_Failed
    QUEST_RESULT(211),  // OnQuestResult
    NOTIFY_HP_DEC_BY_FIELD(212),
    // 213 looks empty
    BALLOON_MSG(214),  //onBalloonMsg
    PLAY_EVENT_SOUND(215),  //CUserLocal::OnPlayEventSound
    Play_MINIGAME_SOUND(216),  //CUserLocal::OnPlayMinigameSound
    MAKER_RESULT(217),  //OnMakerResult
    KOREAN_EVENT(219),  // ???
    OPEN_UI(220),  //OnOpenUI
    SET_DIRECTION_MODE(221),  //SetDirectionMode
    DISABLE_UI(222), // onDisableUI
    HIRE_TUTOR(223),  //OnHireTutor
    TUTOR_MSG(224),  //OnTutorMsg
    COMBO_RESPONSE(225),  //OnIncComboResponse
    RANDOM_EMOTION(226),  //OnRandomEmotion(226)
    RESIGN_QUEST_RETURN(227),  //OnResignQuestReturn(227)
    PASS_MATE_NAME(228),  //OnPassMateName
    RADIO_SCHEDULE(229),  //OnRadioSchedule
    OPEN_SKILL_GUIDE(230),
    NOTICE_MSG(231),  //OnNoticeMsg
    CHAT_MSG(232),  //onChatMsg
    SAY_MSG(233),  // 233 OnSayImage
    SKILL_COOLDOWN(234),  //OnSkillCooltimeSet

    //CMobPool::OnPacket
    SPAWN_MONSTER(236),  //OnMobEnterField
    KILL_MONSTER(237),  //OnMobLeaveField
    SPAWN_MONSTER_CONTROL(238),  //OnMobChangeController

    //cMobPool::onMobPacket
    MOVE_MONSTER(239),  //OnMove
    MOVE_MONSTER_RESPONSE(240),  //OnCtrlAck
    APPLY_MONSTER_STATUS(242),  //OnStatSet
    CANCEL_MONSTER_STATUS(243),  //OnStatReset
    RESET_MONSTER_ANIMATION(244),  //OnSuspendReset
    AFFECTED_MONSTER(245),  //OnAffected
    DAMAGE_MONSTER(246),  //OnDamaged
    SPECIAL_EFFECT_BY_SKILL(247),  //OnSpecialEffectBySkill
    ARIANT_THING(249),  // OnMobCrcKeyChanged or the ariant_thing lol
    SHOW_MONSTER_HP(250),  //OnHPIndicator
    CATCH_MONSTER(251),  //OnCatchEffect
    CATCH_MONSTER_WITH_ITEM(252),  //OnEffectByItem
    SHOW_MAGNET(253),  //OnMobSpeaking
    INC_MOB_CHARGE_COUNT(254),  //OnIncMobChargeCount
    //OnMobSkillDelay(303) v95
    //OnEscortFullPath(304) v95
    //OnEscortStopSay(306) v95
    //OnEscortReturnBefore(307) v95
    //OnNextAttack(309) v95
    MOB_ATTACKED_MOB(255),  //OnMobAttackedByMob(309) v95

    //cNpcPool::onPacket
    SPAWN_NPC(257),  //OnNpcEnterField
    REMOVE_NPC(258),  //OnNpcLeaveField
    SPAWN_NPC_REQUEST_CONTROLLER(259),  //OnNpcChangeController

    //cNpcPool::onNpcPacket
    NPC_ACTION(260),  //OnMove
    NPC_UPDATE_LIMITED_INFO(261),  //OnUpdateLimitedInfo(261)
    NPC_SET_SPECIAL_ACTION(262),  //OnSetSpecialAction(262)
    SET_NPC_SCRIPTABLE(263),  //cNpcTemplate::OnSetNpcScript

    //CEmployeePool::OnPacket
    SPAWN_HIRED_MERCHANT(265),  //OnEmployeeEnterField
    DESTROY_HIRED_MERCHANT(266),  //OnEmployeeLeaveField
    UPDATE_HIRED_MERCHANT(267),  //OnEmployeeMiniRoomBalloon

    //CDropPool::OnPacket
    DROP_ITEM_FROM_MAPOBJECT(268),  //OnDropEnterField
    REMOVE_ITEM_FROM_MAP(269),  //OnDropLeaveField

    CANNOT_SPAWN_KITE(270),
    SPAWN_KITE(271),
    REMOVE_KITE(272),  //CAffectedAreaPool::OnPacket

    SPAWN_MIST(273),  //OnAffectedAreaCreated
    REMOVE_MIST(274),  //OnAffectedAreaRemoved

    //CTownPortalPool::OnPacket
    SPAWN_DOOR(275),  //OnTownPortalCreated
    REMOVE_DOOR(276),  //OnTownPortalRemoved

    //CReactorPool::OnPacket
    REACTOR_HIT(277),  //OnReactorChangeState
    REACTOR_SPAWN(279),  //OnReactorEnterField
    REACTOR_DESTROY(280),  //OnReactorDestroy I assume

    //CField_Snowball
    SNOWBALL_STATE(281),
    HIT_SNOWBALL(282),
    SNOWBALL_MESSAGE(283),
    LEFT_KNOCK_BACK(284),  //OnSnowBallTouch

    //CField_Coconut
    COCONUT_HIT(285),
    COCONUT_SCORE(286),

    //CField_GuildBoss
    GUILD_BOSS_HEALER_MOVE(287),
    GUILD_BOSS_PULLEY_STATE_CHANGE(288),

    //CField_MonsterCarnival::OnPacket
    MONSTER_CARNIVAL_START(289),  //OnEnter
    MONSTER_CARNIVAL_OBTAINED_CP(290),  //OnPersonalCP
    MONSTER_CARNIVAL_PARTY_CP(291), MONSTER_CARNIVAL_SUMMON(292),  //OnRequestResult?
    MONSTER_CARNIVAL_MESSAGE(293),  //OnRequestResult?
    MONSTER_CARNIVAL_DIED(294),  //OnProcessForDeath
    MONSTER_CARNIVAL_LEAVE(295),  //OnShowMemberOutMsg
    SHOW_GAME_RESULT(296),

    //CField_Battlefield::OnPacket
    ARIANT_ARENA_USER_SCORE(297),
    SCORE_UPDATE(299),  //OnScoreUpdate
    TEAM_CHANGED(300),  //OnTeamChanged
    WITCH_TOWER_SCORE_UPDATE(301),  // ????

    HORNTAIL_CAVE(302),  //CField::OnHontailTimer
    ZAKUM_SHRINE(303),  //CField::OnZakumTimer
    NPC_TALK(304),  //CScriptMan::OnScriptMessage
    OPEN_NPC_SHOP(305),
    CONFIRM_SHOP_TRANSACTION(306),
    ADMIN_SHOP_MESSAGE(307),  //lame :P
    ADMIN_SHOP(308),
    STORAGE(309),
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

    PARCEL(322),  //CCashShop:onPacket

    CHARGE_PARAM_RESULT(323),
    QUERY_CASH_RESULT(324),
    CASHSHOP_OPERATION(325),  //OnCashItemResult
    CASHSHOP_PURCHASE_EXP_CHANGED(326),  // found thanks to Arnah (Vertisy)
    CASHSHOP_GIFT_INFO_RESULT(327),
    CASHSHOP_CHECK_NAME_CHANGE(328),  //OnCheckDuplicatedIDResult
    CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT(329),  //OnCheckNameChangePossibleResult
    CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT(331),  //OnCheckTransferWorldPossibleResult
    CASHSHOP_GACHAPON_STAMP_RESULT(332),  //OnCashShopGachaponStampResult
    CASHSHOP_CASH_ITEM_GACHAPON_RESULT(333),
    //ONE_A_DAY(395), //v95
    //NOTICE_FREE_CASH_ITEM(396), //v95

    //CFuncKeyMappedMan:OnPacket
    KEYMAP(335),
    AUTO_HP_POT(336),
    AUTO_MP_POT(337),  //CMapleTVMan:OnPacket

    SEND_TV(341),  //OnSetMessage
    REMOVE_TV(342),  //OnClearMessage
    ENABLE_TV(343),  //OnSendMessageResult

    MTS_OPERATION2(347),
    MTS_OPERATION(348),
    MAPLELIFE_RESULT(349),
    MAPLELIFE_ERROR(350),
    VICIOUS_HAMMER(354),
    VEGA_SCROLL(358);
}