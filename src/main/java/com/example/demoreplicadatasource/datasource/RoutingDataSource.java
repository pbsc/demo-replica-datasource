package com.example.demoreplicadatasource.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource
{
	private static final ThreadLocal<DataSourceType> currentDataSource = new ThreadLocal<>();

	RoutingDataSource(DataSource master, DataSource replica)
	{
		Map<Object, Object> dataSources = new HashMap<>();
		dataSources.put(DataSourceType.MASTER, master);
		dataSources.put(DataSourceType.REPLICA, replica);

		super.setTargetDataSources(dataSources);
		super.setDefaultTargetDataSource(master);
		super.afterPropertiesSet();
	}

	static void setReadonlyDataSource(boolean isReadonly)
	{
		currentDataSource.set(isReadonly ? DataSourceType.REPLICA : DataSourceType.MASTER);
	}

	static void cleanDataSourceType()
	{
		currentDataSource.remove();
	}

	@Override
	protected Object determineCurrentLookupKey()
	{
		log.trace("determineCurrentLookupKey thread: {}", Thread.currentThread().getName());
		log.trace("RoutingDataSource: {}", currentDataSource.get());
		return currentDataSource.get();
	}

	private enum DataSourceType {
		MASTER, REPLICA;
	}
}
