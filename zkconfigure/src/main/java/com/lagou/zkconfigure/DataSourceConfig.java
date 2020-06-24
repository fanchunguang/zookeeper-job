package com.lagou.zkconfigure;

/**
 * 连接池配置对象
 */
public class DataSourceConfig {
    private String url;
    private String username;
    private String password;

    //可选属性
    private Integer initialSize;  //初始连接数，默认0
    private Integer maxActive; //最大连接数，默认8
    private Integer minIdle; //最小闲置数
    private Integer maxWait; ////获取连接的最大等待时间，单位毫秒

    public DataSourceConfig() {}

    public DataSourceConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public DataSourceConfig(String url, String username, String password, Integer initialSize, Integer maxActive, Integer minIdle, Integer maxWait) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.initialSize = initialSize;
        this.maxActive = maxActive;
        this.minIdle = minIdle;
        this.maxWait = maxWait;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

}
