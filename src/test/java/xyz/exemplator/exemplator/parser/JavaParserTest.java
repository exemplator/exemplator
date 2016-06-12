package xyz.exemplator.exemplator.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JavaParserTest {

    @Test
    public void sandboxSplit() throws Exception {
        String fully = "org.example.Example";
        int index = fully.lastIndexOf(".");
        String className = fully;
        if (index != -1) {
            if (index == fully.length() - 1) {
                throw new Exception();
            }
            String packageName = fully.substring(0, index);
            if (!packageName.equals("org.example")) {
                throw new Exception();
            }
            className = fully.substring(index + 1, fully.length());
        }
        Function<String, String> test = new Function<String, String>() {
            @Override
            public String apply(String fully) {
                return fully;
            }
        };
        assertEquals(className, "Example");
    }

    @Test
    public void sandboxParse() throws Exception {
        InputStream resourceAsStream = new FileInputStream("./src/test/java/xyz/exemplator/exemplator/parser/ExampleJava.java");
        CompilationUnit cu = com.github.javaparser.JavaParser.parse(resourceAsStream);
        FieldDeclaration node = (FieldDeclaration) cu.getTypes().get(0).getChildrenNodes().get(0);
        FieldDeclaration node2 = (FieldDeclaration) cu.getTypes().get(0).getChildrenNodes().get(1);
        FieldDeclaration node3 = (FieldDeclaration) cu.getTypes().get(0).getChildrenNodes().get(2);
        BlockStmt blockStmt = (BlockStmt) cu.getTypes().get(0).getChildrenNodes().get(4).getChildrenNodes().get(1);
        System.out.println(cu);
    }
}