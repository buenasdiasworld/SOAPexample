package com.litvinova.soap_users_roles.endpoint;

import com.litvinova.soap_users_roles.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import soap_users_roles.litvinova.com.AddUserRequest;
import soap_users_roles.litvinova.com.AddUserResponse;
import soap_users_roles.litvinova.com.DeleteUserRequest;
import soap_users_roles.litvinova.com.DeleteUserResponse;
import soap_users_roles.litvinova.com.GetAllUsersNoRoleRequest;
import soap_users_roles.litvinova.com.GetAllUsersNoRoleResponse;
import soap_users_roles.litvinova.com.GetUserByLoginRequest;
import soap_users_roles.litvinova.com.GetUserByLoginResponse;
import soap_users_roles.litvinova.com.UpdateUserRequest;
import soap_users_roles.litvinova.com.UpdateUserResponse;

@Endpoint
@AllArgsConstructor
public class UserEndpoint {

  private UserService userService;

  private static final String NAMESPACE_URI = "com.litvinova.soap_users_roles";

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAllUsersNoRoleRequest")
  @ResponsePayload
  public GetAllUsersNoRoleResponse getAllUsers(@RequestPayload GetAllUsersNoRoleRequest request) {

    GetAllUsersNoRoleResponse response = userService.getAllUsers();
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserByLoginRequest")
  @ResponsePayload
  public GetUserByLoginResponse getUserById(@RequestPayload GetUserByLoginRequest request)
      throws NotFoundException {
    GetUserByLoginResponse response = userService.getUserByLogin(request.getUserLogin());
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserRequest")
  @ResponsePayload
  public DeleteUserResponse deleteUser(@RequestPayload DeleteUserRequest request)
      throws NotFoundException {
    DeleteUserResponse response = userService.deleteUserByLogin(request.getUserLogin());
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addUserRequest")
  @ResponsePayload
  public AddUserResponse addUser(@RequestPayload AddUserRequest request) {
    AddUserResponse response = userService.addUser(request);
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserRequest")
  @ResponsePayload
  public UpdateUserResponse updateUser(@RequestPayload UpdateUserRequest request) {
    UpdateUserResponse response = userService.updateUser(request);
    return response;
  }

}
