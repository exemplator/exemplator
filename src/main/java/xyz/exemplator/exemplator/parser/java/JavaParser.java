package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.comments.CommentsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.exemplator.exemplator.data.CodeSample;
import xyz.exemplator.exemplator.parser.Parser;
import xyz.exemplator.exemplator.parser.Selection;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JavaParser implements Parser {
    private static final Logger logger = LoggerFactory.getLogger(JavaParser.class);
    private final CompilationUnit cu;
    private final CodeSample sample;

    public JavaParser(CompilationUnit cu, CodeSample sample) {
        this.cu = cu;
        this.sample = sample;
    }

    public static Optional<Parser> of(CodeSample sample) {
        CompilationUnit cu;
        try {
            // parse the file
            cu = parse(sample);
        } catch (IllegalStateException | TokenMgrError e) {
            logger.error("Unable to parse input", e);
            return Optional.empty();
        }
        return Optional.of(new JavaParser(cu, sample));
    }

    private static CompilationUnit parse(CodeSample sample) throws IllegalStateException {
        try (InputStream codeInputStream = sample.getCodeInputStream()) {
            CompilationUnit cu = callAst(codeInputStream);
            insertComments(cu, sample.getCode());
            return cu;
        } catch (Exception ioe){
            if (ioe instanceof InvocationTargetException) {
                throw new IllegalStateException(ioe.getCause());
            } else {
                throw new IllegalStateException(ioe.getCause());
            }
        }
    }

    //evil vodoo because we are allocating a lot of memory for the code
    private static CompilationUnit callAst(InputStream inputStream) throws Exception {
        String className = "com.github.javaparser.ASTParser";
        Class c = Class.forName(className);
        Constructor constructor = c.getConstructor(InputStream.class);
        constructor.setAccessible(true);
        Object newInstance = constructor.newInstance(inputStream);
        Method compilationUnit = newInstance.getClass().getMethod("CompilationUnit");
        compilationUnit.setAccessible(true);
        return (CompilationUnit) compilationUnit.invoke(newInstance);
    }

    private static void insertComments(CompilationUnit cu, String code) throws Exception {
        String className = "com.github.javaparser.JavaParser";
        Class c = Class.forName(className);
        Method insertComments = c.getDeclaredMethod("insertComments", CompilationUnit.class, String.class);
        insertComments.setAccessible(true);
        insertComments.invoke(null, cu, code);
    }


    @Override
    public List<Selection> getMatches(Command command) {
        Predicate<ImportDeclaration> isMentioned = importDeclaration -> {
            if (importDeclaration.isAsterisk()) {
                if (command.getPackageName().isPresent()) {
                    return command.getPackageName().get().startsWith(importDeclaration.getName().toString());
                } else {
                    return true;
                }
            } else {
                if (command.getPackageName().isPresent()) {
                    return importDeclaration.getName().toString().equals(command.getPackageName().get() + "." + command.getClassName().get());
                } else if (command.getClassName().isPresent()) {
                    return importDeclaration.getName().toString().endsWith(command.getClassName().get());
                } else {
                    return false;
                }
            }
        };


        List<Integer> occurences = command.getMethodName().map(name -> {
            if (!Objects.equals(name, "new")) {
                Pattern pattern = Pattern.compile("\\.\\s*(<.*>)?\\s*" + name + "\\s*\\(");
                String lines[] = sample.getCode().split("\\r?\\n");
                List<Integer> occurencesTemp = new ArrayList<>();
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (pattern.matcher(line).find()) {
                        occurencesTemp.add(i + 1);
                    }
                }
                return occurencesTemp;
            } else {
                return command.getClassName().map(className -> {
                    Pattern pattern = Pattern.compile("new\\s+" + name);
                    String lines[] = sample.getCode().split("\\r?\\n");
                    List<Integer> occurencesTemp = new ArrayList<>();
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (pattern.matcher(line).find()) {
                            occurencesTemp.add(i + 1);
                        }
                    }
                    return occurencesTemp;
                }).orElseGet(ArrayList::new);
            }
        }).orElseGet(ArrayList::new);

        boolean imported = cu.getImports().stream()
                .filter(importDeclaration -> !importDeclaration.isStatic())
                .filter(isMentioned)
                .findAny()
                .isPresent();

        boolean staticImported = cu.getImports().stream()
                .filter(ImportDeclaration::isStatic)
                .filter(isMentioned)
                .findAny()
                .isPresent();



        return cu.getTypes().stream()
                .map(type -> new UsageFinder(cu, type, command, imported, staticImported, occurences,new ArrayList<>()))
                .map(UsageFinder::checkTypeForUsages)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
