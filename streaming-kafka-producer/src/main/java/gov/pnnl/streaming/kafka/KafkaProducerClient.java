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
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class KafkaProducerClient {


    private ExecutorService executorService;

    private final BlockingQueue<Event> eventsQueue;

    private final int threads;

    private final AtomicBoolean canRun;

    private final String name;


    private final String streamName;

    private StatisticsCollection stats;

    private kafka.javaapi.producer.Producer<String, String> producer;


    private final static Logger logger = LoggerFactory
            .getLogger(KafkaProducerClient.class);

    /**
     * @param name       The name of the client, used for debugging purposes
     * @param streamName The name of the stream to send data to
     * @param threads    The number of threads to put in the pool
     */
    public KafkaProducerClient(String name, String streamName,
                               int threads, String brokerList, StatisticsCollection stats) {

        Properties properties = new Properties();
        properties.put("metadata.broker.list", brokerList);
        properties.put("broker.list", brokerList);
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        ProducerConfig producerConfig = new ProducerConfig(properties);
        producer = new kafka.javaapi.producer.Producer<String, String>(
                producerConfig);

        eventsQueue = new LinkedBlockingQueue<Event>();

        this.name = name;
        this.canRun = new AtomicBoolean(true);
        this.threads = threads;
        this.streamName = streamName;
        this.stats = stats;

    }

    /**
     * {@inheritDoc}
     */
    public void connect() {

        if (!canRun.compareAndSet(true, false)) {
            throw new IllegalStateException("Already running");
        }

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(threads, threadFactory);

        for (int i = 0; i < this.threads; i++) {
            KafkaProducerBase p = new KafkaProducerBase(this.eventsQueue, this.producer, this.streamName, this.stats);
            executorService.execute(p);
            logger.info(name + ": New thread started : {}", p);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        logger.info("Stopping the client");
        try {
            executorService.shutdownNow();

            logger.info(name + ": Successfully stopped the client");
        } catch (Exception e) {
            logger.info(
                    name + ": Exception when attempting to stop the client: " + e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    public void post(String data) {
        Event event = new Event(data);
        eventsQueue.offer(event);
    }

}
