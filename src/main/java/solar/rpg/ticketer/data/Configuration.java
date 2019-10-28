package solar.rpg.ticketer.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * A simple configuration system that is used to load configuration values.
 *
 * @author Joshua Skinner
 * @version 1
 * @since 0.1
 */
public final class Configuration {

    private final HashMap<String, String> configValues;

    public Configuration(String fileName, String... requiredKeys) {
        configValues = new HashMap<>();
        try {
            // Create a buffered reader to read over the configuration file.
            new BufferedReader(new InputStreamReader(getConfigStream(fileName), StandardCharsets.UTF_8)).lines().forEach(this::parseLine);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            // Print the error because it is not something that should happen in normal usage.
        }

        // Validate the configuration to ensure that the required keys exist within this configuration.
        validate(requiredKeys);
    }

    /**
     * @param key Key associated with value.
     * @return A string value.
     */
    public String getString(String key) {
        return configValues.get(key);
    }

    /**
     * @param key Key associated with value.
     * @return A string value.
     * @throws NumberFormatException May not be a number. Be careful!
     */
    public Integer getInteger(String key) {
        return Integer.parseInt(configValues.get(key));
    }

    /**
     * Attempt to use existing configuration file, otherwise it will
     * take the default configuration resource file and copy it outside
     * the JAR which can then be properly configured by the user.
     *
     * @param fileName What the file is called.
     * @return An Input Stream belonging to the settings file.
     * @throws IOException File may not exist, etc.
     */
    private InputStream getConfigStream(String fileName) throws IOException {
        // Find out what folder the JAR file is located in.
        String parentDirectory = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().replace("%20", " ");

        File config = new File(String.format("%s/%s", parentDirectory, fileName));
        if (!config.exists())
            // Generate the config file and try again.
            generateConfigFile(Configuration.class.getResourceAsStream(String.format("/%s", fileName)), fileName, parentDirectory);

        if (config.exists())
            // The config file exists! Let's load it!
            return new FileInputStream(config);
        else throw new IllegalStateException("Config file could not be found and default could not be loaded");
    }

    /**
     * Copies the settings resource file outside of the JAR for usage.
     * This way the end-user can modify the settings themselves without
     * hacking into the JAR.
     *
     * @param defaultConfig The Input Stream of the settings resource file.
     * @param fileName      What to call the file.
     */
    private void generateConfigFile(InputStream defaultConfig, String fileName, String parentDirectory) {
        try {
            // Copy the resource configuration file to the directory that the JAR file is in.
            Files.copy(defaultConfig, Paths.get(String.format("%s/%s", parentDirectory, fileName)));
        } catch (Exception e) {
            e.printStackTrace();
            // We were unable to copy over the default settings file for usage.
        }
    }

    /**
     * Parses a single line. Copies the key and value into the Hash Map.
     *
     * @param line The line to parse.
     */
    private void parseLine(String line) {
        // Ignore commented out lines.
        if (line.startsWith("#")) return;

        String[] keyVal = line.split("=");
        // We only want a key and value, split by an '=' sign.
        if (keyVal.length != 2)
            throw new IllegalArgumentException(String.format("Expected a key and value, received %d strings", keyVal.length));

        // Retrieve, and store configuration values.
        String key = keyVal[0].trim();
        String value = keyVal[1].trim();
        configValues.put(key, value);
    }

    /**
     * After adding in all the keys and values, double check
     * that everything the program needs has been added.
     *
     * @param requiredKeys A list of required keys.
     */
    private void validate(String[] requiredKeys) {
        for (String requiredKey : requiredKeys)
            if (!configValues.containsKey(requiredKey))
                throw new IllegalArgumentException(String.format("Required key '%s' not found in configuration", requiredKey));
    }
}