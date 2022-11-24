package com.litvinova.soap_users_roles.repository;

import com.litvinova.soap_users_roles.data.Role;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer> {

  Optional<Role> findById(Integer id);
  Optional<Role> findByName(String name);
}

