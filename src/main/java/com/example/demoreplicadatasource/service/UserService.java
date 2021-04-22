package com.example.demoreplicadatasource.service;

import com.example.demoreplicadatasource.datasource.ReadOnlyConnection;
import com.example.demoreplicadatasource.entity.User;
import com.example.demoreplicadatasource.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService
{
	@Autowired
	UserRepository userRepository;

	// Set datasource as Replica by @Transactional(readOnly = true)
	@Transactional(readOnly = true)
	public User findUserByTransactional(Long id)
	{
		return userRepository.findById(id).orElse(null);
	}

	// Set datasource as Replica by @ReadOnlyConnection

	@Transactional(isolation = Isolation.SERIALIZABLE)
	@ReadOnlyConnection
	public User findUserByReadOnlyConnectionAndTransactional(Long id)
	{
		return userRepository.findById(id).orElse(null);
	}

	// Set datasource as Replica by @ReadOnlyConnection
	@ReadOnlyConnection
	public User findUserByReadOnlyConnection(Long id)
	{
		return userRepository.findById(id).orElse(null);
	}

	@Transactional
	public User updateUserName(Long id, String name)
	{
		Optional<User> userOptional = userRepository.findById(id);
		if (userOptional.isPresent())
		{
			User user = userOptional.get();
			user.setName(name);
			return userRepository.save(user);
		}
		else
		{
			throw new IllegalArgumentException("User not found");
		}
	}

	@Transactional(readOnly = true)
	public User findAndUpdate(Long id, String name)
	{
		findUserByTransactional(id);
		return updateUserName(id, name);
	}
}
