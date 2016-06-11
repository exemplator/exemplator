package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.M;

/**
 * @author LeanderK
 * @version 1.0
 */
public class UsageFinder {
    private final CompilationUnit cu;
    private final TypeDeclaration type;
    private final Command<?> command;
    private final boolean imported;
    private final boolean identifierOnly;
    private final List<QualifiedNameExpr> outerQualifiedNameExprs;

    public UsageFinder(CompilationUnit cu, TypeDeclaration type, Command<?> command, boolean imported, boolean identifierOnly, List<QualifiedNameExpr> outerQualifiedNameExprs) {
        this.cu = cu;
        this.type = type;
        this.command = command;
        this.outerQualifiedNameExprs = outerQualifiedNameExprs;
        this.imported = imported;
        this.identifierOnly = identifierOnly;
    }

    private List<Integer> checkTypeForUsages() {
        boolean identifierOnlyEnabled = command.getIdentifierOnlyValid().test(type, identifierOnly);
        List<LeftQualifier> fieldQualifiers;
        if (command.getClassName().isPresent()) {
            fieldQualifiers = type.getChildrenNodes().stream()
                    .filter(node -> node instanceof FieldDeclaration)
                    .map(node -> (FieldDeclaration) node)
                    .flatMap(field ->
                            getValidFieldDeclarations(
                                    field,
                                    command.getClassName().get(),
                                    command.getPackageName().orElse(null)
                            ).stream()
                    )
                    .collect(Collectors.toList());
        } else {
            fieldQualifiers = new ArrayList<>();
        }
        return type.getChildrenNodes().stream()
                .filter(node -> node instanceof MethodDeclaration)
                .map(node -> (MethodDeclaration) node)
                .flatMap(node -> getUsages(node, fieldQualifiers, identifierOnlyEnabled).stream())
                .collect(Collectors.toList());
    }

    private List<Integer> getUsages(MethodDeclaration method, List<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled) {
        return new ArrayList<>();
    }

    private List<LeftQualifier> getValidFieldDeclarations(FieldDeclaration fieldDeclaration, String className, String packageName) {
        Type type = fieldDeclaration.getType();
        if (type instanceof ReferenceType) {
            Type fieldType = ((ReferenceType) type).getType();
            boolean valid = checkType(fieldType, className, packageName, imported);
            if (valid) {
                return fieldDeclaration.getVariables().stream()
                        .map(VariableDeclarator::getId)
                        .map(VariableDeclaratorId::getName)
                        .map(name -> new LeftQualifier(true, name))
                        .collect(Collectors.toList());
            }
        }

        return fieldDeclaration.getVariables().stream()
                    .filter(variable -> {
                        Expression init = variable.getInit();
                        if (init != null && init instanceof ObjectCreationExpr) {
                            return checkType(((ObjectCreationExpr) init).getType(), className, packageName, imported);
                        }
                        return false;
                    })
                    .map(VariableDeclarator::getId)
                    .map(VariableDeclaratorId::getName)
                    .map(name -> new LeftQualifier(false, name))
                    .collect(Collectors.toList());
    }

    private boolean checkType(Type type, String className, String packageName, boolean imported) {
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType field = (ClassOrInterfaceType) type;
            ClassOrInterfaceType scope = field.getScope();
            boolean scoped = false;
            if (scope != null && packageName != null) {
                if (packageName.equals(scope.toString())) {
                    scoped = true;
                }
            }
            if (!scoped && !imported) {
                return false;
            }
            String typeName = field.getName();
            if (typeName.equals(className)) {
                return true;
            }
        } else if (type instanceof PrimitiveType) {
            String typeName = ((PrimitiveType) type).getType().name();
            if (typeName.equals(className)) {
                return true;
            }
        }
        return false;
    }


}

