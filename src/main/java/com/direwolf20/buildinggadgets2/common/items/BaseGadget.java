package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.capabilities.CapabilityEnergyProvider;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.*;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;

public abstract class BaseGadget extends Item {

    public BaseGadget() {
        super(new Properties()
                .stacksTo(1));
    }

    /**
     * Forge Energy Storage methods
     */

    public abstract int getEnergyMax();

    public abstract int getEnergyCost();

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getEnergyMax();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        return (energy.getEnergyStored() < energy.getMaxEnergyStored());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CapabilityEnergyProvider(stack, getEnergyMax());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY, null)
                .map(e -> Math.min(13 * e.getEnergyStored() / e.getMaxEnergyStored(), 13))
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(e -> Mth.hsvToRgb(Math.max(0.0F, (float) e.getEnergyStored() / (float) e.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F))
                .orElse(super.getBarColor(stack));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Minecraft mc = Minecraft.getInstance();

        if (level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Screen.hasShiftDown();

        if (!sneakPressed) {
            tooltip.add(Component.translatable("buildinggadgets2.tooltips.holdshift",
                            "shift")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            DimBlockPos boundTo = GadgetNBT.getBoundPos(stack);
            if (boundTo != null) {
                tooltip.add(Component.translatable("buildinggadgets2.tooltips.boundto", boundTo.levelKey.location().getPath(), "[" + boundTo.blockPos.toShortString() + "]").setStyle(Styles.GOLD));
            }
        }

        stack.getCapability(ForgeCapabilities.ENERGY, null)
                .ifPresent(energy -> {
                    MutableComponent energyText = !sneakPressed
                            ? Component.translatable("buildinggadgets2.tooltips.energy", MagicHelpers.tidyValue(energy.getEnergyStored()), MagicHelpers.tidyValue(energy.getMaxEnergyStored()))
                            : Component.translatable("buildinggadgets2.tooltips.energy", String.format("%,d", energy.getEnergyStored()), String.format("%,d", energy.getMaxEnergyStored()));
                    tooltip.add(energyText.withStyle(ChatFormatting.GREEN));
                });

    }

    /**
     * Implementation level of for the onAction & onShiftAction methods below.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gadget = player.getItemInHand(hand);

        if (level.isClientSide()) //No client
            return InteractionResultHolder.success(gadget);

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        if (level.getBlockState(lookingAt.getBlockPos()).isAir() && GadgetNBT.getAnchorPos(gadget).equals(GadgetNBT.nullPos))
            return InteractionResultHolder.success(gadget);
        ItemActionContext context = new ItemActionContext(lookingAt.getBlockPos(), lookingAt, player, level, hand, gadget);

        if (player.isShiftKeyDown()) {
            if (GadgetNBT.getSetting(gadget, "bind")) {
                if (bindToInventory(level, player, gadget, lookingAt)) {
                    GadgetNBT.toggleSetting(gadget, "bind"); //Turn off bind
                    return InteractionResultHolder.success(gadget);
                } else {
                    return InteractionResultHolder.fail(gadget);
                }
            }
            return this.onShiftAction(context);
        }

        return this.onAction(context);
    }

    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        return InteractionResultHolder.pass(context.stack());
    }

    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        return InteractionResultHolder.pass(context.stack());
    }

    public boolean bindToInventory(Level level, Player player, ItemStack gadget, BlockHitResult lookingAt) {
        BlockEntity blockEntity = level.getBlockEntity(lookingAt.getBlockPos());
        if (blockEntity != null) {
            LazyOptional<IItemHandler> handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, lookingAt.getDirection());
            if (handler.isPresent()) {
                GadgetNBT.setBoundPos(gadget, new DimBlockPos(level, lookingAt.getBlockPos()));
                GadgetNBT.setToolValue(gadget, lookingAt.getDirection().ordinal(), "binddirection");
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.bindsuccess", lookingAt.getBlockPos().toShortString()), true);
                return true;
            }
        }
        DimBlockPos existingBind = GadgetNBT.getBoundPos(gadget);
        if (existingBind == null)
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.bindfailed"), true);
        else {
            GadgetNBT.clearBoundPos(gadget);
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.bindremoved"), true);
            return true;
        }
        return false;
    }

    /**
     * Rotates through the registered building modes, useful for key bindings.
     *
     * @param stack the gadget
     * @return the selected mode's id
     */
    public ResourceLocation rotateModes(ItemStack stack) {
        ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(this.gadgetTarget());
        var arrayOfModes = new ArrayList<>(modesForGadget); // This is required to work with index's
        var currentMode = GadgetNBT.getMode(stack);

        var modeIndex = arrayOfModes.indexOf(currentMode);

        // Fix the mode or move it back to zero if the next index is outside of the list
        if (modeIndex == -1 || (++modeIndex > arrayOfModes.size())) {
            modeIndex = 0; // Use zero if for some reason we can't find the mode
        }

        var mode = arrayOfModes.get(modeIndex);
        GadgetNBT.setMode(stack, mode);

        return mode.getId();
    }

    public abstract GadgetTarget gadgetTarget();

    public static ItemStack getGadget(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof BaseGadget)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof BaseGadget)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public static BlockPos getHitPos(ItemActionContext context) {
        BlockPos anchorPos = GadgetNBT.getAnchorPos(context.stack());
        return anchorPos.equals(GadgetNBT.nullPos) ? context.pos() : anchorPos;
    }

    public boolean canUndo(Level level, Player player, ItemStack gadget) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        ArrayList<StatePos> undoList = bg2Data.peekUndoList(GadgetNBT.peekUndoList(gadget));
        for (StatePos statePos : undoList) {
            if (!level.isLoaded(statePos.pos)) {
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.undofailedunloaded", statePos.pos.toShortString()), true);
                return false;
            }
        }
        return true;
    }

    public void undo(Level level, Player player, ItemStack gadget) {
        if (!canUndo(level, player, gadget)) return;
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        UUID buildUUID = GadgetNBT.popUndoList(gadget);
        ServerTickHandler.stopBuilding(buildUUID);
        ArrayList<StatePos> undoList = bg2Data.popUndoList(buildUUID);
        if (undoList.isEmpty()) return;
        Collections.reverse(undoList);

        ArrayList<BlockPos> todoList = new ArrayList<>();
        for (StatePos statePos : undoList) {
            todoList.add(statePos.pos);
        }
        boolean giveItemsBack = !player.isCreative(); //Might want more conditions later?
        BuildingUtils.removeTickHandler(level, player, todoList, giveItemsBack, giveItemsBack, gadget);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
