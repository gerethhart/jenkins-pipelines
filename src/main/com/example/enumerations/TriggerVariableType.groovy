package com.example.enumerations

enum TriggerVariableType {
    XPATH("XPath"),
    JSONPATH("JSONPath");

    String value;

    TriggerVariableType(String value) {
        this.value = value
    }
}