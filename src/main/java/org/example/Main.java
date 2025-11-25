package org.example;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Main {
    public static void main(String[] args) {
        MyConfiguration myConfiguration = MyConfiguration.getInstance();
        System.out.println("Api key = " + myConfiguration.getProperty("API_KEY"));
    }
}