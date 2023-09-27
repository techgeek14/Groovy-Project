package com.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;

public class VaadinCodeGenerator {
    public static Map<String, String> map = new HashMap<>();

    public static Map<String, Class<?>> elementMapper = new HashMap<>();

    public static Set<String> imports = new HashSet<>();
    public static List<String> testData = List.of("TestData1", "TestData2","TestData3");

    public static List<String> headerColumns = List.of("col1", "col2", "col3");

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
        elementMapper.put("default", elementMapper.get("div"));
    }
    public static String generateImports(){
        StringBuilder sb = new StringBuilder();
        imports.forEach(packageName -> {
            sb.append("import").append(" ").append(packageName).append(";").append("\n");
        });
        return sb.toString();
    }

    public static String getComponent(String component, String id, String style, String content) {
        StringBuilder sb = new StringBuilder();
        String refVariable = "comp".concat(id.substring(0, 1).toUpperCase().concat(id.substring(1)));
        Class<?> compClass = elementMapper.get(component) != null ? elementMapper.get(component) : elementMapper.get("default");
        String key = compClass.getSimpleName();
        sb.append(generateComponentTemplate(key, refVariable, id, style, content));
        imports.add(compClass.getCanonicalName());
        return sb.toString();
    }

    public static String generateComponentTemplate(String component, String refVariable, String id, String style, String content) {
        StringBuilder sb = new StringBuilder();
        switch (component) {
            case "Grid" -> {
                sb.append(String.format("%s<String> %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, style, id, content));
                sb.append(String.format("%s.setItems(%s);", refVariable, buildListItems())).append("\n");
                headerColumns.forEach(col -> {
                    sb.append(String.format("%s.addColumn(x -> x).setHeader(\"%s\");", refVariable, col)).append("\n");
                });
            }
            case "TextField" -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, style, id, content));
//                sb.append(String.format("tf.setValue(\"abc\");"));
            }
            default -> {
                sb.append(String.format("%s %s = new %s();", component, refVariable, component)).append("\n");
                sb.append(getDefaultTemplateProperties(refVariable, style, id, content));
                sb.append(String.format("%s.setText(\"%s\");", refVariable, content)).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getDefaultTemplateProperties(String refVariable, String style, String id, String content){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s.setId(\"%s\");",refVariable, id)).append("\n");
        sb.append(String.format("%s.getElement().setAttribute(\"style\", \"%s\");", refVariable, style)).append("\n");
        return sb.toString();
    }

    public static String buildListItems(){
        StringBuilder sb = new StringBuilder();
        testData.forEach(x -> {
            sb.append(String.format("\"%s\"", x));
            if(testData.indexOf(x) < testData.size() - 1) {
                sb.append(",");
            }
        });
        return sb.toString();
    }
}
