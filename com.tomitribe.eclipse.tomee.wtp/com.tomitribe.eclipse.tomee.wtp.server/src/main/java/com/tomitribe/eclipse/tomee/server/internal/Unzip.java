package com.tomitribe.eclipse.tomee.server.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

	private static final int BUFFER_SIZE = 8192;

	public static void unzip(File source, File destination) throws IOException {
		FileInputStream fis = null;
		fis = new FileInputStream(source);

		unzip(fis, destination);
	}

	public static void unzip(URL url, File destination) throws IOException {
		unzip(url.openStream(), destination);
	}

	public static void unzip(InputStream is, File destination)
			throws IOException {
		ZipInputStream zis = null;

		try {
			zis = new ZipInputStream(new BufferedInputStream(is));

			ZipEntry entry = null;

			while ((entry = zis.getNextEntry()) != null) {
				String outputFilename = destination + File.separator + entry.getName();

				System.out.println("Extracting file: " + entry.getName());

				if (entry.isDirectory()) {
					File dir = new File(destination, entry.getName());
					dir.mkdirs();
				} else {
					int count;
					BufferedOutputStream dest = null;

					try {
						byte data[] = new byte[BUFFER_SIZE];

						FileOutputStream fos = new FileOutputStream(outputFilename);
						dest = new BufferedOutputStream(fos, BUFFER_SIZE);

						while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
							dest.write(data, 0, count);
						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							dest.flush();
						} catch (Throwable t) {
						}

						try {
							dest.close();
						} catch (Throwable t) {
						}
					}
				}
			}

			zis.close();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				zis.close();
			} catch (Throwable t) {
			}

			try {
				is.close();
			} catch (Throwable t) {
			}
		}
	}

}
