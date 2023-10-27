package com.demo;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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

    public static void init(String className, String packageName, String routeUrl, String pageTitle) {
        Optional<ClassOrInterfaceDeclaration> classBuilder = getClassBuilder(className);
        Set<String> imports = new HashSet<>();

        classBuilder.ifPresent(x -> {
            if (x.getMethodsByName("buildComponent").isEmpty()) {
                x.addExtendedType(HorizontalLayout.class);
                addPackage(packageName, className);
                addField(Map.of(HorizontalLayout.class.getSimpleName(), ROOT), className);

                addConstructor(Map.of(), className);

                x.addAnnotation(createNormalAnnotExpr(Route.class, Map.of("value", new StringLiteralExpr(routeUrl))));
                x.addAnnotation(createNormalAnnotExpr(PageTitle.class, Map.of("value", new StringLiteralExpr(pageTitle))));

                imports.add(HorizontalLayout.class.getCanonicalName());
                imports.add(Route.class.getCanonicalName());
                imports.add(PageTitle.class.getCanonicalName());
                imports.add("jakarta.annotation.PostConstruct");
                addImports(imports, className);

                MethodDeclaration buildComponent = createMethod("buildComponent", className);
                buildComponent.addAnnotation(createMarkerAnnotExpr("PostConstruct"));

                Map<String, List<Expression>> methodCallExpr = new LinkedHashMap<>();
                methodCallExpr.put("getElement", List.of());
                methodCallExpr.put("setAttribute", List.of(new StringLiteralExpr("style"),
                        new StringLiteralExpr("display: flex; flex-direction: row; flex-wrap: wrap; width: 100%; height: 100%;")));

                addMethodBody(buildComponent.getNameAsString(), className,
                        List.of(createGlobalInstance(ROOT, HorizontalLayout.class).getExpression(),
                                accessMethod("add", List.of(new NameExpr(ROOT))),
                                accessMethod(methodCallExpr, ROOT)));
                addBlankSpace("buildComponent", className);
//                buildComponent.getBody().ifPresent(body -> {
//                    body.addStatement(createGlobalInstance(ROOT, HorizontalLayout.class));
//                    body.addStatement(accessMethod("add", List.of(new NameExpr(ROOT))));
//                    body.addStatement(accessMethod(Map.of("getElement", List.of(), "setAttribute", List.of(new StringLiteralExpr("style"), new StringLiteralExpr(""))), ROOT));
//                });
            }

        });
    }
}
