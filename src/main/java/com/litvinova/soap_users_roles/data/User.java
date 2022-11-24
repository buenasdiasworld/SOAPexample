package com.litvinova.soap_users_roles.data;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

  @Id
  @Column(nullable = false)
  private String login;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(
      name = "users_roles",
      joinColumns = {
          @JoinColumn(name = "user_login", foreignKey = @ForeignKey(name = "FK_users_users_roles"))},
      inverseJoinColumns = {
          @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_roles_users_roles"))}
  )
  private List<Role> roles;

}
