package xdi2.core.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginClassLoader extends URLClassLoader {

	private static final Logger log = LoggerFactory.getLogger(PluginClassLoader.class);

	public PluginClassLoader(File[] files, ClassLoader parent) throws IOException {

		super(new URL[0], parent);

		try {

			for (File file : files) this.addJarResource(file);
		} catch (IOException ex) {

			log.error(ex.getMessage(), ex);
			throw ex;
		} catch (Throwable ex) {

			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static boolean isJar(String fileName) {

		return fileName != null && fileName.toLowerCase().endsWith(".jar");
	}

	private static File jarEntryAsFile(JarFile jarFile, JarEntry jarEntry) throws IOException {

		InputStream input = null;
		OutputStream output = null;

		try {

			String name = jarEntry.getName().replace('/', '_');
			int i = name.lastIndexOf(".");
			String extension = i > -1 ? name.substring(i) : "";

			File file = File.createTempFile(name.substring(0, name.length() - extension.length()) + ".", extension);
			file.deleteOnExit();
			input = jarFile.getInputStream(jarEntry);
			output = new FileOutputStream(file);

			int readCount;
			byte[] buffer = new byte[4096];

			if (log.isDebugEnabled()) log.debug("Writing temp file: " + file);

			while ((readCount = input.read(buffer)) != -1) {

				output.write(buffer, 0, readCount);
			}

			return file;
		} finally {

			if (input != null) try { input.close(); } catch (IOException ex) { }
			if (output != null) try { output.close(); } catch (IOException ex) { }
		}
	}

	private void addJarResource(File file) throws IOException {

		if (log.isDebugEnabled()) log.debug("Adding .jar to classpath: " + file.getAbsolutePath());

		JarFile jarFile = new JarFile(file);
		addURL(file.toURI().toURL());

		Enumeration<JarEntry> jarEntries = jarFile.entries();

		while (jarEntries.hasMoreElements()) {

			JarEntry jarEntry = jarEntries.nextElement();

			log.trace("Found .jar entry: " + jarEntry + " (directory? " + jarEntry.isDirectory() + ", jar? " + isJar(jarEntry.getName()) + ")");

			if (! jarEntry.isDirectory() && isJar(jarEntry.getName())) {

				addJarResource(jarEntryAsFile(jarFile, jarEntry));
			}
		}

		jarFile.close();
	}

	@Override  
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		try {

			Class<?> clazz = findLoadedClass(name);
			if (clazz == null) {

				clazz = findClass(name);

				if (resolve) resolveClass(clazz);
			}

			return clazz;
		} catch (ClassNotFoundException ex) {

			return super.loadClass(name, resolve);
		}
	}
}