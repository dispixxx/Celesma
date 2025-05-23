package com.disp.celesmaproject.model;

public class UserProfileDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String avatarUrl;

    // Геттеры и сеттеры
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName; }

    public void setFirstName(String firstName) {
        this.firstName = firstName; }

    public String getLastName() {
        return lastName; }

    public void setLastName(String lastName) {
        this.lastName = lastName; }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}