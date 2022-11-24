package com.litvinova.soap_users_roles.repository;

import com.litvinova.soap_users_roles.data.UserRole;
import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<UserRole, UserRole.UserRoleId> {

}
