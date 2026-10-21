package com.hiresphere.company.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/** Produces URL-safe, unique slugs from a company name. */
final class SlugGenerator {

    private SlugGenerator() {
    }

    static String generate(String input, Predicate<String> exists) {
        String base = slugify(input);
        if (base.isBlank()) {
            base = "company";
        }
        String candidate = base;
        int suffix = 2;
        while (exists.test(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
