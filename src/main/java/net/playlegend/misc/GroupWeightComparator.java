package net.playlegend.misc;

import java.util.Comparator;
import net.playlegend.domain.Group;
import org.jetbrains.annotations.NotNull;

public class GroupWeightComparator implements Comparator<Group> {

    @Override
    public int compare(@NotNull Group o1, @NotNull Group o2) {
        // should order groups by their weight desc (eg: 100, 80, 10)
        return Integer.compare(o2.getWeight(), o1.getWeight());
    }

}
