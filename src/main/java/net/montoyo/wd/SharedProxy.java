/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.core.HasAdvancement;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.data.GuiData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.*;

import javax.annotation.Nonnull;

import static net.minecraftforge.api.distmarker.Dist.CLIENT;

public class SharedProxy {
    public void preInit() {
    }

    public void init() {
    }

    public void postInit() {
    }

    public Level getWorld(ResourceKey<Level> dim) {
        return getServer().getLevel(dim);
    }

    public void enqueue(Runnable r) {
        ServerLifecycleHooks.getCurrentServer().addTickable(r);
    }

    public void displayGui(GuiData data) {
        Log.error("Called SharedProxy.displayGui() on server side...");
    }

    public void trackScreen(TileEntityScreen tes, boolean track) {
    }

    public void onAutocompleteResult(NameUUIDPair pairs[]) {
    }

    public GameProfile[] getOnlineGameProfiles() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream().map(Player::getGameProfile).toArray(GameProfile[]::new);
    }

    public void screenUpdateResolutionInGui(Vector3i pos, BlockSide side, Vector2i res) {
    }

    public void screenUpdateRotationInGui(Vector3i pos, BlockSide side, Rotation rot) {
    }

    public void screenUpdateAutoVolumeInGui(Vector3i pos, BlockSide side, boolean av) {
    }

    public void displaySetPadURLGui(String padURL) {
        Log.error("Called SharedProxy.displaySetPadURLGui() on server side...");
    }

    public void openMinePadGui(int padId) {
        Log.error("Called SharedProxy.openMinePadGui() on server side...");
    }

    public void handleJSResponseSuccess(int reqId, JSServerRequest type, byte[] data) {
        Log.error("Called SharedProxy.handleJSResponseSuccess() on server side...");
    }

    public void handleJSResponseError(int reqId, JSServerRequest type, int errCode, String err) {
        Log.error("Called SharedProxy.handleJSResponseError() on server side...");
    }

    @Nonnull
    public HasAdvancement hasClientPlayerAdvancement(@Nonnull ResourceLocation rl) {
        return HasAdvancement.DONT_KNOW;
    }

    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public void setMiniservClientPort(int port) {
    }

    public void startMiniservClient() {
    }

    public boolean isMiniservDisabled() {
        return false;
    }

    public void closeGui(BlockPos bp, BlockSide bs) {
    }

    public void renderRecipes() {
    }

    public boolean isShiftDown() {
        return false;
    }

}
