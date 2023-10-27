package com.demo;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.demo.JavaParserUtil.*;
import static com.demo.VaadinTemplate.ROOT;
import static com.demo.VaadinTemplate.init;

public class CodeGenerator {

    private static Map<String, List<Expression>> methodExprHolder = new LinkedHashMap<>();

    public static Map<String, Class<?>> elementMapper = new HashMap<>();

    private static List elements = List.of("container", "section");

    private final static Map<String, BiConsumer<Map<String, Object>, Map<String, Object>>> customCompFunction = new HashMap<>();

    static {
        elementMapper.put("img", Image.class);
        elementMapper.put("label", Paragraph.class);
        elementMapper.put("container", HorizontalLayout.class);
        elementMapper.put("section", Div.class);
        elementMapper.put("textField", TextField.class);
        elementMapper.put("button", Button.class);
        elementMapper.put("icon", Icon.class);
        elementMapper.put("passwordField", PasswordField.class);
        elementMapper.put("table", Grid.class);
        elementMapper.put("dropDown", Select.class);
        elementMapper.put("textBlock", TextArea.class);
        elementMapper.put("vaadinIcon", VaadinIcon.class);
        elementMapper.put("a", Anchor.class);
        elementMapper.put("svg", elementMapper.get("icon"));
        elementMapper.put("tabs", TabSheet.class);
        elementMapper.put("tabContent", VerticalLayout.class);
        elementMapper.put("radioButton", RadioButtonGroup.class);
        elementMapper.put("menuBar", MenuBar.class);
        initCustomCompFunction();
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

                if(attributes != null) {
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
                addBlankSpace(methodName, className);
            }
            case "Icon" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, accessField(new NameExpr(elementMapper.get("vaadinIcon").getSimpleName()), "QUESTION_CIRCLE_O")).getExpression()));
                addImports(Set.of(elementMapper.get("vaadinIcon").getCanonicalName(), compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);
                addBlankSpace(methodName, className);
            }
            case "TabSheet" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                customCompFunction.getOrDefault("TabSheet", (k, v) -> System.out.println("Component Not found"))
                        .accept(Map.of("compClass", compClass, "referenceVariable", referenceVariable,
                                "methodName", methodName, "className", className), attributes);

            }
            case "RadioButtonGroup" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, String.class, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);
                addBlankSpace(methodName, className);
            }
            case "MenuBar" -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);

                customCompFunction.getOrDefault("menuItem", (k, v) -> System.out.println("Component Not found"))
                        .accept(Map.of("compClass", compClass, "referenceVariable", referenceVariable,
                                "methodName", methodName, "className", className), attributes);

            }
            default -> {
                addMethodBody(methodName, className, List.of(createInstance(compClass, null, referenceVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), className);
                getDefaultTemplateProperties(compClass, attributes, referenceVariable, parentId, methodName, className);
                addBlankSpace(methodName, className);
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
                List<String> classList = (List<String>) attributes.get("classNames");
                if(classList != null && classList.size() > 0){
                    List<Expression> classNames = classList.stream().map(r -> new StringLiteralExpr(r)).collect(Collectors.toList());
                    methodExprHolder.clear();
                    methodExprHolder.put("addClassNames", classNames);
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                }
            }

            if (isAbstractTextField(component)) {
                if(attributes.get("label") != null) {
                    methodExprHolder.clear();
                    methodExprHolder.put("setLabel", List.of(new StringLiteralExpr((String) attributes.get("label"))));
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                }
            } else if(isHtmlContainer(component) && Arrays.stream(component.getMethods()).anyMatch(x -> StringUtils.equalsIgnoreCase(x.getName(), "setSrc"))) {
                    if(attributes.get("src") != null){
                        methodExprHolder.clear();
                        methodExprHolder.put("setSrc", List.of(new StringLiteralExpr((String) attributes.get("src"))));
                        addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                    }
            } else {
                if(attributes.get("text") != null) {
                    methodExprHolder.clear();
                    methodExprHolder.put("setText", List.of(new StringLiteralExpr((String) attributes.get("text"))));
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                }
            }

            methodExprHolder.clear();
            methodExprHolder.put("add", List.of(new NameExpr(refVariable)));
            addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, StringUtils.isBlank(parentId) ? ROOT : parentId)));

            if(attributes.get("items") != null) {
                List<String> items = (List<String>) attributes.get("items");
                if(items != null && items.size() > 0) {
                    List<Expression> itemList = items.stream().map(r -> new StringLiteralExpr(r)).collect(Collectors.toList());
                    methodExprHolder.clear();
                    methodExprHolder.put("setItems", itemList);
                    addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, refVariable)));
                }
            }


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

    private static String exludeDigit(String content){
        return content.replaceAll("\\d", "");
    }

    public static String generateRefVariable(Class<?> component) {
        String randomGeneratedCode = UUID.randomUUID().toString().split("-")[0].substring(0, 6).toString();
        String prefix = component.getSimpleName().toLowerCase();
        return prefix.concat(randomGeneratedCode.substring(0, 1).toUpperCase().concat(randomGeneratedCode.substring(1)));
    }


    public static void initCustomCompFunction(){
        customCompFunction.put("TabSheet", tabComponent());
        customCompFunction.put("menuItem", menuComponent());
    }

    public static BiConsumer<Map<String, Object>, Map<String, Object>> tabComponent(){
        return (compAttr, attributes) -> {
            Class<?> compClass = (Class<?>) compAttr.getOrDefault("compClass", null);
            String referenceVariable = (String) compAttr.getOrDefault("referenceVariable", null);
            String parentId = (String) compAttr.getOrDefault("parentId", null);
            String methodName = (String) compAttr.getOrDefault("methodName", null);
            String className = (String) compAttr.getOrDefault("className", null);

            List<Map<String, Object>> contentMap = attributes.get("content") != null ? (List<Map<String, Object>>) attributes.get("content") : null;
            attributes.remove("content");
            getDefaultTemplateProperties(compClass, attributes != null ? attributes : null, referenceVariable, parentId, methodName, className);

            if(contentMap != null && contentMap.size() > 0) {
                contentMap.forEach(tab -> {
                    Map<String, Object> tabMap = tab;
                    if (tabMap.get("tabContent") != null) {
                        Class<?> content = elementMapper.get("tabContent");
                        String contentRefVariable = generateRefVariable(content);
                        addMethodBody(methodName, className, List.of(createInstance(content, null, contentRefVariable, null).getExpression()));
                        addImports(Set.of(content.getCanonicalName()), className);
                        methodExprHolder.clear();
                        methodExprHolder.put("add", List.of(new StringLiteralExpr((String) tabMap.get("tabName")), new NameExpr(contentRefVariable)));
                        addMethodBody(methodName, className, List.of(accessMethod(methodExprHolder, referenceVariable)));
                        addBlankSpace(methodName, className);
                        extractNode((Map<String, Object>) tabMap.get("tabContent"), className, methodName, contentRefVariable, null);
                    }
                });
            }
        };
    }

    public static BiConsumer<Map<String, Object>, Map<String, Object>> menuComponent(){
        return (compAttr, attr) -> {
            extractMenuComponent((List<Map<String, Object>>) attr.get("menu"), compAttr, null);
        };
    }

    public static void extractMenuComponent(List<Map<String, Object>> menuItemMap, Map<String, Object> compAttr, String parentId) {
        String referenceVariable = (String) compAttr.getOrDefault("referenceVariable", null);
        String methodName = (String) compAttr.getOrDefault("methodName", null);
        String className = (String) compAttr.getOrDefault("className", null);

        if (menuItemMap != null && menuItemMap.size() > 0) {
            menuItemMap.forEach(item -> {
                addImports(Set.of(MenuItem.class.getCanonicalName()), className);
                String itemRefVariable = parentId == null ? generateRefVariable(MenuItem.class) : generateRefVariable(SubMenu.class);
                methodExprHolder.clear();
                if (parentId != null && StringUtils.isNotBlank(parentId)) {
                    methodExprHolder.put("getSubMenu", List.of());
                }
                methodExprHolder.put("addItem", List.of(new StringLiteralExpr((String) item.get("name"))));
                addMethodBody(methodName, className, List.of(assiginExpression(declareVariable(MenuItem.class, itemRefVariable),
                        accessMethod(methodExprHolder, parentId == null ? referenceVariable : parentId))));
                if (item.get("menu") != null) {
                    extractMenuComponent((List<Map<String, Object>>) item.get("menu"), compAttr, itemRefVariable);
                    addBlankSpace(methodName, className);
                }
            });
        }
    }
    public static void main(String[] args) {
        System.out.println("----- Executing Main Method -----");
        addField(Map.of("Integer", "id", "String", "FirstName"), "TestScreen");
        EmptyStmt blankSpace = new EmptyStmt();
        getClassBuilder("TestScreen").get().addMethod("check");

        Optional<BlockStmt> methodBody = getMethod("TestScreen", "check");

//        List<Statement> statement = new ArrayList<>();
//        statement.add(new ExpressionStmt(createInstance(HorizontalLayout.class,null, "hLayout", null).getExpression()));
//        statement.add(new ExpressionStmt(createInstance(VerticalLayout.class,null, "vLayout", null).getExpression()));
//        getClassBuilder("TestScreen").get().getMethodsByName("check").get(0).getBody().get()
//                        .getStatements().addAll(statement);

        System.out.println(getContent("TestScreen"));
    }
}
