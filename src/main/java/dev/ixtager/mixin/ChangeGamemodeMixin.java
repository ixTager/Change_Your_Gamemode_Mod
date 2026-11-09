package dev.ixtager.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerList.class)
public class ChangeGamemodeMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        PlayerList playerList = (PlayerList) (Object) this;
        var server = playerList.getServer();

        if (server != null && server.isSingleplayer()) {
            WorldData worldData = server.getWorldData();

            if (worldData instanceof PrimaryLevelData primaryData && !primaryData.isAllowCommands()) {

                PrimaryLevelDataAccessor dataAccessor = (PrimaryLevelDataAccessor) primaryData;
                LevelSettings oldSettings = dataAccessor.getSettings();

                LevelSettings newSettings = new LevelSettings(
                        oldSettings.levelName(),
                        oldSettings.gameType(),
                        oldSettings.hardcore(),
                        oldSettings.difficulty(),
                        true,
                        oldSettings.gameRules(),
                        oldSettings.getDataConfiguration()
                );

                dataAccessor.setSettings(newSettings);

                GameProfile profile = player.getGameProfile();
                NameAndId nameAndId = new NameAndId(profile.id(), profile.name());

                playerList.op(nameAndId);

                server.getCommands().sendCommands(player);
            }
        }
    }
}