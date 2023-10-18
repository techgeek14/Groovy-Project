package com.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.inject.PostConstructIntrospector;
import org.checkerframework.checker.units.qual.N;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import static com.demo.JavaParserUtil.*;
import static com.demo.VaadinTemplate.*;

public class VaadinCodeGenerator {
    public static Map<String, String> map = new HashMap<>();

    public static Map<String, Class<?>> elementMapper = new HashMap<>();

    public static Set<String> imports = new HashSet<>();
    public static List<String> testData = List.of("TestData1", "TestData2","TestData3");

    public static List<String> headerColumns = List.of("col1", "col2", "col3");

    public static StringBuilder components = new StringBuilder();

    private static Map<String, List<Expression>> methodExprHolder = new LinkedHashMap<>();

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
            sb.append(generateComponentTemplate(compClass, refVariable, element));
            imports.add(compClass.getCanonicalName());
            components.append(sb);
        }
    }

    public static String generateComponentTemplate(Class<?> compClass, String refVariable, Element element) {
        String component = compClass.getSimpleName();
        StringBuilder sb = new StringBuilder();
        VaadinTemplate.init("Login", "com.example.application.views.main");
        switch (component) {
            case "Grid" -> {
                addMethodBody("buildComponent", "Login", List.of(createInstance(compClass, String.class, refVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), "Login");
                sb.append(String.format("%s<String> %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element));
                sb.append(String.format("%s.setItems(%s);", refVariable, buildListItems())).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setItems", List.of(new StringLiteralExpr(buildListItems())));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
                headerColumns.forEach(col -> {
                    sb.append(String.format("%s.addColumn(x -> x).setHeader(\"%s\");", refVariable, col)).append("\n");
                });
            }
            case "Component" -> {
                System.out.println("TODO " + element.tagName());
                sb.append("\n");
            }
            case "Anchor" -> {
                addMethodBody("buildComponent", "Login", List.of(createInstance(compClass, null, refVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), "Login");
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");

                sb.append(String.format("%s.setTitle(\"%s\");", refVariable, element.attributes().get("title"))).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setTitle", List.of(new StringLiteralExpr(element.attributes().get("title"))));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));

                sb.append(String.format("%s.setHref(\"%s\");", refVariable, element.attributes().get("href"))).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setHref", List.of(new StringLiteralExpr(element.attributes().get("href"))));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));

                sb.append(String.format("%s.setTarget(\"%s\");", refVariable, element.attributes().get("target"))).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setTarget", List.of(new StringLiteralExpr(element.attributes().get("target"))));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
            }
            case "Icon" -> {
                addMethodBody("buildComponent", "Login", List.of(createInstance(compClass, null, refVariable, accessField(new NameExpr(elementMapper.get("vaadinIcon").getSimpleName()), "QUESTION_CIRCLE_O")).getExpression()));
                addImports(Set.of(elementMapper.get("vaadinIcon").getCanonicalName(), elementMapper.get("svg").getCanonicalName()), "Login");
                imports.add(elementMapper.get("vaadinIcon").getCanonicalName());
                sb.append(String.format("%s %s = new %s(%s.QUESTION_CIRCLE_O);", component, refVariable, component, elementMapper.get("vaadinIcon").getSimpleName())).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");
            }
            default -> {
//                body.addStatement(createInstance(compClass, null, refVariable, null));
                addMethodBody("buildComponent", "Login", List.of(createInstance(compClass, null, refVariable, null).getExpression()));
                addImports(Set.of(compClass.getCanonicalName()), "Login");
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element)).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getDefaultTemplateProperties(String refVariable, Element element){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s.setId(\"%s\");",refVariable, element.id())).append("\n");
        methodExprHolder.clear();
        methodExprHolder.put("setId", List.of(new StringLiteralExpr(element.id())));
        addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));

        String style = Arrays.stream(element.attributes().get("style").split("\n")).map(String::trim).collect(Collectors.joining(" "));
        if(StringUtils.isNotBlank(style)){
            sb.append(String.format("%s.getElement().setAttribute(\"style\", \"%s\");", refVariable, style)).append("\n");
            methodExprHolder.clear();
            methodExprHolder.put("getElement", List.of());
            methodExprHolder.put("setAttribute", List.of(new StringLiteralExpr("style"), new StringLiteralExpr(style)));
            addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
        }
        if(! element.attributes().get("class").isEmpty()){
            String classNames = element.attributes().get("class").replace(" ", "\",\"");
            if(StringUtils.isNotBlank(classNames)){
                sb.append(String.format("%s.addClassNames(\"%s\");", refVariable, classNames)).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("addClassNames", List.of(new StringLiteralExpr(classNames)));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
            }
        }
        String elementContent = getElementContent(element);
        if(StringUtils.isNotBlank(elementContent) && isHtmlContainer(element)){
            sb.append(String.format("%s.setText(\"%s\");", refVariable, elementContent.trim())).append("\n");
            methodExprHolder.clear();
            methodExprHolder.put("setText", List.of(new StringLiteralExpr(elementContent.trim())));
            addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
        }
        if(element.attributes().isEmpty()){
            if(element.attributes().get("parentId").isEmpty()){
                sb.append(String.format("content.add(%s);", refVariable)).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("add", List.of(new NameExpr(refVariable)));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, ROOT)));
            }
        } else {
            if(! element.attributes().get("value").isEmpty() && isHtmlContainer(element)){
                sb.append(String.format("%s.setText(\"%s\");", refVariable, element.attributes().get("value").trim())).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setText", List.of(new StringLiteralExpr(element.attributes().get("value").trim())));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
            }
            if(element.attributes().get("parentId").isEmpty()){
                sb.append(String.format("content.add(%s);", refVariable)).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("add", List.of(new NameExpr(refVariable)));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, ROOT)));
            } else {
                if(! element.attributes().get("compType").isEmpty() && isFlexCompType(element)){
                    sb.append(String.format("%s.add(%s);", element.attributes().get("parentId"), refVariable)).append("\n");
                    methodExprHolder.clear();
                    methodExprHolder.put("add", List.of(new NameExpr(refVariable)));
                    addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, element.attributes().get("parentId"))));
                }
            }
            if(element.tagName().equalsIgnoreCase("input") && ! element.attributes().get("value").isEmpty() && isAbstractTextField(element)){
                sb.append(String.format("%s.setValue(\"%s\");", refVariable, element.attributes().get("value"))).append("\n");
                methodExprHolder.clear();
                methodExprHolder.put("setValue", List.of(new StringLiteralExpr(element.attributes().get("value"))));
                addMethodBody("buildComponent", "Login", List.of(accessMethod(methodExprHolder, refVariable)));
            }
        }
        addMethodBody("buildComponent", "Login", List.of());
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
//        String className = "Login";
//        String packageName = "com.example.application.views.main";
//        VaadinTemplate.init(className, packageName);
        return getContent("Login");
    }

    public static void main(String[] args) {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration("com.example.application.views.main");
//        cu.setImport(0, new ImportDeclaration("com.vaadin.flow.component.orderedlayout.VerticalLayout", false,false));

        ClassOrInterfaceDeclaration book = cu.addClass("TestScreen");
        book.addField("String", "title");
        book.addField("Person", "author");

//        book.addAnnotation(new JavaParser().parseAnnotation("@Route(value = \"testScreen\")").getResult().get());

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

            LambdaExpr lambdaExpr = new LambdaExpr();
            lambdaExpr.setParameters(NodeList.nodeList(new Parameter(new UnknownType(), "x")));
            lambdaExpr.setEnclosingParameters(true);
            lambdaExpr.setBody(new ExpressionStmt(new NameExpr("x.setHeader()")));
            body.addStatement(lambdaExpr);

            body.addStatement(createInstance(Icon.class, null, "icon", accessField(new NameExpr(VaadinIcon.class.getSimpleName()), "QUESTION_CIRCLE_O")));
            StringLiteralExpr exp1 =  new StringLiteralExpr("s1");
            StringLiteralExpr exp2 =  new StringLiteralExpr("s2");
            List<StringLiteralExpr> exps = List.of(exp1, exp2);

            body.addStatement(createInstance(Grid.class, String.class, "grid", null));
//            body.addStatement(accessMethod("grid", null, "setItems", List.of(convertStringLiteralExpToString(exps))));

            Map<String, List<Expression>> map = Map.of("setItems", List.of(convertStringLiteralExpToString(exps)));
            Expression expression = accessMethod(map, "grid");
            body.addStatement(expression);

            body.addStatement(createInstance(HorizontalLayout.class,null, "layout", null));
            Map<String, List<Expression>> map1 = new LinkedHashMap<>();
            map1.put("getElement", List.of());
            map1.put("setAttribute", List.of(new StringLiteralExpr("style"), new StringLiteralExpr("color: red;")));

            body.addStatement(accessMethod(map1, "layout"));

            ObjectCreationExpr obc =new ObjectCreationExpr();
            obc.setType(HorizontalLayout.class);
            AssignExpr as = new AssignExpr();
            as.setTarget(new NameExpr("root"));
            as.setValue(obc);
            as.setOperator(AssignExpr.Operator.ASSIGN);
            body.addStatement(as);

//            MethodCallExpr mx = new MethodCallExpr();
//            mx.setName("add");
//            mx.setArguments(NodeList.nodeList(new NameExpr("root")));
            body.addStatement(accessMethod("add", List.of(new NameExpr("root"))));
        }));

        Map<String, Expression> m2 = new LinkedHashMap<>();
        m2.put("value", new StringLiteralExpr("/home"));
        m2.put("layout", new NameExpr("UI.class"));
        m2.put("absolute", new NameExpr("true"));
        book.addAnnotation(createNormalAnnotExpr(Route.class, m2));

        MethodDeclaration testMethod = book.addMethod("testMethod");
        testMethod.addAnnotation(createMarkerAnnotExpr("PostConstruct"));

        System.out.println(cu.toString());
    }
}
