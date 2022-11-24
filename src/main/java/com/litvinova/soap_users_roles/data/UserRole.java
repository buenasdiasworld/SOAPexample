package com.litvinova.soap_users_roles.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users_roles")
@Data
public class UserRole {

  @EmbeddedId
  UserRoleId userRoleId;

  public UserRole(Role role, User user) {
    this.userRoleId = new UserRoleId(role.getId(), user.getLogin());
    this.role = role;
    this.user = user;
  }

  public UserRole() {
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("roleId")
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userLogin")
  private User user;

  @EqualsAndHashCode
  @Embeddable
  public static class UserRoleId implements Serializable {

    @Getter
    @Setter
    @Column(name = "role_id")
    Integer roleId;

    @Getter
    @Setter
    @Column(name = "user_login")
    String userLogin;

    public UserRoleId(Integer roleId, String userLogin) {
      this.roleId = roleId;
      this.userLogin = userLogin;

    }

    private UserRoleId() {
    }
  }
}
