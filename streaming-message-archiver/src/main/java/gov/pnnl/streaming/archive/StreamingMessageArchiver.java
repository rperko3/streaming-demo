package gov.pnnl.streaming.archive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
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
        String zipDirectory = args[2];

        new StreamingMessageArchiver().messageArchiver(messagesPerZip, threads, zipDirectory);
    }

    private void messageArchiver(int messagesPerZip, int threads, String zipDirectory) {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(threads, threadFactory);

        for (int i = 0; i < threads; i++) {
            Zipper zipper = new Zipper(zipDirectory);
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

    	private String zipDirectory;

		public Zipper(String zipDirectory) {
    		this.zipDirectory = zipDirectory;
    	}
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
        	String filename = System.currentTimeMillis() + "-tweets.zip";
        	File tmpFile = new File(System.getProperty("java.io.tmpdir"), filename);
        	File outputFile = new File(zipDirectory, filename);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("tweets.json");
            zos.putNextEntry(ze);
            zos.write(input.getBytes());
            zos.closeEntry();
            zos.close();
            
            Files.move(tmpFile.toPath(), outputFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }

    }
}
