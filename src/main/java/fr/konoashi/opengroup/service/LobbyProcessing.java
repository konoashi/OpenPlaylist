package fr.konoashi.opengroup.service;

import fr.konoashi.opengroup.App;
import fr.konoashi.opengroup.ProducerConsumerService;
import fr.konoashi.opengroup.util.Utils;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.TimerTask;

import static fr.konoashi.opengroup.App.producerConsumer;
import static fr.konoashi.opengroup.App.reachedDouble;

public class LobbyProcessing extends TimerTask {


    @Override
    public void run() {

        JSONObject nextGame = Utils.nextPublicGameData();
        if (nextGame == null) return;
        if (nextGame.get("msUntilStart") == null) return;
        if(Objects.equals(nextGame.getString("gameID"), App.previousGameId)) return;

        App.previousGameId = nextGame.getString("gameID");

        JSONObject gameConfig =  nextGame.getJSONObject("gameConfig");
        if (gameConfig == null) return;
        if (gameConfig.get("gameMode") == null) return;
        if (gameConfig.get("gameMap") == null) return;

        System.out.println("New game found: " + gameConfig.getString("gameMap") + " in " + gameConfig.getString("gameMode") + " mode." +
                " Game ID: " + nextGame.getString("gameID") +
                ", Time until start: " + nextGame.getInt("msUntilStart") / 1000 + " seconds."
                + ", Players: " + nextGame.getInt("numClients") + "/" + gameConfig.getInt("maxPlayers")
                + ", Playlist size: " + App.dataList.size() + "/" + App.PLAYLIST_LENGTH
        );

        gameConfig.remove("maxPlayers");
        gameConfig.remove("instantBuild");
        gameConfig.remove("infiniteGold");
        gameConfig.remove("infiniteTroops");
        gameConfig.remove("gameType");
        gameConfig.remove("disabledUnits");
        gameConfig.remove("disableNPCs");
        gameConfig.remove("difficulty");


        gameConfig.put("creationDate",(nextGame.getInt("msUntilStart")) -App.TIME_OF_WAIT + System.currentTimeMillis());

        App.dataList.add(gameConfig);


        if (!reachedDouble) {
            if (App.dataList.size() == App.PLAYLIST_LENGTH + 1) {
                System.out.println("Reached playlist length, removing first entry.");
                App.dataList.remove(0);
            }
            if (App.dataList.size() > App.QUEUE_SIZE_DUPLICATE) {
                if (doubleMap()) {
                    System.out.println("Double map detected: " + App.dataList.get(App.dataList.size() - 1).getString("gameMap") + " in " + App.dataList.get(App.dataList.size() - 1).getString("gameMode") + " mode.");
                    removePreviousPlaylist(endingPreviousPlaylistIndex());
                    App.reachedDouble = true;
                }
            }
        }
        if (reachedDouble) {
            if(App.dataList.size() == App.PLAYLIST_LENGTH) {
                App.dataList.clear();
            }
            if (App.dataList.size() == 1) {
                System.out.println("First entry added to the playlist: " + App.dataList.get(0).getString("gameMap") + " in " + App.dataList.get(0).getString("gameMode") + " mode.");
                long baseTimestamp = App.dataList.get(0).getLong("creationDate");
                for (int i = 0; i < 2000; i++) {
                    try {
                        ProducerConsumerService.timestamps.put(baseTimestamp-i);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                new Thread(() ->{
                    try {
                        producerConsumer.run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }

    }

    public boolean doubleMap() {
        int size = App.dataList.size();
        List<JSONObject> mostRecent = App.dataList.subList(Math.max(0, size - 6), size-1);

        for (JSONObject jsonObject : mostRecent) {
            assert App.dataList.get(size-1) != null;
            if (jsonObject.getString("gameMap").equals(App.dataList.get(size-1).getString("gameMap"))) {
                return true;
            }
        }
        return false;
    }

    public int endingPreviousPlaylistIndex() {
        int size = App.dataList.size();
        for (int i = size; i-- > 0; ) {
            if (App.dataList.get(i).getString("gameMode").equals("Team")) {
                return i;
            }
        }
        return -1;
    }

    public void removePreviousPlaylist(int index) {
        System.out.println(index);
        System.out.println(App.dataList.size());
        if (index < 0 || index >= App.dataList.size()) return;
        for (int i = 0; i <= index; i++) {
            App.dataList.remove(0);
        }
    }
}