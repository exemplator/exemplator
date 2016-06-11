package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Set<LeftQualifier> fieldQualifiers;
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
                    .collect(Collectors.toSet());
        } else {
            fieldQualifiers = new HashSet<>();
        }
        return type.getChildrenNodes().stream()
                .filter(node -> node instanceof MethodDeclaration)
                .map(node -> (MethodDeclaration) node)
                .flatMap(node -> getUsages(node, fieldQualifiers, identifierOnlyEnabled).stream())
                .collect(Collectors.toList());
    }

    private List<Integer> getUsages(MethodDeclaration method, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled) {
        method.getChildrenNodes().stream()
                .filter(node -> node instanceof BlockStmt)
                .map(node -> (BlockStmt) node)
                .flatMap(node -> getUsagesForMethod(node, fieldQualifiers, identifierOnlyEnabled).stream());
        return new ArrayList<>();
    }

    private List<Integer> getUsagesForMethod(BlockStmt stmt, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled) {
        Set<LeftQualifier> localVars = new HashSet<>();
        for (Statement statement : stmt.getStmts()) {
            if (statement instanceof ExpressionStmt) {
                Expression expression = ((ExpressionStmt) statement).getExpression();
                if (expression instanceof VariableDeclarationExpr) {
                    if (!command.getClassName().isPresent()) {
                        continue;
                    }
                    String classname = command.getClassName().get();
                    VariableDeclarationExpr declr = (VariableDeclarationExpr) expression;
                    Type type = declr.getType();
                    boolean valid = checkType(type, classname, command.getPackageName().orElse(null), imported);
                    declr.getVars().stream()
                            .map(var -> var.getId().toString())
                            .map(id -> new LeftQualifier(valid, id))
                            .filter(qual -> fieldQualifiers.contains(qual) || localVars.contains(qual))
                            .forEach(qual -> {
                                if (qual.isSameType() && localVars.contains(qual)) {
                                    localVars.remove(qual);
                                } else {
                                    localVars.add(qual);
                                }
                            });
                } else if (expression instanceof AssignExpr) {
                    if (!command.getClassName().isPresent()) {
                        continue;
                    }
                    String classname = command.getClassName().get();
                    AssignExpr declr = (AssignExpr) expression;
                    Expression target = declr.getTarget();
                    if (target instanceof NameExpr) {
                        NameExpr nameExpr = (NameExpr) target;
                        LeftQualifier leftQualifier = new LeftQualifier(false, nameExpr.getName());
                        if (fieldQualifiers.contains(leftQualifier) || localVars.contains(leftQualifier)) {
                            ((AssignExpr) expression).getTarget()
                            boolean valid = checkType(type, classname, command.getPackageName().orElse(null), imported)
                        }
                    } else if (target instanceof FieldAccessExpr) {
                        FieldAccessExpr accessExpr = (FieldAccessExpr) target;
                    }
                }
            }
        }
        return null;
    }

    private Set<LeftQualifier> getValidFieldDeclarations(FieldDeclaration fieldDeclaration, String className, String packageName) {
        Type type = fieldDeclaration.getType();
        if (type instanceof ReferenceType) {
            Type fieldType = ((ReferenceType) type).getType();
            boolean valid = checkType(fieldType, className, packageName, imported);
            if (valid) {
                return fieldDeclaration.getVariables().stream()
                        .map(VariableDeclarator::getId)
                        .map(VariableDeclaratorId::getName)
                        .map(name -> new LeftQualifier(true, name))
                        .collect(Collectors.toSet());
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
                    .collect(Collectors.toSet());
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

