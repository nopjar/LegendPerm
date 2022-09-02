package net.playlegend.misc;

import java.util.Comparator;
import net.playlegend.domain.Group;
import org.jetbrains.annotations.NotNull;

public class GroupWeightComparator implements Comparator<Group> {

    @Override
    public int compare(@NotNull Group o1, @NotNull Group o2) {
        return Integer.compare(o2.getWeight(), o1.getWeight());
    }

}
