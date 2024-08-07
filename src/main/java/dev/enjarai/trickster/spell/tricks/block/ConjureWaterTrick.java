package dev.enjarai.trickster.spell.tricks.block;

import dev.enjarai.trickster.particle.ModParticles;
import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import dev.enjarai.trickster.spell.tricks.Trick;
import dev.enjarai.trickster.spell.tricks.blunder.BlockOccupiedBlunder;
import dev.enjarai.trickster.spell.tricks.blunder.BlunderException;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ConjureWaterTrick extends Trick {
    public ConjureWaterTrick() {
        super(Pattern.of(3, 0, 4, 2, 5));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var pos = expectInput(fragments, FragmentType.VECTOR, 0);

        var blockPos = pos.toBlockPos();
        var bucket = Items.WATER_BUCKET;
        expectCanBuild(ctx, blockPos);

        if (!(ctx.getWorld().getBlockState(blockPos).isAir()
                || ctx.getWorld().getBlockState(blockPos).getBlock() instanceof Waterloggable
                || ctx.getWorld().getBlockState(blockPos).isOf(Blocks.CAULDRON))
        ) {
            throw new BlockOccupiedBlunder(this);
        }

        var state = ctx.getWorld().getBlockState(blockPos);
        ctx.useMana(this, 15);

        if (state.getBlock() == Blocks.CAULDRON) {
            ctx.getWorld().setBlockState(blockPos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, LeveledCauldronBlock.MAX_LEVEL), 3);
        } else if (!tryPlaceWater(
                ctx.getWorld(),
                blockPos
        ) && bucket instanceof BucketItem) {
            ((BucketItem) bucket).placeFluid(null, ctx.getWorld(), blockPos, null);
        }
        ctx.setWorldAffected();


        var particlePos = blockPos.toCenterPos();
        ctx.getWorld().spawnParticles(
                ModParticles.PROTECTED_BLOCK, particlePos.x, particlePos.y, particlePos.z,
                1, 0, 0, 0, 0
        );

        return VoidFragment.INSTANCE;
    }

    private boolean tryPlaceWater(World world, BlockPos pos) {
        Storage<FluidVariant> target = FluidStorage.SIDED.find(world, pos, Direction.UP);
        if (target == null) {
            return false;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long insertedAmount = target.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
            if (insertedAmount > 0) {
                transaction.commit();
                return true;
            }
        }
        return false;
    }
}
