package xyz.exemplator.exemplator.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.function.Function;

import static com.sun.tools.doclint.Entity.nu;

/**
 * @author LeanderK
 * @version 1.0
 */
public class ExampleJava {
    private final ArrayList<List> test = new ArrayList<>();
    private final int x = 3;
    private List<String> test2;
    private java.util.Set<String> test3;

    public void test() {
        List<String> list = new ArrayList<>();
        list.add("haha");
        test2 = new LinkedList<>();
        this.test2 = new LinkedList<>();
        test2();
        System.out.println(list);
        for (String test : list) {
            System.out.println(test);
        }

        list.forEach(System.out::println);

        list.stream()
                .forEach(test -> {
                    System.out.println(test);
                });

        Function<String, String> whaat = s -> s;
        Arrays.sort(new String[]{"a", "b"});
        this.test2.add("");

        if (list != null) {
            System.out.println(list);
        } else if (list != null) {
            System.out.println(list);
        } else {
            System.out.println(list);
        }
    }

    public void test2() {

    }
}
