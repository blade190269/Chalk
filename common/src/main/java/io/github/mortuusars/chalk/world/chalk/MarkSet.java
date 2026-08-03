package io.github.mortuusars.chalk.world.chalk;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MarkSet {
    public static final Codec<MarkSet> CODEC = Codec.simpleMap(Direction.CODEC, Mark.CODEC, StringRepresentable.keys(Direction.values())).codec()
          .xmap(map -> {
              Mark[] marks = new Mark[6];
              for (Direction direction : Direction.values()) {
                  marks[direction.get3DDataValue()] = map.get(direction);
              }
              return new MarkSet(marks);
          }, set -> {
              Map<Direction, Mark> map = new HashMap<>();

              for (Direction direction : Direction.values()) {
                  @Nullable Mark mark = set.get(direction);
                  if (mark != null) {
                      map.put(direction, mark);
                  }
              }

              return map;
          });

    private final Mark[] marks;

    public MarkSet(Mark[] marks) {
        this.marks = marks;
    }

    public MarkSet() {
        this(new Mark[6]);
    }

    public void forEach(BiConsumer<Direction, Mark> consumer) {
        for (int i = 0; i < 6; i++) {
            @Nullable Mark mark = marks[i];
            if (mark != null) {
                consumer.accept(Direction.from3DDataValue(i), mark);
            }
        }
    }

    public @Nullable Mark get(Direction face) {
        return marks[face.get3DDataValue()];
    }

    public @Nullable Mark get(int index) {
        if (index < 0 || index >= 6) return null;
        return marks[index];
    }

    public void set(Direction face, Mark mark) {
        marks[face.get3DDataValue()] = mark;
    }

    public void remove(Direction face) {
        marks[face.get3DDataValue()] = null;
    }

    public boolean isEmpty() {
        for (Mark mark : marks) {
            if (mark != null) {
                return false;
            }
        }
        return true;
    }

    public Mark[] copyArray() {
        return marks.clone();
    }
}