// src/main/java/com/example/agricultural_product/dto/ExpertInfoDTO.java
package com.example.agricultural_product.dto;

public class ExpertInfoDTO {
    private Long id; // 对应 user_id
    private String name; // 对应 users.name
    private String specialty; // 对应 tb_expert_profiles.specialization
    private String avatar; // 对应 tb_expert_profiles.photo_url
    private String title; // 数据库中没有，由后端填充

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}