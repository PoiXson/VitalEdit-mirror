// Generated for: {{{TITLE}}}
// {{{TIMESTAMP}}}
package com.poixson.{{{NAME-LOWER}}};

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public abstract class PoiXsonPluginLoader {



	public PoiXsonPluginLoader() {
	}



	// -------------------------------------------------------------------------------
	// state enum



	protected static enum LibraryExtractResult {
		FOUND  ('=', "Found",     "green"      ),
		EXTRACT('+', "Extracted", "gold"       ),
		UPDATED('U', "Updated",   "blue"       ),
		REMOVED('-', "Removed",   "dark_purple"),
		ERROR  ('E', "Error",     "red"        ),
;
		public final char   chr;
		public final String title;
		public final String color;

		private LibraryExtractResult(final char chr, final String title, final String color) {
			this.chr   = chr;
			this.title = title;
			this.color = color;
		}

	}



	// extract libraries from plugin.jar:/libs/
	protected void extract_libs(final String path_server,
			final String path_plugin, final String path_libs)
			throws LibraryLoaderException {
		final LinkedList<String> found = new LinkedList<String>();
		final CodeSource source = this.getClass().getProtectionDomain().getCodeSource();
		final String plugin_file = source.getLocation().getPath();
		final TreeMap<String, LibraryExtractResult> results = new TreeMap<String, LibraryExtractResult>();
		JarFile jar_plugin = null;
		try {
			jar_plugin = new JarFile(plugin_file);
			final Enumeration<JarEntry> jar_entries = jar_plugin.entries();
			//LOOP_JAR_ENTRIES:
			while (jar_entries.hasMoreElements()) {
				final JarEntry jar_entry = jar_entries.nextElement();
				final String file_entry = jar_entry.getName();
				// match libs/*.jar
				if (file_entry.startsWith("libs/")
				&&  file_entry.endsWith(".jar")) {
					final String file_local = MergPths(path_plugin, file_entry);
					LibraryExtractResult state = null;
					try {
						state = this.extract_lib(file_local, file_entry, jar_plugin, jar_entry);
						found.addLast(file_local);
					} catch (LibraryLoaderException e) {
						state = LibraryExtractResult.ERROR;
						e.printStackTrace();
					}
					results.put(file_entry, state);
				} // end match libs/*.jar
			} // end LOOP_JAR_ENTRIES
			// remove unused libraries
			final File[] files = (new File(path_libs)).listFiles( (_path, name) -> name.endsWith(".jar") );
			for (final File file : files) {
				final String f = file.toString();
				if (!found.remove(f)) {
					if (!file.delete())
						throw new IOException("Failed to remove unused library: "+f);
					results.put(MakePathRel(path_plugin, f), LibraryExtractResult.REMOVED);
				}
			}
		} catch (IOException e) {
			throw new LibraryLoaderException("Failed to extract library from: "+plugin_file, e);
		} finally {
			if (!results.isEmpty()) {
				final String plugin_name = "VitalCore";
				final StringBuilder msg = (new StringBuilder())
					.append("Libraries for: ").append(MakePathRel(path_server, plugin_file));
				for (final Entry<String, LibraryExtractResult> entry : results.entrySet()) {
					msg.append('\n').append(String.format(
						" [%s] %s %s",
						plugin_name,
						entry.getValue().chr,
						entry.getKey()
					));
				}
				this.log_info(msg.toString());
			}
			try {
				if (jar_plugin != null)
					jar_plugin.close();
			} catch (IOException ignore) {}
		}
	}



	// extract a library
	protected LibraryExtractResult extract_lib(
			final String file_local, final String file_entry,
			final JarFile jar_plugin, final JarEntry jar_entry)
			throws LibraryLoaderException {
		LibraryExtractResult state = null;
		InputStream in_loc = null;
		InputStream in_res = null;
		OutputStream out   = null;
		try {
			if ((new File(file_local)).isFile())
			in_loc = new FileInputStream(file_local);
			in_res = jar_plugin.getInputStream(jar_entry);
			if (in_res == null) throw new LibraryLoaderException("Failed to read library file from plugin jar: "+file_entry);
			final int size = JarEntrySize(jar_entry, file_entry);
			final byte[] data = CompareStreams(in_loc, in_res, size);
			// use existing file
			if (data == null) {
				state = LibraryExtractResult.FOUND;
			} else
			// file needs extracting
			if (in_loc == null) {
				state = LibraryExtractResult.EXTRACT;
			// local file found but needs updating
			} else {
				state = LibraryExtractResult.UPDATED;
			}
			SWITCH_STATE:
			switch (state) {
			case EXTRACT:
			case UPDATED:
				// extract file
				out = new FileOutputStream(new File(file_local), false);
				out.write(data);
				break SWITCH_STATE;
			case FOUND: break SWITCH_STATE;
			default: throw new RuntimeException("Invalid library extraction state: "+state.toString());
			}
		} catch (IOException e) {
			throw new LibraryLoaderException("Failed to extract library: "+file_entry, e);
		} finally {
			if (in_loc != null) { try { in_loc.close(); } catch (IOException ignore) {} }
			if (in_res != null) { try { in_res.close(); } catch (IOException ignore) {} }
			if (out    != null) { try { out   .close(); } catch (IOException ignore) {} }
		}
		return state;
	}



	// -------------------------------------------------------------------------------
	// utility functions



	protected boolean create_dir(final String dir) throws IOException {
		if (!(new File(dir)).isDirectory()) {
			if (!(new File(dir)).mkdir())
				throw new IOException();
			this.log_info("Created dir: "+dir);
			return true;
		}
		return false;
	}



	protected static String MergPths(final String...paths) {
		if (paths.length == 0) return null;
		final LinkedList<String> parts = new LinkedList<String>();
		boolean is_absolute = false;
		// prep parts
		{
			boolean first = true;
			LOOP_PATHS:
			for (final String path : paths) {
				if (path == null || path.isEmpty())
					continue LOOP_PATHS;
				if (first) {
					// absolute path
					if (path.startsWith(File.separator))
						is_absolute = true;
				}
				String pth = path;
				if (File.separatorChar == pth.charAt(0))              pth = pth.substring(1);
				if (File.separatorChar == pth.charAt(pth.length()-1)) pth = pth.substring(pth.length()-1);
				// multiple parts
				if (pth.contains(File.separator)) {
					final String[] array = pth.split(File.separator);
					for (final String p : array) {
						if (p != null && !p.isEmpty()) {
							String add = p;
							LOOP_TRIM:
							while (!add.isEmpty()) {
								switch (add.charAt(0)) {
								case ' ':  case '\t':
								case '\r': case '\n':
									add = add.substring(1);
									continue LOOP_TRIM;
								default: break;
								}
								switch (add.charAt(add.length()-1)) {
								case ' ':  case '\t':
								case '\r': case '\n':
									add = add.substring(1);
									continue LOOP_TRIM;
								default: break;
								}
								break LOOP_TRIM;
							} // end LOOP_TRIM
							if (!add.isEmpty())
								parts.addLast(add);
						}
					}
				// single path part
				} else {
					String add = pth;
					LOOP_TRIM:
					while (!add.isEmpty()) {
						SWITCH_TRIM:
						switch (add.charAt(0)) {
						case ' ':  case '\t':
						case '\r': case '\n':
							add = add.substring(1);
							continue LOOP_TRIM;
						default: break SWITCH_TRIM;
						}
						SWITCH_TRIM:
						switch (add.charAt(add.length()-1)) {
						case ' ':  case '\t':
						case '\r': case '\n':
							add = add.substring(1);
							continue LOOP_TRIM;
						default: break SWITCH_TRIM;
						}
						break LOOP_TRIM;
					} // end LOOP_TRIM
					if (!add.isEmpty())
						parts.addLast(add);
				}
				first = false;
			} // end LOOP_PATHS
		}
		// resolve ../
		int num_parts = parts.size();
		for (int index=0; index<num_parts; index++) {
			final String entry = parts.get(index);
			if ("..".equals(entry)) {
				parts.remove(index);
				if (index > 0)
					parts.remove(--index);
				index--;
				num_parts -= 2;
			}
		}
		// build path
		{
			final StringBuilder path = new StringBuilder();
			if (!parts.isEmpty()) {
				for (final String part : parts) {
					if (part.isEmpty()) continue;
					if (is_absolute
					|| !path.isEmpty())
						path.append(File.separatorChar);
					path.append(part);
				}
			}
			return path.toString();
		}
	}



	protected static String MakePathRel(final String base, final String path) {
		if (path.startsWith(base)) {
			final String pth = path.substring(base.length());
			return (pth.startsWith("/") ? pth.substring(1) : pth);
		} else {
			return path;
		}
 	}



	protected static int JarEntrySize(final JarEntry entry, final String file) {
		final long size = entry.getSize();
		if (size >= Integer.MAX_VALUE) throw new RuntimeException("Library file is abnormally large! "+file);
		return (int) size;
	}



//TODO: improve performance of this
	protected static byte[] CompareStreams(
			final InputStream in_loc, final InputStream in_res,
			final int size) throws IOException {
		if (in_res == null) throw new NullPointerException("in_res");
		final byte[] data = new byte[size];
		int i = 0;
		int byte_loc = 0;
		int byte_res = 0;
		//LOOP_VERIFY:
		while (true) {
			if (in_loc != null)
			byte_loc = in_loc.read();
			byte_res = in_res.read();
			// files differ
			if (in_loc == null
			||  byte_loc != byte_res) {
				if (byte_res < 0 || byte_res > 255) throw new IOException("Invalid byte");
				data[i++] = (byte) byte_res;
				// finish loading file
				while (true) {
					final int byt = in_res.read();
					// finished reading
					if (byt == -1) {
						if (i != size) {
							throw new IOException(String.format(
								"Invalid extracted size: %d expected: %d",
								Integer.valueOf(i), Integer.valueOf(size)
							));
						}
						return data;
					}
					// store to return
					if (byt < 0 || byt > 255) throw new IOException("Invalid byte");
					data[i++] = (byte) byt;
				}
			}
			// finished reading, files match
			if (byte_res == -1)
				return null;
			// store to return if different
			if (byte_res < 0 || byte_res > 255) throw new IOException("Invalid byte");
			data[i++] = (byte) byte_res;
		} // end LOOP_VERIFY
	}



	// -------------------------------------------------------------------------------
	// logging and exceptions



	protected abstract void log_info(final String msg);



	public static class LibraryLoaderException extends Exception {
		private static final long serialVersionUID = 1L;

		public LibraryLoaderException(                                   ) { super(      ); }
		public LibraryLoaderException(final String msg                   ) { super(msg   ); }
		public LibraryLoaderException(                  final Throwable e) { super(     e); }
		public LibraryLoaderException(final String msg, final Throwable e) { super(msg, e); }

	}



}
