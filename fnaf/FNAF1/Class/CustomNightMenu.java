package Class;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomNightMenu extends JPanel {
    static final int PRESET_ALL_0 = 0;
    static final int PRESET_ALL_20 = 1;
    static final int PRESET_HARD = 2;
    static final int PRESET_RANDOM = 3;
    static String lastPreset = "";
    static final long PRESET_COOLDOWN_MS = 300;
    static long lastPresetTime = 0;
    static boolean presetLocked = false;
    static boolean all20Lock = lastPreset.equals("ALL 20");

    static int[] difficulty = {0, 0, 0, 0, 0,0};
    static String[] names = {"Freddy", "Bonnie", "Chica", "Foxy", "Golden Freddy","Puppet"};
    static int selected = 0;

    public CustomNightMenu() {
        setBackground(Color.BLACK);
        setFocusable(true);

        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "left");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "right");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "start");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");

        am.put("up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selected = (selected - 1 + names.length) % names.length;
                repaint();
            }
        });

        am.put("down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selected = (selected + 1) % names.length;
                repaint();
            }
        });

        am.put("left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (all20Lock) return;
                difficulty[selected] = Math.max(0, difficulty[selected] - 1);
                repaint();
            }
        });

        am.put("right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (all20Lock) return;
                difficulty[selected] = Math.min(20, difficulty[selected] + 1);
                repaint();
            }
        });

        am.put("start", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Main.startCustomNight(difficulty);
            }
        });
    boolean all20Lock = lastPreset.equals("ALL20");

        am.put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Window w = SwingUtilities.getWindowAncestor(CustomNightMenu.this);
                if (w != null) w.dispose();

                SwingUtilities.invokeLater(() -> {
                    Main.returnToMenu();
                });
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "preset0");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), "preset0");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "preset20");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "preset20");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "presetHard");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), "presetHard");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "presetRandom");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "presetRandom");

        am.put("preset0", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tryApplyPreset(PRESET_ALL_0);
            }
        });

        am.put("preset20", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tryApplyPreset(PRESET_ALL_20);
            }
        });

        am.put("presetHard", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tryApplyPreset(PRESET_HARD);
            }
        });

        am.put("presetRandom", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tryApplyPreset(PRESET_RANDOM);
            }
        });

        SwingUtilities.invokeLater(() -> requestFocusInWindow());

    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (!lastPreset.isEmpty()) {
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.setColor(getPresetColor(lastPreset));
            g2.drawString("PRESET: " + lastPreset, 280, 70);
        }


        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        g2.setColor(Color.WHITE);

        int y = 120;

        for (int i = 0; i < names.length; i++) {
            boolean sel = i == selected;

            g2.setColor(sel ? Color.YELLOW : Color.WHITE);
            g2.drawString(
                (sel ? "> " : "  ") + names[i],
                200, y
            );

            g2.drawRect(450, y - 18, 60, 24);
            int d = difficulty[i];

            Color valueColor;
            if (d == 0) valueColor = Color.GRAY;
            else if (d <= 5) valueColor = new Color(100, 255, 100);
            else if (d <= 10) valueColor = Color.YELLOW;
            else if (d <= 15) valueColor = Color.ORANGE;
            else valueColor = Color.RED;

            g2.setColor(valueColor);
            g2.drawString(String.format("%2d", d), 465, y);

            y += 40;
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(Color.GRAY);
        g2.drawString(
                "← →: Modify  ↑ ↓: Select  ENTER: Begin  ESC: Return  \n [1]ALL 0  [2]ALL 20  [3]HARD  [4]RANDOM",
                60, getHeight() - 40
            );
    }

    private void applyPreset(int preset) {
        
        lastPreset = switch (preset) {
            case PRESET_ALL_0 -> "ALL 0";
            case PRESET_ALL_20 -> "ALL 20";
            case PRESET_HARD -> "HARD";
            case PRESET_RANDOM -> "RANDOM";
            default -> "";
        };
        switch (preset) {
            case PRESET_ALL_0 -> {
                for (int i = 0; i < difficulty.length; i++) difficulty[i] = 0;
                all20Lock = false;
            }
            case PRESET_ALL_20 -> {
                for (int i = 0; i < difficulty.length; i++) difficulty[i] = 20;
                all20Lock = true;
            }
            case PRESET_HARD -> {
                difficulty[0] = 15;
                difficulty[1] = 15;
                difficulty[2] = 15;
                difficulty[3] = 18;
                difficulty[4] = 10;
                difficulty[5]=18;
                all20Lock = false;
            }
            case PRESET_RANDOM -> {
                for (int i = 0; i < difficulty.length; i++)
                    difficulty[i] = (int)(Math.random() * 21);
                all20Lock = false;
            }
        }
        repaint();
    }

    private void tryApplyPreset(int preset) {
        long now = System.currentTimeMillis();

        if (now - lastPresetTime < PRESET_COOLDOWN_MS) {
            presetLocked = true;
            repaint();
            return;
        }

        lastPresetTime = now;
        presetLocked = false;

        applyPreset(preset);
    }

    private Color getPresetColor(String preset) {
        return switch (preset) {
            case "ALL 0" -> new Color(0, 200, 0);
            case "ALL 20" -> Color.RED;
            case "HARD" -> Color.ORANGE;
            case "RANDOM" -> Color.MAGENTA;
            default -> Color.WHITE;
        };
    }

    public static boolean is_all20(){
        return all20Lock;
    }
}
