package com.seaway.game.common.entity.mysql;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String username;

    private String roleName;

    private String name;

    private String password;

    private boolean enabled;

    private String createdBy;

    private Date created;

    private Date lastUpdate;

}
