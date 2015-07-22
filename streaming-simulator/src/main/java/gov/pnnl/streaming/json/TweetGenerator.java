package gov.pnnl.streaming.json;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by perk387 on 7/20/15.
 */
@Component
public class TweetGenerator {

    private final Log log = LogFactory.getLog(getClass());

    @Value("${tweets}")
    private String tweetFilePath;

    private List<String> tweets = new ArrayList<>(1000);
    private Random random = new Random();

    public TweetGenerator() {

    }

    public TweetGenerator(String pathToSampleTweets) throws Exception {
        this.tweetFilePath = pathToSampleTweets;
        init();
    }

    @PostConstruct
    public void init() throws Exception {
        try {

            log.info("loading tweet file " + tweetFilePath);

            BufferedReader reader;

            if (tweetFilePath.endsWith("json")) {
                reader = new BufferedReader(new FileReader(tweetFilePath));
            } else {
                CompressorInputStream compressorInputStream = new CompressorStreamFactory()
                        .createCompressorInputStream(new BufferedInputStream(new FileInputStream(tweetFilePath)));
                InputStreamReader compressorReader = new InputStreamReader(compressorInputStream);
                reader = new BufferedReader(compressorReader);
            }

            String msg;

            int cnt = 0;
            while ((msg = reader.readLine()) != null) {
                cnt++;
                tweets.add(msg);
            }
            log.info(cnt + " tweets loaded");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String fetchRandomTweet() {
        return tweets.get(random.nextInt(tweets.size()));
    }
}
