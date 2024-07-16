package dev.enjarai.trickster.spell;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The pattern indexes
 *     1
 *   0   2
 * 3   4   5
 *   6   7
 *     8
 */
public record Pattern(List<PatternEntry> entries) {
    public static final Codec<Pattern> CODEC = PatternEntry.CODEC
            .listOf(0, Integer.MAX_VALUE).xmap(Pattern::new, Pattern::entries);
    public static final Pattern EMPTY = Pattern.of();

    public static Pattern from(List<Byte> pattern) {
        var list = new ArrayList<PatternEntry>();
        Byte last = null;
        for (var current : pattern) {
            if (last != null) {
                if (last < current) {
                    list.add(new PatternEntry(last, current));
                } else {
                    list.add(new PatternEntry(current, last));
                }
            }
            last = current;
        }
        list.sort(PatternEntry::compareTo);
        return new Pattern(ImmutableList.copyOf(list));
    }

    public static Pattern of(int... pattern) {
        return from(Stream.of(ArrayUtils.toObject(pattern)).map(Integer::byteValue).toList());
    }

    public boolean isEmpty() {
        return entries().isEmpty();
    }

    public boolean contains(int point) {
        byte realPoint = (byte) point;
        for (PatternEntry entry : entries) {
            if (entry.p1 == realPoint || entry.p2 == realPoint) {
                return true;
            }
        }
        return false;
    }

    public record PatternEntry(byte p1, byte p2) implements Comparable<PatternEntry> {
        public static final Codec<PatternEntry> CODEC = Codec.BYTE.listOf(2, 2)
                .xmap(list -> new PatternEntry(list.getFirst(), list.getLast()), entry -> List.of(entry.p1, entry.p2));

        @Override
        public int compareTo(@NotNull Pattern.PatternEntry o) {
            var p1Compare = Integer.compare(p1, o.p1);
            return p1Compare == 0 ? Integer.compare(p2, o.p2) : p1Compare;
        }
    }
}
