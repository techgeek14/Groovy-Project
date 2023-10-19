package com.demo;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.stream.Collectors;

import static com.demo.JavaParserUtil.*;
import static com.demo.VaadinTemplate.ROOT;
import static com.demo.VaadinTemplate.init;

public class CodeGenerator {

    private static Map<String, List<Expression>> methodExprHolder = new LinkedHashMap<>();

    public static Map<String, Class<?>> elementMapper = new HashMap<>();

    private static List elements = List.of("container", "section");

    static {
//        elementMapper.put("default", List.of("id", "src", "alt", "text", "className", "style", "label", "name"));
        elementMapper.put("img", Image.class);
        elementMapper.put("label", Paragraph.class);
        elementMapper.put("container", HorizontalLayout.class);
        elementMapper.put("section", Div.class);
        elementMapper.put("textField", TextField.class);
        elementMapper.put("button", Button.class);
        elementMapper.put("icon", Icon.class);
        elementMapper.put("password", PasswordField.class);
        elementMapper.put("table", Grid.class);
        elementMapper.put("dropDown", Select.class);
        elementMapper.put("textBlock", TextArea.class);
        elementMapper.put("vaadinIcon", VaadinIcon.class);
        elementMapper.put("a", Anchor.class);
        elementMapper.put("svg", Icon.class);
    }
    public static void getComponent(Class<?> compClass, Map<String, Object> attributes, String referenceVariable, String parentId, String methodName, String className){
        switch (compClass.getSimpleName()) {
            case "Grid" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, String.class, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                methodExprHolder.clear();
//                methodExprHolder.put("setItems", List.of(new StringLiteralExpr(buildListItems())));
//                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
            }
            case "Component" -> {
                System.out.println("TODO");
            }
            case "Anchor" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);

                if(attributes.get("title") != null) {
                    methodExprHolder.clear();
                    methodExprHolder.put("setTitle", List.of(new StringLiteralExpr((String) attributes.get("title"))));
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, referenceVariable)));
                }

                if(attributes.get("src") != null) {
                    methodExprHolder.clear();
                    methodExprHolder.put("setHref", List.of(new StringLiteralExpr((String) attributes.get("src"))));
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, referenceVariable)));
                }

                if(attributes.get("target") != null) {
                    methodExprHolder.clear();
                    methodExprHolder.put("setTarget", List.of(new StringLiteralExpr((String) attributes.get("target"))));
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, referenceVariable)));
                }
            }
            case "Icon" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, accessField(new NameExpr(elementMapper.get("vaadinIcon").getSimpleName()), "QUESTION_CIRCLE_O")).getExpression()));
                addImports(Set.of(elementMapper.get("vaadinIcon").getCanonicalName(), compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);
            }
            default -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);
            }
        }
    }

    public static void getDefaultTemplateProperties(Class<?> component, Map<String, Object> attributes, String refVariable, String parentId, String methodName, String className) {
        if (attributes != null) {
            if(attributes.get("id") != null) {
                methodExprHolder.clear();
                methodExprHolder.put("setId", List.of(new StringLiteralExpr((String) attributes.get("id"))));
                addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
            }

            if(attributes.get("style") != null) {
                methodExprHolder.clear();
                methodExprHolder.put("getElement", List.of());
                methodExprHolder.put("setAttribute", List.of(new StringLiteralExpr("style"), new StringLiteralExpr((String) attributes.get("style"))));
                addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
            }

            if(attributes.get("classNames") != null) {
                List<String> classList = (List<String>) attributes.get("className");
                if(classList != null){
                    List<Expression> classNames = classList.stream().map(r -> new StringLiteralExpr(r)).collect(Collectors.toList());
                    methodExprHolder.clear();
                    methodExprHolder.put("addClassNames", classNames);
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                }
            }

            if (isHtmlContainer(component) && attributes.get("text") != null) {
                methodExprHolder.clear();
                methodExprHolder.put("setText", List.of(new StringLiteralExpr((String) attributes.get("text"))));
                addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
            }

            methodExprHolder.clear();
            methodExprHolder.put("add", List.of(new NameExpr(refVariable)));
            addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, StringUtils.isBlank(parentId) ? ROOT : parentId)));
        }
    }

    public static boolean isFlexCompType(Class<?> component) {
        return  component != null && (FlexComponent.class.isAssignableFrom(component) ||
                HasOrderedComponents.class.isAssignableFrom(component) ||
                HasComponents.class.isAssignableFrom(component));
    }

    public static boolean isHtmlContainer(Class<?> component){
        return component != null && HtmlContainer.class.isAssignableFrom(component) || HasText.class.isAssignableFrom(component);
    }

    public static boolean isAbstractTextField(Class<?> component){
        return component != null && AbstractField.class.isAssignableFrom(component);
    }

    public static String generateComponent(Map<String, String> classDetails, List<Map<String, Object>> contentHolder){
        init(classDetails.get("screen"), classDetails.get("packageName"), classDetails.get("routeUrl"), classDetails.get("pageTitle"));
        contentHolder.forEach(content -> {
            extractNode(content, classDetails.get("screen"), "buildComponent",  null, ROOT);
        });
        return getContent(classDetails.get("screen"));
    }

    public static void extractNode(Map<String, Object> map, String className, String methodName, String parentId, String rootId) {
        map.forEach((k, v) -> {
            System.out.println("component: " + k);
            if (elements.contains(exludeDigit(k))) {
                Class<?> component = elementMapper.get(exludeDigit(k));
                if (component != null) {
                    System.out.println("Mapped component: " + component.getSimpleName());
                    String refVariable = generateRefVariable(component);
                    getComponent(component, (Map<String, Object>) v, refVariable, parentId, methodName, className);
                    Map<String, Object> innerMap = (Map<String, Object>) v;
                    extractNode(innerMap, className, methodName, refVariable, rootId);
                }
            } else {
                String id = parentId == null ? ROOT : parentId;
                Class<?> component = elementMapper.get(exludeDigit(k));
                if (component != null) {
                    System.out.println("Mapped component: " + component.getSimpleName());
                    String refVariable = generateRefVariable(component);
                    getComponent(component, (Map<String, Object>) v, refVariable, id, methodName, className);
                }
            }
        });
    }

//    public static void main(String[] args) {
//        System.out.println(generateRefVariable(Image.class));
//    }

    private static String exludeDigit(String content){
        return content.replaceAll("\\d", "");
    }

    public static String generateRefVariable(Class<?> component) {
        String randomGeneratedCode = UUID.randomUUID().toString().split("-")[0].substring(0, 6).toString();
        String prefix = component.getSimpleName().toLowerCase();
        return prefix.concat(randomGeneratedCode.substring(0, 1).toUpperCase().concat(randomGeneratedCode.substring(1)));
    }
}
