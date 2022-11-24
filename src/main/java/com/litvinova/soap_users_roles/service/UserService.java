package com.litvinova.soap_users_roles.service;

import com.litvinova.soap_users_roles.data.Role;
import com.litvinova.soap_users_roles.data.User;
import com.litvinova.soap_users_roles.repository.RoleRepository;
import com.litvinova.soap_users_roles.repository.UserRepository;
import com.litvinova.soap_users_roles.utils.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import soap_users_roles.litvinova.com.AddUserRequest;
import soap_users_roles.litvinova.com.AddUserResponse;
import soap_users_roles.litvinova.com.DeleteUserResponse;
import soap_users_roles.litvinova.com.GetAllUsersNoRoleResponse;
import soap_users_roles.litvinova.com.GetUserByLoginResponse;
import soap_users_roles.litvinova.com.RoleDto;
import soap_users_roles.litvinova.com.ServiceStatus;
import soap_users_roles.litvinova.com.UpdateUserRequest;
import soap_users_roles.litvinova.com.UpdateUserResponse;
import soap_users_roles.litvinova.com.UserNoRoleDto;
import soap_users_roles.litvinova.com.UserWithRolesDto;


@RequiredArgsConstructor
@Service

public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  // Получать список пользователей из БД (без ролей)

  public GetAllUsersNoRoleResponse getAllUsers() {

    List<User> users = userRepository.findAll();
    List<UserNoRoleDto> usersDtoList = new ArrayList<>();

    users.forEach(entity -> {
      UserNoRoleDto userDto = new UserNoRoleDto();
      BeanUtils.copyProperties(entity, userDto);
      usersDtoList.add(userDto);
    });
    GetAllUsersNoRoleResponse response = new GetAllUsersNoRoleResponse();
    response.getUserNoRoleDto().addAll(usersDtoList);
    response.getUserNoRoleDto().forEach(t -> System.out.println(t.getLogin()));
    return response;
  }

  // Получать конкретного пользователя (с его ролями) из БД
  @Transactional
  public GetUserByLoginResponse getUserByLogin(String login) throws NotFoundException {

    GetUserByLoginResponse response = new GetUserByLoginResponse();
    Optional<User> userOptional = userRepository.findByLogin(login);

    if (userOptional.isEmpty()) {
      ServiceStatus serviceStatus = new ServiceStatus();
      response.setServiceStatus(serviceStatus);
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(login + " not found");
      return response;
    }

    User userEntity = userOptional.get();
    UserWithRolesDto userDto = new UserWithRolesDto();

    BeanUtils.copyProperties(userEntity, userDto);
    List<RoleDto> rolesDto = new ArrayList<>();
    userEntity.getRoles().forEach(roleEntity -> {
      RoleDto roleDto = new RoleDto();
      BeanUtils.copyProperties(roleEntity, roleDto);
      rolesDto.add(roleDto);
    });
    userDto.getRoles().addAll(rolesDto);
    response.setUserWithRolesDto(userDto);
    return response;
  }

  // Удалять пользователя в БД

  public DeleteUserResponse deleteUserByLogin(String login) {

    DeleteUserResponse response = new DeleteUserResponse();
    ServiceStatus serviceStatus = new ServiceStatus();
    response.setServiceStatus(serviceStatus);

    boolean flag = true;

    Optional<User> userOptional = userRepository.findByLogin(login);
    if (userOptional.isEmpty()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(login + " not found");
      return response;
    }

    User user = userOptional.get();

    try {
      userRepository.delete(user);
    } catch (Exception e) {
      flag = false;
    }

    if (flag == false) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage("Exception while deleting Entity id=" + login);
    } else {
      serviceStatus.setStatusCode("SUCCESS");
      serviceStatus.setMessage("Content Deleted Successfully");
    }

    return response;

  }

  //Добавлять нового пользователя с ролями в БД.

  public AddUserResponse addUser(AddUserRequest newUserRequest) {

    AddUserResponse response = new AddUserResponse();
    ServiceStatus serviceStatus = new ServiceStatus();
    response.setServiceStatus(serviceStatus);

    Optional<User> userOptional = userRepository.findByLogin(newUserRequest.getLogin());

    if (!userOptional.isEmpty()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(newUserRequest.getLogin() + " already exists");
      return response;
    }

    User user = new User();
    BeanUtils.copyProperties(newUserRequest, user);

    Validation requestValidation = validateUser(user);

    if (!requestValidation.isSuccessful()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(requestValidation.getMessage() + " " + user.getLogin());
      return response;
    }

    Validation result = addOrUpdateUser(user, newUserRequest.getRoles());

    if (!result.isSuccessful()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(result.getMessage() + " " + user.getLogin());
    } else {
      serviceStatus.setStatusCode("SUCCESS");
      serviceStatus.setMessage("Added Successfully");
    }

    return response;
  }

  //Редактировать существующего пользователя в БД.
  // Если в запросе на редактирование передан массив ролей, система должна обновить список ролей пользователя в БД -
  // новые привязки добавить, неактуальные привязки удалить.

  public UpdateUserResponse updateUser(UpdateUserRequest updateUserRequest) {

    UpdateUserResponse response = new UpdateUserResponse();
    ServiceStatus serviceStatus = new ServiceStatus();
    response.setServiceStatus(serviceStatus);

    Optional<User> userOptional = userRepository.findByLogin(updateUserRequest.getLogin());

    if (userOptional.isEmpty()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(updateUserRequest.getLogin() + " not found");
      return response;
    }

    User user = userOptional.get();
    BeanUtils.copyProperties(updateUserRequest, user);

    Validation requestValidation = validateUser(user);

    if (!requestValidation.isSuccessful()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(requestValidation.getMessage() + " " + user.getLogin());
      return response;
    }

    Validation result = addOrUpdateUser(user, updateUserRequest.getRoles());

    if (!result.isSuccessful()) {
      serviceStatus.setStatusCode("FAIL");
      serviceStatus.setMessage(result.getMessage() + " " + user.getLogin());
    } else {
      serviceStatus.setStatusCode("SUCCESS");
      serviceStatus.setMessage("Added Successfully");
    }
    return response;

  }

  private Validation addOrUpdateUser(User user, List<RoleDto> rolesInRequest) {

    Validation check = new Validation();
    check.setSuccessful(true);

    List<Role> roles = new ArrayList<>();
    rolesInRequest.forEach(roleDto -> {

      Optional<Role> roleOptional = roleRepository.findByName(roleDto.getName());

      Role role = roleOptional.isPresent() ?
          roleOptional.get() :
          null;
      if (role != null) {
        roles.add(role);
      }
    });

    // логичнее проверить роли в методе validateUser,
    // но мне показалось оптимальнее относительно количества запросов в бд перенести в метод добавления/обновления

    if (roles.isEmpty()) { // пользователь прислал несуществующие роли
      check.setSuccessful(false);
      check.setMessage("Enter valid roles");
      return check;
    }
    user.setRoles(roles);

    try {
      userRepository.save(user);
    } catch (Exception e) {
      check.setSuccessful(false);
      check.setMessage("Could not proceed");
    }
    return check;
  }

  private Validation validateUser(User request) {

    Validation result = new Validation();
    result.setSuccessful(true);
    StringBuilder errors = new StringBuilder();

    if (request.getLogin().isEmpty()) {
      result.setSuccessful(false);
      errors.append("empty login\n");
    }
    if (request.getName().isEmpty()) {
      result.setSuccessful(false);
      errors.append("empty name\n");
    }
    if (request.getPassword().isEmpty()) {
      result.setSuccessful(false);
      errors.append("empty password\n");
    }
    if (!request.getPassword().matches(".*(?=.*[A-Z]).+(?=.*\\d).*")) {
      result.setSuccessful(false);
      errors.append("password should contain at least one upper case and one number\n");
    }
    if (request.getPassword().length() < 6) {
      result.setSuccessful(false);
      errors.append("password should consist at least of 6 characters\n");
    }
    result.setMessage(errors.toString());
    return result;
  }

}
