package com.supshop.sup_shop_backend.model;


import jakarta.persistence.*;

@Entity
@Table(name = "product_attributes")
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @Column(name = "attribute_value", nullable = false, length = 255)
    private String attributeValue;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}

    public String getAttributeName() {return attributeName;}
    public void setAttributeName(String attributeName) {this.attributeName = attributeName;}

    public String getAttributeValue() {return attributeValue;}
    public void setAttributeValue(String attributeValue) {this.attributeValue = attributeValue;}
}
