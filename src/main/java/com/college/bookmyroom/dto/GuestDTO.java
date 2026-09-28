package com.college.bookmyroom.dto;

import jakarta.validation.constraints.NotBlank;

public class GuestDTO {

    private Long id;

    @NotBlank(message = "Guest name should not be empty")
    private String name;

    @NotBlank(message = "Guest phone should not be empty")
    private String phone;

    public GuestDTO() {
    }

    public GuestDTO(Long id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
