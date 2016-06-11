package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.exemplator.exemplator.parser.Parser;
import xyz.exemplator.exemplator.parser.conditions.ClassCondition;
import xyz.exemplator.exemplator.parser.conditions.Condition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JavaParser implements Parser {
    private static final Logger logger = LoggerFactory.getLogger(JavaParser.class);
    private final CompilationUnit cu;

    public JavaParser(CompilationUnit cu) {
        this.cu = cu;
    }

    public static Optional<Parser> of(InputStream inputStream) {
        CompilationUnit cu;
        try {
            // parse the file
            cu = com.github.javaparser.JavaParser.parse(inputStream);
        } catch (ParseException e) {
            logger.error("Unable to parse input", e);
            return Optional.empty();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Unable to close input", e);
            }
        }
        return Optional.of(new JavaParser(cu));
    }

    public Optional<Integer> test(Condition condition) {
        if (condition instanceof ClassCondition) {
            return testClass((ClassCondition) condition);
        }
        return Optional.empty();
    }

    private Optional<Integer> testClass(ClassCondition classCondition) {
        String fullClassName = classCondition.getFullClassName();
//        cu.getTypes().get(0).
//        splitFullClassName(fullClassName)
//                .map(split -> {
//                    if (split.length == 2) {
//
//                    }
//
//                })
        int index = fullClassName.lastIndexOf(".");
        String className = fullClassName;
        if (index != -1) {
            if (index == fullClassName.length() - 1) {
                logger.debug("illegal classname {}", fullClassName);
                return Optional.empty();
            }
            String packageName = fullClassName.substring(0, index);
            if (!packageName.equals(cu.getPackage().getPackageName())) {
                logger.debug("package name not equal! {} != {}", packageName, cu.getPackage().getPackageName());
                return Optional.empty();
            }
            className = fullClassName.substring(index + 1, fullClassName.length());
        }
        return Optional.empty();
    }

    private Optional<ClassInfo> splitFullClassName(String fullClass) {
        int index = fullClass.lastIndexOf(".");
        if (index != -1) {
            if (index == fullClass.length() - 1) {
                logger.debug("illegal classname {}", fullClass);
                return Optional.empty();
            }
            String packageName = fullClass.substring(0, index);
            String className = fullClass.substring(index + 1, fullClass.length());
            return Optional.of(new ClassInfo(packageName, className));
        } else {
            String[] result = new String[1];
            result[0] = fullClass;
            return Optional.of(new ClassInfo(null, fullClass));
        }
    }

    private List<Integer> getUsages(ClassInfo classInfo, Command<?> command) {
        Predicate<ImportDeclaration> isMentioned = importDeclaration -> {
            if (importDeclaration.isAsterisk()) {
                if (classInfo.getPackageName().isPresent()) {
                    return classInfo.getPackageName().get().startsWith(importDeclaration.getName().toString());
                } else {
                    return true;
                }
            } else {
                if (classInfo.getPackageName().isPresent()) {
                    return importDeclaration.getName().toString().equals(classInfo.toString());
                } else {
                    return importDeclaration.getName().toString().endsWith(classInfo.getClassName());
                }
            }
        };

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

        cu.getTypes().stream();
                //.map()
        return null;
    }

    private List<Integer> checkType(TypeDeclaration type, Command<?> command, Set<String> valid,
                                    boolean imported, boolean staticImported) {
        boolean staticImportedEnabled = command.getIdentifierOnlyValid().test(type, staticImported);
        return null;
    }

    private static class ClassInfo {
        private final String packageName;
        private final String className;

        private ClassInfo(String packageName, String className) {
            this.packageName = packageName;
            this.className = className;
        }

        public Optional<String> getPackageName() {
            return Optional.ofNullable(packageName);
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            if (packageName != null) {
                return packageName + "." + className;
            } else {
                return className;
            }
        }
    }
}
