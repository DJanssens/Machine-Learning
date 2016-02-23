package net.zomis.machlearn.text;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextFeatureBuilder {

    public static final Comparator<Map.Entry<String, Integer>> SORT_BY_VALUE =
        Comparator.<Map.Entry<String, Integer>, Integer>comparing(Map.Entry::getValue)
            .reversed();

    private final int nGrams;
    private final Map<String, Integer> counts;
    private final Predicate<String> featureFilter;

    public TextFeatureBuilder(int nGrams, Predicate<String> featureFilter) {
        if (nGrams < 1) {
            throw new IllegalArgumentException("nGrams must be positive, was " + nGrams);
        }
        this.nGrams = nGrams;
        this.counts = new HashMap<>();
        this.featureFilter = featureFilter;
    }

    public void add(String processed) {
        List<String> sections = Arrays.asList(processed.split(" "));
        sections = sections.stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
        for (int i = 0; i <= sections.size() - nGrams; i++) {
            List<String> values = sections.subList(i, i + nGrams);
            String value = String.join(" ", values).trim();
            if (value.isEmpty()) {
                continue;
            }
            if (featureFilter.test(value)) {
                counts.merge(value, 1, Integer::sum);
            }
        }
    }

    public TextFeatureMapper mapper(int maxLimit) {
        String[] features = counts.entrySet().stream()
            .sorted(SORT_BY_VALUE)
            .limit(maxLimit).map(Map.Entry::getKey)
            .collect(Collectors.toList())
            .toArray(new String[maxLimit]);
        return new TextFeatureMapper(features);
    }

    public Map<String, Integer> getCounts() {
        return new HashMap<>(counts);
    }
}
