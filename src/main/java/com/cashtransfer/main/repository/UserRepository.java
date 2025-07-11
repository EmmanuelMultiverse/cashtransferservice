package com.cashtransfer.main.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.cashtransfer.main.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByUsername(String username);

}
