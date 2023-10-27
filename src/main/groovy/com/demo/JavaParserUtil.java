package com.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnknownType;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.ivy.util.CollectionUtils;

import javax.swing.text.html.Option;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JavaParserUtil {

    public static JavaParser parser;

    private static Map<String, CompilationUnit> unitMap = new HashMap();

    private static final String STRIP_SEMICOLON = "\\h;";
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
        return unitMap.containsKey(className) ? unitMap.get(className).toString().replaceAll(STRIP_SEMICOLON, "") : "";
    }

    public static void addPackage(String packageName, String className){
        getCompilationUnitByClassName(className).setPackageDeclaration(packageName);
    }

    public static void addImports(Set<String> imports, String className){
        Set<String> defaultImports = getCompilationUnitByClassName(className).getImports().stream().map(id -> id.getName().asString()).collect(Collectors.toSet());
        defaultImports.addAll(imports);

        NodeList<ImportDeclaration> nodeList = new NodeList<>();
        defaultImports.forEach(imp -> {
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

    public static ExpressionStmt createInstance(Class<?> instance, Class<?> typeGeneric, String referenceVariable, Expression arg) {
        ExpressionStmt statement = new ExpressionStmt();
        ObjectCreationExpr object = new ObjectCreationExpr();
        object.setType(instance);

        AssignExpr assignExpr = new AssignExpr();
        statement.setExpression(assignExpr);
        assignExpr.setOperator(AssignExpr.Operator.ASSIGN);

        if (typeGeneric != null) {
            String target = String.format("%s<%s> %s", instance.getSimpleName(), typeGeneric.getSimpleName(), referenceVariable);
            assignExpr.setTarget(new NameExpr(target));
            assignExpr.setValue(object);
        } else {
            assignExpr.setTarget(new VariableDeclarationExpr(object.getType(), referenceVariable));
            assignExpr.setValue(object);
        }

        if(arg != null) {
            NodeList<Expression> nodeList = new NodeList<>();
            nodeList.add(arg);
            object.setArguments(nodeList);
        }
        return statement;
    }

    public static Expression accessField(Expression arg1, String arg2){
        return new FieldAccessExpr(arg1, arg2);
    }

    public static Expression convertStringLiteralExpToString(List<StringLiteralExpr> strExpList){
        return new NameExpr(strExpList.stream().map(StringLiteralExpr::toString).collect(Collectors.joining(",")));
    }

    public static Expression accessMethod(Map<String, List<Expression>> map, String accessorName) {
        MethodCallExpr expr = new MethodCallExpr();
        String ref = accessorName;
        for (Map.Entry<String, List<Expression>> entry : map.entrySet()) {
            expr = new MethodCallExpr(new NameExpr(ref), entry.getKey());
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                for (Expression arg : entry.getValue()) {
                    expr.addArgument(arg);
                }
            }
            ref = new String(expr.toString());
        }
        return expr;
    }

    public static Expression accessMethod(String methodName, List<Expression> args){
        MethodCallExpr method = new MethodCallExpr();
        method.setName(methodName);
        NodeList<Expression> nodeList = new NodeList<>();
        args.stream().forEach(r -> nodeList.add(r));
        method.setArguments(nodeList);
        return method;
    }

    public static NormalAnnotationExpr createNormalAnnotExpr(Class<? extends Annotation> annotationClass, Map<String, Expression> attributes){
        NodeList<MemberValuePair> pairs = new NodeList<>();
        NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr();
        annotationExpr.setName(annotationClass.getSimpleName());
        annotationExpr.setPairs(pairs);

        if(attributes != null && ! attributes.isEmpty()) {
            attributes.forEach((k, v) -> {
                try {
                    Optional<Method> field = Arrays.stream(annotationClass.getDeclaredMethods()).filter(f -> StringUtils.equalsIgnoreCase(f.getName(), k)).findFirst();
                    field.ifPresent(r -> {
                        MemberValuePair memberValuePair = new MemberValuePair();
                        memberValuePair.setName(r.getName());
                        memberValuePair.setValue(v);
                        pairs.add(memberValuePair);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        return annotationExpr;
    }

    public static MarkerAnnotationExpr createMarkerAnnotExpr(String annotationName){
        return new MarkerAnnotationExpr(annotationName);
    }

    public static ExpressionStmt createGlobalInstance(String refVariable,  Class<?> clazz) {
        ExpressionStmt stmt = new ExpressionStmt();
        ObjectCreationExpr instance = new ObjectCreationExpr();
        instance.setType(clazz);
        AssignExpr assignExpr = new AssignExpr();
        assignExpr.setTarget(new NameExpr(refVariable));
        assignExpr.setValue(instance);
        assignExpr.setOperator(AssignExpr.Operator.ASSIGN);

        stmt.setExpression(assignExpr);
        return stmt;
    }

    public static void addMethodBody(String methodName, String className, List<Expression> statements){
        getMethod(className, methodName).ifPresent(s -> {
            statements.forEach(ex -> s.addStatement(ex));
        });
    }

    public static Expression assiginExpression(Expression leftArgs, Expression rightArgs){
        return new AssignExpr(leftArgs, rightArgs, AssignExpr.Operator.ASSIGN);
    }

    public static Expression declareVariable(Class<?> type, String refVariable){
        return new VariableDeclarationExpr(new TypeParameter(type.getSimpleName()), refVariable);
    }

    public static void addBlankSpace(String methodName, String className) {
        getMethod(className, methodName).ifPresent(s -> s.getStatements().add(new EmptyStmt()));
    }

    public static Optional<BlockStmt> getMethod(String className, String methodName){
        Optional<ClassOrInterfaceDeclaration> builder = getClassBuilder(className);
        if(builder.isPresent() && builder.get().getMethodsByName(methodName).size() > 0) {
            return builder.get().getMethodsByName(methodName).get(0).getBody();
        }
        return null;
    }
}
