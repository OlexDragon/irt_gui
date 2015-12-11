
package irt.gui.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class GuiUtility {

	/**
	 * Return Files or JarEntries from the path
	 * @param resourceFolder - path to the resource folder
	 * @return - List of Files or JarEntries
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<Object> getResourceFiles(String resourceFolder) throws IOException, URISyntaxException {
		
		final File jarFile = new File(GuiUtility.class.getProtectionDomain().getCodeSource().getLocation().getPath());

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
		    final URL url = GuiUtility.class.getResource(resourceFolder);
		    if (url != null) {
		            return Arrays
		            		.stream(new File(url.toURI()).listFiles())
		            		.collect(Collectors.toList());
		    }
		}
		return null;
	}

}
