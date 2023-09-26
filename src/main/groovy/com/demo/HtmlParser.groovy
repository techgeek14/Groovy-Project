package com.demo

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import groovy.transform.builder.Builder
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import javax.tools.JavaCompiler
import javax.tools.ToolProvider
import java.util.stream.Stream


String charsetName = "UTF-8"
File source = new File("C:\\Users\\r.raju\\Desktop\\Groovy\\source.html");
//Document doc = Jsoup.parse(source, charsetName);
//Element htmlElement = doc;


//htmlElement.body().children().forEach {element -> {
//    printInnerChildren(element, "");
//    println "------- End of Parent Node ---------";
//}}


public String getElementId(Element element){
    String elementId = (element.id() == null || element.id().isBlank()) ? element.tag().name.concat(":").concat(UUID.randomUUID().toString())
            : element.tag().name.concat(":").concat(element.id());
    return elementId;
}

public void printInnerChildren(Element element, String parentId) {
    Elements tableElement = element.select("table tbody");
    if(! tableElement.isEmpty()){
        println "Iterating table element";
        tableElement.forEach {x -> {
            println x;
        }}
    }
    String rootId = StringUtil.isBlank(parentId) ? "[" + getElementId(element) + "]": parentId;
    if (element.childrenSize() > 0) {
        println "Root Id: ${rootId} Children Count: ${element.childrenSize()}";
        element.children().forEach {childElement -> {
            String childId = rootId.concat("-").concat(getElementId(childElement));
            println("Child: ${childId}");
            println "-------------------------------------------";
            printInnerChildren(childElement, childId);
        }}
    } else {
        println "Root Id: ${rootId}";
    }
}

Map<String, String> imports = new HashMap<>();
imports.put(Route.getSimpleName(), Route.canonicalName);
imports.put(PageTitle.getSimpleName(), PageTitle.canonicalName);
imports.put("PostConstruct", "import jakarta.annotation.PostConstruct;");


public void updateImports(Object component, Map<String, String> map) {
    switch (component){
        case Button -> map.containsKey(Button.getSimpleName()) ? null : map.put(Button.getSimpleName(), Button.canonicalName);
        case Grid -> map.containsKey(Grid.getSimpleName()) ? null : map.put(Grid.getSimpleName(), Grid.canonicalName);
        case Div -> map.containsKey(Div.getSimpleName()) ? null : map.put(Div.getSimpleName(), Div.canonicalName);
        case HorizontalLayout -> map.containsKey(HorizontalLayout.getSimpleName()) ? null : map.put(HorizontalLayout.getSimpleName(), HorizontalLayout.canonicalName);
        case TextField -> map.containsKey(TextField.getSimpleName()) ? null : map.put(TextField.getSimpleName(), TextField.canonicalName);
        case Span -> map.containsKey(Span.getSimpleName()) ? null : map.put(Span.getSimpleName(), Span.canonicalName);
        case Label -> map.containsKey(Label.getSimpleName()) ? null : map.put(Label.getSimpleName(), Label.canonicalName);
        case Notification -> map.containsKey(Notification.getSimpleName()) ? null : map.put(Notification.getSimpleName(), Notification.canonicalName);
        case Key -> map.containsKey(Key.getSimpleName()) ? null : map.put(Key.getSimpleName(), Key.canonicalName);
    }
}

List<Object> componentList =  Stream.of(HorizontalLayout, TextField, Button, Key, Notification);
componentList.forEach {comp -> {
    updateImports(comp, imports);
}}

def text = '$packageInfo\n' +
        '\n' +
        '$imports\n'+
        '@PageTitle("$pageTitle")\n' +
        '@Route(value = "$routeValue")\n' +
        'public class MainView extends HorizontalLayout {\n' +
        '\n' +
        '    private TextField name;\n' +
        '    private Button sayHello;\n' +
        '\n' +
        '    public MainView() {\n' +
        '        name = new TextField("Your name");\n' +
        '        sayHello = new Button("Say hello");\n' +
        '        sayHello.addClickListener(e -> {\n' +
        '            Notification.show("Hello " + name.getValue());\n' +
        '        });\n' +
        '        sayHello.addClickShortcut(Key.ENTER);\n' +
        '\n' +
        '        setMargin(true);\n' +
        '        setVerticalComponentAlignment(Alignment.END, name, sayHello);\n' +
        '\n' +
        '        add(name, sayHello);\n' +
        '    }\n' +
        '\n' +
        '}\n'

def binding =
        ["pageTitle": "HomeScreen", "routeValue": "/home", "packageInfo": "package com.example.application.views.main;", "imports": VaadinTemplate.generateImports(imports)]

//def engine = new groovy.text.SimpleTemplateEngine()
//def template = engine.createTemplate(text).make(binding)
//println template;

String id = UUID.randomUUID().toString().split("-")[0].substring(0, 4);
println VaadinTemplate.componentGenerator("div", id, "color:red; border: 1px;");
println VaadinTemplate.map;
println VaadinTemplate.getInstanceComponentTemplate();
println VaadinTemplate.getActionListenerTemplate();
