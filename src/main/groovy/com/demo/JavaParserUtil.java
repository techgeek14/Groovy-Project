package com.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.util.*;

public class JavaParserUtil {

    public static JavaParser parser;

    private static Map<String, CompilationUnit> unitMap = new HashMap();

    private static CompilationUnit getCompilationUnitByClassName(String className) {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass(className);
        return unitMap.getOrDefault(className, unitMap.put(className, cu));
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

    public static void addConstructor(Map<String, String> params, String className){
        Optional<ClassOrInterfaceDeclaration> builder = getClassBuilder(className);
        builder.ifPresent(classBuilder -> {
            ConstructorDeclaration constructorBuilder = new ConstructorDeclaration();
            constructorBuilder.setModifiers(Modifier.publicModifier().getKeyword());
            BlockStmt blockStmt = new BlockStmt();
            constructorBuilder.setBody(blockStmt);
            classBuilder.getConstructors().add(constructorBuilder);

            params.forEach((type, colName) -> {
                classBuilder.addField(type, colName, Modifier.publicModifier().getKeyword());
                constructorBuilder.addParameter(type, colName);
                blockStmt.addStatement(new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), colName), new NameExpr(colName),
                        AssignExpr.Operator.ASSIGN));
            });
        });
    }

    public static void addField(Map<String, String> fields, String className){
        Optional<ClassOrInterfaceDeclaration> builder = getClassBuilder(className);
        builder.ifPresent(classBuilder -> {
            fields.forEach((type, colName) -> {
                classBuilder.addField(type, colName);
            });
        });
    }

    public static String getContent(String className) {
        return unitMap.containsKey(className) ? unitMap.get(className).toString() : "";
    }
}
