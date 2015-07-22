package gov.pnnl.streaming.archive;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by perk387 on 7/21/15.
 */
public class StreamingMessageArchiver {

    private ExecutorService executorService;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("StreamingMessageArchiver messagesPerZip threads");
            System.exit(-1);
        }

        int messagesPerZip = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
//        String zipDirectory = args[2];

        new StreamingMessageArchiver().messageArchiver(messagesPerZip, threads);
    }

    private void messageArchiver(int messagesPerZip, int threads) {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(threads, threadFactory);

        for (int i = 0; i < threads; i++) {
            Zipper zipper = new Zipper();
            executorService.execute(zipper);
        }

        long msgCount = 0;
        long start = System.currentTimeMillis();


        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));

            StringBuilder messages = new StringBuilder();
            String input;

            while ((input = br.readLine()) != null) {
                msgCount++;
                messages.append(input).append('\n');
                if (msgCount % messagesPerZip == 0) {
                    messageQueue.add(messages.toString());
//                    zipUpMessages(messages.toString());
                    messages.setLength(0);
                }
            }

        } catch (Exception e) {
            // if we get here, our client has broken, throw away and
            // recreate
            e.printStackTrace();
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("\n" + msgCount + " message in " + end + "ms");

//        }
    }


    class Zipper implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    String value = messageQueue.take();
                    zipUpMessages(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void zipUpMessages(String input) throws IOException {
            FileOutputStream fos = new FileOutputStream(System.currentTimeMillis() + "-tweets.zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("tweets.json");
            zos.putNextEntry(ze);
            zos.write(input.getBytes());
            zos.closeEntry();
            zos.close();
        }

    }
}
