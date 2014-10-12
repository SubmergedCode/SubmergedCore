package uk.submergedcode.SubmergedCore.commands.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class Completions {

    private Set<String> completions;

    public Completions() {
        completions = Sets.newHashSet();
    }

    public Completions(Iterable<String> completions) {
        completions = Sets.newHashSet(completions);
    }

    public void add(String completion) {
        completions.add(completion);
    }

    public List<String> getCompletions() {
        return ImmutableList.copyOf(completions);
    }
}
