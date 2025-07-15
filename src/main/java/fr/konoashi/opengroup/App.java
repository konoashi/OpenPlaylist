package fr.konoashi.opengroup;

import fr.konoashi.opengroup.service.LobbyProcessing;
import org.json.JSONObject;

import java.util.*;

public class App {

    public static final int QUEUE_SIZE_DUPLICATE = 5;

    public static final int TIME_OF_WAIT = 60000;

    public static final int PLAYLIST_LENGTH = 104; //6

    public static ProducerConsumerService producerConsumer;

    public static boolean reachedDouble = false;

    public static ArrayList<JSONObject> dataList = new ArrayList<>();
    public static String previousGameId = "";

    public static void main(String[] args) throws Exception {
        new App();
    }
    public App() throws Exception {

        System.out.println("OpenGroup started. Waiting for lobby data...");
        Timer timer = new Timer(false);
        timer.scheduleAtFixedRate(new LobbyProcessing(), 0, 500);
        producerConsumer = new ProducerConsumerService();
    }

}
