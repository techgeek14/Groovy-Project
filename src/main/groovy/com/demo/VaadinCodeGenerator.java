package com.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
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

    public static Map<String, String> refVariableMap = new LinkedHashMap<>();

    private static Map<String, String> inputTypeList = new HashMap<>() {{
        put("button", "btn");
        put("text", "txtField");
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
    }
    public static String generateImports(){
        StringBuilder sb = new StringBuilder();
        imports.forEach(packageName -> {
            sb.append("import").append(" ").append(packageName).append(";").append("\n");
        });
        return sb.toString();
    }

    public static void getComponent(Element element, String refVariable) {
        StringBuilder sb = new StringBuilder();
        Class<?> compClass = elementMapper.get(getElementByTagName(element)) != null ? elementMapper.get(getElementByTagName(element)) : elementMapper.get("default");
        String key = compClass.getSimpleName();
        sb.append(generateComponentTemplate(key, refVariable, element));
//        sb.append("\n");
        imports.add(compClass.getCanonicalName());
        components.append(sb).append("\n");
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
            }
            case "Anchor" -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(String.format("%s.setTitle(\"%s\");", refVariable, element.attributes().get("title"))).append("\n");
                sb.append(String.format("%s.setHref(\"%s\");", refVariable, element.attributes().get("href"))).append("\n");
                sb.append(String.format("%s.setTarget(\"%s\");", refVariable, element.attributes().get("target"))).append("\n");
            }
            case "Icon" -> {
                imports.add(elementMapper.get("vaadinIcon").getCanonicalName());
                sb.append(String.format("%s %s = new %s(%s.QUESTION_CIRCLE_O);", component, refVariable, component, elementMapper.get("vaadinIcon").getSimpleName())).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element));
            }
//            case "TextField" -> {
//                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
//                sb.append(getDefaultTemplateProperties(refVariable, style, id));
////                sb.append(String.format("tf.setValue(\"abc\");"));
//            }
            default -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, element));
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
            sb.append(String.format("%s.setText(\"%s\");", refVariable, elementContent)).append("\n");
        }
        if(! element.attributes().isEmpty()){
            if(element.attributes().get("parentId").isEmpty()){
                sb.append(String.format("content.add(%s);", refVariable));
            } else {
                if(! element.attributes().get("compType").isEmpty() && isFlexCompType(elementMapper.get(element.attributes().get("compType")))){
                    sb.append(String.format("%s.add(%s);", element.attributes().get("parentId"), refVariable));
                }
            }
            if(! element.attributes().get("value").isEmpty() && isHtmlContainer(element)){
                sb.append(String.format("%s.setText(\"%s\");", refVariable, element.attributes().get("value").trim())).append("\n");
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
            if(! element.attributes().isEmpty() && ! element.attributes().get("type").isEmpty()){
                return element.attributes().get("type");
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
        String elementId = UUID.randomUUID().toString().split("-")[0].substring(0, 6).toString();
        String refVariable = getRefVariablePrefix(element).concat(elementId.substring(0, 1).toUpperCase().concat(elementId.substring(1)));
        return refVariable;
    }

    public static String getRefVariablePrefix(Element element){
        return switch (element.tagName()){
            case "input" -> inputTypeList.get(element.attributes().get("type"));
            default -> "comp";
        };
    }

    public static boolean isFlexCompType(Class<?> component) {
        return  component != null ? Stream.of(component.getInterfaces())
                .anyMatch(i -> i.getSimpleName().equalsIgnoreCase(FlexComponent.class.getSimpleName()) ||
                        i.getSimpleName().equalsIgnoreCase(HasComponents.class.getSimpleName())): false;
    }

    public static boolean isHtmlContainer(Element element){
        String tag = getElementByTagName(element);
        Class<?> component = elementMapper.get(tag) != null ? elementMapper.get(tag): Component.class;
        if(component != null && component.getSuperclass() != null){
            return Stream.of(component.getSuperclass()).anyMatch(p -> p.getSimpleName().equalsIgnoreCase(HtmlContainer.class.getSimpleName()));
        }
        return false;
    }
}
