package com.example.demoreplicadatasource.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class ReplicaAwareTransactionManager implements PlatformTransactionManager
{

	private final PlatformTransactionManager transactionManager;

	ReplicaAwareTransactionManager(PlatformTransactionManager transactionManager)
	{
		this.transactionManager = transactionManager;
	}

	@Override
	public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) throws TransactionException
	{
		boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
		boolean isTxReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		boolean isReadOnly = transactionDefinition.isReadOnly();
		if (isTxActive && isTxReadOnly && !isReadOnly)
		{
			throw new CannotCreateTransactionException("Can not request RW transaction from initialized readonly transaction");
		}
		if (!isTxActive)
		{
			RoutingDataSource.setReadonlyDataSource(transactionDefinition.isReadOnly());
		}
		/*if (isTxActive && isTxReadOnly && isReadOnly)
		{
			RoutingDataSource.setReadonlyDataSource(true);
		} else
		{
			RoutingDataSource.setReadonlyDataSource(false);
		} */

		return transactionManager.getTransaction(transactionDefinition);
	}

	@Override
	public void commit(TransactionStatus transactionStatus) throws TransactionException
	{
		transactionManager.commit(transactionStatus);
	}

	@Override
	public void rollback(TransactionStatus transactionStatus) throws TransactionException
	{
		transactionManager.rollback(transactionStatus);
	}
}
