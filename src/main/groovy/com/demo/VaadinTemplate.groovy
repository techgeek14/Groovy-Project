package com.demo

static String getTemplate() {
    return 'package $packageInfo;\n' +
            '\n' +
            'import com.vaadin.flow.component.orderedlayout.VerticalLayout;\n' +
            'import com.vaadin.flow.component.orderedlayout.HorizontalLayout;\n' +
            '$imports\n' +
            '\n' +
            '@PageTitle("HomeScreen")\n' +
            '@Route(value = "/$route")\n' +
            'public class $className extends HorizontalLayout {\n' +
            '\n' +
            '    private VerticalLayout content = new VerticalLayout();\n' +
            '\n' +
            '    public $className() {\n' +
            '    }\n' +
            '\n' +
            '    @PostConstruct\n' +
            '    void initComponent() {\n' +
            '        $components' +
            '        add(content);\n' +
            '    }\n' +
            '\n' +
            '}'
}





