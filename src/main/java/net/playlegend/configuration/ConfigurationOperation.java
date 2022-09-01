package net.playlegend.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationOperation {

    private final String path;
    private final String filename;

    public ConfigurationOperation(String path, String filename) {
        this.path = path;
        this.filename = filename;
    }

    public void copyIfNotExist() throws IOException {
        File file = new File(path, filename);
        if (file.exists())
            return;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null)
                throw new IOException("Could not find resource: " + filename);

            FileUtils.copyInputStreamToFile(is, file);
        }
    }

    public YamlConfiguration loadAsYaml() throws IOException {
        File file = new File(path, filename);
        if (!file.exists()) {
            // should not happen as with plugin load the resources are getting copied into the plugin directory
            throw new IOException("Could not find resource in plugin directory!");
        }

        return YamlConfiguration.loadConfiguration(file);
    }

}
