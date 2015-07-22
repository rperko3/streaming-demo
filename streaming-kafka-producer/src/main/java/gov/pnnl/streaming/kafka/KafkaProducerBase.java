/*
 * Copyright 2013-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package gov.pnnl.streaming.kafka;

import gov.pnnl.streaming.stats.StatisticsCollection;
import kafka.producer.KeyedMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Runnable class responsible for sending items on the queue to Kinesis
 *
 * @author corbetn
 */
public class KafkaProducerBase implements Runnable {
    private static final Log LOG = LogFactory.getLog(KafkaProducerBase.class);

    /**
     * Reference to the queue
     */
    private final BlockingQueue<Event> eventsQueue;


    /**
     * Reference to the Amazon Kinesis Client
     */
    private final kafka.javaapi.producer.Producer<String, String> producer;


    /**
     * The topic name that we are sending to
     */
    private final String topic;


    private StatisticsCollection stats;

    private final static Logger logger = LoggerFactory
            .getLogger(KafkaProducerBase.class);


    /**
     * @param eventsQueue The queue that holds the records to send to Kinesis
     */
    public KafkaProducerBase(BlockingQueue<Event> eventsQueue,
                             kafka.javaapi.producer.Producer<String, String> producer, String topic, StatisticsCollection stats) {
        this.eventsQueue = eventsQueue;
        this.producer = producer;
        this.topic = topic;
        this.stats = stats;

    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

        while (true) {
            try {
                // get message from queue - blocking so code will wait here for work to do
                Event event = eventsQueue.take();

                KeyedMessage<String, String> message = new KeyedMessage<String, String>(topic, new String(event.getData().array()));

                try {
                    producer.send(message);
                    stats.increment(StatisticsCollection.Key.KAFKA_MESSAGE_PUT);

                } catch (Throwable t) {
                    LOG.warn("Caught throwable while processing", t);
                }


            } catch (Exception e) {
                // didn't get record - move on to next\
                e.printStackTrace();

                stats.increment(StatisticsCollection.Key.KAFKA_PUT_ERROR);
            }
        }

    }
}
