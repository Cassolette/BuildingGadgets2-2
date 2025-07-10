package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Copy extends BaseMode {
    public Copy() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "copy");
    }

    private boolean verifyAreaSize(AABB area, Player player) {
        int maxAxis = 500; //Todo Config?
        if (area.getXsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "x", maxAxis, area.getXsize()), false);
            return false;
        }
        if (area.getYsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "y", maxAxis, area.getYsize()), false);
            return false;
        }
        if (area.getZsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "z", maxAxis, area.getZsize()), false);
            return false;
        }
        Stream<BlockPos> areaStream = BlockPos.betweenClosedStream(area);
        long size = areaStream.count();
        int maxSize = 100000;
        if (size > maxSize) { //Todo Config?
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.areatoolarge", maxSize, size), false);
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) return coordinates; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return coordinates;


        AABB area = new AABB(copyStart, copyEnd);
        if (!verifyAreaSize(area, player)) {
            return coordinates;
        }
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos) && !(level.getBlockState(pos).getBlock() instanceof RenderBlock))
                coordinates.add(new StatePos(GadgetUtils.cleanBlockState(level.getBlockState(pos)), pos.subtract(copyStart)));
            else
                coordinates.add(new StatePos(Blocks.AIR.defaultBlockState(), pos.subtract(copyStart))); //We need to have a block in EVERY position, so write air if invalid
        });
        return coordinates;
    }

    /**
     * Collects whitelisted block entity data allowed by Copy gadget, such as sign texts.
     */
    public ArrayList<TagPos> collectTEs(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<TagPos> teData = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) return teData; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return teData;


        AABB area = new AABB(copyStart, copyEnd);
        if (!verifyAreaSize(area, player)) {
            return teData;
        }
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            BlockState blockState = level.getBlockState(pos);
            if (GadgetUtils.isValidBlockState(blockState, level, pos) && !(blockState.getBlock() instanceof RenderBlock)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && blockState.is(BlockTags.SIGNS)) { // Signs are valid to copy
                    CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                    TagPos tagPos = new TagPos(blockTag, pos.subtract(copyStart));
                    teData.add(tagPos);
                }
            }
        });
        return teData;
    }
}
