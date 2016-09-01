
package irt.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import irt.IrtGuiProperties;
import irt.controllers.ScheduledNodeAbstract;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
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

	public static List<Menu> createMamu(String startsWith, EventHandler<ActionEvent> onActionMenuItem) {
		final Properties properties = IrtGuiProperties.selectFromProperties(startsWith);
		final Set<Entry<Object, Object>> entrySet = properties.entrySet();

		//Create menus
		List<SimpleEntry<Menu, ToggleGroup>> ms = entrySet
														.parallelStream()
														.filter(en->!((String)en.getKey()).contains("items"))
														.filter(en->((String)en.getKey()).contains("name"))
														.map(en->{
															Menu m = new Menu((String) en.getValue());
															m.setUserData(((String)en.getKey()).replace("name", ""));
															return m;
														})
														.map(m->new AbstractMap.SimpleEntry<>(m, new ToggleGroup()))
														.collect(Collectors.toList());
		
		//Create MenuItems
		ms
		.parallelStream()
		.forEach(sa->{
			final Menu menu = sa.getKey();
			final Properties ps = IrtGuiProperties.selectFromProperties(properties, menu.getUserData() + "items");
			final Set<Entry<Object, Object>> es = ps.entrySet();
			List<RadioMenuItem> mis = es
										.stream()
										.filter(en->((String)en.getKey()).contains("name"))
										.map(en->{
											RadioMenuItem mi = new RadioMenuItem((String) en.getValue());
											mi.setToggleGroup(sa.getValue());
											mi.setUserData(ps.getProperty(((String) en.getKey()).replace("name", "fxml")));
											mi.setOnAction(onActionMenuItem);
											return mi;
										})
										.sorted((m1, m2)->m1.getText().compareTo(m2.getText()))
										.collect(Collectors.toList());
			menu.getItems().addAll(mis);
		});

		return ms
				.parallelStream()
				.map(sa->sa.getKey())
				.collect(Collectors.toList());
	}

	public static String getResourcePath(URL url){
		String base = GuiUtility.class.getResource("/").getPath();
		String path = Optional.ofNullable(url).map(URL::getPath).orElse("");
		final String res = new File(base).toURI().relativize(new File(path).toURI()).toString();
		return res.indexOf("file")==0 ? res : '/' + res;
	}
}
