package com.supshop.sup_shop_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public String getName() { return name; }
    public void sеtName(String name) { this.name = name; }
}
