/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.utilities.*;

import java.util.function.Supplier;

public class SMessageScreenCtrl implements Runnable {

    public static final int CTRL_SET_URL = 0;
    public static final int CTRL_SHUT_DOWN = 1;
    public static final int CTRL_ADD_FRIEND = 2;
    public static final int CTRL_REMOVE_FRIEND = 3;
    public static final int CTRL_SET_RIGHTS = 4;
    public static final int CTRL_SET_RESOLUTION = 5;
    public static final int CTRL_TYPE = 6;
    public static final int CTRL_REMOVE_UPGRADE = 7;
    public static final int CTRL_LASER_DOWN = 8;
    public static final int CTRL_LASER_MOVE = 9;
    public static final int CTRL_LASER_UP = 10;
    public static final int CTRL_JS_REQUEST = 11;
    public static final int CTRL_SET_ROTATION = 12;
    public static final int CTRL_SET_URL_REMOTE = 13;
    public static final int CTRL_SET_AUTO_VOL = 14;

    private int ctrl;
    private ResourceLocation dim;
    private Vector3i pos;
    private BlockSide side;
    private String url;
    private NameUUIDPair friend;
    private ServerPlayer player;
    private int friendRights;
    private int otherRights;
    private Vector2i vec2i;
    private String text;
    private BlockPos soundPos;
    private ItemStack toRemove;
    private int jsReqID;
    private JSServerRequest jsReqType;
    private Object[] jsReqData;
    private Rotation rotation;
    private Vector3i remoteLoc;
    private boolean autoVol;

    public SMessageScreenCtrl() {
    }

    public static SMessageScreenCtrl setURL(TileEntityScreen tes, BlockSide side, String url, Vector3i remoteLocation) {
        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = (remoteLocation == null) ? CTRL_SET_URL : CTRL_SET_URL_REMOTE;
        ret.dim = tes.getLevel().dimension().location();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.url = url;

        if(remoteLocation != null)
            ret.remoteLoc = remoteLocation;

        return ret;
    }

