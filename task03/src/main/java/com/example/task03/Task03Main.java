package com.example.task03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class Task03Main {

    private static final String DICTIONARY = "/singular.txt";

    private static final Charset DICTIONARY_CHARSET = Charset.forName("windows-1251");
    private static final Pattern RUSSIAN_WORD = Pattern.compile("[а-яё]{3,}");

    public static void main(String[] args) throws IOException {

        try (InputStream dictionary = Task03Main.class.getResourceAsStream(DICTIONARY)) {
            List<Set<String>> anagrams = findAnagrams(dictionary, DICTIONARY_CHARSET);
            for (Set<String> anagram : anagrams) {
                System.out.println(anagram);
            }
        }

    }

    public static List<Set<String>> findAnagrams(InputStream inputStream, Charset charset) {
        Locale russian = Locale.forLanguageTag("ru");
        Comparator<String> alphabeticalOrder = Collator.getInstance(russian)::compare;
        alphabeticalOrder = alphabeticalOrder.thenComparing(Comparator.naturalOrder());
        Map<String, NavigableSet<String>> groups = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.toLowerCase(russian);
                if (!RUSSIAN_WORD.matcher(word).matches()) {
                    continue;
                }
                char[] letters = word.toCharArray();
                Arrays.sort(letters);
                String key = new String(letters);
                if (!groups.containsKey(key)) {
                    groups.put(key, new TreeSet<>(alphabeticalOrder));
                }
                groups.get(key).add(word);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<NavigableSet<String>> anagrams = new ArrayList<>();
        for (NavigableSet<String> group : groups.values()) {
            if (group.size() >= 2) {
                anagrams.add(group);
            }
        }
        anagrams.sort(Comparator.comparing(NavigableSet::getFirst, alphabeticalOrder));
        return new ArrayList<>(anagrams);
    }
}
