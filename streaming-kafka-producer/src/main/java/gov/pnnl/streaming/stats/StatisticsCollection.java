package gov.pnnl.streaming.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsCollection {

    private String name;

    public StatisticsCollection(String firehose) {
        this();
        this.name = firehose;
    }

    public enum Key {KAFKA_MESSAGE_PUT, KAFKA_PUT_ERROR}

    ;

    private Map<Key, AtomicLong> stats;

    public StatisticsCollection() {
        stats = new HashMap<Key, AtomicLong>();

        for (Key key : Key.values()) {
            stats.put(key, new AtomicLong(0L));
        }
    }

    public void outStats() {
        System.out.println("\n***********\n" + name + " " + new Date());
        for (Key key : Key.values()) {
            System.out.println(name + ":" + key + ":" + stats.get(key));
        }
    }

    public void setStatValue(Key key, Long value) {
        stats.get(key).set(value);
    }

    public void increment(Key key) {
        stats.get(key).incrementAndGet();
    }

    public long getStatValue(Key key) {
        return stats.get(key).get();
    }
}
