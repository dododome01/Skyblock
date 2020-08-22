package com.github.dododome01.worldgen;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import com.github.dododome01.mixins.StructurePieceAccessor;

import java.util.Random;

public class StructureHelper
{
    public static BlockPos getBlockInStructurePiece(StructurePiece piece, int x, int y, int z)
    {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        return new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
    }

    public static BlockState getBlockAt(BlockView blockView, int x, int y, int z, StructurePiece piece)
    {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        int i = access.invokeApplyXTransform(x, z);
        int j = access.invokeApplyYTransform(y);
        int k = access.invokeApplyZTransform(x, z);
        BlockPos blockPos = new BlockPos(i, j, k);
        return !piece.getBoundingBox().contains(blockPos) ? Blocks.AIR.getDefaultState() : blockView.getBlockState(blockPos);
    }

    public static void fillWithOutline(ProtoChunk chunk, int i, int j, int k, int l, int m, int n, BlockState blockState, BlockState inside, boolean bl, StructurePiece piece)
    {
        for (int o = j; o <= m; ++o)
        {
            for (int p = i; p <= l; ++p)
            {
                for (int q = k; q <= n; ++q)
                {
                    if (!bl || !getBlockAt(chunk, p, o, q, piece).isAir())
                    {
                        if (o != j && o != m && p != i && p != l && q != k && q != n)
                        {
                            setBlockInStructure(piece, chunk, inside, p, o, q);
                        }
                        else
                        {
                            setBlockInStructure(piece, chunk, blockState, p, o, q);
                        }
                    }
                }
            }
        }

    }

    public static boolean addChest(ProtoChunk chunk, Random random, int x, int y, int z, Identifier lootTableId, /*@Nullable*/ BlockState block, StructurePiece piece)
    {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
        if (piece.getBoundingBox().contains(pos) && chunk.getBlockState(pos).getBlock() != Blocks.CHEST)
        {
            if (block == null)
            {
                block = StructurePiece.method_14916(chunk, pos, Blocks.CHEST.getDefaultState());
            }

            setBlockInChunk(chunk, pos, block);
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity)
            {
                ((ChestBlockEntity) blockEntity).setLootTable(lootTableId, random.nextLong());
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public static void setBlockInStructure(StructurePiece piece, ProtoChunk chunk, BlockState state, int x, int y, int z)
    {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = getBlockInStructurePiece(piece, x, y, z);
        if (piece.getBoundingBox().contains(pos))
        {
            BlockMirror mirror = access.getMirror();
            if (mirror != BlockMirror.NONE)
                state = state.mirror(mirror);
            BlockRotation rotation = piece.getRotation();
            if (rotation != BlockRotation.NONE)
                state = state.rotate(rotation);

            setBlockInChunk(chunk, pos, state);
        }
    }

    public static void setBlockInChunk(ProtoChunk chunk, BlockPos pos, BlockState state)
    {
        if (chunk.getPos().equals(new ChunkPos(pos)))
        {
            chunk.setBlockState(pos, state, false);
        }
    }

    public static void setBlockEntityInChunk(ProtoChunk chunk, BlockPos pos, CompoundTag tag)
    {
        if (chunk.getPos().equals(new ChunkPos(pos)))
        {
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            System.out.println(tag);
            chunk.addPendingBlockEntityTag(tag);
        }
    }

    public static void fillAirAndLiquidDownwards(ProtoChunk chunk, BlockState blockState, int x, int y, int z, StructurePiece piece)
    {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        int i = access.invokeApplyXTransform(x, z);
        int j = access.invokeApplyYTransform(y);
        int k = access.invokeApplyZTransform(x, z);
        if (piece.getBoundingBox().contains(new BlockPos(i, j, k)))
        {
            while ((chunk.getBlockState(new BlockPos(i, j, k)).isAir() || chunk.getBlockState(new BlockPos(i, j, k)).getMaterial().isLiquid()) && j > 1)
            {
                setBlockInChunk(chunk, new BlockPos(i, j, k), blockState);
                --j;
            }
        }
    }

    public static void generatePillars(ProtoChunk chunk, StructureWorldAccess world, EnderDragonFight fight)
    {
        for (EndSpikeFeature.Spike spike : EndSpikeFeature.getSpikes(world))
        {
            if (spike.isInChunk(new BlockPos(spike.getCenterX(), 45, spike.getCenterZ())))
            {
                PillarHelper.generateSpike(chunk, world, new Random(), spike, fight);
            }
        }
    }

    public static void processStronghold(ProtoChunk chunk, WorldAccess world)
    {
        for (long startPosLong : chunk.getStructureReferences(StructureFeature.STRONGHOLD))
        {
            ChunkPos startPos = new ChunkPos(startPosLong);
            ProtoChunk startChunk = (ProtoChunk) world.getChunk(startPos.x, startPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart stronghold = startChunk.getStructureStart(StructureFeature.STRONGHOLD);
            ChunkPos pos = chunk.getPos();
            if (stronghold != null && stronghold.getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ()))
            {
                for (Object piece : stronghold.getChildren())
                {
                    if (((StructurePiece)piece).getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ()))
                    {
                        if (piece instanceof StrongholdGenerator.PortalRoom)
                            generateStrongholdPortalRoom(chunk, (StrongholdGenerator.PortalRoom) piece, new Random(startPosLong));
                    }
                }
            }
        }
    }

    public static void generateStrongholdPortalRoom(ProtoChunk chunk, StrongholdGenerator.PortalRoom room, Random random)
    {
        BlockState northFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState southFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState eastFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState westFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);

        setBlockInStructure(room, chunk, northFrame, 4, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 5, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 6, 3, 8);
        setBlockInStructure(room, chunk, southFrame, 4, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 5, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 6, 3, 12);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 9);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 10);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 11);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 9);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 10);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 11);
    }
}