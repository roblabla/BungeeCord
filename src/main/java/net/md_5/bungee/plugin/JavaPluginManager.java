package net.md_5.bungee.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import lombok.Getter;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.event.Event;
import net.md_5.bungee.event.EventManager;
import net.md_5.bungee.event.HandlerList;
import net.md_5.bungee.event.SimpleEventManager;

/**
 * Plugin manager to handle loading and saving other JavaPlugin's. This class is
 * itself a plugin for ease of use.
 */
public class JavaPluginManager
{

    /**
     * Set of loaded plugins.
     */
    @Getter
    private final Set<JavaPlugin> plugins = new HashSet<>();
    private static final Pattern pattern = Pattern.compile(".*\\.jar");
    private final EventManager eventManager = new SimpleEventManager($());

    /**
     * Load all plugins from the plugins folder. This method must only be called
     * once per instance.
     */
    public void loadPlugins()
    {
        File dir = new File("plugins");
        dir.mkdir();

        for (File file : dir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return pattern.matcher(name).matches();
            }
        }))
        {
            try
            {
                JarFile jar = new JarFile(file);
                ZipEntry entry = jar.getEntry("plugin.yml");
                if (entry == null)
                {
                    throw new InvalidPluginException("Jar does not contain a plugin.yml");
                }

                PluginDescription description;
                try (InputStream is = jar.getInputStream(entry))
                {
                    description = PluginDescription.load(is);
                }
                URLClassLoader classloader = new URLClassLoader(new URL[]
                        {
                            file.toURI().toURL()
                        }, getClass().getClassLoader());
                Class<?> clazz = Class.forName(description.getMain(), true, classloader);
                Class<? extends JavaPlugin> subClazz = clazz.asSubclass(JavaPlugin.class);
                JavaPlugin plugin = subClazz.getDeclaredConstructor().newInstance();

                plugin.description = description;
                plugin.onEnable();
                plugins.add(plugin);

                $().info("Loaded plugin: " + plugin.description.getName());
            } catch (Exception ex)
            {
                $().severe("Could not load plugin: " + file);
                ex.printStackTrace();
            }
        }
        HandlerList.bakeAll();
    }

    public void disablePlugins()
    {
        for (JavaPlugin p : plugins)
        {
            p.onDisable();
            HandlerList.unregisterAll(p);
        }
    }

    public <T extends Event> T callEvent(T event)
    {
        return eventManager.callEvent(event);
    }
}
