package com.example.demoreplicadatasource.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EntityScan
@EnableTransactionManagement
@EnableJpaRepositories
@Slf4j
public class DatabaseConfig
{
	@Bean
	@ConfigurationProperties(prefix = "demo.datasource.master")
	public HikariConfig masterConfiguration()
	{
		return new HikariConfig();
	}

	@Bean
	@ConfigurationProperties(prefix = "demo.datasource.replica")
	public HikariConfig replicaConfiguration()
	{
		return new HikariConfig();
	}

	@Bean
	public DataSource routingDataSource() {
		DataSource hikariDataSourceMaster = loggingProxy("master", new HikariDataSource(masterConfiguration()));
		DataSource hikariDataSourceReplica;
		try
		{
			hikariDataSourceReplica = loggingProxy("replica", new HikariDataSource(replicaConfiguration()));
		}
		catch (Exception e)
		{
			// if missing replica database properties or connecting fail then route to master
			log.error("Replica datasource config failure");
			hikariDataSourceReplica = hikariDataSourceMaster;
		}

		return new RoutingDataSource(hikariDataSourceMaster,hikariDataSourceReplica);
	}

	private DataSource loggingProxy(String name, DataSource dataSource) {
		SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
		loggingListener.setLogLevel(SLF4JLogLevel.INFO);
		loggingListener.setLogger(name);
		loggingListener.setWriteDataSourceName(false);
		return ProxyDataSourceBuilder
				.create(dataSource)
				.name(name)
				.listener(loggingListener)
				.build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
		return builder
				.dataSource(routingDataSource())
				.packages("com.example.demoreplicadatasource.entity")
				.build();
	}

	// Using @Transactional to route datasource need to custom PlatformTransactionManager
	/*@Bean
	@Primary
	public PlatformTransactionManager transactionManager(@Qualifier("jpaTxManager") PlatformTransactionManager wrapped) {
		return new ReplicaAwareTransactionManager(wrapped);
	}

	@Bean(name = "jpaTxManager")
	public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}*/
}
