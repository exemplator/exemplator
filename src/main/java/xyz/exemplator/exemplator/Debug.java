package xyz.exemplator.exemplator;

import org.apache.http.HttpException;
import xyz.exemplator.exemplator.data.CodeSearch;

import java.util.ArrayList;
import java.util.List;

public class Debug {
    public static void main(String[] args) {
        CodeSearch codeSearch = new CodeSearch();
        List<String> strings = new ArrayList<>();
        strings.add("reverse");
        strings.add("test");
        try {
            codeSearch.fetch(strings, 1);
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }
}
