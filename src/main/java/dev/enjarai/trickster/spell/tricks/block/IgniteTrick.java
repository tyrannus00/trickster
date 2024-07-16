package dev.enjarai.trickster.spell.tricks.block;

import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import dev.enjarai.trickster.spell.tricks.Trick;
import dev.enjarai.trickster.spell.tricks.blunder.*;
import net.minecraft.block.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class IgniteTrick extends Trick {
    public IgniteTrick() {
        super(Pattern.of(1, 5, 8, 7, 6, 3, 1));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var input = supposeInput(fragments, 0).orElseThrow(() -> new MissingInputsBlunder(this));

        var targetPos = supposeType(input, FragmentType.VECTOR);
        var targetEntity = supposeType(input, FragmentType.ENTITY);

        if (targetEntity.isPresent()) {
            var entity = targetEntity
                    .get()
                    .getEntity(ctx)
                    .orElseThrow(() -> new UnknownEntityBlunder(this));

            entity.setOnFireFor(5f);
            ctx.setWorldAffected();
            ctx.useMana(this, 5);

            return VoidFragment.INSTANCE;
        } else if (targetPos.isPresent()) {
            var blockPos = targetPos.get().toBlockPos();
            expectCanBuild(ctx, blockPos);

            var caster = ctx.getCaster().orElseThrow(() -> new UnknownEntityBlunder(this));
            var world = ctx.getWorld();
            var blockState = world.getBlockState(blockPos);

            if (!CampfireBlock.canBeLit(blockState) && !CandleBlock.canBeLit(blockState) && !CandleCakeBlock.canBeLit(blockState)) {
                var cantIgnite = true;

                for (var direction : Direction.values()) {
                    if (AbstractFireBlock.canPlaceAt(world, blockPos, direction)) {
                        world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos), 11);
                        world.emitGameEvent(caster, GameEvent.BLOCK_PLACE, blockPos);
                        cantIgnite = false;
                    }
                }

                if (cantIgnite) {
                    throw new BlockOccupiedBlunder(this);
                }
            } else {
                world.setBlockState(blockPos, blockState.with(Properties.LIT, true), 11);
                world.emitGameEvent(caster, GameEvent.BLOCK_CHANGE, blockPos);
            }

            world.playSound(caster, blockPos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            ctx.setWorldAffected();
            ctx.useMana(this, 5);

            return VoidFragment.INSTANCE;
        }

        return VoidFragment.INSTANCE;
    }
}
