package com.webonise.tomcat8.redisession;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import com.webonise.tomcat8.redisession.redisclient.Redis;

public class RedisSessionManager extends RedisSessionManagerBase {
  private static final Log LOG = LogFactory.getLog(RedisSessionManager.class);

  private volatile String host;
  private volatile int port;
  private volatile int database;
  private volatile int timeout;
  private volatile String password;

  public RedisSessionManager() {
    // TODO load properties from webapp
    // String firstLocation = context.getServletContext().getRealPath("/redis.properties");
    String secondLocation = System.getProperty("catalina.base") + "/conf/redis.properties";
    
    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new SystemConfiguration());
    // addConfigIfFileExists(config, firstLocation);
    addConfigIfFileExists(config, secondLocation);
    
    setHost(config.getString("redis.host", Redis.DEFAULT_HOST));
    setPort(config.getInt("redis.port", Redis.DEFAULT_PORT));
    setTimeout(config.getInt("redis.timeout", Redis.DEFAULT_TIMEOUT));
    setDatabase(config.getInt("redis.database", Redis.DEFAULT_DATABASE));
    setPassword(config.getString("redis.password"));
    setMaxInactiveInterval(config.getInt("redis.max.inactive", Long.valueOf(TimeUnit.HOURS.toSeconds(1)).intValue()));
    setMetakeyExpire(config.getInt("redis.metakey.expire", Long.valueOf(TimeUnit.HOURS.toSeconds(1)).intValue()));
    startRedis();
  }

  private void addConfigIfFileExists(CompositeConfiguration config, String filename) {
    if (StringUtils.isBlank(filename)) {
      return;
    }
    File file = new File(filename);
    if (file.exists()) {
      try {
        config.addConfiguration(new PropertiesConfiguration(file));
        LOG.info("Loaded config from file [" + file.getAbsolutePath() + "]");
      } catch (ConfigurationException e) {
        LOG.warn("Could not load config file [" + file.getAbsolutePath() + "], " + e.getMessage());
      }
    } else {
      LOG.info("Skip loading config file [" + file.getAbsolutePath() + "]");
    }
  }

  private void startRedis() {
    LOG.info("Starting redis managed sessions");
    setRedis(new Redis(host, port, timeout, password, database));
  }
  
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    if (port < 0 || port > 0xFFFF) {
      port = Redis.DEFAULT_PORT;
    }
    this.port = port;
  }

  public int getDatabase() {
    return database;
  }

  public void setDatabase(int database) {
    if (database < 0) {
      database = Redis.DEFAULT_PORT;
    }
    this.database = database;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    if (timeout < 0) {
      timeout = Redis.DEFAULT_TIMEOUT;
    }
    this.timeout = timeout;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    if (StringUtils.isBlank(host)) {
      host = Redis.DEFAULT_HOST;
    }
    this.host = host;
  }
}