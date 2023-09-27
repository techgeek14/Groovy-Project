package com.demo

static String getTemplate() {
    return 'package $packageInfo;\n' +
            '\n' +
            'import com.vaadin.flow.component.orderedlayout.HorizontalLayout;\n' +
            '$imports\n' +
            '\n' +
            '@PageTitle("HomeScreen")\n' +
            '@Route(value = "/$route")\n' +
            'public class $screen extends HorizontalLayout {\n' +
            '\n' +
            '    private VerticalLayout content = new VerticalLayout();\n' +
            '\n' +
            '    public $screen() {\n' +
            '    }\n' +
            '\n' +
            '    @PostConstruct\n' +
            '    void initComponent() {\n' +
            '        $components\n' +
            '        add(content);\n' +
            '    }\n' +
            '\n' +
            '}'
}





