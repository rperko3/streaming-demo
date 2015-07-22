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
import gov.pnnl.streaming.stats.StatisticsCollectionOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

public class StreamingProducerKafka {

    private static final String TOPIC_1 = "kafka.topic.firehose";
    private static final String TOPIC_2 = "kafka.topic.tenpercent";
    private static final String DEFAULT_PROP_FILE_NAME = "streaming-kafka-producer";
    private static final String BROKER_LIST = "kafka.brokerlist";
    private static final String PRODUCER_THREADS = "kafka.producer.threads.per.topic";

    private static final Log LOG = LogFactory.getLog(StreamingProducerKafka.class);

    public static void main(String[] args) {
        StreamingProducerKafka client = new StreamingProducerKafka();
        String arg = args.length == 1 ? args[0] : null;
        client.run(arg);
    }

    public void run(String propFilePath) {
        loadFileProperties(propFilePath, DEFAULT_PROP_FILE_NAME);

        String topicFireHose = System.getProperty(TOPIC_1);
        String topicTenPercent = System.getProperty(TOPIC_2);
        String brokerList = System.getProperty(BROKER_LIST);
        int threads = Integer.parseInt(System.getProperty(PRODUCER_THREADS));

        StatisticsCollection stats = new StatisticsCollection("firehose");
        StatisticsCollectionOutput statsOut = new StatisticsCollectionOutput(30000, stats);
        statsOut.start();

        StatisticsCollection stats2 = new StatisticsCollection("ten-percent");
        StatisticsCollectionOutput statsOut2 = new StatisticsCollectionOutput(30000, stats2);
        statsOut2.start();

//		stats.setStatValue(StatisticsCollection.Key.APPLICATION_START, System.currentTimeMillis());


        while (true) {
            /**
             * Set up your blocking queues: Be sure to size these properly based
             * on expected TPS of your stream
             */
//			BlockingQueue<String> msgQueue = new StatsAwareLinkedBlockingQueue(10000, stats);
            LOG.info("Got connection to Twitter");

            KafkaProducerClient firehose = new KafkaProducerClient(topicFireHose, topicFireHose, threads, brokerList, stats);
            KafkaProducerClient tenPercent = new KafkaProducerClient(topicTenPercent, topicTenPercent, threads, brokerList, stats2);
            firehose.connect();
            tenPercent.connect();

            LOG.info("Got connection to Kafka");
            long msgCount = 0;
            long start = System.currentTimeMillis();


            try {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(System.in));

                String input;

                while ((input = br.readLine()) != null) {

                    msgCount++;
                    firehose.post(input);

                    if (msgCount % 10 == 0)
                        tenPercent.post(input);
                }

            } catch (Exception e) {
                // if we get here, our client has broken, throw away and
                // recreate
                e.printStackTrace();
            }
            long end = System.currentTimeMillis() - start;
            System.out.println("\n" + msgCount + " message in " + end + "ms");

        }
    }

    private void loadFileProperties(String propFilePath, String defaultName) {
        try {

            if (propFilePath == null) {
                String userHome = System.getProperty("user.home");
                propFilePath = userHome + "/" + defaultName + ".properties";
            }

            InputStream propFile = new FileInputStream(propFilePath);

            Properties p = new Properties(System.getProperties());
            p.load(propFile);
            System.setProperties(p);

            // got them from the file
            LOG.info("Got properties from file");

        } catch (FileNotFoundException e) {
            // file is not there...
        } catch (IOException e) {
            // can't read file
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
