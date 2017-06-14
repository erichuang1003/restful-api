package com.kyy.demo.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
@EnableTransactionManagement
public class TranscationConfig {

	@Autowired
	private DataSource dataSource;

	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public TransactionInterceptor transactionInterceptor() {
		TransactionInterceptor interceptor = new TransactionInterceptor();
		interceptor.setTransactionManager(transactionManager());
		Properties properties = new Properties();
		properties.setProperty("get*", "PROPAGATION_REQUIRED,readOnly");
		properties.setProperty("list*", "PROPAGATION_REQUIRED,readOnly");
		properties.setProperty("query*", "PROPAGATION_REQUIRED,readOnly");
		properties.setProperty("*", "PROPAGATION_REQUIRED");
		interceptor.setTransactionAttributes(properties);
		return interceptor;
	}

	@Bean
	public BeanNameAutoProxyCreator transactionAutoProxy() {
		BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
		creator.setProxyTargetClass(false);
		creator.setBeanNames(new String[] { "*ServiceImpl" });
		creator.setInterceptorNames(new String[] { "transactionInterceptor" });
		return creator;
	}
}
