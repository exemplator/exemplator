package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.junit.Test;
import xyz.exemplator.exemplator.parser.Selection;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author LeanderK
 * @version 1.0
 */
public class UsageFinderTest {
    @Test
    public void test() throws Exception {
        InputStream resourceAsStream = new FileInputStream("./src/test/java/xyz/exemplator/exemplator/parser/ExampleJava.java");
        CompilationUnit cu = com.github.javaparser.JavaParser.parse(resourceAsStream);
        TypeDeclaration typeDeclaration = cu.getTypes().get(0);
        Command command = new Command((typeDeclaration1, aBoolean) -> false, "List", "java.util", null);
        UsageFinder usageFinder = new UsageFinder(cu, typeDeclaration, command, true, false, new ArrayList<>());
        List<Selection> selections = usageFinder.checkTypeForUsages();
        System.out.println(selections);
    }
}