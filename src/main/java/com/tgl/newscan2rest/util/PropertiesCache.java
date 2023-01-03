package com.tgl.newscan2rest.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesCache {

	private static final Logger logger = LogManager.getLogger(PropertiesCache.class);

    private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
	public static final String PROPS_FILE_NAME = "tgl-scan.properties";

    public enum PROP_KEY {
    	UPDATE_MAVEN_URL("update.maven.url"),
    	UPDATE_SCAN_APP_URL("update.scan.app.url"),
    	UPDATE_SCAN_CONFIG_URL("update.scan.config.url"),
    	UPDATE_CONN_TIMEOUT("update.connRead.timeout"),
    	UI_WIN_X("ui.window.x"),
    	UI_WIN_Y("ui.window.y"),
    	UI_WIN_WIDTH("ui.window.width"),
    	UI_WIN_HEIGHT("ui.window.height"),
    	UI_DIVIDER_POS("ui.divider.width"),
    	EBAO_HOST("ebao.host"),
//    	EBAO_TEST_HOST("ebao.host.test"), // 開發測試用隱藏屬性
    	EBAO_USER_NAME("ebao.userName"),
    	UPLOAD_LOG_WHEN_SUCCESS("upload.logWhenSuccess"),
    	PAGE_WARNING_WIN_X("page.warning.window.x"),
    	PAGE_WARNING_WIN_Y("page.warning.window.y"),
    	PAGE_WARNING_WIN_WIDTH("page.warning.window.width"),
    	PAGE_WARNING_WIN_HEIGHT("page.warning.window.height"),
    	UPLOAD_WIN_X("upload.window.x"),
    	UPLOAD_WIN_Y("upload.window.y"),
    	UPLOAD_WIN_WIDTH("upload.window.width"),
    	UPLOAD_WIN_HEIGHT("upload.window.height");

        private String propName;

        PROP_KEY(String _propName) {
            this.propName = _propName;
        }

        public String propName() {
            return propName;
        }
    }

	private static PropertiesCache instance  = new PropertiesCache();

	public static PropertiesCache getInstance() {
		return PropertiesCache.instance;
	}

	private PropertiesCache() {
		// Private constructor to restrict new instances
		reload();
	}

	private final Properties properties = new Properties() {
		private static final long serialVersionUID = 1L;

		@Override
		public Set<Object> keySet() {
			return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
		}

		@Override
		public Set<Map.Entry<Object, Object>> entrySet() {
			Set<Map.Entry<Object, Object>> set1 = super.entrySet();
			Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<Map.Entry<Object, Object>>(set1.size());

			Iterator<Map.Entry<Object, Object>> iterator = set1.stream().sorted(new Comparator<Map.Entry<Object, Object>>() {
				@Override
				public int compare(java.util.Map.Entry<Object, Object> o1, java.util.Map.Entry<Object, Object> o2) {
					return o1.getKey().toString().compareTo(o2.getKey().toString());
				}
			}).iterator();

			while (iterator.hasNext())
				set2.add(iterator.next());

			return set2;
		}

		@Override
		public synchronized Enumeration<Object> keys() {
			return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		}
	};
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public Set<String> getAllPropertyNames() {
		return properties.stringPropertyNames();
	}

	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	public Object removeProperty(String key) {
		return properties.remove(key);
	}

	public void reload() {
        Path propertiesPath = Paths.get(CONFIG_DIR + File.separator + PROPS_FILE_NAME);
		try ( BufferedReader reader = Files.newBufferedReader(propertiesPath, StandardCharsets.UTF_8) ) {
		    properties.load(reader);
		} catch (NoSuchFileException e) {
			logger.error("Properties file " + PROPS_FILE_NAME + " not found!");
        } catch (IOException e) {
        	logger.error("Failed to load properties file " + PROPS_FILE_NAME + " !", e);
		}
    	validateProperties();
	}

	public void flush() throws IOException {
        Path imageArchivePath = Paths.get(CONFIG_DIR);
        Path scanConfigPath = Paths.get(CONFIG_DIR + File.separator + PROPS_FILE_NAME);

        if (!Files.isDirectory(imageArchivePath))
            Files.createDirectory(imageArchivePath);

        try ( Writer out = Files.newBufferedWriter(scanConfigPath, StandardCharsets.UTF_8) ) {
			properties.store(out, "TGL-Scan Application configuration properties");
        }
	}

	private void validateProperties() {
		boolean syncPropFile = false;
		String value = null;
		for (PROP_KEY key : PROP_KEY.values()) {
			if (containsKey(key.propName())) {
				value = getProperty(key.propName());
				switch (key) {
				case UI_WIN_X:
				case UI_WIN_Y:
				case UI_WIN_WIDTH:
				case UI_WIN_HEIGHT:
				case UPDATE_CONN_TIMEOUT:
					if (null!=value && value.trim().length()>0) {
						try {
							Integer.parseInt(value);
						} catch (Exception e) {
							logger.error("Property " + key.propName() + " must be numeric!");
							setProperty(key.propName(), "");
							syncPropFile = true;
						}
					} else if (null!=value && value.trim().length()==0) {
						setProperty(key.propName(), "");
						syncPropFile = true;
					}
					break;
				case UI_DIVIDER_POS:
					if (null!=value && value.trim().length()>0) {
						try {
							Float.parseFloat(value);
						} catch (Exception e) {
							logger.error("Property " + key.propName() + " must be numeric!");
							setProperty(key.propName(), "");
							syncPropFile = true;
						}
					} else if (value.length()>0 && value.trim().length()==0) {
						setProperty(key.propName(), "");
						syncPropFile = true;
					}
					break;
				case UPLOAD_LOG_WHEN_SUCCESS:
					if (null!=value && value.trim().length()>0) {
						try {
							Boolean.parseBoolean(value);
						} catch (Exception e) {
							logger.error("Property " + key.propName() + " must be true or false!");
							setProperty(key.propName(), "");
							syncPropFile = true;
						}
					} else if (value.length()>0 && value.trim().length()==0) {
						setProperty(key.propName(), "false");
						syncPropFile = true;
					}
					break;
				default:
					break;
				}
			} else {
				setProperty(key.propName(), "");
				syncPropFile = true;
			}
		}

		if (syncPropFile) {
			try {
				flush();
			} catch (IOException e) {
				logger.error("Failed to write properties file " + PROPS_FILE_NAME + " !");
			}
		}
	}

}
