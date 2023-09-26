package com.demo

import groovy.transform.builder.Builder
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.stream.Stream


String charsetName = "UTF-8"
File source = new File("C:\\Users\\r.raju\\Desktop\\Groovy\\source.html");
Document doc = Jsoup.parse(source, charsetName);
Element htmlElement = doc;


htmlElement.body().children().forEach {element -> {
    printInnerChildren(element, "");
    println "------- End of Parent Node ---------";
}}


public String getElementId(Element element){
    String elementId = (element.id() == null || element.id().isBlank()) ? element.tag().name.concat(":").concat(UUID.randomUUID().toString())
            : element.tag().name.concat(":").concat(element.id());
    return elementId;
}

public void printInnerChildren(Element element, String parentId) {
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

