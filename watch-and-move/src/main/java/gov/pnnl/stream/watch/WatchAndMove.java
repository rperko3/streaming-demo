package gov.pnnl.stream.watch;


import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class WatchAndMove {

    public static void main(String[] args) throws Exception {
        // get the directory we want to watch, using the Paths singleton class
        Path toWatch = Paths.get(args[0]);

        if (toWatch == null) {
            throw new UnsupportedOperationException("Directory not found");
        }

        // make a new watch service that we can register interest in
        // directories and files with.
        WatchService myWatcher = toWatch.getFileSystem().newWatchService();

        // start the file watcher thread below
        MyWatchQueueReader fileWatcher = new MyWatchQueueReader(myWatcher, args[0]);
        Thread th = new Thread(fileWatcher, "FileWatcher");
        th.start();

        // register a file
        toWatch.register(myWatcher, ENTRY_CREATE);
        th.join();
    }

    /**
     * This Runnable is used to constantly attempt to take from the watch
     * queue, and will receive all events that are registered with the
     * fileWatcher it is associated. In this sample for simplicity we
     * just output the kind of event and name of the file affected to
     * standard out.
     */
    private static class MyWatchQueueReader implements Runnable {

        /**
         * the watchService that is passed in from above
         */
        private WatchService myWatcher;
        private final String pathToWatch;
        private UploadToS3 uploadToS3 = new UploadToS3();

        public MyWatchQueueReader(WatchService myWatcher, String toWatch) {
            this.myWatcher = myWatcher;
            this.pathToWatch = toWatch;
        }

        /**
         * In order to implement a file watcher, we loop forever
         * ensuring requesting to take the next item from the file
         * watchers queue.
         */
        @Override
        public void run() {
            try {
                // get the first event before looping
                WatchKey key = myWatcher.take();
                while (key != null) {
                    // we have a polled event, now we traverse it and
                    // receive all the states from it
                    for (WatchEvent event : key.pollEvents()) {
                        System.out.printf("Received %s event for file: %s\n", event.kind(), pathToWatch + "/" + event.context());
                        uploadToS3.upload("bulk-delivery", event.context().toString(), pathToWatch + "/" + event.context().toString());
                    }
                    key.reset();
                    key = myWatcher.take();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Stopping thread");
        }
    }
}
