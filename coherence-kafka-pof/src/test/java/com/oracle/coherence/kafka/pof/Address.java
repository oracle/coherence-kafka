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
import com.tangosol.io.pof.schema.annotation.PortableType;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Aleks Seovic  2021.01.21
 */
//@PortableType(id = 2)
public class Address implements PortableObject, Serializable
    {
    @Portable
    private String street;
    @Portable
    private String city;
    @Portable
    private String state;
    @Portable
    private String zip;

    public String getStreet()
        {
        return street;
        }

    public Address setStreet(String street)
        {
        this.street = street;
        return this;
        }

    public String getCity()
        {
        return city;
        }

    public Address setCity(String city)
        {
        this.city = city;
        return this;
        }

    public String getState()
        {
        return state;
        }

    public Address setState(String state)
        {
        this.state = state;
        return this;
        }

    public String getZip()
        {
        return zip;
        }

    public Address setZip(String zip)
        {
        this.zip = zip;
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
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(zip, address.zip);
        }

    public int hashCode()
        {
        return Objects.hash(street, city, state, zip);
        }

    public String toString()
        {
        return "Address{" +
               "street='" + street + '\'' +
               ", city='" + city + '\'' +
               ", state='" + state + '\'' +
               ", zip='" + zip + '\'' +
               '}';
        }

    public void readExternal(PofReader in) throws IOException
        {
        street = in.readString(0);
        city   = in.readString(1);
        state  = in.readString(2);
        zip    = in.readString(3);
        }

    public void writeExternal(PofWriter out) throws IOException
        {
        out.writeString(0, street);
        out.writeString(1, city);
        out.writeString(2, state);
        out.writeString(3, zip);
        }
    }
