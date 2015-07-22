package gov.pnnl.streaming;

import gov.pnnl.streaming.json.TweetGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.springframework.http.HttpStatus.OK;

/**
 * User: perk387
 * Date: 12/31/14
 * <p/>
 * REST interface to the Hadoop query API
 */
@RestController
public class StreamingController {

    private final Log log = LogFactory.getLog(getClass());

    @Value("${rate}")
    private Integer rate;

    @Autowired
    private TweetGenerator tweetGenerator;

    private boolean alive = false;

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    public ResponseEntity<String> echo(@RequestParam(value = "query", defaultValue = "echo") String query) {
        return new ResponseEntity<String>(query, OK);
    }

    @RequestMapping(value = "/throttle", method = RequestMethod.GET)
    public ResponseEntity<String> throttle(@RequestParam(value = "mps", defaultValue = "1000") Integer mps) {
        this.rate = mps;

        log.info("setting throttling to " + mps + " messages per second");

        return new ResponseEntity<>("new throttle speed:" + mps, OK);
    }

    @RequestMapping(value = "/kill", method = RequestMethod.GET)
    public ResponseEntity<String> kill() {
        alive = false;
        return new ResponseEntity<>("killed", OK);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/stream",
            method = RequestMethod.GET)
    public void powerTracker(HttpServletResponse response) throws Exception {


        long totalStart = System.currentTimeMillis();
        long start;
        long count = 0;

        log.info("rate is " + rate + " per second");
        alive = true;

        int iterations = 0;
        int subRate; // = rate / 4;
        PrintWriter responseWriter = response.getWriter();

        while (alive) {

            if (responseWriter.checkError()) {
                break;
            }

            //recalculate for on the fly throttling
            subRate = rate / 4;

            start = System.currentTimeMillis();

            for (int i = 0; i < subRate; i++) {
                responseWriter.write(tweetGenerator.fetchRandomTweet());
                responseWriter.write('\n');
                count++;
            }
            iterations++;

            if (iterations % 8 == 0)
                log.info("still running ...");

            long timeLapse = (start + 250) - System.currentTimeMillis();
            Thread.sleep(timeLapse > 0 ? timeLapse : 250);
        }

        long time = System.currentTimeMillis() - totalStart;

        log.info("done");
        log.info(time + " ms total tweets:" + count);
        log.info("actual rate:" + ((float) count / (time / 1000)) + " tweets per second");
    }
}
