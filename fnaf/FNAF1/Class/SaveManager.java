package Class;

import java.io.*;

public class SaveManager {

    private static final String SAVE_FILE = "FNAF1/save.dat";

    private static int night = 1;
    private static int stars = 0;

    public static void load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("night=")) {
                    night = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("stars=")) {
                    stars = Integer.parseInt(line.substring(6));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            File dir = new File("FNAF1");
            if (!dir.exists()) dir.mkdirs();

            try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                pw.println("night=" + night);
                pw.println("stars=" + stars);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int loadNight() {
        return night;
    }

    public static void saveNight(int n) {
        night = n;
        save();
    }

    public static int getStars() {
        return stars;
    }

    public static void unlockStars(int n) {
        if (n > stars) {
            stars = n;
            save();
        }
    }

    public static void reset() {
        night = 1;
        stars = 0;
        File file = new File(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
