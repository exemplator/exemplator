package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import xyz.exemplator.exemplator.parser.Selection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author LeanderK
 * @version 1.0
 */
public class UsageFinder {
    private final CompilationUnit cu;
    private final TypeDeclaration type;
    private final Command command;
    private final boolean imported;
    private final boolean identifierOnly;
    private final List<QualifiedNameExpr> outerQualifiedNameExprs;

    public UsageFinder(CompilationUnit cu, TypeDeclaration type, Command command, boolean imported, boolean identifierOnly, List<QualifiedNameExpr> outerQualifiedNameExprs) {
        this.cu = cu;
        this.type = type;
        this.command = command;
        this.outerQualifiedNameExprs = outerQualifiedNameExprs;
        this.imported = imported;
        this.identifierOnly = identifierOnly;
    }

    List<Selection> checkTypeForUsages() {
        //TODO check for extends or static imports!
//        boolean identifierOnlyEnabled = command.getIdentifierOnlyValid().test(type, identifierOnly);
//        boolean identifierOnlyEnabled = type.
        boolean identifierOnlyEnabled = false;
        Set<LeftQualifier> fieldQualifiers = new HashSet<>();
        List<Selection> usages = new ArrayList<>();
        if (command.getClassName().isPresent()) {
            for (Node node : type.getChildrenNodes()) {
                if (node instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) node;
                    usages.addAll(handleArgs(field.getVariables(), fieldQualifiers, new HashSet<>(), identifierOnlyEnabled, new HashSet<>(), new HashSet<>()));

                    fieldQualifiers.addAll(getValidFieldDeclarations(field,
                                    command.getClassName().get(), command.getPackageName().orElse(null)));
                }
            }
        }
        List<Selection> selections = type.getChildrenNodes().stream()
                .filter(node -> node instanceof MethodDeclaration)
                .map(node -> (MethodDeclaration) node)
                .flatMap(node -> {
                    List<Parameter> parameters = node.getParameters();
                    HashSet<LeftQualifier> methodSet = new HashSet<>();
                    methodSet.addAll(fieldQualifiers);
                    if (command.getClassName().isPresent()) {
                        methodSet.addAll(parameters.stream()
                                .filter(parameter -> checkType(parameter.getType(), command.getClassName().get(), command.getPackageName().orElse(null), imported))
                                .map(parameter -> new LeftQualifier(true, parameter.getId().getName()))
                                .collect(Collectors.toList()));
                    }

                    return getUsages(node, methodSet, identifierOnlyEnabled).stream();
                })
                .collect(Collectors.toList());
        usages.addAll(selections);
        return usages;
    }

    private List<Selection> getUsages(MethodDeclaration method, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled) {
        return method.getChildrenNodes().stream()
                .filter(node -> node instanceof BlockStmt)
                .map(node -> (BlockStmt) node)
                .flatMap(node -> getUsagesForBlock(node, fieldQualifiers, identifierOnlyEnabled, new HashSet<>()).stream())
                .collect(Collectors.toList());
    }

    private List<Selection> getUsagesForBlock(BlockStmt stmt, Set<LeftQualifier> fieldQualifiers,
                                              boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars) {
        return getUsagesForBlock(stmt, fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, new HashSet<>(), new HashSet<>());
    }

    private List<Selection> getUsagesForBlock(BlockStmt stmt, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars,  Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking) {
        List<Selection> usages = new ArrayList<>();
        for (Statement statement : stmt.getStmts()) {
            if (statement instanceof ExpressionStmt) {
                usages.addAll(handleMethodExpressionStmt(fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking, (ExpressionStmt) statement));
            } else if (statement instanceof ForeachStmt) {
                ForeachStmt foreachStmt = (ForeachStmt) statement;
                Statement body = foreachStmt.getBody();
                if (body != null && body instanceof BlockStmt) {
                    usages.addAll(getUsagesForBlock((BlockStmt) body, fieldQualifiers, identifierOnlyEnabled,
                            inheritedLocalVars, localVars, localVarsBlocking));
                }
            } else if (statement instanceof IfStmt) {
                usages.addAll(getSelectionsFromIf((IfStmt) statement, fieldQualifiers, identifierOnlyEnabled,
                        inheritedLocalVars, localVars, localVarsBlocking));
            } else if (statement instanceof TryStmt) {
                TryStmt tryStmt = (TryStmt) statement;
                HashSet<LeftQualifier> newParent = new HashSet<>();
                newParent.addAll(fieldQualifiers);
                newParent.addAll(localVars);
                HashSet<LeftQualifier> local = new HashSet<>();
                tryStmt.getResources()
                        .forEach(declr -> handleVariableDeclarationExpr(newParent, identifierOnlyEnabled, local, localVarsBlocking, usages, inheritedLocalVars, declr));
                BlockStmt tryBlock = tryStmt.getTryBlock();
                if (tryBlock != null) {
                    usages.addAll(getUsagesForBlock(tryBlock, fieldQualifiers, identifierOnlyEnabled, localVars, local, localVarsBlocking));
                }

                tryStmt.getCatchs().forEach(catchClause -> {
                    getUsagesForBlock(catchClause.getCatchBlock(), fieldQualifiers, identifierOnlyEnabled, localVars, local, localVarsBlocking);
                });
                BlockStmt finallyBlock = tryStmt.getFinallyBlock();
                if (finallyBlock != null) {
                    usages.addAll(getUsagesForBlock(finallyBlock, fieldQualifiers, identifierOnlyEnabled, localVars, local, localVarsBlocking));
                }
            } else if (statement instanceof ReturnStmt) {
                Expression expression = ((ReturnStmt) statement).getExpr();
                usages.addAll(handleMethodExpression(fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking, expression));
            }
        }
        return usages;
    }

    private List<Selection> handleMethodExpressionStmt(Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars, Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking, ExpressionStmt statement) {
        return handleMethodExpression(fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking, statement.getExpression());
    }

    private List<Selection> handleMethodExpression(Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars, Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking, Expression expression) {
        List<Selection> usages = new ArrayList<>();
        if (expression instanceof VariableDeclarationExpr) {
            handleVariableDeclarationExpr(fieldQualifiers, identifierOnlyEnabled, localVars, localVarsBlocking, usages, inheritedLocalVars, (VariableDeclarationExpr) expression);
            return usages;
        } else if (expression instanceof AssignExpr) {
            if (!command.getClassName().isPresent()) {
                return usages;
            }
            String classname = command.getClassName().get();
            AssignExpr declr = (AssignExpr) expression;
            Expression target = declr.getTarget();
            if (target instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr) target;
                LeftQualifier leftQualifier = new LeftQualifier(false, nameExpr.getName());
                if (fieldQualifiers.contains(leftQualifier) || localVars.contains(leftQualifier)) {
                    Expression value = declr.getValue();
                    if (value instanceof ObjectCreationExpr) {
                        ObjectCreationExpr creationExpr = (ObjectCreationExpr) value;
                        ClassOrInterfaceType type = creationExpr.getType();
                        boolean valid = checkType(type, classname, command.getPackageName().orElse(null), imported);
                        if (!valid) {
                            if (fieldQualifiers.contains(leftQualifier)) {
                                fieldQualifiers.remove(leftQualifier);
                            } else if (localVars.contains(leftQualifier)) {
                                localVars.remove(leftQualifier);
                            }
                        }
                    }
                }
            } else if (target instanceof FieldAccessExpr) {
                FieldAccessExpr accessExpr = (FieldAccessExpr) target;
                Expression scope = accessExpr.getScope();
                if (scope instanceof ThisExpr) {
                    Expression value = declr.getValue();
                    if (value instanceof ObjectCreationExpr) {
                        LeftQualifier leftQualifier = new LeftQualifier(false, accessExpr.getField());
                        ObjectCreationExpr creationExpr = (ObjectCreationExpr) value;
                        ClassOrInterfaceType type = creationExpr.getType();
                        boolean valid = checkType(type, classname, command.getPackageName().orElse(null), imported);
                        if (!valid) {
                            if (fieldQualifiers.contains(leftQualifier)) {
                                fieldQualifiers.remove(leftQualifier);
                            }
                        }
                    }
                }
            }
        } else if (expression instanceof MethodCallExpr) {
            Optional<Selection> optional = handleMethodCallExpr((MethodCallExpr) expression, fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking);
            optional.ifPresent(usages::add);
        }

        return usages;
    }

    private Optional<Selection> handleMethodCallExpr(MethodCallExpr expression, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars, Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking) {
        for (Node node : expression.getChildrenNodes()) {
            if (node instanceof MethodCallExpr) {
                return handleMethodCallExpr((MethodCallExpr) node, fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking);
            }
        }

        boolean sameType = true;
        if (command.getClassName().isPresent()) {
            Expression scope = expression.getScope();
            if (scope != null && scope instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr) scope;
                String name = nameExpr.getName();
                sameType = isClassApplicableForMethodCall(fieldQualifiers, inheritedLocalVars, localVars,
                        localVarsBlocking, name);
            } else if (scope != null && scope instanceof FieldAccessExpr) {
                FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) scope;
                Expression fieldScope = fieldAccessExpr.getScope();
                String field = fieldAccessExpr.getField();
                if (fieldScope instanceof ThisExpr) {
                    sameType = isClassApplicableForMethodCall(fieldQualifiers, new HashSet<>(), new HashSet<>(),
                            localVarsBlocking, field);
                } else if (fieldScope instanceof FieldAccessExpr) {
                    Boolean packageMatches = command.getPackageName()
                            .map(packageName -> packageName.equals(fieldScope.toString()))
                            .orElse(true);
                    if (packageMatches) {
                        sameType = isClassApplicableForMethodCall(fieldQualifiers, inheritedLocalVars, localVars,
                                localVarsBlocking, field);
                    } else {
                        sameType = false;
                    }
                } else {
                    sameType = isClassApplicableForMethodCall(fieldQualifiers, inheritedLocalVars, localVars,
                            localVarsBlocking, field);
                }
            } else {
                if (!identifierOnlyEnabled && command.getClassName().isPresent()) {
                    sameType = false;
                }
            }
        }
        if (!sameType) {
            return Optional.empty();
        }
        boolean classnameActive = command.getClassName().isPresent();
        boolean methodActive = command.getMethodName().isPresent();
        Boolean matches = command.getMethodName().map(method -> expression.getName().equals(method))
                .orElse(true);
        boolean validUsage = false;
        if (classnameActive && !methodActive) {
            validUsage = true;
        } else if (methodActive && matches) {
            validUsage = true;
        }
        if (validUsage) {
            return Optional.of(getSelection(expression));
        } else {
            return Optional.empty();
        }
    }

    private void handleVariableDeclarationExpr(Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled,
                                               Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking, List<Selection> usages, Set<LeftQualifier> inheritedLocalVars, VariableDeclarationExpr expression) {
        VariableDeclarationExpr declr = expression;
        usages.addAll(handleArgs(declr.getVars(), localVars, fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVarsBlocking));
        if (!command.getClassName().isPresent()) {
            return;
        }
        String classname = command.getClassName().get();
        Type type = declr.getType();
        boolean valid = checkType(type, classname, command.getPackageName().orElse(null), imported);

        declr.getVars().stream()
                .map(var -> var.getId().toString())
                .map(id -> new LeftQualifier(valid, id))
                .forEach(qual -> {
                    if (qual.isSameType()) {
                        localVars.add(qual);
                    } else if (fieldQualifiers.contains(qual)) {
                        localVarsBlocking.add(qual);
                    }
                });
    }

    private boolean isClassApplicableForMethodCall(Set<LeftQualifier> fieldQualifiers, Set<LeftQualifier> inheritedLocalVars, Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking, String name) {
        LeftQualifier leftQualifier = new LeftQualifier(true, name);
        boolean matchesLeftQual = true;
        if (!localVars.contains(leftQualifier) && !inheritedLocalVars.contains(leftQualifier)) {
            if (!fieldQualifiers.contains(leftQualifier) || localVarsBlocking.contains(leftQualifier)) {
                matchesLeftQual = false;
            }
        }
        if (!matchesLeftQual) {
            if (imported && command.getClassName().isPresent()) {
                return name.equals(command.getClassName().get());
            } else {
                return false;
            }
        }
        return true;
    }

    private List<Selection> getSelectionsFromIf(IfStmt ifStmtParam, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars,  Set<LeftQualifier> localVars, Set<LeftQualifier> localVarsBlocking) {
        List<Selection> selections = new ArrayList<>();
        IfStmt ifStmt = ifStmtParam;
        while (ifStmt != null) {
            Statement thenStmt = ifStmt.getThenStmt();
            if (thenStmt instanceof BlockStmt) {
                selections.addAll(getUsagesForBlock((BlockStmt) thenStmt, fieldQualifiers, identifierOnlyEnabled,
                        inheritedLocalVars, localVars, localVarsBlocking));
            }
            Statement elseStmt = ifStmt.getElseStmt();
            if (elseStmt instanceof BlockStmt) {
                selections.addAll(getUsagesForBlock((BlockStmt) elseStmt, fieldQualifiers, identifierOnlyEnabled,
                        inheritedLocalVars, localVars, localVarsBlocking));
                ifStmt = null;
            } else if (elseStmt instanceof IfStmt) {
                ifStmt = (IfStmt) elseStmt;
            } else {
                ifStmt = null;
            }
        }
        return new ArrayList<>();
    }

    private List<Selection> handleArgs(List<VariableDeclarator> variables, Set<LeftQualifier> localVars, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled, Set<LeftQualifier> inheritedLocalVars, Set<LeftQualifier> localVarsBlocking) {
        return variables.stream()
                .map(VariableDeclarator::getInit)
                .filter(Objects::nonNull)
                .flatMap(init -> {
                    if (init instanceof LambdaExpr) {
                        return handleMaybeLambda(init, fieldQualifiers, identifierOnlyEnabled, imported, localVars).stream();
                    } else if (init instanceof MethodCallExpr) {
                        MethodCallExpr methodCallExpr = (MethodCallExpr) init;
                        return handleMethodCallExpr(methodCallExpr, fieldQualifiers, identifierOnlyEnabled, inheritedLocalVars, localVars, localVarsBlocking)
                                .map(Stream::of)
                                .orElse(Stream.empty());
                    } else {
                       return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Selection> handleMaybeLambda(Expression expression, Set<LeftQualifier> fieldQualifiers, boolean identifierOnlyEnabled,
                                             boolean imported, Set<LeftQualifier> localVars) {
        if (expression instanceof LambdaExpr) {
            LambdaExpr lambda = (LambdaExpr) expression;
            Statement body = lambda.getBody();
            if (body instanceof BlockStmt) {
                return getUsagesForBlock((BlockStmt) body, fieldQualifiers, identifierOnlyEnabled, localVars);
            }
        } else if (expression instanceof MethodReferenceExpr) {
            MethodReferenceExpr method = (MethodReferenceExpr) expression;
            Expression type = method.getScope();
            if (type instanceof TypeExpr) {
                TypeExpr typeExpr = (TypeExpr) type;
                Type realType = typeExpr.getType();
                Boolean classMatches = command.getClassName()
                        .map(className -> checkType(realType, className, command.getPackageName().orElse(null), imported))
                        .orElse(true);
                boolean isMatch = false;
                if (classMatches) {
                    if (command.getMethodName().isPresent()) {
                        isMatch = method.getIdentifier().equals(command.getMethodName().get());
                    } else {
                        isMatch = true;
                    }
                }

                if (isMatch) {
                    List<Selection> selections = new ArrayList<>();
                    selections.add(getSelection(method));
                    return selections;
                }
            }
        }
        return new ArrayList<>();
    }

    private Selection getSelection(Node node) {
        Position begin = new Position(node.getBeginLine(), node.getBeginColumn());
        Position end = new Position(node.getEndLine(), node.getEndColumn());
        return new Selection(begin, end);
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
        } else if (type instanceof ReferenceType) {
            return checkType(((ReferenceType) type).getType(), className, packageName, imported);
        }
        return false;
    }
}

