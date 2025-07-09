package com.direwolf20.buildinggadgets2.util;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class LevelReaderWrapper implements LevelReader  {
    private final LevelReader realLevel;

    public LevelReaderWrapper(LevelReader realLevel) {
        this.realLevel = realLevel;
    }

    @Override
    public @Nullable ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
        return realLevel.getChunk(pX, pZ, pRequiredStatus, pNonnull);
    }

    /**
     * @param pChunkX
     * @param pChunkZ
     * @deprecated
     */
    @Override
    public boolean hasChunk(int pChunkX, int pChunkZ) {
        return realLevel.hasChunk(pChunkX, pChunkZ);
    }

    @Override
    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ) {
        return realLevel.getHeight(pHeightmapType, pX, pZ);
    }

    @Override
    public int getSkyDarken() {
        return realLevel.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return realLevel.getBiomeManager();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pPos) {
        return realLevel.getBiome(pPos);
    }

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(AABB pAabb) {
        return realLevel.getBlockStatesIfLoaded(pAabb);
    }

    @Override
    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
        return realLevel.getBlockTint(pBlockPos, pColorResolver);
    }

    @Override
    public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
        return realLevel.getBrightness(pLightType, pBlockPos);
    }

    @Override
    public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
        return realLevel.getRawBrightness(pBlockPos, pAmount);
    }

    @Override
    public boolean canSeeSky(BlockPos pBlockPos) {
        return realLevel.canSeeSky(pBlockPos);
    }

    /**
     * Gets the biome at the given quart positions.
     * Note that the coordinates passed into this method are 1/4 the scale of block coordinates.
     *
     * @param pX
     * @param pY
     * @param pZ
     */
    @Override
    public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ) {
        return realLevel.getNoiseBiome(pX, pY, pZ);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
        return realLevel.getUncachedNoiseBiome(pX, pY, pZ);
    }

    @Override
    public boolean isClientSide() {
        return realLevel.isClientSide();
    }

    /**
     * @deprecated
     */
    @Override
    public int getSeaLevel() {
        return realLevel.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return realLevel.dimensionType();
    }

    @Override
    public int getMinBuildHeight() {
        return realLevel.getMinBuildHeight();
    }

    @Override
    public int getMaxBuildHeight() {
        return realLevel.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return realLevel.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return realLevel.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return realLevel.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pPos) {
        return realLevel.isOutsideBuildHeight(pPos);
    }

    @Override
    public boolean isOutsideBuildHeight(int pY) {
        return realLevel.isOutsideBuildHeight(pY);
    }

    @Override
    public int getSectionIndex(int pY) {
        return realLevel.getSectionIndex(pY);
    }

    @Override
    public int getSectionIndexFromSectionY(int pSectionIndex) {
        return realLevel.getSectionIndexFromSectionY(pSectionIndex);
    }

    @Override
    public int getSectionYFromSectionIndex(int pSectionIndex) {
        return realLevel.getSectionYFromSectionIndex(pSectionIndex);
    }

    @Override
    public int getHeight() {
        return realLevel.getHeight();
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos) {
        return realLevel.getHeightmapPos(pHeightmapType, pPos);
    }

    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     *
     * @param pPos
     */
    @Override
    public boolean isEmptyBlock(BlockPos pPos) {
        return realLevel.isEmptyBlock(pPos);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos pPos) {
        return realLevel.canSeeSkyFromBelowWater(pPos);
    }

    @Override
    public float getPathfindingCostFromLightLevels(BlockPos pPos) {
        return realLevel.getPathfindingCostFromLightLevels(pPos);
    }

    /**
     * @param pPos
     * @deprecated
     */
    @Override
    public float getLightLevelDependentMagicValue(BlockPos pPos) {
        return realLevel.getLightLevelDependentMagicValue(pPos);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pPos) {
        return realLevel.getChunk(pPos);
    }

    @Override
    public ChunkAccess getChunk(int pChunkX, int pChunkZ) {
        return realLevel.getChunk(pChunkX, pChunkZ);
    }

    @Override
    public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus) {
        return realLevel.getChunk(pChunkX, pChunkZ, pRequiredStatus);
    }

    @Override
    public @Nullable BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ) {
        return realLevel.getChunkForCollisions(pChunkX, pChunkZ);
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
        return realLevel.isUnobstructed(pEntity, pShape);
    }

    @Override
    public boolean isUnobstructed(BlockState pState, BlockPos pPos, CollisionContext pContext) {
        return realLevel.isUnobstructed(pState, pPos, pContext);
    }

    @Override
    public boolean isUnobstructed(Entity pEntity) {
        return realLevel.isUnobstructed(pEntity);
    }

    @Override
    public boolean noCollision(AABB pCollisionBox) {
        return realLevel.noCollision(pCollisionBox);
    }

    @Override
    public boolean noCollision(Entity pEntity) {
        return realLevel.noCollision(pEntity);
    }

    @Override
    public boolean noCollision(@Nullable Entity pEntity, AABB pCollisionBox) {
        return realLevel.noCollision(pEntity, pCollisionBox);
    }

    @Override
    public boolean isWaterAt(BlockPos pPos) {
        return realLevel.isWaterAt(pPos);
    }

    /**
     * Checks if any of the blocks within the aabb are liquids.
     *
     * @param pBb
     */
    @Override
    public boolean containsAnyLiquid(AABB pBb) {
        return realLevel.containsAnyLiquid(pBb);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos pPos) {
        return realLevel.getMaxLocalRawBrightness(pPos);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos pPos, int pAmount) {
        return realLevel.getMaxLocalRawBrightness(pPos, pAmount);
    }

    /**
     * @param pX
     * @param pZ
     * @deprecated
     */
    @Override
    public boolean hasChunkAt(int pX, int pZ) {
        return realLevel.hasChunkAt(pX, pZ);
    }

    /**
     * @param pPos
     * @deprecated
     */
    @Override
    public boolean hasChunkAt(BlockPos pPos) {
        return realLevel.hasChunkAt(pPos);
    }

    /**
     * @param center
     * @param range
     * @deprecated
     */
    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return realLevel.isAreaLoaded(center, range);
    }

    @Override
    public boolean hasChunksAt(BlockPos pFrom, BlockPos pTo) {
        return realLevel.hasChunksAt(pFrom, pTo);
    }

    /**
     * @param pFromX
     * @param pFromY
     * @param pFromZ
     * @param pToX
     * @param pToY
     * @param pToZ
     * @deprecated
     */
    @Override
    public boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX, int pToY, int pToZ) {
        return realLevel.hasChunksAt(pFromX, pFromY, pFromZ, pToX, pToY, pToZ);
    }

    /**
     * @param pFromX
     * @param pFromZ
     * @param pToX
     * @param pToZ
     * @deprecated
     */
    @Override
    public boolean hasChunksAt(int pFromX, int pFromZ, int pToX, int pToZ) {
        return realLevel.hasChunksAt(pFromX, pFromZ, pToX, pToZ);
    }

    @Override
    public RegistryAccess registryAccess() {
        return realLevel.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return realLevel.enabledFeatures();
    }

    @Override
    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey) {
        return realLevel.holderLookup(pRegistryKey);
    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        return realLevel.getShade(pDirection, pShade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return realLevel.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return realLevel.getWorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return realLevel.getEntityCollisions(pEntity, pCollisionBox);
    }

    @Override
    public Iterable<VoxelShape> getCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return realLevel.getCollisions(pEntity, pCollisionBox);
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return realLevel.getBlockCollisions(pEntity, pCollisionBox);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(@Nullable Entity pEntity, AABB pBox) {
        return realLevel.collidesWithSuffocatingBlock(pEntity, pBox);
    }

    @Override
    public Optional<BlockPos> findSupportingBlock(Entity pEntity, AABB pBox) {
        return realLevel.findSupportingBlock(pEntity, pBox);
    }

    @Override
    public Optional<Vec3> findFreePosition(@Nullable Entity pEntity, VoxelShape pShape, Vec3 pPos, double pX, double pY, double pZ) {
        return realLevel.findFreePosition(pEntity, pShape, pPos, pX, pY, pZ);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pPos) {
        return realLevel.getBlockEntity(pPos);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pPos, BlockEntityType<T> pBlockEntityType) {
        return realLevel.getBlockEntity(pPos, pBlockEntityType);
    }

    @Override
    public BlockState getBlockState(BlockPos p_45571_) {
        return realLevel.getBlockState(p_45571_);
    }

    @Override
    public FluidState getFluidState(BlockPos pPos) {
        return realLevel.getFluidState(pPos);
    }

    @Override
    public int getLightEmission(BlockPos pPos) {
        return realLevel.getLightEmission(pPos);
    }

    @Override
    public int getMaxLightLevel() {
        return realLevel.getMaxLightLevel();
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB pArea) {
        return realLevel.getBlockStates(pArea);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext pContext) {
        return realLevel.isBlockInLine(pContext);
    }

    /**
     * Checks if there's block between {@code from} and {@code to} of context.
     * This uses the collision shape of provided block.
     *
     * @param pContext
     */
    @Override
    public BlockHitResult clip(ClipContext pContext) {
        return realLevel.clip(pContext);
    }

    @Override
    public @Nullable BlockHitResult clipWithInteractionOverride(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos, VoxelShape pShape, BlockState pState) {
        return realLevel.clipWithInteractionOverride(pStartVec, pEndVec, pPos, pShape, pState);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape pShape, Supplier<VoxelShape> pBelowShapeSupplier) {
        return realLevel.getBlockFloorHeight(pShape, pBelowShapeSupplier);
    }

    @Override
    public double getBlockFloorHeight(BlockPos pPos) {
        return realLevel.getBlockFloorHeight(pPos);
    }

    /**
     * Returns the direct redstone signal emitted from the given position in the given direction.
     *
     * <p>
     * NOTE: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     *
     * @param pPos
     * @param pDirection
     */
    @Override
    public int getDirectSignal(BlockPos pPos, Direction pDirection) {
        return realLevel.getDirectSignal(pPos, pDirection);
    }

    /**
     * Returns the direct redstone signal the given position receives from neighboring blocks.
     *
     * @param pPos
     */
    @Override
    public int getDirectSignalTo(BlockPos pPos) {
        return realLevel.getDirectSignalTo(pPos);
    }

    /**
     * Returns the control signal emitted from the given position in the given direction.
     * If {@code diodesOnly} is {@code true}, this method returns the direct signal emitted if
     * and only if this position is occupied by a diode (i.e. a repeater or comparator).
     * Otherwise, if this position is occupied by a
     * {@linkplain Blocks#REDSTONE_BLOCK redstone block},
     * this method will return the redstone signal emitted by it. If not, this method will
     * return the direct signal emitted from this position in the given direction.
     *
     * <p>
     * NOTE: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     *
     * @param pPos
     * @param pDirection
     * @param pDiodesOnly
     */
    @Override
    public int getControlInputSignal(BlockPos pPos, Direction pDirection, boolean pDiodesOnly) {
        return realLevel.getControlInputSignal(pPos, pDirection, pDiodesOnly);
    }

    /**
     * Returns whether a redstone signal is emitted from the given position in the given direction.
     *
     * <p>
     * NOTE: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     *
     * @param pPos
     * @param pDirection
     */
    @Override
    public boolean hasSignal(BlockPos pPos, Direction pDirection) {
        return realLevel.hasSignal(pPos, pDirection);
    }

    /**
     * Returns the redstone signal emitted from the given position in the given direction.
     * This is the highest value between the signal emitted by the block itself, and the direct signal
     * received from neighboring blocks if the block is a redstone conductor.
     *
     * <p>
     * NOTE: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     *
     * @param pPos
     * @param pDirection
     */
    @Override
    public int getSignal(BlockPos pPos, Direction pDirection) {
        return realLevel.getSignal(pPos, pDirection);
    }

    /**
     * Returns whether the given position receives any redstone signal from neighboring blocks.
     *
     * @param pPos
     */
    @Override
    public boolean hasNeighborSignal(BlockPos pPos) {
        return realLevel.hasNeighborSignal(pPos);
    }

    /**
     * Returns the highest redstone signal the given position receives from neighboring blocks.
     *
     * @param pPos
     */
    @Override
    public int getBestNeighborSignal(BlockPos pPos) {
        return realLevel.getBestNeighborSignal(pPos);
    }

    /**
     * Computes the shade for a given normal.
     * Alternate version of the vanilla method taking in a {@link Direction}.
     *
     * @param normalX
     * @param normalY
     * @param normalZ
     * @param shade
     */
    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return realLevel.getShade(normalX, normalY, normalZ, shade);
    }

    /**
     * Get the {@link BlockEntity} at the given position if it exists.
     * <p>
     * {@link Level#getBlockEntity(BlockPos)} would create a new {@link BlockEntity} if the
     * {@link Block} has one, but it has not been placed in the world yet
     * (This can happen on world load).
     *
     * @param pos
     * @return The BlockEntity at the given position or null if it doesn't exist
     */
    @Override
    public @Nullable BlockEntity getExistingBlockEntity(BlockPos pos) {
        return realLevel.getExistingBlockEntity(pos);
    }

    /**
     * Retrieves the model data manager for this level.
     * This will be {@code null} on a server level.
     */
    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return realLevel.getModelDataManager();
    }
}
