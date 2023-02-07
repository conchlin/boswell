/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

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
package net;

import java.util.LinkedHashMap;
import java.util.Map;

import network.opcode.RecvOpcode;
import net.server.channel.handlers.*;
import net.server.handlers.AliveAckHandler;
import net.server.handlers.LoginRequiringNoOpHandler;
import net.server.handlers.login.*;

public final class PacketProcessor {

    private final static Map<String, PacketProcessor> instances = new LinkedHashMap<>();
    private MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvOpcode op : RecvOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public MaplePacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        return handlers[packetId];
    }

    public void registerHandler(RecvOpcode code, MaplePacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Error registering handler - " + code.name());
        }
    }

    public synchronized static PacketProcessor getProcessor(int world, int channel) {
        final String lolpair = world + " " + channel;
        PacketProcessor processor = instances.get(lolpair);
        if (processor == null) {
            processor = new PacketProcessor();
            processor.reset(channel);
            instances.put(lolpair, processor);
        }
        return processor;
    }

    public void reset(int channel) {
        handlers = new MaplePacketHandler[handlers.length];

        registerHandler(RecvOpcode.AliveAck, new AliveAckHandler());
        //registerHandler(RecvOpcode.CUSTOM_PACKET, new CustomPacketHandler());
        if (channel < 0) {
            //LOGIN HANDLERS
            registerHandler(RecvOpcode.ConfirmEULA, new ConfirmEULAHandler());
            registerHandler(RecvOpcode.CheckPinCode, new CheckPinCodeHandler());
            registerHandler(RecvOpcode.WorldInfoRequest, new WorldInfoRequestHandler());
            registerHandler(RecvOpcode.SelectWorld, new SelectWorldHandler());
            registerHandler(RecvOpcode.SelectCharacter, new SelectCharacterHandler());
            registerHandler(RecvOpcode.CheckPassword, new CheckPasswordHandler());
            registerHandler(RecvOpcode.RELOG, new RelogRequestHandler());
            registerHandler(RecvOpcode.WorldRequest, new WorldInfoRequestHandler());
            registerHandler(RecvOpcode.CheckUserLimit, new ServerStatusRequestHandler());
            registerHandler(RecvOpcode.CheckDuplicatedID, new CheckCharNameHandler());
            registerHandler(RecvOpcode.CreateNewCharacter, new CreateCharHandler());
            registerHandler(RecvOpcode.DeleteCharacter, new DeleteCharHandler());
            registerHandler(RecvOpcode.ViewAllChar, new ViewAllCharHandler());
            registerHandler(RecvOpcode.SelectCharacterByVAC, new SelectCharacterByVACHandler());
            registerHandler(RecvOpcode.UpdatePinCode, new UpdatePinCodeHandler());
            registerHandler(RecvOpcode.GuestIDLogin, new GuestIDLoginHandler());
            registerHandler(RecvOpcode.REGISTER_PIC, new RegisterPicHandler());
            registerHandler(RecvOpcode.CHAR_SELECT_WITH_PIC, new CharSelectedWithPicHandler());
            registerHandler(RecvOpcode.SetGender, new SetGenderHandler());
            registerHandler(RecvOpcode.VIEW_ALL_WITH_PIC, new ViewAllCharSelectedWithPicHandler());
            registerHandler(RecvOpcode.VIEW_ALL_PIC_REGISTER, new ViewAllCharRegisterPicHandler());
        } else {
            //CHANNEL HANDLERS
            registerHandler(RecvOpcode.CheckNameChangePossible, new CheckNameChangePossibleHandler());
            registerHandler(RecvOpcode.CheckDuplicatedID, new CheckDuplicatedIDHandler());
            registerHandler(RecvOpcode.CheckTransferWorldPossible, new CheckTransferWorldPossibleHandler());
            registerHandler(RecvOpcode.TransferChannelRequest, new TransferChannelRequestHandler());
            registerHandler(RecvOpcode.STRANGE_DATA, LoginRequiringNoOpHandler.getInstance());
            registerHandler(RecvOpcode.UserChat, new UserChatHandler());
            registerHandler(RecvOpcode.Whisper, new WhisperHandler());
            registerHandler(RecvOpcode.SelectNpc, new SelectNPCHandler());
            registerHandler(RecvOpcode.ScriptMessageAnswer, new ScriptMessageAnswerHandler());
            registerHandler(RecvOpcode.QuestRequest, new QuestRequestHandler());
            registerHandler(RecvOpcode.ThrowGrenade, new ThrowGrenadeHandler());
            registerHandler(RecvOpcode.ShopRequest, new ShopRequestHandler());
            registerHandler(RecvOpcode.GatherItemRequest, new GatherItemRequestHandler());
            registerHandler(RecvOpcode.ChangeSlotPositionRequest, new ChangeSlotPositionRequestHandler());
            registerHandler(RecvOpcode.DropMoneyRequest, new DropMoneyRequestHandler());
            registerHandler(RecvOpcode.MigrateIn, new PlayerLoggedinHandler());
            registerHandler(RecvOpcode.TransferFieldRequest, new TransferFieldRequestHandler());
            registerHandler(RecvOpcode.MobMove, new MobMoveHandler());
            registerHandler(RecvOpcode.MeleeAttack, new MeleeAttackHandler());
            registerHandler(RecvOpcode.ShootAttack, new ShootAttackHandler());
            registerHandler(RecvOpcode.MagicAttack, new MagicAttackHandler());
            registerHandler(RecvOpcode.UserHit, new UserHitHandler());
            registerHandler(RecvOpcode.UserMove, new UserMoveHandler());
            registerHandler(RecvOpcode.ConsumeCashItemUseRequest, new ConsumeCashItemUseRequestHandler());
            registerHandler(RecvOpcode.StatChangeItemUseRequest, new StatChangeItemUseRequestHandler());
            registerHandler(RecvOpcode.PortalScrollUseRequest, new StatChangeItemUseRequestHandler());
            registerHandler(RecvOpcode.UpgradeItemUseRequest, new UpgradeItemUseRequestHandler());
            registerHandler(RecvOpcode.MobSummonItemUseRequest, new MobSummonItemUseRequestHandler());
            registerHandler(RecvOpcode.UserEmotion, new UserEmotionHandler());
            registerHandler(RecvOpcode.HEAL_OVER_TIME, new HealOvertimeHandler());
            registerHandler(RecvOpcode.DropPickUpRequest, new DropPickUpRequestHandler());
            registerHandler(RecvOpcode.CharacterInfoRequest, new CharInfoRequestHandler());
            registerHandler(RecvOpcode.SkillUseRequest, new SkillUseRequestHandler());
            registerHandler(RecvOpcode.PortalTeleportRequest, new PortalTeleportRequestHandler());
            registerHandler(RecvOpcode.SkillCancelRequest, new SkillCancelRequestHandler());
            registerHandler(RecvOpcode.StatChangeItemCancelRequest, new StatChangeItemCancelRequestHandler());
            registerHandler(RecvOpcode.MiniRoom, new MiniRoomHandler());
            registerHandler(RecvOpcode.AbilityUpRequest, new AbilityUpRequestHandler());
            registerHandler(RecvOpcode.SkillUpRequest, new SkillUpRequestHandler());
            registerHandler(RecvOpcode.FuncKeyMappedModified, new FuncKeyMappedModifiedHandler());
            registerHandler(RecvOpcode.PortalScriptRequest, new PortalScriptRequestHandler());
            registerHandler(RecvOpcode.TrunkRequest, new TrunkRequestHandler());
            registerHandler(RecvOpcode.GivePopularityRequest, new GivePopularityRequestHandler());
            registerHandler(RecvOpcode.PartyRequest, new PartyRequestHandler());
            registerHandler(RecvOpcode.PartyResult, new PartyResultHandler());
            registerHandler(RecvOpcode.GroupMessage, new GroupMessageHandler());
            registerHandler(RecvOpcode.EnterTownPortalRequest, new EnterTownPortalRequestHandler());
            registerHandler(RecvOpcode.MigrateToITCRequest, new MigrateToITCRequestHandler());
            registerHandler(RecvOpcode.MigrateToCashShopRequest, new MigrateToCashShopRequestHandler());
            registerHandler(RecvOpcode.SummonedHit, new DamageSummonHandler());
            registerHandler(RecvOpcode.SummonedMove, new SummonMoveHandler());
            registerHandler(RecvOpcode.SummonedAttack, new SummonDamageHandler());
            registerHandler(RecvOpcode.FriendRequest, new FriendRequestHandler());
            registerHandler(RecvOpcode.ActivateEffectItem, new ActivateEffectItemHandler());
            registerHandler(RecvOpcode.PortableChairSitRequest, new PortableChairSitRequestHandler());
            registerHandler(RecvOpcode.SitRequest, new SitRequestHandler());
            registerHandler(RecvOpcode.ReactorHit, new ReactorHitHandler());
            registerHandler(RecvOpcode.GuildRequest, new GuildRequestHandler());
            registerHandler(RecvOpcode.GuildResult, new GuildResultHandler());
            registerHandler(RecvOpcode.GuildBBS, new GuildBBSOperationHandler());
            registerHandler(RecvOpcode.SkillPrepareRequest, new SkillPrepareRequestHandler());
            registerHandler(RecvOpcode.Messenger, new MessengerHandler());
            registerHandler(RecvOpcode.NpcAction, new NpcActionHandler());
            registerHandler(RecvOpcode.CHECK_CASH, new TouchingCashShopHandler());
            registerHandler(RecvOpcode.CASHSHOP_OPERATION, new CashOperationHandler());
            registerHandler(RecvOpcode.COUPON_CODE, new CouponCodeHandler());
            registerHandler(RecvOpcode.ActivatePetRequest, new ActivatePetRequestHandler());
            registerHandler(RecvOpcode.PetMove, new PetMoveHandler());
            registerHandler(RecvOpcode.PetAction, new PetActionHandler());
            registerHandler(RecvOpcode.PetInteractionRequest, new PetInteractionRequestHandler());
            registerHandler(RecvOpcode.PetFoodItemUseRequest, new PetFoodItemUseRequestHandler());
            registerHandler(RecvOpcode.PetDropPickUpRequest, new PetDropPickUpRequestHandler());
            registerHandler(RecvOpcode.MobApplyCtrl, new MobApplyCtrlHandler());
            registerHandler(RecvOpcode.MobSelfDestruct, new MobSelfDestructHandler());
            registerHandler(RecvOpcode.TemporaryStatUpdateRequest, new TemporaryStatUpdateRequestHandler());
            registerHandler(RecvOpcode.SkillLearnItemUseRequest, new SkillLearnItemUseRequestHandler());
            registerHandler(RecvOpcode.MacroSysDataModified, new MacroSysDataModifiedHandler());
            registerHandler(RecvOpcode.MemoRequest, new MemoRequestHandler());
            registerHandler(RecvOpcode.ADBoardClose, new ADBoardCloseHandler());
            registerHandler(RecvOpcode.TamingMobFoodItemUseRequest, new TamingMobFoodItemUseRequestHandler());
            registerHandler(RecvOpcode.MTS_OPERATION, new MTSHandler());
            registerHandler(RecvOpcode.MarriageRequest, new RingActionHandler());
            registerHandler(RecvOpcode.CoupleMessage, new CoupleMessageHandler());
            registerHandler(RecvOpcode.PET_AUTO_POT, new PetAutoPotHandler());
            registerHandler(RecvOpcode.PetUpdateExceptionListRequest, new PetUpdateExceptionListRequestHandler());
            registerHandler(RecvOpcode.ShopScannerRequest, new ShopScannerRequestHandler());
            registerHandler(RecvOpcode.ShopLinkRequest, new ShopLinkRequestHandler());
            registerHandler(RecvOpcode.BodyAttack, new BodyAttackHandler());
            registerHandler(RecvOpcode.MapTransferRequest, new MapTransferRequestHandler());
            registerHandler(RecvOpcode.EntrustedShopRequest, new EntrustedShopRequestHandler());
            registerHandler(RecvOpcode.BanMapByMob, new BanMapByMobHandler());
            registerHandler(RecvOpcode.MobAttackMob, new MobAttackMobHandler());
            registerHandler(RecvOpcode.ClaimRequest, new ClaimRequestHandler());
            registerHandler(RecvOpcode.MonsterBookSetCover, new MonsterBookSetCoverHandler());
            registerHandler(RecvOpcode.AbilityMassUpRequest, new AbilityMassUpRequestHandler());
            registerHandler(RecvOpcode.ItemMakeRequest, new ItemMakeRequestHandler());
            registerHandler(RecvOpcode.ADD_FAMILY, new FamilyAddHandler());
            registerHandler(RecvOpcode.USE_FAMILY, new FamilyUseHandler());
            registerHandler(RecvOpcode.USE_HAMMER, new UseHammerHandler());
            registerHandler(RecvOpcode.ScriptItemUseRequest, new ScriptItemUseRequestHandler());
            registerHandler(RecvOpcode.ReactorTouch, new ReactorTouchHandler());
            registerHandler(RecvOpcode.SummonedSkill, new BeholderHandler());
            registerHandler(RecvOpcode.Admin, new AdminCommandHandler());
            registerHandler(RecvOpcode.AdminLog, new AdminLogHandler());
            registerHandler(RecvOpcode.AllianceRequest, new AllianceOperationHandler());
            registerHandler(RecvOpcode.AllianceResult, new DenyAllianceRequestHandler());
            registerHandler(RecvOpcode.USE_SOLOMON_ITEM, new UseSolomonHandler());
            registerHandler(RecvOpcode.USE_GACHA_EXP, new UseGachaExpHandler());
            registerHandler(RecvOpcode.NewYearCardRequest, new NewYearCardRequestHandler());
            registerHandler(RecvOpcode.CASHSHOP_SURPRISE, new CashShopSurpriseHandler());
            registerHandler(RecvOpcode.LotteryItemUseRequest, new LotteryItemUseRequestHandler());
            registerHandler(RecvOpcode.UseGachaponRemoteRequest, new UseGachaponRemoteRequestHandler());
            registerHandler(RecvOpcode.ACCEPT_FAMILY, new AcceptFamilyHandler());
            registerHandler(RecvOpcode.ParcelRequest, new ParcelRequestHandler());
            registerHandler(RecvOpcode.UpgradeTombEffect, new UpgradeTombEffectHandler());
            registerHandler(RecvOpcode.PLAYER_MAP_TRANSFER, new PlayerMapTransitionHandler());
            registerHandler(RecvOpcode.USE_MAPLELIFE, new UseMapleLifeHandler());
            registerHandler(RecvOpcode.USE_CATCH_ITEM, new UseCatchItemHandler());
            registerHandler(RecvOpcode.MobHitByMob, new MobHitByMobHandler());
            registerHandler(RecvOpcode.PARTY_SEARCH_REGISTER, new PartySearchRegisterHandler());
            registerHandler(RecvOpcode.PARTY_SEARCH_START, new PartySearchStartHandler());
            registerHandler(RecvOpcode.PARTY_SEARCH_UPDATE, new PartySearchUpdateHandler());
            registerHandler(RecvOpcode.SortItemRequest, new SortItemRequestHandler());
            registerHandler(RecvOpcode.SnowBallTouch, new SnowballTouchHandler());
            registerHandler(RecvOpcode.SnowBallHit, new SnowBallHitHandler());
            registerHandler(RecvOpcode.CoconutHit, new CoconutHitHandler());
            registerHandler(RecvOpcode.RequestIncCombo, new RequestIncComboHandler());
            registerHandler(RecvOpcode.TalkToTutor, new TalkToTutorHandler());
            registerHandler(RecvOpcode.StoreBankRequest, new StoreBankRequestHandler());
            registerHandler(RecvOpcode.MCarnivalRequest, new MCarnivalRequestHandler());
            registerHandler(RecvOpcode.RemoteShopOpenRequest, new RemoteShopOpenRequestHandler());
            registerHandler(RecvOpcode.WEDDING_ACTION, new WeddingHandler());
            registerHandler(RecvOpcode.WEDDING_TALK, new WeddingTalkHandler());
            registerHandler(RecvOpcode.WEDDING_TALK_MORE, new WeddingTalkMoreHandler());
            registerHandler(RecvOpcode.UseWaterOfLife, new UseWaterOfLifeHandler());
            registerHandler(RecvOpcode.BroadcastMsg, new AdminChatHandler());
            registerHandler(RecvOpcode.DragonMove, new DragonMoveHandler());
            registerHandler(RecvOpcode.OPEN_ITEMUI, new RaiseUIStateHandler());
            registerHandler(RecvOpcode.USE_ITEMUI, new RaiseIncExpHandler());
        }
    }
}