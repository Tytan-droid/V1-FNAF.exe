package Class;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final Map<String, Clip> sounds = new HashMap<>();

    public static void loadAll(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Dossier introuvable : " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        if (files == null) return;

        for (File file : files) {
            loadOne(file);
        }
    }

    private static void loadOne(File file) {
        try {
            String name = file.getName()
                              .replace(".wav", "")
                              .toLowerCase();

            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            sounds.put(name, clip);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement : " + file.getName());
            e.printStackTrace();
        }
    }

    public static void play(String name) {
        Clip clip = sounds.get(name.toLowerCase());
        if (clip == null) return;

        if (clip.isRunning())
            clip.stop();

        clip.setFramePosition(0);
        clip.start();
    }

    public static void loop(String name) {
        Clip clip = sounds.get(name.toLowerCase());
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }


    public static void stop(String name) {
        Clip clip = sounds.get(name.toLowerCase());
        if (clip != null)
            clip.stop();
    }

    public static void stopAll() {
        for (Clip clip : sounds.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public static void playRandomAmbience() {
    String[] sounds = {
        "foxy-dum-dum-movie_QqCMzwk",
        "downloads_p20UxnT",
        "five-nights-at-freddys-foxys-song-youtube2",
        "Circus",
        "creepy-kid-laugh-fnaf"
    };

    String sound = sounds[(int)(Math.random() * sounds.length)];
    SoundManager.play(sound);
}

}
