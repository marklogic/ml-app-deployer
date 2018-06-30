package com.marklogic.appdeployer.command;

import com.marklogic.client.ext.helper.LoggingObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Defines operations for managing whether a module needs to be installed or not.
 */
public class ResourceFileManagerImpl extends LoggingObject implements ResourceFileManager {

	public static final String DEFAULT_FILE_PATH = "build/ml-gradle/resource-timestamps.properties";

	private static MessageDigest md5Digest;
	static {
		try {
			md5Digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private Properties props;
	private String hashFilePath;

	public ResourceFileManagerImpl() {
		this(DEFAULT_FILE_PATH);
	}

	public ResourceFileManagerImpl(String propertiesFilePath) {
		props = new Properties();
		this.hashFilePath = propertiesFilePath;
		initialize();
	}

	@Override
	public void initialize() {
		File propertiesFile = new File(hashFilePath);
		logger.info("Loading properties from: " + propertiesFile.getAbsolutePath());
		propertiesFile.getParentFile().mkdirs();
		if (propertiesFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(propertiesFile);
				if (logger.isDebugEnabled()) {
					logger.debug("Loading properties from: " + propertiesFile.getAbsolutePath());
				}
				props.load(fis);
			} catch (Exception e) {
				logger.warn("Unable to load properties, cause: " + e.getMessage());
			} finally {
				try {
					fis.close();
				} catch (Exception e) {
					logger.warn(e.getMessage());
				}
			}
		}
	}

	@Override
	public boolean hasFileBeenModifiedSinceLastDeployed(File file) {
		String lastDeployedChecksum = null;
		try {
			lastDeployedChecksum = computeFileChecksum(md5Digest, file);
		} catch (IOException ie) {
			throw new RuntimeException("Unable to compute hash on file from path: " + file.getAbsolutePath() + "; cause: " + ie.getMessage(), ie);
		}
		if (lastDeployedChecksum != null) {
			String key = buildKey(file);
			String lastChecksum = props.getProperty(key);
			if (logger.isDebugEnabled()) {
				logger.debug("lastDeployedChecksum: " + lastDeployedChecksum);
				logger.debug("key: " + key);
				logger.debug("lastChecksum: " + lastChecksum);
			}
			if (lastChecksum != null) {
				return !lastChecksum.equals(lastDeployedChecksum);
			} else {
				return true;
			}
		} else {
			logger.info("Hmm, really shouldn't get here. lastDeployedChecksum should never be null here. Should have thrown an exception if there was a problem. Probably need to refactor this out.");
			return false;
		}
	}

	@Override
	public void saveLastDeployedHash(File file) {
		String key = buildKey(file);
		String fileChecksum = null;
		try {
			fileChecksum = computeFileChecksum(md5Digest, file);
		} catch (IOException ie) {
			throw new RuntimeException("Unable to compute hash on file from path: " + file.getAbsolutePath() + "; cause: " + ie.getMessage(), ie);
		}
		props.setProperty(key, fileChecksum);
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(hashFilePath));
			props.store(fw, "");
		} catch (Exception e) {
			logger.warn("Unable to store properties, cause: " + e.getMessage());
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}
	}

	private String computeFileChecksum(MessageDigest digest, File file) throws IOException {
		//Get file input stream for reading the file content
		FileInputStream fis = new FileInputStream(file);

		//Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		//Read file data and update in message digest
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		};

		//close the stream; We don't need it now.
		fis.close();

		//Get the hash's bytes
		byte[] bytes = digest.digest();

		//This bytes[] has bytes in decimal format;
		//Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		//return complete hash
		return sb.toString();
	}

	/**
	 * Lower-casing avoids some annoying issues on Windows where sometimes you get "C:" at the start, and other times
	 * you get "c:". This of course will be a problem if you for some reason have modules with the same names but
	 * differing in some cases, but I'm not sure why anyone would do that.
	 *
	 * @param file
	 * @return
	 */
	protected String buildKey(File file) {
		return file.getAbsolutePath().toLowerCase();
	}
}
