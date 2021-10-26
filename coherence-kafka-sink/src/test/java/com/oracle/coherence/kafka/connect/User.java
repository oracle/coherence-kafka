/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import java.io.Serializable;

public class User implements Serializable
    {
    private String firstName;

    private int age;

    public User()
        {
        }

    public User(String firstname, int age)
        {
        this.firstName = firstname;
        this.age = age;
        }

    public String getFirstName()
        {
        return this.firstName;
        }

    public void setFirstName(String name)
        {
        this.firstName = name;
        }

    public int getAge()
        {
        return this.age;
        }

    public void setAge(int age)
        {
        this.age = age;
        }

    @Override
    public String toString()
        {
        return "User(" + firstName + ", " + age + ")";
        }

    @Override
    public boolean equals(Object o)
        {
        if (o instanceof User)
            {
            User u = (User) o;

            return getFirstName().equals(u.getFirstName()) && getAge() == u.getAge();
            }

        return false;
        }
    }
