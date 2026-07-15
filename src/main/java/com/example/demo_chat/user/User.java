package com.example.demo_chat.user;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** R2DBC entity mapped to {@code demo_chat.users} (schema set via {@code spring.r2dbc.url}). */
@Table("users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Id private UUID id;

  @Column("first_name")
  private String firstName;

  @Column("last_name")
  private String lastName;

  private String email;

  private String phone;

  private String login;

  private String password;
}
