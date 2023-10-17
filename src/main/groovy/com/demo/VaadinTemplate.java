package com.demo;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;

import static com.demo.JavaParserUtil.*;

public class VaadinTemplate {

    public static final String HEADER = "header";
    public static final String BODY = "body";
    public static final String SIDE_BAR = "sideBar";

    public static final String FOOTER = "footer";

    public static final String ROOT = "root";

    public static void init(String className, String packageName) {
        Optional<ClassOrInterfaceDeclaration> classBuilder = getClassBuilder(className);
        Set<String> imports = new HashSet<>();
        classBuilder.ifPresent(x -> {
            if (x.getMethodsByName("buildComponent").isEmpty()) {
                x.addExtendedType(HorizontalLayout.class);
                addPackage(packageName, className);
                addField(Map.of(HorizontalLayout.class.getSimpleName(), ROOT), className);

                x.addAnnotation(createNormalAnnotExpr(Route.class, Map.of("value", new StringLiteralExpr("/login"))));
                x.addAnnotation(createNormalAnnotExpr(PageTitle.class, Map.of("value", new StringLiteralExpr("LoginPage"))));

                imports.add(HorizontalLayout.class.getCanonicalName());
                //            imports.add(VerticalLayout.class.getCanonicalName());
                imports.add(Route.class.getCanonicalName());
                imports.add(PageTitle.class.getCanonicalName());
                imports.add("jakarta.annotation.PostConstruct");
                addImports(imports, className);

                MethodDeclaration buildComponent = createMethod("buildComponent", className);
                buildComponent.addAnnotation(createMarkerAnnotExpr("PostConstruct"));

                buildComponent.getBody().ifPresent(body -> {
                    body.addStatement(createGlobalInstance(ROOT, HorizontalLayout.class));
                    body.addStatement(accessMethod("add", List.of(new NameExpr(ROOT))));
                });
            }

        });
    }
}
