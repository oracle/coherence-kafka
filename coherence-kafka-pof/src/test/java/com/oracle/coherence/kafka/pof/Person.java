/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.schema.annotation.Portable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Aleks Seovic  2021.01.21
 */
//@PortableType(id = 1, version = 1)
public class Person implements PortableObject, Serializable
    {
    @Portable
    private String name;

    @Portable
    private int age;

    @Portable(since = 1)
    private Address address;

    public String getName()
        {
        return name;
        }

    public Person setName(String name)
        {
        this.name = name;
        return this;
        }

    public int getAge()
        {
        return age;
        }

    public Person setAge(int age)
        {
        this.age = age;
        return this;
        }

    public Address getAddress()
        {
        return address;
        }

    public Person setAddress(Address address)
        {
        this.address = address;
        return this;
        }

    public boolean equals(Object o)
        {
        if (this == o)
            {
            return true;
            }
        if (o == null || getClass() != o.getClass())
            {
            return false;
            }
        Person person = (Person) o;
        return age == person.age &&
               Objects.equals(name, person.name) &&
               Objects.equals(address, person.address);
        }

    public int hashCode()
        {
        return Objects.hash(name, age, address);
        }

    public String toString()
        {
        return "Person{" +
               "name='" + name + '\'' +
               ", age=" + age +
               ", address=" + address +
               '}';
        }

    public void readExternal(PofReader in) throws IOException
        {
        name = in.readString(0);
        age = in.readInt(1);
        address = in.readObject(2);
        }

    public void writeExternal(PofWriter out) throws IOException
        {
        out.writeString(0, name);
        out.writeInt(1, age);
        out.writeObject(2, address);
        }
    }
