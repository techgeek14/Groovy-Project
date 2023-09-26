package com.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VaadinTemplate {

    public static Map<String, String> map = new HashMap<>();

    public static List<String> actionListener = new ArrayList<>();

    public static List<String> instanceComponent = new ArrayList<>();

    static {
        map.put(Route.class.getSimpleName(), Route.class.getCanonicalName());
        map.put(PageTitle.class.getSimpleName(), PageTitle.class.getCanonicalName());
        map.put("PostConstruct", "import jakarta.annotation.PostConstruct");
    }
    public static String generateImports(Map<String, String> imports){
        StringBuilder sb = new StringBuilder();
        imports.values().forEach(v -> {
            sb.append("import").append(" ").append(v).append(";").append("\n");
        });
        return sb.toString();
    }

    public static String componentGenerator(String component, String id, String style){
        StringBuilder sb = new StringBuilder();
        String variable = component.concat(id.substring(0,1).toUpperCase().concat(id.substring(1)));
        switch(component){
            case "div" -> {
                instanceComponent.add(String.format("Div %s;", variable));
                sb.append(String.format("%s = new Div();", variable));
                sb.append("\n");
                sb.append(String.format("%s.setId(\"%s\");",variable, id));
                sb.append("\n");
                sb.append(String.format("%s.getElement().setAttribute(\"style\", \"%s\");", variable, style));
                sb.append("\n");

                actionListener.add(String.format("%s.addClickListener(r -> {\n" +
                        "                    System.out.println(\"%s Clicked !!\");\n" +
                        "                });", variable, variable).concat("\n"));
            }
            case "input" -> {
                instanceComponent.add(String.format("TextField %s;", variable));
                sb.append(String.format("%s = new TextField();", variable));
                sb.append("\n");
                sb.append(String.format("%s.setId(\"%s\");", variable, id));
                sb.append("\n");
                sb.append(String.format("%s.getElement().setAttribute(\"style\", \"%s\");", variable, style));
                sb.append("\n");

                actionListener.add(String.format("%s.addValueChangeListener(tf -> {\n" +
                        "                    System.out.println(\"%s Invoked !!!\");\n" +
                        "                });", variable, variable).concat("\n"));
            }
        }
        return sb.toString();
    }

    public static String getInstanceComponentTemplate(){
        return instanceComponent.stream().collect(Collectors.joining());
    }

    public static String getActionListenerTemplate(){
        return actionListener.stream().collect(Collectors.joining());
    }
}
