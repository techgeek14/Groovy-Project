package com.demo

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HtmlContainer
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.util.stream.Stream

String charsetName = "UTF-8"
File source = new File("C:\\Users\\angry\\Desktop\\Groovy-script\\employee.html");
Document doc = Jsoup.parse(source, charsetName);
Element htmlElement = doc;


htmlElement.body().children().forEach {element -> {
    processDomElement(element, "");
}}

void processDomElement(Element element, String parentId) {
    String rootId = StringUtils.isEmpty(parentId) ? VaadinCodeGenerator.generateRefVariable(element) : parentId;
    if (element.attributes().get("parentId").isEmpty()) {
        VaadinCodeGenerator.getComponent(element, rootId);
    }
    element.children().forEach { childElement -> {
        if(! VaadinCodeGenerator.skipElement(element)){
            String childId = VaadinCodeGenerator.generateRefVariable(childElement);
            childElement.attributes().add("parentId", rootId);
            childElement.attributes().add("compType", VaadinCodeGenerator.getElementByTagName(element));
            VaadinCodeGenerator.getComponent(childElement, childId);
            processDomElement(childElement, childId);
        }
      }
    }
}

def template = VaadinTemplate.getTemplate();
def className = "TestScreen";

def binding =
        ["packageInfo": "com.example.application.views.main",
         "imports": VaadinCodeGenerator.generateImports(), "route": "testScreen",
         "className": className, "components": VaadinCodeGenerator.components.toString()]

//def engine = new groovy.text.SimpleTemplateEngine()
//String content = engine.createTemplate(template).make(binding);

def f = new File('templates/VaadinSkeleton.template')
def engine = new groovy.text.GStringTemplateEngine()
def content = engine.createTemplate(f).make(binding)
println content.toString()

def path = "D:\\my-todo\\src\\main\\java\\com\\example\\application\\views\\main\\${className}.java";
try {
    def file = new File(path) // Create a new File object
    file.text = content
    println "Data written to $path successfully."
} catch (Exception e) {
    println "An error occurred: $e"
}

