package com.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JavaParserUtil {

    public static JavaParser parser;

    private static Map<String, CompilationUnit> unitMap = new HashMap();

    private static CompilationUnit getCompilationUnitByClassName(String className) {
        return unitMap.computeIfAbsent(className, func -> {
            CompilationUnit cu = new CompilationUnit();
            cu.addClass(className);
            return cu;
        });
    }

    public static JavaParser getParser(){
        if(parser == null) {
            return new JavaParser();
        }
        return parser;
    }

    public static Optional<ClassOrInterfaceDeclaration> getClassBuilder(String className) {
        return getCompilationUnitByClassName(className).getClassByName(className);
    }

    public static void addConstructor(Map<String, String> params, String className) {
        try {
            Optional<ClassOrInterfaceDeclaration> builder = getClassBuilder(className);
            builder.ifPresent(classBuilder -> {
                ConstructorDeclaration constructorBuilder = new ConstructorDeclaration();
                constructorBuilder.setName(classBuilder.getName());
                constructorBuilder.setModifiers(Modifier.publicModifier().getKeyword());
                BlockStmt blockStmt = new BlockStmt();
                constructorBuilder.setBody(blockStmt);
                classBuilder.getMembers().add(constructorBuilder);

                params.forEach((type, colName) -> {
                    classBuilder.getFieldByName(colName).ifPresentOrElse(x -> {
                        System.out.println(String.format("%s already exists !!!", colName));
                    }, () -> classBuilder.addField(type, colName, Modifier.publicModifier().getKeyword()));
                    constructorBuilder.addParameter(type, colName);
                    blockStmt.addStatement(new AssignExpr(
                            new FieldAccessExpr(new ThisExpr(), colName), new NameExpr(colName),
                            AssignExpr.Operator.ASSIGN));
                });
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addField(Map<String, String> fields, String className) {
        try {
            Optional<ClassOrInterfaceDeclaration> builder = getClassBuilder(className);
            builder.ifPresent(classBuilder -> {
                fields.forEach((type, colName) -> {
                    classBuilder.getFieldByName(colName).ifPresentOrElse(x -> {
                        System.out.println(String.format("%s already exists !!!", colName));
                    }, () -> {
                        classBuilder.addField(type, colName, Modifier.publicModifier().getKeyword());
                    });
                });
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getContent(String className) {
        return unitMap.containsKey(className) ? unitMap.get(className).toString() : "";
    }

    public static void addPackage(String packageName, String className){
        getCompilationUnitByClassName(className).setPackageDeclaration(packageName);
    }

    public static void addImports(Set<String> imports, String className){
        NodeList<ImportDeclaration> nodeList = new NodeList<>();
        imports.forEach(imp -> {
            nodeList.add(new ImportDeclaration(imp, false, false));
        });
        getCompilationUnitByClassName(className).setImports(nodeList);
    }

    public static MethodDeclaration createMethod(String methodName, String className) {
        if (getClassBuilder(className).isPresent()) {
            return getClassBuilder(className).get().addMethod(methodName, Modifier.publicModifier().getKeyword());
        }
        return null;
    }

    public static ExpressionStmt createInstance(Class<?> instance, String referenceVariable) {
        ExpressionStmt statement = new ExpressionStmt();
        ObjectCreationExpr object = new ObjectCreationExpr();
        object.setType(instance);
        statement.setExpression(new AssignExpr(
                new VariableDeclarationExpr(object.getType(), referenceVariable),
                object,
                AssignExpr.Operator.ASSIGN));
        return statement;
    }

    public static MethodCallExpr accessMethod(String accessorName, String method1, String method2, List<Expression> arguments) {
        MethodCallExpr methodCallExpr = StringUtils.isBlank(method1) ? new MethodCallExpr(new NameExpr(accessorName), method2)
                : new MethodCallExpr(new FieldAccessExpr(new NameExpr(accessorName), method1 + "()"), method2);

        if (arguments != null) {
            arguments.forEach(exp -> {
                methodCallExpr.addArgument(exp);
            });
        }
        return methodCallExpr;
    }
}