    public SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, NameUUIDPair friend, boolean del) {
        ctrl = del ? CTRL_REMOVE_FRIEND : CTRL_ADD_FRIEND;
        dim = tes.getLevel().dimension().location();
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        this.friend = friend;
    }

    public SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, int fr, int or) {
        ctrl = CTRL_SET_RIGHTS;
        dim = tes.getLevel().dimension().location();
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        friendRights = fr;
        otherRights = or;
    }

    public SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, ItemStack toRem) {
        ctrl = CTRL_REMOVE_UPGRADE;
        dim = tes.getLevel().dimension().location();
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        toRemove = toRem;
    }

    public SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, Rotation rot) {
        ctrl = CTRL_SET_ROTATION;
        dim = tes.getLevel().dimension().location();
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        rotation = rot;
    }

    public static SMessageScreenCtrl type(TileEntityScreen tes, BlockSide side, String text, BlockPos soundPos) {
        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = CTRL_TYPE;
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.dim = tes.getLevel().dimension().location();
        ret.side = side;
        ret.text = text;
        ret.soundPos = soundPos;

        return ret;
    }

    public static SMessageScreenCtrl vec2(TileEntityScreen tes, BlockSide side, int ctrl, Vector2i vec) {
        if(!isVec2Ctrl(ctrl))
            throw new RuntimeException("Called SMessageScreenCtrl.vec2() with non-vec2 control message " + ctrl);

        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = ctrl;
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.dim = tes.getLevel().dimension().location();
        ret.side = side;
        ret.vec2i = vec;

        return ret;
    }

    public static SMessageScreenCtrl laserUp(TileEntityScreen tes, BlockSide side) {
        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = CTRL_LASER_UP;
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.dim = tes.getLevel().dimension().location();
        ret.side = side;

        return ret;
    }

    public static SMessageScreenCtrl jsRequest(TileEntityScreen tes, BlockSide side, int reqId, JSServerRequest reqType, Object ... data) {
        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = CTRL_JS_REQUEST;
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.dim = tes.getLevel().dimension().location();
        ret.side = side;
        ret.jsReqID = reqId;
        ret.jsReqType = reqType;
        ret.jsReqData = data;

        return ret;
    }

    public static SMessageScreenCtrl autoVol(TileEntityScreen tes, BlockSide side, boolean av) {
        SMessageScreenCtrl ret = new SMessageScreenCtrl();
        ret.ctrl = CTRL_SET_AUTO_VOL;
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.dim = tes.getLevel().dimension().location();
        ret.side = side;
        ret.autoVol = av;

        return ret;
    }

    private static boolean isVec2Ctrl(int msg) {
        return msg == CTRL_SET_RESOLUTION || msg == CTRL_LASER_DOWN || msg == CTRL_LASER_MOVE;
    }

    public static SMessageScreenCtrl decode(FriendlyByteBuf buf) {
        SMessageScreenCtrl message = new SMessageScreenCtrl();
        message.ctrl = buf.readByte();
        message.dim = buf.readResourceLocation();
        message.pos = new Vector3i(buf);
        message.side = BlockSide.fromInt(buf.readByte());

        if(message.ctrl == CTRL_SET_URL)
            message.url = buf.readUtf();
        else if(message.ctrl == CTRL_ADD_FRIEND || message.ctrl == CTRL_REMOVE_FRIEND)
            message.friend = new NameUUIDPair(buf);
        else if(message.ctrl == CTRL_SET_RIGHTS) {
            message.friendRights = buf.readByte();
            message.otherRights = buf.readByte();
        } else if(isVec2Ctrl(message.ctrl))
            message.vec2i = new Vector2i(buf);
        else if(message.ctrl == CTRL_TYPE) {
            message.text = buf.readUtf();

            int sx = buf.readInt();
            int sy = buf.readInt();
            int sz = buf.readInt();
            message.soundPos = new BlockPos(sx, sy, sz);
        } else if(message.ctrl == CTRL_REMOVE_UPGRADE)
            message.toRemove = buf.readItem();
        else if(message.ctrl == CTRL_JS_REQUEST) {
            message.jsReqID = buf.readInt();
            message.jsReqType = JSServerRequest.fromID(buf.readByte());

            if(message.jsReqType != null)
                message.jsReqData = message.jsReqType.deserialize(buf);
        } else if(message.ctrl == CTRL_SET_ROTATION)
            message.rotation = Rotation.values()[buf.readByte() & 3];
        else if(message.ctrl == CTRL_SET_URL_REMOTE) {
            message.url = buf.readUtf();
            message.remoteLoc = new Vector3i(buf);
        } else if(message.ctrl == CTRL_SET_AUTO_VOL)
            message.autoVol = buf.readBoolean();

        return message;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(ctrl);
        buf.writeResourceLocation(dim);
        pos.writeTo(buf);
        buf.writeByte(side.ordinal());

        if(ctrl == CTRL_SET_URL)
            buf.writeUtf(url);
        else if(ctrl == CTRL_ADD_FRIEND || ctrl == CTRL_REMOVE_FRIEND)
            friend.writeTo(buf);
        else if(ctrl == CTRL_SET_RIGHTS) {
            buf.writeByte(friendRights);
            buf.writeByte(otherRights);
        } else if(isVec2Ctrl(ctrl))
            vec2i.writeTo(buf);
        else if(ctrl == CTRL_TYPE) {
            buf.writeUtf(text);
            buf.writeInt(soundPos.getX());
            buf.writeInt(soundPos.getY());
            buf.writeInt(soundPos.getZ());
        } else if(ctrl == CTRL_REMOVE_UPGRADE)
            buf.writeItem(toRemove);
        else if(ctrl == CTRL_JS_REQUEST) {
            buf.writeInt(jsReqID);
            buf.writeByte(jsReqType.ordinal());

            if(!jsReqType.serialize(buf, jsReqData))
                throw new RuntimeException("Could not serialize CTRL_JS_REQUEST " + jsReqType);
        } else if(ctrl == CTRL_SET_ROTATION)
            buf.writeByte(rotation.ordinal());
        else if(ctrl == CTRL_SET_URL_REMOTE) {
            buf.writeUtf(url);
            remoteLoc.writeTo(buf);
        } else if(ctrl == CTRL_SET_AUTO_VOL)
            buf.writeBoolean(autoVol);
    }

    @Override
    public void run() {
        if(side == null) {
            Log.warning("Caught invalid packet from %s (UUID %s) referencing an invalid block side", player.getName(), player.getGameProfile().getId().toString());
            return;
        }

        try {
            runUnsafe();
        } catch(MissingPermissionException e) {
            Log.errorEx("I have reasons to believe %s (UUID %s) is a hacker, but don't take my word for it...", e, e.getPlayer().getName(), e.getPlayer().getGameProfile().getId().toString());
        }
    }

    private void checkPermission(TileEntityScreen scr, int right) throws MissingPermissionException {
        int prights = scr.getScreen(side).rightsFor(player);
        if((prights & right) == 0)
            throw new MissingPermissionException(right, player);
    }

    private void runUnsafe() throws MissingPermissionException {
        Level world = player.level;
        BlockPos bp = pos.toBlock();

        if(!world.dimension().location().equals(dim))
            return; //Out of range (dimension mismatch)

        if(ctrl == CTRL_SET_URL_REMOTE) {
            double reachDist = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            BlockPos blockPos = remoteLoc.toBlock();

            if(player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > reachDist * reachDist)
                return; //Out of range (player reach distance)

            BlockState bs = world.getBlockState(blockPos);
            if(bs.getBlock() != BlockInit.blockServer.get() && bs.getBlock() != BlockInit.blockRControl.get() &&
                    bs.getBlock() != BlockInit.blockKeyBoard.get() && bs.getBlock() != BlockInit.blockRedControl.get()
                    && bs.getValue(BlockPeripheral.type) != DefaultPeripheral.REMOTE_CONTROLLER)
                return; //I call it hax...
        } else if(player.distanceToSqr(bp.getX(), bp.getY(), bp.getZ()) > (128 * 128))
            return; //Out of range (range problem)

        BlockEntity te = world.getBlockEntity(bp);
        if(te == null || !(te instanceof TileEntityScreen)) {
            Log.error("TileEntity at %s is not a screen; can't control it!", pos.toString());
            return;
        }

        TileEntityScreen tes = (TileEntityScreen) te;

        if(ctrl == CTRL_SET_URL || ctrl == CTRL_SET_URL_REMOTE) {
            checkPermission(tes, ScreenRights.CHANGE_URL);
            tes.setScreenURL(side, url);
        } else if(ctrl == CTRL_SHUT_DOWN) {
            //TODO
            //checkPermission(tes, ScreenRights.CHANGE_URL);
            //tes.removeScreen(side);
        } else if(ctrl == CTRL_ADD_FRIEND) {
            checkPermission(tes, ScreenRights.MANAGE_FRIEND_LIST);
            tes.addFriend(player, side, friend);
        } else if(ctrl == CTRL_REMOVE_FRIEND) {
            checkPermission(tes, ScreenRights.MANAGE_FRIEND_LIST);
            tes.removeFriend(player, side, friend);
        } else if(ctrl == CTRL_SET_RIGHTS) {
            TileEntityScreen.Screen scr = tes.getScreen(side);

            if(scr != null) {
                int fr = scr.owner.uuid.equals(player.getGameProfile().getId()) ? friendRights : scr.friendRights;
                int or = (scr.rightsFor(player) & ScreenRights.MANAGE_OTHER_RIGHTS) == 0 ? scr.otherRights : otherRights;

                if(scr.friendRights != fr || scr.otherRights != or)
                    tes.setRights(player, side, fr, or);
            }
        } else if(ctrl == CTRL_SET_RESOLUTION) {
            checkPermission(tes, ScreenRights.CHANGE_RESOLUTION);
            tes.setResolution(side, vec2i);
        } else if(ctrl == CTRL_TYPE) {
            checkPermission(tes, ScreenRights.CLICK);
            tes.type(side, text, soundPos);
        } else if(ctrl == CTRL_REMOVE_UPGRADE) {
            checkPermission(tes, ScreenRights.MANAGE_UPGRADES);
            tes.removeUpgrade(side, toRemove, player);
        } else if(ctrl == CTRL_LASER_DOWN || ctrl == CTRL_LASER_MOVE)
            tes.laserDownMove(side, player, vec2i, ctrl == CTRL_LASER_DOWN);
        else if(ctrl == CTRL_LASER_UP)
            tes.laserUp(side, player);
        else if(ctrl == CTRL_JS_REQUEST) {
            if(jsReqType == null || jsReqData == null)
                Log.warning("Caught invalid JS request from player %s (UUID %s)", player.getName(), player.getGameProfile().getId().toString());
            else
                tes.handleJSRequest(player, side, jsReqID, jsReqType, jsReqData);
        } else if(ctrl == CTRL_SET_ROTATION) {
            checkPermission(tes, ScreenRights.CHANGE_RESOLUTION);
            tes.setRotation(side, rotation);
        } else if(ctrl == CTRL_SET_AUTO_VOL) {
            checkPermission(tes, ScreenRights.MANAGE_UPGRADES); //because why not
            tes.setAutoVolume(side, autoVol);
        } else
            Log.warning("Caught SMessageScreenCtrl with invalid control ID %d from player %s (UUID %s)", ctrl, player.getName(), player.getGameProfile().getId().toString());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        player = contextSupplier.get().getSender();
        contextSupplier.get().enqueueWork(this);
        contextSupplier.get().setPacketHandled(true);
    }

}
