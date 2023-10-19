package com.demo

import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper();
def jsonContent = jsonSlurper.parse(new File("C:\\Users\\r.raju\\Desktop\\screen1.json"));

Map<String, String> classDetails = new HashMap<>();
classDetails.put("screen", jsonContent.screen);
classDetails.put("packageName", jsonContent.packageName);
classDetails.put("routeUrl", jsonContent.routeUrl);
classDetails.put("pageTitle", jsonContent.pageTitle);

List<Map<String, Object>> contentHolder = List.of(jsonContent.header, jsonContent.sideBar, jsonContent.body, jsonContent.footer);
//Map<String, Object> bodyMap = jsonContent.body;

//bodyMap.forEach {k, v -> {
//    CodeGenerator.extractNode(bodyMap);
//}}


def content = CodeGenerator.generateComponent(classDetails, contentHolder);
def path = "D:\\my-todo\\src\\main\\java\\com\\example\\application\\views\\main\\${jsonContent.screen}.java";
try {
    def file = new File(path) // Create a new File object
    file.text = content;
    println "Data written to $path successfully."
} catch (Exception e) {
    println "An error occurred: $e"
}