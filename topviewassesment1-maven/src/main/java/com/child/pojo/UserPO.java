package com.child.pojo;

import java.util.Objects;

/**
 * 用户表
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class UserPO {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String oldCar;

    public String getOldCar() {
        return oldCar;
    }

    public void setOldCar(String oldCar) {
        this.oldCar = oldCar;
    }

    @Override
    public String toString() {
        return "UserPO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", oldCar='" + oldCar + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPO userPO = (UserPO) o;
        return Objects.equals(id, userPO.id) && Objects.equals(name, userPO.name) && Objects.equals(email, userPO.email) && Objects.equals(address, userPO.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, address);
    }

    public UserPO(Long id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
    }

    public UserPO(Long id, String name, String email, String address, String oldCar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.oldCar = oldCar;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserPO() {
    }
}
