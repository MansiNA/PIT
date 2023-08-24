package com.example.application.data.entity;

public class Person implements Cloneable{
    private Integer id;
    private String firstName;
    private String lastName;

    /**
     * No-arg constructor required by Crud to be able to instantiate a new bean
     * when the new item button is clicked.
     */
    public Person() {
    }

    public Person(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Person clone() {
        try {
            return (Person)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
