package com.example.demoreplicadatasource;

import com.example.demoreplicadatasource.entity.User;
import com.example.demoreplicadatasource.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.demoreplicadatasource.repository")
public class DemoReplicaDatasourceApplication
{
	private static final Logger log = LoggerFactory.getLogger(DemoReplicaDatasourceApplication.class);
	static UserService userService;

	public static void main(String[] args)
	{
		ConfigurableApplicationContext context = SpringApplication.run(DemoReplicaDatasourceApplication.class, args);

		userService = context.getBean(UserService.class);
		findUserByReadOnlyConnection();
		findUserByReadOnlyConnectionAndTransactional();
		findUserByTransactional();
		updateUser();
	}

	static void findUserByReadOnlyConnection()
	{
		User user = userService.findUserByReadOnlyConnection(1L);
		log.info("findUserByReadOnlyConnection: {}", user.toString());
	}

	static void findUserByReadOnlyConnectionAndTransactional()
	{
		User user = userService.findUserByReadOnlyConnectionAndTransactional(1L);
		log.info("findUserByReadOnlyConnectionAndTransactional: {}", user.toString());
	}

	static void findUserByTransactional()
	{
		User user3 = userService.findUserByTransactional(1L);
		log.info("findUserByTransactional: {}", user3.toString());
	}

	static void updateUser()
	{
		User user = userService.updateUserName(1L, "test1-updated-----");
		log.info("updateUser: {}", user.toString());
	}
}
