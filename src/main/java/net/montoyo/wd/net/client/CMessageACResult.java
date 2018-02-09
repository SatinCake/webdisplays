/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.net.Message;
import net.montoyo.wd.utilities.NameUUIDPair;

@Message(messageId = 6, side = Side.CLIENT)
public class CMessageACResult implements IMessage, Runnable {

    private NameUUIDPair[] result;

    public CMessageACResult() {
    }

    public CMessageACResult(GameProfile gp) {
        result = new NameUUIDPair[] { new NameUUIDPair(gp) };
    }

    public CMessageACResult(GameProfile[] gps) {
        result = new NameUUIDPair[gps.length];

        for(int i = 0; i < gps.length; i++)
            result[i] = new NameUUIDPair(gps[i]);
    }

    public CMessageACResult(NameUUIDPair[] pairs) {
        result = pairs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int cnt = buf.readByte();
        result = new NameUUIDPair[cnt];

        for(int i = 0; i < cnt; i++)
            result[i] = new NameUUIDPair(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(result.length);

        for(NameUUIDPair pair : result)
            pair.writeTo(buf);
    }

    @Override
    public void run() {
        WebDisplays.PROXY.onAutocompleteResult(result);
    }

}