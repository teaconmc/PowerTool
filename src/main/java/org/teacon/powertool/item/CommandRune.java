package org.teacon.powertool.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.network.client.OpenItemScreen;
import org.teacon.powertool.utils.DelayServerExecutor;
import org.teacon.powertool.utils.VanillaUtils;

@NonNullByDefault
public class CommandRune extends Item implements IScreenProviderItem {
    public CommandRune(Properties properties) {
        super(properties);
    }
    
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }
    
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return stack.getOrDefault(PowerToolDataComponents.CYCLE, 0); // getOrDefault(Supplier<DataComponentType<T>>, T) is from NeoForge
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        String command = held.get(PowerToolDataComponents.COMMAND);
        if ((command == null || (player.getAbilities().instabuild && player.isCrouching())) && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new OpenItemScreen(player.getItemInHand(hand), hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            String command = stack.get(PowerToolDataComponents.COMMAND); // get(Supplier<DataComponentType<T>>) is from NeoForge
            if (command != null) {
                VanillaUtils.runCommand(command, livingEntity);
                if (Boolean.TRUE.equals(stack.get(PowerToolDataComponents.CONSUME)) && (!(livingEntity instanceof Player player) || !player.getAbilities().instabuild)) {
                    stack.shrink(1);
                }
                // Yes, you can make it consumable
                // Viva la data component!
                //并不行 如果设置Damage=1,MaxDamage=1,使用后使整组物品都会出现耐久条,而你并不能设置MaxDamage=0 --xkball
//                EquipmentSlot slot = stack.equals(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND))
//                        ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
//                stack.hurtAndBreak(1, livingEntity, slot);
            }
            var delayCommands = stack.get(PowerToolDataComponents.DELAYED_COMMANDS);
            if (delayCommands != null && livingEntity instanceof Player player) {
                var i = 0;
                for (var pair : delayCommands) {
                    i += pair.delay;
                    if (!pair.command.isEmpty())
                        DelayServerExecutor.addTask(i, (server) -> VanillaUtils.runCommand(pair.command, server, player.getUUID()));
                }
            }
            
        }
        return stack;
    }
    
    public record DelayedCommandData(int delay, String command) {
        
        public static final Codec<DelayedCommandData> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                Codec.INT.fieldOf("delay").forGetter(o -> o.delay),
                Codec.STRING.fieldOf("command").forGetter(o -> o.command)
        ).apply(ins, DelayedCommandData::new));
    }
}
