package rummy;

import java.util.Properties;
import rummy.engine.RummyGameFactory;

public class Driver {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/game1.properties";

    public static void main(String[] args) {
        final Properties properties = PropertiesLoader.loadPropertiesFile(DEFAULT_PROPERTIES_PATH);
        String logResult = RummyGameFactory.create(properties).runApp();
        System.out.println("logResult = " + logResult);
    }

}