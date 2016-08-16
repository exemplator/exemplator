package xyz.exemplator.exemplator.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.function.Function;

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
        test.add(null);
        System.out.println(list);
        for (String test : list) {
            System.out.println(test);
        }

        try {
            InputStream in = new FileInputStream("test");
            in.read();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        try (InputStream in = new FileInputStream("test")) {
            in.read();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        list.forEach(System.out::println);

        list.stream()
                .forEach(test -> {
                    System.out.println(test);
                });

        Function<String, String> whaat = s -> s;
        Arrays.sort(new String[]{"a", "b"});
        java.util.Arrays.sort(new String[]{"a", "b"});
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
