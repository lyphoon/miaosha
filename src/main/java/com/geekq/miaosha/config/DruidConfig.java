package com.geekq.miaosha.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Data
@Configuration
@ConfigurationProperties(prefix="spring.datasource")
public class DruidConfig {

	private String url;
	private String username;
	private String password;
	private String driverClassName;
	private String type;
	private String filters;
	private int maxActive;
	private int initialSize;
	private int minIdle;
	private long maxWait;
	private long timeBetweenEvictionRunsMillis;
	private long minEvictableIdleTimeMillis;
	private String validationQuery;
	private boolean testWhileIdle;
	private boolean testOnBorrow;
	private boolean testOnReturn;
	private boolean poolPreparedStatements;
	private int maxOpenPreparedStatements;

    /**
     * 这个地方是配置了druid的可视化界面地址，可以通过它来查看所有执行的sql的情况
     * @return
     */
	@Bean
	public ServletRegistrationBean druidSverlet() {
		ServletRegistrationBean reg = new ServletRegistrationBean();
		reg.setServlet(new StatViewServlet());  //Druid自己的处理器
		reg.addUrlMappings("/druid/*");
		reg.addInitParameter("loginUsername", "joshua");
		reg.addInitParameter("loginPassword", "123456");
		reg.addInitParameter("logSlowSql", "true");  //显示慢的sql
		reg.addInitParameter("slowSqlMillis", "1000");  //设置慢sql的标准
		return reg;
	}

    /**
     * 配置数据源，这个地方使用了Druid的数据源
     *
     * DataSource数据源是一个接口，核心的功能是获取数据库连接（getConnection(...)）
     */
	@Bean
	public DataSource druidDataSource() {
		 	DruidDataSource datasource = new DruidDataSource();
	        datasource.setUrl(url);
	        datasource.setUsername(username);
	        datasource.setPassword(password);
	        datasource.setDriverClassName(driverClassName);
	        datasource.setInitialSize(initialSize);
	        datasource.setMinIdle(minIdle);
	        datasource.setMaxActive(maxActive);
	        datasource.setMaxWait(maxWait);
	        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	        datasource.setValidationQuery(validationQuery);
	        datasource.setTestWhileIdle(testWhileIdle);
	        datasource.setTestOnBorrow(testOnBorrow);
	        datasource.setTestOnReturn(testOnReturn);
	        datasource.setPoolPreparedStatements(poolPreparedStatements);
	        datasource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
	        try {
	            datasource.setFilters(filters);  //druid内置有一个StateFilter，用于统计监控数据
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return datasource;
	}
	

	
}
