package org.teacon.powertool.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.client.gui.ExamineHoloGlassScreen;
import org.teacon.powertool.network.client.OpenItemScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExamineHoloGlass extends ArmorItem implements IScreenProviderItem{
    
    //TODO 头部模型渲染
    public ExamineHoloGlass() {
        super(PowerToolItems.HOLO_GLASS_ARMOR_MATERIAL, Type.HELMET, new Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new OpenItemScreen(player.getItemInHand(hand),hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
    
    public static Collection<TagKey<Block>> getOutLinedBlockTags(){
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return List.of();
        var result = new ArrayList<TagKey<Block>>();
        var headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        addTags(headItem, result);
        addTags(player.getMainHandItem(),result);
        addTags(player.getOffhandItem(),result);
        return result;
    }
    
    public static Collection<Block> getOtherLinedBlocks(){
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return List.of();
        var result = new ArrayList<Block>();
        var headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        addBlocks(headItem, result);
        addBlocks(player.getMainHandItem(),result);
        addBlocks(player.getOffhandItem(),result);
        return result;
    }
    
    private static void addTags(ItemStack stack, List<TagKey<Block>> tagList) {
        if (stack.getItem() instanceof ExamineHoloGlass) {
            var tags = stack.get(PowerToolItems.BLOCK_TAGS_DATA);
            if (tags != null) tagList.addAll(tags.tags);
        }
    }
    
    private static void addBlocks(ItemStack stack, List<Block> blockList) {
        if (stack.getItem() instanceof ExamineHoloGlass) {
            var blocks = stack.get(PowerToolItems.BLOCKS_DATA);
            if(blocks != null){
                blockList.addAll(blocks.blocks.stream().map(BuiltInRegistries.BLOCK::get).filter(b -> b != Blocks.AIR).toList());
            }
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot) {
        return () -> new ExamineHoloGlassScreen(slot,stack.get(PowerToolItems.BLOCK_TAGS_DATA),stack.get(PowerToolItems.BLOCKS_DATA));
    }
    
    public record BlockTagsComponent(List<TagKey<Block>> tags) {
        
        private static final Codec<TagKey<Block>> TAG_KEY_CODEC = TagKey.codec(Registries.BLOCK);
        
        public static final Codec<BlockTagsComponent> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                TAG_KEY_CODEC.listOf().fieldOf("tags").forGetter(o -> o.tags)
        ).apply(ins, BlockTagsComponent::new));
        
        public static final StreamCodec<ByteBuf, BlockTagsComponent> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        
    }
    
    public record BlockComponents(List<ResourceLocation> blocks){
        
        public static final Codec<BlockComponents> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                ResourceLocation.CODEC.listOf().fieldOf("blocks").forGetter(o -> o.blocks)
        ).apply(ins, BlockComponents::new));
        
        public static final StreamCodec<ByteBuf, BlockComponents> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    }
}
