### Demo for master and replica datasource

* Two database: mysql_master(read and write), mysql_replica(read only).
  
* To connect replica database, use @ReadOnlyConnection in public method 

* @Transactional can still be used as usual while using @ReadOnlyConnection

### Advantages

* Improve performance of writes and read through spreading the load among multiple replicas 
 
* Data security - run backup services on the replica without corrupting the corresponding source data

* Analytics - the analysis of the information can take place on the replica without affecting the performance of the source

### Disadvantages
* The replication is asynchronous, it means that some committed on master transactions may be not available on slave if the master fails.

### Migration plan
* Add replica database properties into db.properties file
* Set up master-slave replication in MyQSL, set the current existing database as master
* Config both master database and replica database in APP
* Set up dynamic DataSource Routing and add replica database configuration fault tolerance  
* Add custom annotation @ReadOnlyConnection, route the datasource to replica database in aspect 
* Add @ReadOnlyConnection annotation in public methods which only need read only transaction
* No impact to existing api. The transaction will be routed to default database if no @ReadOnlyConnection added.

### Custom annotation VS Spring-based solution (@Transactional)
#### Spring @Transactional
* To route datasource using spring @Transactional, need to custom PlatformTransactionManager.
  The readOnly in @Transactional just serves as a hint for the actual transaction subsystem;
  it will not necessarily cause failure of write access attempts. 
  A transaction manager which cannot interpret the read-only hint will not throw an exception when asked for a read-only transaction but rather silently ignore the hint.
* Hard to do migration
#### Custom annotation @ReadOnlyConnection
* No affect to existing code
* Easy to do migration
* Can work with spring @Transactional together. It's possible set transaction attributes as usual while using @ReadOnlyConnection.

###  AWS working with read replicas
   https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_MySQL.Replication.ReadReplicas.html
   https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_ReadRepl.html

  
### Reference
* How to set up master slave replication
  https://dev.mysql.com/doc/refman/5.6/en/replication-howto.html
* Docker MySQL master-slave replication
  https://github.com/vbabak/docker-mysql-master-slave

