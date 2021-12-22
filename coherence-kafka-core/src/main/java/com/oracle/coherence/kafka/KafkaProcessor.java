/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka;

import com.tangosol.config.ConfigurationException;
import com.tangosol.config.xml.ElementProcessor;
import com.tangosol.config.xml.ProcessingContext;
import com.tangosol.config.xml.XmlSimpleName;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

/**
 * Element processor that will parse a kafka:producer element
 *
 */
@XmlSimpleName("producer")
public class KafkaProcessor
        implements ElementProcessor<KafkaProducerBuilder>
    {
    @Override
    public KafkaProducerBuilder process(ProcessingContext context, XmlElement xmlElement)
            throws ConfigurationException
        {
        // locate the definition of a 'producer' element and attempt to parse it
        if (XmlHelper.hasElement(xmlElement, "producer"))
            {
            xmlElement = xmlElement.getElement("producer");
            }

        return context.inject(new KafkaProducerBuilder(xmlElement), xmlElement);
        }
    }
