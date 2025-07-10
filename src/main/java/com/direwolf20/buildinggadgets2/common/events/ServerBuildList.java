package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.util.DimBlockPos;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerBuildList {
    public enum BuildType {
        BUILD,
        EXCHANGE,
        DESTROY,
        UNDO_DESTROY,
        CUT
    }

    public Level level;
    public ArrayList<StatePos> statePosList;
    public ArrayList<TagPos> teData;
    public byte renderType;
    public UUID playerUUID;
    public int originalSize;
    public ArrayList<StatePos> actuallyBuildList = new ArrayList<>();
    public boolean needItems;
    public boolean returnItems;
    public ItemStack gadget;
    public UUID buildUUID;
    public BuildType buildType;
    public boolean dropContents;
    public HashMap<BlockPos, RetryEntry> retryList = new HashMap<>();
    public BlockPos cutStart = BlockPos.ZERO;
    public BlockPos lookingAt = BlockPos.ZERO;
    public DimBlockPos boundPos;
    public int direction;
    public boolean isBuildFromCut = false;

    public ServerBuildList(Level level, ArrayList<StatePos> statePosList, byte renderType, UUID playerUUID, boolean needItems, boolean returnItems, UUID buildUUID, ItemStack gadget, BuildType buildType, boolean dropContents, BlockPos lookingAt, DimBlockPos boundPos, int direction) {
        this.level = level;
        this.statePosList = statePosList;
        this.renderType = renderType;
        this.playerUUID = playerUUID;
        this.originalSize = statePosList.size();
        this.needItems = needItems;
        this.buildUUID = buildUUID;
        this.returnItems = returnItems;
        this.gadget = gadget.copy();
        this.buildType = buildType;
        this.dropContents = dropContents;
        this.lookingAt = lookingAt;
        this.boundPos = boundPos;
        this.direction = direction;
    }

    public void addToBuiltList(StatePos statePos) {
        this.actuallyBuildList.add(statePos);
    }

    public void updateActuallyBuiltList(StatePos statePos) {
        for (StatePos entry : actuallyBuildList) {
            if (entry.pos.equals(statePos.pos)) {
                entry.state = statePos.state;
                break;
            }
        }
    }

    private CompoundTag getTagForPos(BlockPos pos, boolean peek) {
        CompoundTag compoundTag = new CompoundTag();
        if (teData == null || teData.isEmpty()) return compoundTag;
        BlockPos blockPos = pos.subtract(lookingAt);
        Iterator<TagPos> iterator = teData.iterator();
        while (iterator.hasNext()) {
            TagPos data = iterator.next();
            if (data.pos.equals(blockPos)) {
                compoundTag = data.tag;
                if (!peek)
                    iterator.remove();
                break;
            }
        }
        return compoundTag;
    }

    public CompoundTag getTagForPos(BlockPos pos) {
        return getTagForPos(pos, false);
    }

    public CompoundTag peekTagForPos(BlockPos pos) {
        return getTagForPos(pos, true);
    }

    public Direction getDirection() {
        if (direction == -1) return null;
        return Direction.values()[direction];
    }

    public final static class RetryEntry {
        public StatePos statePos;
        public int retryCount = 0;
        public HashMap<Direction, BlockState> adjacentBlockStates = new HashMap<>();

        private RetryEntry(StatePos statePos) {
            this.statePos = statePos;
            this.retryCount = 0;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerBuildList.class);
    private int highestRetryCount = 0;
    private boolean hadRetrySuccess = true;
    /**
     * After canSurvive() checks, mark block for retrying when none of the adjacent blocks are
     * RenderBlockBE (i.e. has already conveerted to the real block state), and the adjacent block
     * states have changed.
     * @param blockPos
     * @param statePos
     */
    public void markForRetry(BlockPos blockPos, StatePos statePos) {
        if (!this.retryList.containsKey(blockPos)) {
            RetryEntry retryEntry = new RetryEntry(statePos);
            LOGGER.debug("[BG2] New Block {} audit:", blockPos, statePos.getTag());
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = blockPos.relative(direction);
                LOGGER.debug("[BG2]  - Adj block at {}: {}", adjacentPos, level.getBlockState(adjacentPos).getBlock().getName());
                retryEntry.adjacentBlockStates.put(direction, level.getBlockState(adjacentPos));
            }
            this.retryList.put(blockPos, retryEntry);
            this.highestRetryCount++;
        }
        LOGGER.warn("[BG2] Block {} at {} failed canSurvive (retry count {})", statePos.state.getBlock().getName(), blockPos, this.retryList.get(blockPos).retryCount);
    }
    public void unMarkRetry(BlockPos blockPos) {
        if (this.retryList.containsKey(blockPos)) {
            LOGGER.debug("[BG2] Block {} at {} succeed canSurvive ! (retry count {})", this.retryList.get(blockPos).statePos.state.getBlock().getName(), blockPos, this.retryList.get(blockPos).retryCount);
            hadRetrySuccess = true;
            this.retryList.remove(blockPos);
        }
        
    }

    public void consumeRetryEntry() {
        if (this.statePosList.isEmpty()) {
            if (!this.retryList.isEmpty()) { //We retry the blocks that initially failed canSurvive checks after all other blocks have been placed
                if (hadRetrySuccess) {
                    Iterator<HashMap.Entry<BlockPos, RetryEntry>> iterator = this.retryList.entrySet().iterator();
                    while (iterator.hasNext()) {
                        HashMap.Entry<BlockPos, RetryEntry> entry = iterator.next();
                        BlockPos blockPos = entry.getKey();
                        RetryEntry retryEntry = entry.getValue();
                        StatePos statePos = retryEntry.statePos;
                        
                        HashMap<Direction, BlockState> newAdjacentBlockStates = new HashMap<>();
                        boolean hasAdjacentRenderBlock = false; // for each adjacent block, check if any are RenderBlockBE
                        LOGGER.debug("[BG2] Block {} audit:", blockPos, statePos.getTag());
                        for (Direction direction : Direction.values()) {
                            BlockPos adjacentPos = blockPos.relative(direction);
                            LOGGER.debug("[BG2]  - Adj block at {}: {}", adjacentPos, level.getBlockState(adjacentPos).getBlock().getName());
                            if (level.getBlockEntity(adjacentPos) instanceof RenderBlockBE) {
                                hasAdjacentRenderBlock = true;
                            }
                            newAdjacentBlockStates.put(direction, level.getBlockState(adjacentPos));
                        }

                        boolean hasAdjacentChanged = !retryEntry.adjacentBlockStates.equals(newAdjacentBlockStates);
                        if (hasAdjacentChanged) {
                            retryEntry.adjacentBlockStates = newAdjacentBlockStates;
                        } else { // TODO remove
                            LOGGER.debug("[BG2] Adjacent block states still match for {}", statePos.pos);
                        }

                        // Retry when:
                        // - none of the adjacent blocks are RenderBlockBE (i.e. has already conveerted to the real block state)
                        // - and the adjacent block states have changed
                        if (!hasAdjacentRenderBlock) {
                            // Increment retry count whenever we attempt to match all adjacent blocks 
                            retryEntry.retryCount++;
                            int maxRetries = this.highestRetryCount; // TODO rmv
                            if (retryEntry.retryCount > maxRetries) {
                                LOGGER.warn("[BG2] Block {} Exceeded max retries: {}", blockPos, retryEntry.retryCount);
                                //iterator.remove();
                                //continue;
                            }
                            if (hasAdjacentChanged) {
                                this.statePosList.add(statePos);
                                hadRetrySuccess = false;
                                LOGGER.debug("[BG2] Retrying block at {}: {}", blockPos, statePos.getTag());
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Had no success the previous time, not retrying again...");
                    this.retryList.clear();
                }
            }
        }
    }
}
