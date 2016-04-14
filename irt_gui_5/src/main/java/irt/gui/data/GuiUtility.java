
package irt.gui.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.components.ScheduledNodeAbstract;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

public class GuiUtility {

	/**
	 * Return Files or JarEntries from the path
	 * @param resourceFolder - path to the resource folder
	 * @return - List of Files or JarEntries
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<Object> getResourceFiles(final String resourceFolder) throws IOException, URISyntaxException {
		
		final String path = GuiUtility.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		final File jarFile = new File(path);

		if(jarFile.isFile()) {  // Run with JAR file
			try(final JarFile jar = new JarFile(jarFile);){

				return jar
						.stream()
						.filter(entry->entry.getName().startsWith(resourceFolder))
						.filter(entry->!entry.getName().equals(resourceFolder))
						.map(entry->{
							final String name = entry.getName().replace(resourceFolder, "");
							return new JarEntry(name);
						})
						.collect(Collectors.toList());
			}
		} else { // Run with IDE

			final String name = resourceFolder.startsWith("\\") || resourceFolder.startsWith("/") ? resourceFolder : "/" + resourceFolder;
			final URL url = GuiUtility.class.getResource(name);
		    if (url != null) {
		            return Arrays
		            		.stream(new File(url.toURI()).listFiles())
		            		.collect(Collectors.toList());
		    }
		}
		return null;
	}

	public static void createMamuItems(String startsWith, EventHandler<ActionEvent> action, ObservableList<MenuItem> menuItems) {

		final ToggleGroup toggleGroup = new ToggleGroup();

		Properties properties = IrtGuiProperties.selectFromProperties(startsWith);

		List<MenuItem> menus = properties
							.entrySet()
							.parallelStream()
							.filter(p->((String)p.getKey()).endsWith(ScheduledNodeAbstract.NAME))
							.map(p->{
								final RadioMenuItem mi = new RadioMenuItem((String) p.getValue()); //This can be changed by internalization
								final String key = (String)p.getKey();
								mi.setId(key.substring(0, key.indexOf(ScheduledNodeAbstract.NAME)));
								Platform.runLater(()->{
									mi.setToggleGroup(toggleGroup);
									mi.setOnAction(action);
								});
								return mi;
							})
							.sorted((mi1, mi2)->mi1.getText().compareTo(mi2.getText()))
							.collect(Collectors.toList());

		menuItems.addAll(menus);
	}
}
