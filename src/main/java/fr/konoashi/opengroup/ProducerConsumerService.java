package fr.konoashi.opengroup;

import fr.konoashi.opengroup.service.BasePlaylist;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.concurrent.*;

import static java.lang.System.err;

public class ProducerConsumerService {

    public static final BlockingQueue<Long> timestamps = new ArrayBlockingQueue<>(3000);

    /**
     * A consumer that handles incoming messages from a queue.
     */
    public static class MessageHandler implements Runnable
    {
        private final BlockingQueue<JSONArray> queue;

        public MessageHandler(BlockingQueue<JSONArray> queue)
        {
            this.queue = queue;
        }

        @Override
        public void run()
        {
            // Consume messages from queue until interrupted

            boolean interrupted = false;
            while (!interrupted) {
                try {
                    JSONArray playlist = queue.take();
                    handle(playlist);
                } catch (InterruptedException ex) {
                    err.println("The message handler was interrupred: " +
                            ex.getMessage());
                    interrupted = true;
                }
            }
        }

        private void handle(JSONArray playlist)
        {
            System.out.println("Handling playlist: " + playlist.toString());
            for (int i = 0; i < App.dataList.size(); i++) {
                if (!App.dataList.get(i).getString("gameMap").equals(playlist.getJSONObject(i).getString("gameMap")) ||
                        !App.dataList.get(i).getString("gameMode").equals(playlist.getJSONObject(i).getString("gameMode"))) {
                    break;
                }
            }
            queue.add(playlist);
            if (timestamps.size() == 0 && queue.size() == 1) {
                // If all timestamps have been generated and the queue size is 1, return this playlist
                JSONArray finalPlaylist = queue.poll();
                if (finalPlaylist != null) {
                    System.out.println("Final Playlist: " + finalPlaylist.toString());
                }
            }
        }
    }

    /**
     * A producer that create playlists from timestamps.
     */
    public static class MessageProducer implements Runnable
    {
        private final BlockingQueue<JSONArray> queue;

        public MessageProducer(BlockingQueue<JSONArray> queue)
        {
            this.queue = queue;
        }

        @Override
        public void run()
        {
            // Fetch messages from external source

            boolean interrupted = false, full = false;
            int n = 0;
            while (!interrupted && !full) {
                JSONArray playlist = null;
                // Simulate events coming from external source
                try {
                    playlist = generatePlaylist(timestamps.take());
                    System.out.println("Generated playlist #" + (++n) + ": " + playlist.toString());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // If a new message arrived, add it to queue
                try {
                    queue.add(playlist);
                } catch (IllegalStateException ex) {
                    err.println("Too many playlists! " + ex.getMessage());
                    full = true; // stops further processing
                }
            }
        }

        private JSONArray generatePlaylist(long timestamp) {
            // Simulate generating a playlist from a timestamp
            ArrayList<String> basePlaylist = BasePlaylist.generateBasePlaylist();
            System.out.println("Base playlist: " + basePlaylist.toString());
            for (int i = 0; i < 10000; i++) {
                JSONArray shuffledPlaylist = new JSONArray();
                if (BasePlaylist.shufflePlaylist(shuffledPlaylist, basePlaylist, timestamp)) {
                    return shuffledPlaylist;
                }
            }
            return new JSONArray();
        }
    }

    private static final int QUEUE_SIZE = 30000;

    public void run() throws Exception
    {
        var pool = Executors.newCachedThreadPool();
        //ExecutorService executor = Executors.newFixedThreadPool(5);

        BlockingQueue<JSONArray> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        // Spawn producer/consumer threads

        pool.submit(new MessageProducer(queue));
        pool.submit(new MessageProducer(queue));
        pool.submit(new MessageProducer(queue));
        pool.submit(new MessageProducer(queue));

        pool.submit(new MessageHandler(queue));


        System.out.println("Processing messages...");

        // Wait for graceful termination

        //pool.shutdown();
        //executor.awaitTermination(30, TimeUnit.MINUTES); // do not receive more tasks

        // Note: executor.awaitTermination if we want main() to block here
        // waiting for tasks to complete
    }
}
