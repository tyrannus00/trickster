package dev.enjarai.trickster.spell.tricks.func;

import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.tricks.Trick;
import dev.enjarai.trickster.spell.tricks.blunder.BlunderException;

import java.util.List;

public class ExecuteTrick extends Trick {
    public ExecuteTrick() {
        super(Pattern.of(3, 4, 5, 8, 7, 6, 3, 0, 1, 4, 7));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var executable = expectInput(fragments, FragmentType.SPELL_PART, 0);
        ctx.pushPartGlyph(fragments.subList(1, fragments.size()));
        ctx.pushStackTrace(-2);
        var result = executable.run(ctx);
        ctx.popStackTrace();
        ctx.popPartGlyph();
        return result;
    }
}
