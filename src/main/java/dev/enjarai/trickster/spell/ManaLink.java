package dev.enjarai.trickster.spell;

import dev.enjarai.trickster.cca.ManaComponent;
import dev.enjarai.trickster.cca.ModEntityCumponents;
import dev.enjarai.trickster.spell.tricks.Trick;
import dev.enjarai.trickster.spell.tricks.blunder.BlunderException;
import dev.enjarai.trickster.spell.tricks.blunder.NotEnoughManaBlunder;
import net.minecraft.entity.LivingEntity;

public class ManaLink {
    public final ManaPool owner;
    public final LivingEntity source;
    public final ManaComponent manaPool;
    public final float taxRatio;
    private float availableMana;

    public ManaLink(ManaPool owner, LivingEntity source, float ownerHealth, float availableMana) {
        this.owner = owner;
        this.source = source;
        this.manaPool = ModEntityCumponents.MANA.get(source);
        this.taxRatio = ownerHealth / source.getHealth();
        this.availableMana = availableMana;
    }

    public float useMana(Trick trickSource, float amount) throws BlunderException {
        if (!owner.decrease(amount / taxRatio))
            throw new NotEnoughManaBlunder(trickSource, amount);

        float oldMana = manaPool.get();
        float result = availableMana;

        if (amount > availableMana) {

            if (!manaPool.decrease(availableMana))
                availableMana -= oldMana;
            else
                availableMana = 0;
        } else {
            if (!manaPool.decrease(amount))
                availableMana -= oldMana;
            else
                availableMana -= amount;
        }

        return result - availableMana;
    }

    public float getAvailable() {
        return availableMana;
    }
}
