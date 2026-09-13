package com.supshop.sup_shop_backend.model;


import jakarta.persistence.*;


@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logo_url;

    @Column(columnDefinition = "TEXT")
    private String description;

    //geters and setters


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getLogo_url() {return logo_url;}

    public void setLogo_url(String logo_url) {this.logo_url = logo_url;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}


}
