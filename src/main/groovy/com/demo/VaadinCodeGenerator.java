package com.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.N;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VaadinCodeGenerator {
    public static Map<String, String> map = new HashMap<>();

    public static Map<String, Class<?>> elementMapper = new HashMap<>();

    public static Set<String> imports = new HashSet<>();
    public static List<String> testData = List.of("TestData1", "TestData2","TestData3");

    public static List<String> headerColumns = List.of("col1", "col2", "col3");

    public static StringBuilder components = new StringBuilder();

    private static Map<String, String> inputTypeList = new HashMap<>() {{
        put("button", "btn");
        put("text", "txtField");
        put("password", "passField");
        put("checkbox", "chkBox");
        put("submit", "btn");
        put("file", "fileUpload");
        put("number", "numField");
        put("image", "img");
        put("email", "emailField");
        put("radio", "radioBtn");
        put("date", "dateComp");
    }};

    static {
        imports.add(Route.class.getCanonicalName());
        imports.add(PageTitle.class.getCanonicalName());
        imports.add("jakarta.annotation.PostConstruct");

        elementMapper.put("div", Div.class);
        elementMapper.put("text", TextField.class);
        elementMapper.put("password", PasswordField.class);
        elementMapper.put("table", Grid.class);
        elementMapper.put("button", Button.class);
        elementMapper.put("span", Span.class);
        elementMapper.put("section", Section.class);
        elementMapper.put("main", Main.class);
        elementMapper.put("footer", Footer.class);
        elementMapper.put("select", Select.class);
        elementMapper.put("textarea", TextArea.class);
        elementMapper.put("p", Paragraph.class);
        elementMapper.put("submit", elementMapper.get("button"));
        elementMapper.put("img", Image.class);
        elementMapper.put("svg", Icon.class);
        elementMapper.put("default", Component.class);
        elementMapper.put("vaadinIcon", VaadinIcon.class);
        elementMapper.put("label", elementMapper.get("span"));
        elementMapper.put("nav", HorizontalLayout.class);
        elementMapper.put("i", elementMapper.get("svg"));
        elementMapper.put("a", Anchor.class);
        elementMapper.put("h2", elementMapper.get("span"));
        elementMapper.put("h5", elementMapper.get("span"));
        elementMapper.put("b", elementMapper.get("span"));
        elementMapper.put("form", elementMapper.get("div"));
    }
    public static String generateImports(){
        StringBuilder sb = new StringBuilder();
        imports.forEach(packageName -> {
            sb.append("import").append(" ").append(packageName).append(";").append("\n");
        });
        return sb.toString();
    }

    public static void getComponent(Element element, String refVariable) {
        if(! skipElement(element)){
            StringBuilder sb = new StringBuilder();
            Class<?> compClass = elementMapper.get(getElementByTagName(element)) != null ? elementMapper.get(getElementByTagName(element)) : elementMapper.get("default");
            String key = compClass.getSimpleName();
            sb.append(generateComponentTemplate(key, refVariable, element));
            imports.add(compClass.getCanonicalName());
            components.append(sb);
        }
    }

    public static String generateComponentTemplate(String component, String refVariable, Element element) {
        StringBuilder sb = new StringBuilder();
        switch (component) {
            case "Grid" -> {
                sb.append(String.format("%s<String> %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element));
                sb.append(String.format("%s.setItems(%s);", refVariable, buildListItems())).append("\n");
                headerColumns.forEach(col -> {
                    sb.append(String.format("%s.addColumn(x -> x).setHeader(\"%s\");", refVariable, col)).append("\n");
                });
            }
            case "Component" -> {
                System.out.println("TODO " + element.tagName());
                sb.append("\n");
            }
            case "Anchor" -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");
                sb.append(String.format("%s.setTitle(\"%s\");", refVariable, element.attributes().get("title"))).append("\n");
                sb.append(String.format("%s.setHref(\"%s\");", refVariable, element.attributes().get("href"))).append("\n");
                sb.append(String.format("%s.setTarget(\"%s\");", refVariable, element.attributes().get("target"))).append("\n");
            }
            case "Icon" -> {
                imports.add(elementMapper.get("vaadinIcon").getCanonicalName());
                sb.append(String.format("%s %s = new %s(%s.QUESTION_CIRCLE_O);", component, refVariable, component, elementMapper.get("vaadinIcon").getSimpleName())).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");
            }
            default -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getDefaultTemplateProperties(String refVariable, Element element){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s.setId(\"%s\");",refVariable, element.id())).append("\n");
        String style = Arrays.stream(element.attributes().get("style").split("\n")).map(String::trim).collect(Collectors.joining(" "));
        if(StringUtils.isNotBlank(style)){
            sb.append(String.format("%s.getElement().setAttribute(\"style\", \"%s\");", refVariable, style)).append("\n");
        }
        if(! element.attributes().get("class").isEmpty()){
            String classNames = element.attributes().get("class").replace(" ", "\",\"");
            if(StringUtils.isNotBlank(classNames)){
                sb.append(String.format("%s.addClassNames(\"%s\");", refVariable, classNames)).append("\n");
            }
        }
        String elementContent = getElementContent(element);
        if(StringUtils.isNotBlank(elementContent) && isHtmlContainer(element)){
            sb.append(String.format("%s.setText(\"%s\");", refVariable, elementContent.trim())).append("\n");
        }
        if(element.attributes().isEmpty()){
            if(element.attributes().get("parentId").isEmpty()){
                sb.append(String.format("content.add(%s);", refVariable)).append("\n");
            }
        } else {
            if(! element.attributes().get("value").isEmpty() && isHtmlContainer(element)){
                sb.append(String.format("%s.setText(\"%s\");", refVariable, element.attributes().get("value").trim())).append("\n");
            }
            if(element.attributes().get("parentId").isEmpty()){
                sb.append(String.format("content.add(%s);", refVariable)).append("\n");
            } else {
                if(! element.attributes().get("compType").isEmpty() && isFlexCompType(element)){
                    sb.append(String.format("%s.add(%s);", element.attributes().get("parentId"), refVariable)).append("\n");
                }
            }
            if(element.tagName().equalsIgnoreCase("input") && ! element.attributes().get("value").isEmpty() && isAbstractTextField(element)){
                sb.append(String.format("%s.setValue(\"%s\");", refVariable, element.attributes().get("value"))).append("\n");
            }
        }
        return sb.toString();
    }

    public static String buildListItems() {
        StringBuilder sb = new StringBuilder();
        testData.forEach(x -> {
            sb.append(String.format("\"%s\"", x));
            if(testData.indexOf(x) < testData.size() - 1) {
                sb.append(",");
            }
        });
        return sb.toString();
    }

    public static String getElementByTagName(Element element){
        if(element.tagName().equalsIgnoreCase("input")){
            if(! element.attributes().isEmpty() && element.attributes().hasKey("type")){
                return element.attributes().get("type");
            } else {
                return "text";
            }
        }
        return element.tagName();
    }
    public static String getElementContent(Element element){
        StringBuilder content = new StringBuilder();
        Optional<String> elementContent = element.textNodes().stream().filter(tx -> ! tx.isBlank()).map(TextNode::text).findFirst();
        if (elementContent.isPresent()) {
            if(elementContent.get().contains("\"")){
                String str = elementContent.get().replace("\"", "\\\"");
                content.append(str);
            } else {
                content.append(elementContent.get());
            }
        }
        return content.toString();
    }

    public static String generateRefVariable(Element element){
        String refVariable = "";
        String elementId = UUID.randomUUID().toString().split("-")[0].substring(0, 6).toString();
        String prefix = getRefVariablePrefix(element);
        if(prefix == null){
            System.out.println("Null element found: " + element.tagName());
        } else {
            refVariable = getRefVariablePrefix(element).concat(elementId.substring(0, 1).toUpperCase().concat(elementId.substring(1)));
        }
        return refVariable;
    }

    public static String getRefVariablePrefix(Element element){
        if(element.tagName().equalsIgnoreCase("input")) {
            if(element.attributes() != null && ! element.attributes().isEmpty()){
                return element.attributes().hasKey("type") ? inputTypeList.get(element.attributes().get("type")) : inputTypeList.get("text");
            }
        }
        return "comp";
    }

    public static boolean isFlexCompType(Element element) {
        Class<?> component = elementMapper.get(element.attributes().get("compType"));
        return  component != null && (FlexComponent.class.isAssignableFrom(component) ||
                        HasOrderedComponents.class.isAssignableFrom(component) ||
                        HasComponents.class.isAssignableFrom(component));
    }

    public static boolean isHtmlContainer(Element element){
        String tag = getElementByTagName(element);
        Class<?> component = elementMapper.get(tag) != null ? elementMapper.get(tag): Component.class;
        return component != null && HtmlContainer.class.isAssignableFrom(component) || HasText.class.isAssignableFrom(component);
    }

    public static boolean isAbstractTextField(Element element){
        String tag = getElementByTagName(element);
        Class<?> component = elementMapper.get(tag) != null ? elementMapper.get(tag): Component.class;
        return component != null && AbstractField.class.isAssignableFrom(component);
    }

    public static boolean skipElement(Element element){
        List<String> tags = List.of("table", "svg", "br", "tbody", "script");
        return tags.contains(element.tagName());
    }

    public static String getJavaContent() {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration("com.example.application.views.main");
//        cu.setImport(0, new ImportDeclaration("com.vaadin.flow.component.orderedlayout.VerticalLayout", false,false));

        ClassOrInterfaceDeclaration book = cu.addClass("TestScreen");
        book.addField("String", "title");
        book.addField("Person", "author");

        book.addAnnotation(new JavaParser().parseAnnotation("@Route(value = \"testScreen\")").getResult().get());

        book.addConstructor(Modifier.publicModifier().getKeyword())
                .addParameter("String", "title")
                .addParameter("Person", "author")
                .setBody(new BlockStmt()
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "title"), new NameExpr("title"),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "author"),
                                new NameExpr("author"),
                                AssignExpr.Operator.ASSIGN))));

        book.addMethod("getTitle", Modifier.publicModifier().getKeyword()).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("title"))));

        book.addMethod("getAuthor", Modifier.publicModifier().getKeyword()).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("author"))));

        book.addMethod("buildComponent").setBody(
                new BlockStmt().addStatement(new ExpressionStmt(
                        new NameExpr("Integer k = new Integer()")
                ))
        );
        System.out.println(cu.toString());
        return cu.toString();
    }

    public static void main(String[] args) {
        CompilationUnit cu = new CompilationUnit();

        cu.setPackageDeclaration("com.example.application.views.main");
//        cu.setImport(0, new ImportDeclaration("com.vaadin.flow.component.orderedlayout.VerticalLayout", false,false));

        ClassOrInterfaceDeclaration book = cu.addClass("TestScreen");
        book.addField("String", "title");
        book.addField("Person", "author");

        book.addAnnotation(new JavaParser().parseAnnotation("@Route(value = \"testScreen\")").getResult().get());

        book.addConstructor(Modifier.publicModifier().getKeyword())
                .addParameter("String", "title")
                .addParameter("Person", "author")
                .setBody(new BlockStmt()
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "title"), new NameExpr("title"),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "author"),
                                new NameExpr("author"),
                                AssignExpr.Operator.ASSIGN))));

        book.addMethod("getTitle", Modifier.publicModifier().getKeyword()).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("title"))));

        book.addMethod("getAuthor", Modifier.publicModifier().getKeyword()).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("author"))));

        book.addMethod("buildComponent");

        Optional<MethodDeclaration> buildComponent = book.getMethodsByName("buildComponent").stream().findFirst();

        buildComponent.ifPresent(methodDeclaration -> methodDeclaration.getBody().ifPresent(body -> {
            ObjectCreationExpr object = new ObjectCreationExpr();
            object.setType(HorizontalLayout.class);

            body.addStatement(new ExpressionStmt(new AssignExpr(
                    new VariableDeclarationExpr(object.getType(), "layout"),
                    object,
                    AssignExpr.Operator.ASSIGN)));

            Arrays.stream(HorizontalLayout.class.getMethods()).collect(Collectors.toList()).stream()
                    .filter(x -> x.getName().equalsIgnoreCase("setId"))
                    .forEach(x-> {
                System.out.println(x.getName());
            });
            MethodCallExpr method = new MethodCallExpr(new NameExpr("layout"), "setId");
            method.addArgument(new StringLiteralExpr("12"));

            ObjectCreationExpr grid = new ObjectCreationExpr();
            grid.setType(Grid.class);

            String typeGeneric = String.format("%s<%s> %s", Grid.class.getSimpleName(), String.class.getSimpleName(), "gird");

            body.addStatement(new ExpressionStmt(new AssignExpr(new NameExpr(typeGeneric), grid, AssignExpr.Operator.ASSIGN)));
            body.addStatement(method);
            body.addStatement(new FieldAccessExpr(new NameExpr("layout"), "setStyle"));
        }));
        System.out.println(cu.toString());
    }
}
