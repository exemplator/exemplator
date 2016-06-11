package xyz.exemplator.exemplator;

import org.apache.http.HttpException;
import xyz.exemplator.exemplator.data.CodeSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        CodeSearch codeSearch = new CodeSearch();
        try {
            List<String> list = new ArrayList<>();
            list.add("reverse");
            list.add("test");

            codeSearch.fetch(list, 1);
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }
}
