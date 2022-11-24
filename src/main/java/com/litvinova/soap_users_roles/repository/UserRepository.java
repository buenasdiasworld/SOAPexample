package com.litvinova.soap_users_roles.repository;

import com.litvinova.soap_users_roles.data.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

 List<User> findAll();

 Optional<User> findByLogin(String login);



}

