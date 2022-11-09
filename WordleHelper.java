package wordle;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.function.*;
import java.util.stream.*;

// @author Mouli, 2022/01/30

public class WordleHelper {
    public static void main(String[] args) {

        // green string length must be 5
        String green = "A....";

        // yellow list size must be 5
        List<String> yellow =
                Arrays.asList("", "L", "", "", "P");

        // no restriction in length
        String grey = "PP";


        Wordle.of(green, yellow, grey).findClosest();

    }
}


class Wordle {
    private final String green;
    private final List<String> yellow;
    private final String grey;

    // get Dictionary
    private final List<String> dict = Dictionary.getDict();

    // private constructor
    private Wordle(String green, List<String> yellow, String grey) {
        // green string to uppercase
        this.green = green.toUpperCase();

        // yellow list to uppercase
        yellow.replaceAll(String::toUpperCase);
        this.yellow = yellow;

        // grey string to uppercase
        this.grey = grey.toUpperCase();
    }


    // use static method to initialise Wordle
    public static Wordle of(String green, List<String> yellow, String grey) {
        return new Wordle(green, yellow, grey);
    }


    // find the closest match
    public void findClosest() {
        filterGrey();
        filterYellow();
        filterGreen();
    }

    // remove words containing `grey` letters
    // from dictionary
    private void filterGrey() {

        // convert grey string to char list
        var greys = grey.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        // map each char of greys with their frequencies
        var freqMap = greys.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()));

        // loop the green string
        // if grey char list contains char from
        // green string then increment value of
        // the key of freqMap by 1 each time
        IntStream.range(0, green.length())
                .filter(i -> greys.contains(green.charAt(i)))
                .forEach(i -> freqMap.merge(
                        green.charAt(i), 1L, Long::sum));

        // remove those words from dictionary which
        // contains grey letters of given frequencies
        freqMap.forEach((key, value) ->
                dict.removeIf(word ->
                        word.matches(
                                (".*"+key).repeat(value.intValue()) +".*")));
    }


    private void filterYellow() {
        // only extract those words that contains
        // yellow letters
        yellow.forEach(s ->
                dict.removeIf(str -> !str.contains(s)));

        // remove those words from Dictionary that
        // contains yellow letters in same position
        IntStream.range(0, yellow.size())
                .<Predicate<? super String>>
                        mapToObj(i -> s ->
                        String.valueOf(s.charAt(i))
                                .equals(yellow.get(i))
                )
                .forEach(dict::removeIf);
    }


    private void filterGreen() {
        // check for words that contains `green`
        // letters in given position
        dict.forEach(word ->
                Map.of(green, word)
                        .entrySet().stream()
                        .filter(e ->
                                IntStream.range(0, e.getKey().length())
                                        .noneMatch(i ->
                                                e.getKey().charAt(i) != '.'
                                                && ( e.getKey().charAt(i)
                                                     != e.getValue().charAt(i))
                                        )
                        )
                        .map(Map.Entry::getValue)
                        .forEach(System.out::println));
    }
}

interface Dictionary {
    static List<String> getDict() {

        try(var dictIs = new URL(
                // dictionary url
                 "https://raw.githubusercontent.com/seanpatlan/wordle-words/main/valid-words.csv").openStream()) {

            return
                    Pattern.compile("\n")
                            .splitAsStream(
                                    new String(dictIs.readAllBytes()))
                            .map(s ->
                                    // replacing non alphabets
                                    s.replaceAll("[^a-zA-Z]", "")
                                            // making words uppercase
                                            .toUpperCase())
                            // extracting only those words from
                            // dictionary that are of length 5
                            .filter(s -> s.length() == 5)
                            .distinct()
                            .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }
}

