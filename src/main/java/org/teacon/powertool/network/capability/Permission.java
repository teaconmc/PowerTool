package org.teacon.powertool.network.capability;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.network.PowerToolNetwork;
import org.teacon.powertool.network.client.UpdatePermissionPacket;

import java.util.Optional;

public class Permission {

    public static final Capability<Permission> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = new ResourceLocation(PowerTool.MODID, "permission");

    private Boolean canUseGameMasterBlock;
    private Boolean canSwitchGameMode;

    public Optional<Boolean> isCanUseGameMasterBlock() {
        return Optional.ofNullable(canUseGameMasterBlock);
    }

    public void setCanUseGameMasterBlock(boolean canUseGameMasterBlock) {
        this.canUseGameMasterBlock = canUseGameMasterBlock;
    }

    public Optional<Boolean> isCanSwitchGameMode() {
        return Optional.ofNullable(canSwitchGameMode);
    }

    public void setCanSwitchGameMode(boolean canSwitchGameMode) {
        this.canSwitchGameMode = canSwitchGameMode;
    }

    @Mod.EventBusSubscriber
    public static class Provider implements ICapabilityProvider {

        private final Permission permission = new Permission();
        private final LazyOptional<Permission> provider = LazyOptional.of(() -> permission);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, this.provider);
        }

        private static final PermissionNode<Boolean> GAMEMODE = new PermissionNode<>(
            "minecraft", "command.gamemode", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.hasPermissions(2)
        );
        private static final PermissionNode<Boolean> COMMAND_BLOCK = new PermissionNode<>(
            "minecraft", "use_gamemaster_block", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.getAbilities().instabuild && player.hasPermissions(2)
        );

        @SubscribeEvent
        public static void on(PermissionGatherEvent.Nodes event) {
            event.addNodes(GAMEMODE, COMMAND_BLOCK);
        }
    }

    public static void updatePermission(ServerPlayer player) {
        PowerToolNetwork.channel().send(
            PacketDistributor.PLAYER.with(() -> player),
            new UpdatePermissionPacket(
                PermissionAPI.getPermission(player, Provider.COMMAND_BLOCK),
                PermissionAPI.getPermission(player, Provider.GAMEMODE)
            )
        );
    }
}
