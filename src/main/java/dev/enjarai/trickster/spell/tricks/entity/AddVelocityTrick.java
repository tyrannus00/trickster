package dev.enjarai.trickster.spell.tricks.entity;

import dev.enjarai.trickster.spell.*;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import dev.enjarai.trickster.spell.tricks.Trick;
import dev.enjarai.trickster.spell.tricks.blunder.BlunderException;
import dev.enjarai.trickster.spell.tricks.blunder.UnknownEntityBlunder;

import java.util.List;

public class AddVelocityTrick extends Trick {
    public AddVelocityTrick() {
        super(Pattern.of(4, 6, 0, 1, 2, 8, 4));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var target = expectInput(fragments, FragmentType.ENTITY, 0)
                .getEntity(ctx)
                .orElseThrow(() -> new UnknownEntityBlunder(this));

        fragments = tryWard(ctx, target, fragments);

        var velocity = expectInput(fragments, FragmentType.VECTOR, 1);
        var lengthSquared = velocity.vector().lengthSquared();
        ctx.useMana(this, (float) lengthSquared * 2f);
        target.addVelocity(velocity.vector().x(), velocity.vector().y(), velocity.vector().z());
        target.velocityModified = true;
        ctx.setWorldAffected();

        return VoidFragment.INSTANCE;
    }
}
