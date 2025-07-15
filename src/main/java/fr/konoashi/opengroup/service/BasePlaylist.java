package fr.konoashi.opengroup.service;

import fr.konoashi.opengroup.util.PseudoRandom;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BasePlaylist {
    public static ArrayList<String> generateBasePlaylist() {
         ArrayList<String> basePlaylist = new ArrayList<>();
        /*addMapType(basePlaylist, "World", 3);
        addMapType(basePlaylist, "Europe", 2);
        addMapType(basePlaylist, "Africa", 2);
        addMapType(basePlaylist, "Australia", 1);
        addMapType(basePlaylist, "North America", 1);
        addMapType(basePlaylist, "Britannia", 1);
        addMapType(basePlaylist, "Gateway to the Atlantic", 1);
        addMapType(basePlaylist, "Iceland", 1);
        addMapType(basePlaylist, "South America", 1);
        addMapType(basePlaylist, "Deglaciated Antarctica", 1);
        addMapType(basePlaylist, "Europe Classic", 1);
        addMapType(basePlaylist, "Mena", 1);
        addMapType(basePlaylist, "Pangaea", 1);
        addMapType(basePlaylist, "Asia", 1);
        addMapType(basePlaylist, "Mars", 1);
        addMapType(basePlaylist, "Between Two Seas", 1);
        addMapType(basePlaylist, "East Asia", 1);
        addMapType(basePlaylist, "Black Sea", 1);
        addMapType(basePlaylist, "Faroe Islands", 1);
        addMapType(basePlaylist, "Falkland Islands", 1);
        addMapType(basePlaylist, "Baikal", 1);
        addMapType(basePlaylist, "Halkidiki", 1);
        addMapType(basePlaylist, "Strait of Gibraltar", 1);
        addMapType(basePlaylist, "Italia", 1);*/

        //v23.17
        addMapType(basePlaylist, "World", 3);
        addMapType(basePlaylist, "Europe", 2);
        addMapType(basePlaylist, "Europe Classic", 1);
        addMapType(basePlaylist, "Mena", 1);
        addMapType(basePlaylist, "North America", 1);
        addMapType(basePlaylist, "South America", 1);
        addMapType(basePlaylist, "Black Sea", 1);
        addMapType(basePlaylist, "Africa", 2);
        addMapType(basePlaylist, "Pangaea", 1);
        addMapType(basePlaylist, "Asia", 1);
        addMapType(basePlaylist, "Mars", 1);
        addMapType(basePlaylist, "Britannia", 1);
        addMapType(basePlaylist, "Gateway to the Atlantic", 1);
        addMapType(basePlaylist, "Australia", 1);
        addMapType(basePlaylist, "Iceland", 1);
        addMapType(basePlaylist, "Japan", 1);
        addMapType(basePlaylist, "Between Two Seas", 1);
        addMapType(basePlaylist, "Faroe Islands", 1);
        addMapType(basePlaylist, "Deglaciated Antarctica", 1);
        addMapType(basePlaylist, "Falkland Islands", 1);
        addMapType(basePlaylist, "Baikal", 1);
        addMapType(basePlaylist, "Halkidiki", 1);

        return basePlaylist;
    }

    public static void addMapType(ArrayList<String> basePlaylist, String name, int occurence) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Map name cannot be null or empty");
        }
        if (occurence <= 0) {
            throw new IllegalArgumentException("Occurrence must be greater than zero");
        }
        for (int i = 0; i < occurence; i++) {
            basePlaylist.add(name);
        }
    }



    public static boolean shufflePlaylist(JSONArray shuffledPlaylist, ArrayList<String> basePlaylist, long timestamp) {

        final PseudoRandom rand = new PseudoRandom(timestamp);

        List<String> ffa1 = rand.shuffleArray(basePlaylist);
        List<String> ffa2 = rand.shuffleArray(basePlaylist);
        List<String> ffa3 = rand.shuffleArray(basePlaylist);
        List<String> team = rand.shuffleArray(basePlaylist);

        System.out.println("debug1 " + ffa1);
        System.out.println("debug2 " + ffa2);
        System.out.println("debug3 " + ffa3);
        System.out.println("debug4 " + team);

        for (int i = 0; i < basePlaylist.size(); i++) {
            if (!canAddNextMap(shuffledPlaylist, ffa1, "Free For All")) {
                System.out.println("bruh1");
                return false;
            }
            if (!canAddNextMap(shuffledPlaylist, ffa2, "Free For All")) {
                System.out.println("bruh2");
                return false;
            }
            if (!canAddNextMap(shuffledPlaylist, ffa3, "Free For All")) {
                System.out.println("bruh3");
                return false;
            }
            if (!canAddNextMap(shuffledPlaylist, team, "Team")) {
                System.out.println("bruh4");
                return false;
            }
        }
        System.out.println("true");
        return true;
    }

    public static boolean canAddNextMap(JSONArray shuffledPlaylist, List<String> basePlaylist, String gameMode) {
        final int NON_CONSECUTIVE_NUM = 5;

        int start = Math.max(basePlaylist.size() - NON_CONSECUTIVE_NUM, 0);
        ArrayList<String> names = new ArrayList<>();
        for (int i = start; i < shuffledPlaylist.length(); i++) {
            names.add(shuffledPlaylist.getJSONObject(i).getString("gameMap"));
        }
        List<String> lastMaps = names.subList(start, basePlaylist.size());


        for (int i = 0; i < basePlaylist.size(); i++) {
            String candidateMap = basePlaylist.get(i);

            if (lastMaps.contains(candidateMap)) {
                continue;
            }

            basePlaylist.subList(0, i+1).clear();

            JSONObject newEntry = new JSONObject();
            newEntry.put("gameMap", candidateMap);
            newEntry.put("gameMode", gameMode);
            shuffledPlaylist.put(newEntry);

            return true;
        }

        return false;
    }


}
