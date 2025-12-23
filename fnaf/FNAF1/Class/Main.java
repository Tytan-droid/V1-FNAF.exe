package Class;

import javax.swing.*;
import java.awt.event.ActionEvent;

import java.awt.Cursor;
import java.awt.Dimension;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Class.animatronics.*;
import Class.rooms.*;
import Class.GamePanel.CameraMap;
import Class.SoundManager;

public class Main {

    public static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS;

    private static volatile boolean cam = false;
    private static volatile boolean light_left = false;
    private static volatile boolean light_right = false;
    private static volatile int cam_id = 0;
    private static volatile boolean left_door_close = false;
    private static volatile boolean right_door_close = false;
    private static volatile boolean running = true;
    private static volatile int position = 0; // 0 gauche, 1 droite
    private static volatile boolean can_play = true;
    private static boolean rewindKeyDown = false;

    private static L_animatronics L_a;
    private static Rooms_Graph rg;
    private static GamePanel panel;
    private static JFrame gameFrame;
    private static JFrame menuFrame;
    private static JFrame gameOverFrame;
    private static JFrame winFrame;
    private static float hintAlpha = 0f;
    private static boolean hintFadeIn = true;

    private static boolean glitchActive = false;
    private static int glitchTicks = 0;
    private static Timer shakeTimer;

    private static boolean powerOut = false;
    private static long powerOutStart = 0;
    private static long nightStartTime;
    private static final int SECONDS_PER_HOUR = 30;
    private static int currentNight = 1;

    private static volatile int power_usage = 1;
    private static volatile int power = 1000 * 60;

    public static boolean staticActive = false;
    public static long staticStartTime;
    public static final int STATIC_DURATION = 500;


    private enum PowerOutPhase {
        DARKEN,
        EYES,
        JUMPSCARE,
        DONE
    }

    private static PowerOutPhase powerOutPhase = null;
    private static long powerOutPhaseStart;


    private static final String[] CAMERA_IDS = {
            "CAM1A","CAM1B","CAM1C","CAM2A","CAM2B","CAM3",
            "CAM4A","CAM4B","CAM5","CAM6","CAM7"
    };
    static String[] hints = {
        "ESC during the night: return to menu",
        "Close the doors only if necessary",
        "Don't forget th music box...",
        "Listen to the footsteps...",
        "Don't blink for too long",
        "They are already watching you",
        "Mais où touvent-ils toute cette énergie ?",
        "Ur ur ur ur...",
        "Was that the bite of 67 ?",
        "Pizza !"
    };


    private static final Map<String, Long> cooldowns = new HashMap<>();

    private static boolean canUse(String action, long cooldownMs) {
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(action, 0L);
        if (now - last >= cooldownMs) {
            cooldowns.put(action, now);
            return can_play;
        }
        return false;
    }

    public static void main(String[] args) {
        SaveManager.load();
        SoundManager.loadAll("FNAF1/Sounds");
        currentNight = SaveManager.loadNight();
        initialise_menu();
    }

    public static void night(int num) {
        running = true;
        nightStartTime = System.currentTimeMillis();
        cam = false;
        light_left = false;
        light_right = false;
        left_door_close = false;
        right_door_close = false;
        cam_id = 0;
        position = 0;
        power_usage = 1;
        power = 1000 * 60;
        powerOut = false;
        powerOutStart = 0;
        can_play=true;
        SoundManager.loop("Eerie ambience largesca");
        L_a = new L_animatronics();
        if (num == 1) L_a.L_animatronics_Builder_n1();
        if (num == 2) L_a.L_animatronics_Builder_n2();
        if (num == 3) L_a.L_animatronics_Builder_n3();
        if (num == 4) L_a.L_animatronics_Builder_n4();
        if (num == 5) L_a.L_animatronics_Builder_n5();
        if (num == 6) L_a.L_animatronics_Builder_n6();

        rg = new Rooms_Graph();
        rg.Rooms_Graph_Builder();
        gameFrame = new JFrame("FNAF");
        gameFrame.setSize(800, 500);
        gameFrame.setUndecorated(true);
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        panel = new GamePanel();
        gameFrame.add(panel);
        gameFrame.setVisible(true);
        gameFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (canUse("ESC", 500)) {
                        returnToMenu();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!canUse("CAM", 500)) return;
                    if (cam) remove_cam();
                    else {
                        put_cam();
                        if(light_left||light_right){
                            power_usage--;
                        }
                        remove_light(0);
                        remove_light(1);
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_Q|| e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (!canUse("LEFT", 300)) return;
                    if (cam) switch_cam_left();
                    else {
                        turn_left();
                        if (light_right) {
                            remove_light(1);
                            put_light();
                        }
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (!canUse("RIGHT", 300)) return;
                    if (cam) switch_cam_right();
                    else {
                        turn_right();
                        if (light_left) {
                            remove_light(0);
                            put_light();
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (!rewindKeyDown && !cam) {
                        rewindKeyDown = true;
                        getAnimatronics().get_puppet().rewind();
                    }
                    return;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_UP)
                    && rewindKeyDown) {

                    rewindKeyDown = false;
                    getAnimatronics().get_puppet().end_rewind();
                }
            }
        });
        gameFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !cam) {
                    if (canUse("LIGHT", 150)) {put_light(); power_usage++;}
                }
                else if (SwingUtilities.isRightMouseButton(e) && !cam) {
                    if (canUse("DOOR", 200)) door(rg);
                }
                if (cam){
                    Point p = e.getPoint();
                    for (Map.Entry<String, Rectangle> entry : CameraMap.cameras.entrySet()) {
                        if (entry.getValue().contains(p)) {
                            switchCamera(entry.getKey());
                            break;
                        }
                    }
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)&&(light_left||light_right)) {
                    remove_light(position); power_usage--;
                }
            }
        });
        gameFrame.setFocusable(true);
        gameFrame.requestFocusInWindow();

        new Thread(Main::run).start();
    }

    private static void run() {
        while (running) {
            long start = System.currentTimeMillis();
            update();
            render();
            long sleep = FRAME_TIME - (System.currentTimeMillis() - start);
            if (sleep > 0) {
                try { Thread.sleep(sleep); }
                catch (InterruptedException ignored) {}
            }
        }
    }

    private static void update() {
        if(cam){
            getAnimatronics().get_puppet().end_rewind();
            rewindKeyDown=false;
        }
        if (rewindKeyDown && !cam) {
            getAnimatronics().get_puppet().rewind();
        }

        if (getHour() >= 6) {
            nightWin();
            return;
        }

        if (!powerOut && power <= 0) {
            triggerPowerOut();
            return;
        }

        if (powerOut) {
            checkPowerOutKill();
            return;
        }

        L_a.move_all_animatronics(rg);
        power -= power_usage * 2.5;
    }

    private static void triggerPowerOut() {
        powerOut = true;
        powerOutStart = System.currentTimeMillis();
        powerOutPhase = PowerOutPhase.DARKEN;
        powerOutPhaseStart = powerOutStart;

        can_play = false;
        cam = false;
        light_left = false;
        light_right = false;

        if (left_door_close) open_left_door(rg);
        if (right_door_close) open_right_door(rg);

        power_usage = 1;

        SoundManager.stopAll();
        SoundManager.play("Powerdown");
        SoundManager.play("Footsteps");

        if (panel != null) {
            panel.startPowerOutAnimation();
        }
    }


    private static void checkPowerOutKill() {

        if (getHour() >= 6) {
            nightWin();
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - powerOutPhaseStart;

        switch (powerOutPhase) {

            case DARKEN:
                SoundManager.play("Deep_Steps");
                powerOutPhase = PowerOutPhase.EYES;
                powerOutPhaseStart = now;
                break;

            case EYES:
                if (elapsed >= 3000) {
                    powerOutPhase = PowerOutPhase.JUMPSCARE;
                    powerOutPhaseStart = now;
                    if (panel != null) {
                        panel.enableFreddyEyes(true);
                        SoundManager.stop("Deep_Steps");
                        SoundManager.play("fnaf-1-beatbox");
                }
            }
                break;

            case JUMPSCARE:
                if (elapsed >= 12500) {
                    panel.enableFreddyEyes(false);
                    powerOutPhase = PowerOutPhase.DONE;
                    SoundManager.stopAll();
                    elapsed=13000;
                }
            case DONE:
                if (elapsed >= 15000) {
                        L_animatronics la = getAnimatronics();
                        for (abstrac_animatronic a : la.get_L()) {
                            if (a instanceof Freddy) {
                                a.set_coter(0);
                                startJumpscare(a);
                                break;
                            }
                        }
                    }
                break;
        }
    }

    private static void render() {
        if (panel != null) panel.repaint();
    }

    public static int getPosition() { return position; }
    public static L_animatronics getAnimatronics() { return L_a; }
    public static boolean isLightLeft() { return light_left; }
    public static boolean isLightRight() { return light_right; }
    public static boolean isCam() { return cam; }

    public static String getCurrentCamera() {
        if (cam_id >= 0 && cam_id < CAMERA_IDS.length) return CAMERA_IDS[cam_id];
        return CAMERA_IDS[0];
    }

    public static void switchCamera(String cam_Id) {
        if (Main.getCurrentCamera().equals(cam_Id)) return;
        for(int id =0; id<CAMERA_IDS.length;id++){
            if (CAMERA_IDS[id].equals(cam_Id)){
                cam_id=id;
            }
        }
        SoundManager.play("fnaf2-camera");
        GamePanel.camNoiseStrength = 0.7f;
        GamePanel.camSwitching = true;        
        }

    public static void triggerStatic() {
        staticActive = true;
        staticStartTime = System.currentTimeMillis();
    }

    private static void put_cam() {
        SoundManager.play("fnaf-open-camera-sound");
        power_usage++;
        cam = true;
        triggerStatic();
    }

    public static void remove_cam() {
        power_usage--;
        cam = false;
    }

    private static void put_light() {
        SoundManager.play("fnaf-light-sound");
        if (position == 0) light_left = true;
        else light_right = true;
    }

    private static void remove_light(int pos) {
        if (pos == 0) light_left = false;
        else light_right = false;
    }

    private static void turn_left() { position = 0; }
    private static void turn_right() { position = 1; }

    private static void switch_cam_left() {
        SoundManager.play("fnaf2-camera");
        cam_id = (cam_id > 0) ? cam_id - 1 : 10;
        GamePanel.camNoiseStrength = 0.7f;
        GamePanel.camSwitching = true;    
        }

    private static void switch_cam_right() {
        SoundManager.play("fnaf2-camera");
        cam_id = (cam_id < 10) ? cam_id + 1 : 0;
        GamePanel.camNoiseStrength = 0.7f;
        GamePanel.camSwitching = true;    
        }

    private static volatile long lastBlink = 0;

    public static void blinkCamera(int delayMs) {
        long now = System.currentTimeMillis();
        if (!cam) return;
        if (now - lastBlink < 300) return;
        lastBlink = now;
        if (panel != null) panel.startCameraBlackout(300);
    }

    public static void close_left_door(Rooms_Graph rg) {
        SoundManager.play("door_slamming_fnaf_1_sound_effects");
        power_usage++;
        rg.removeEdge(rg.getRoom("You"), rg.getRoom("Door_Left"));
        left_door_close = true;
    }

    public static void close_right_door(Rooms_Graph rg) {
        SoundManager.play("door_slamming_fnaf_1_sound_effects");
        power_usage++;
        rg.removeEdge(rg.getRoom("You"), rg.getRoom("Door_Right"));
        right_door_close = true;
    }

    public static void open_right_door(Rooms_Graph rg) {
        power_usage--;
        rg.addEdge(rg.getRoom("You"), rg.getRoom("Door_Right"));
        right_door_close = false;
    }

    public static void open_left_door(Rooms_Graph rg) {
        power_usage--;
        rg.addEdge(rg.getRoom("You"), rg.getRoom("Door_Left"));
        left_door_close = false;
    }

    public static void door(Rooms_Graph rg) {
        if (position == 0) {
            if (left_door_close) open_left_door(rg);
            else close_left_door(rg);
        } else {
            if (right_door_close) open_right_door(rg);
            else close_right_door(rg);
        }
    }

    public static void set_running_false(){
        running=false;
    }

    public static void initialise_menu() {
        if (menuFrame != null) return;

        int nightNumber = Math.max(1, currentNight);
        String randomHint = hints[(int)(Math.random() * hints.length)];

        SoundManager.loop("fnaf-music-box-109");

        menuFrame = new JFrame("FNAF");
        menuFrame.setSize(800, 500);
        menuFrame.setUndecorated(true);
        menuFrame.setResizable(false);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);

        JLabel title = new JLabel("Five Nights at Freddy's");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel hint = new JLabel(randomHint) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hintAlpha)
                );
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("Arial", Font.PLAIN, 14));
        hint.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel continueBtn = createMenuItem("CONTINUE", () -> startNight(nightNumber));

        JLabel nightLabel = new JLabel("Night " + nightNumber);
        nightLabel.setForeground(Color.GRAY);
        nightLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nightLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel restartBtn = createMenuItem("NEW GAME", () -> {
            SaveManager.saveNight(1);
            startNight(1);
        });

        JLabel customBtn = createMenuItem("CUSTOM NIGHT", () -> {
            openCustomNight();
        });

        boolean customUnlocked = SaveManager.getStars() >= 2;

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(hint);
        panel.add(Box.createVerticalStrut(40));
        panel.add(continueBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nightLabel);
        panel.add(Box.createVerticalStrut(25));
        panel.add(restartBtn);
        if (customUnlocked) {
            panel.add(Box.createVerticalStrut(15));
            panel.add(customBtn);
        }
        panel.add(Box.createVerticalGlue());

        new Timer(40, e -> {
            if (hintFadeIn) hintAlpha += 0.02f;
            else hintAlpha -= 0.02f;

            if (hintAlpha >= 1f) hintFadeIn = false;
            if (hintAlpha <= 0.2f) hintFadeIn = true;

            hintAlpha = Math.max(0.2f, Math.min(1f, hintAlpha));
            hint.repaint();
        }).start();

        new Timer(120, e -> {

            if (!glitchActive && Math.random() < 0.03) {
                glitchActive = true;
                glitchTicks = 5 + (int)(Math.random() * 6);
            }

            if (glitchActive && --glitchTicks <= 0)
                glitchActive = false;

            title.setText(glitchText("Five Nights at Freddy's"));
            hint.setText(glitchText(randomHint));
            continueBtn.setText(glitchText("CONTINUE"));
            nightLabel.setText(glitchText("Night " + nightNumber));
            restartBtn.setText(glitchText("NEW GAME"));
            customBtn.setText(glitchText("CUSTOM NIGHT"));

        }).start();

        InputMap im = menuFrame.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = menuFrame.getRootPane().getActionMap();

        List<JLabel> menuItems = new ArrayList<>();
        menuItems.add(continueBtn);
        menuItems.add(restartBtn);

        if (customUnlocked) {
            menuItems.add(customBtn);
        }
        final int[] selected = {0};

        Runnable refreshSelection = () -> {
            for (int i = 0; i < menuItems.size(); i++) {
                menuItems.get(i).setForeground(
                    i == selected[0] ? Color.YELLOW : Color.WHITE
                );
            }
        };
        refreshSelection.run();

        im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        am = panel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "select");


        am.put("up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selected[0] = (selected[0] - 1 + menuItems.size()) % menuItems.size();
                refreshSelection.run();
            }
        });

        am.put("down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selected[0] = (selected[0] + 1) % menuItems.size();
                refreshSelection.run();
            }
        });

        am.put("select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JLabel target = menuItems.get(selected[0]);
                target.dispatchEvent(new MouseEvent(
                    target,
                    MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(),
                    0, 1, 1, 1, false
                ));
            }
        });


        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        am.put("exit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        Point base = menuFrame.getLocation();

        shakeTimer = new Timer(50, e -> {
            if (menuFrame == null || !menuFrame.isDisplayable()) {
                ((Timer)e.getSource()).stop();
                return;
            }
            if (Math.random() < 0.05) {
                int dx = (int)(Math.random() * 6 - 3);
                int dy = (int)(Math.random() * 6 - 3);
                menuFrame.setLocation(base.x + dx, base.y + dy);
            } else {
                menuFrame.setLocation(base);
            }
        });
        shakeTimer.start();

        JPanel starsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image star = GamePanel.loadStar();
                if (star == null) return;

                int stars = SaveManager.getStars();
                int size = 50;
                int padding = 8;

                Graphics2D g2 = (Graphics2D) g.create();
                float alpha = 0.7f + (float)Math.sin(System.currentTimeMillis() * 0.005) * 0.3f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                for (int i = 0; i < stars; i++) {
                    g2.drawImage(
                        star,
                        padding + i * (size + 6),
                        padding,
                        size,
                        size,
                        this
                    );
                }
                g2.dispose();
            }
        };
        new Timer(100, e -> starsPanel.repaint()).start();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.BLACK);

        root.add(starsPanel, BorderLayout.NORTH);
        root.add(panel, BorderLayout.CENTER);

        menuFrame.setContentPane(root);
        menuFrame.setVisible(true);
        starsPanel.setOpaque(false);
        starsPanel.setPreferredSize(new Dimension(120, 100));

        starsPanel.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            starsPanel.invalidate();
            starsPanel.validate();
            starsPanel.repaint();
        });

    }

    private static JLabel createMenuItem(String text, Runnable action) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.setMaximumSize(new Dimension(300, 40));
        label.setPreferredSize(new Dimension(300, 40));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        label.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.YELLOW);
            }

            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.WHITE);
            }

            public void mousePressed(MouseEvent e) {
                action.run();
            }
        });

        return label;
    }


    public static boolean left_door_close() { return left_door_close; }
    public static boolean right_door_close() { return right_door_close; }

    public static void returnToMenu() {

        running = false;
        stopRandomSounds();
        SoundManager.stopAll();
        SoundManager.loop("fnaf-music-box-109");
        SaveManager.load();

        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }

        if (menuFrame != null) {
            menuFrame.dispose();
            menuFrame = null;
        }

        SwingUtilities.invokeLater(() -> {
            initialise_menu();
        });
    }


    private static void startNight(int night) {
        SoundManager.stop("fnaf-music-box-109");
        stopMenuEffects();
        startRandomSounds();
        menuFrame.dispose();
        menuFrame = null;
        night(night);
    }

    private static void stopMenuEffects() {
    if (shakeTimer != null) {
        shakeTimer.stop();
        shakeTimer = null;
    }
}


    public static void gameOver() {
        running = false;
        SoundManager.stopAll();
        SoundManager.play("bite_of_87_fnaf");
        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }
        if (gameOverFrame != null) return;
        gameOverFrame = new JFrame("GAME OVER");
        gameOverFrame.setSize(800, 500);
        gameOverFrame.setUndecorated(true);
        gameOverFrame.setResizable(false);
        gameOverFrame.setLocationRelativeTo(null);
        gameOverFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(java.awt.Color.BLACK);
        JLabel title = new JLabel("GAME OVER");
        title.setForeground(java.awt.Color.RED);
        title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 60));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JLabel info = new JLabel("PRESS ENTER to return to menu");
        info.setForeground(java.awt.Color.WHITE);
        info.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
        info.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(info);
        panel.add(Box.createVerticalGlue());
        gameOverFrame.add(panel);
        gameOverFrame.setVisible(true);
        gameOverFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER||e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameOverFrame.dispose();
                    gameOverFrame = null;
                    initialise_menu();
                }
            }
        });
    }
    public static int getPower() {
        return power;
    }
    public static int getPowerUsage(){
        return power_usage;
    }
    public static int getHour() {
        long elapsedSeconds = (System.currentTimeMillis() - nightStartTime) / 1000;
        int hour = (int) (elapsedSeconds / SECONDS_PER_HOUR);

        if (hour > 6) hour = 6;
        return hour;
    }
    public static String getHourString() {
        int hour = getHour();
        if (hour == 0) return "12 AM";
        return hour + " AM";
    }

    public static void nightWin() {
        if (currentNight == 5) {
            SaveManager.unlockStars(1);
        }

        if (currentNight == 6) {
            SaveManager.unlockStars(2);
        }

        if (CustomNightMenu.is_all20()) {
            SaveManager.unlockStars(3);
        }
        if (!running) return;
        running = false;
        if (currentNight < 6) {
            currentNight++;
            SaveManager.saveNight(currentNight);
        }
        stopRandomSounds();
        SaveManager.load();
        SoundManager.stopAll();
        SoundManager.play("fnaf-chimes");

        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }

        if (winFrame != null) return;

        winFrame = new JFrame("6 AM");
        winFrame.setSize(800, 500);
        winFrame.setUndecorated(true);
        winFrame.setResizable(false);
        winFrame.setLocationRelativeTo(null);
        winFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);

        JLabel title = new JLabel("6 AM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 72));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel win = new JLabel("YOU WON !");
        win.setForeground(Color.GREEN);
        win.setFont(new Font("Arial", Font.PLAIN, 22));
        win.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("PRESS ENTER to return to menu");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("Arial", Font.PLAIN, 14));
        hint.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(win);
        panel.add(Box.createVerticalStrut(30));
        panel.add(hint);
        panel.add(Box.createVerticalGlue());

        winFrame.add(panel);
        winFrame.setVisible(true);

        winFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    winFrame.dispose();
                    winFrame = null;
                    SoundManager.stopAll();
                    initialise_menu();
                }
            }
        });
    }

    public static void cant_play(){
        can_play=false;
    }
    public static GamePanel getGamePanel() {
        return panel;
    }
    public static void startJumpscare(abstrac_animatronic a) {
        running = false;
        stopRandomSounds();
        SaveManager.load();
        panel.startSlideJumpscare(a);
    }

    private static String glitchText(String s) {
        if (!glitchActive) return s;

        char[] c = s.toCharArray();
        int changes = 1 + (int)(Math.random() * 2);

        for (int i = 0; i < changes; i++) {
            int idx = (int)(Math.random() * c.length);
            if (c[idx] != ' ')
                c[idx] = (char)(33 + Math.random() * 50);
        }
        return new String(c);
    }

    public static void openCustomNight() {
        if (menuFrame != null) menuFrame.dispose();

        menuFrame = new JFrame("Custom Night");
        menuFrame.setSize(800, 500);
        menuFrame.setUndecorated(true);
        menuFrame.setLocationRelativeTo(null);

        CustomNightMenu panel = new CustomNightMenu();
        menuFrame.setContentPane(panel);
        menuFrame.setVisible(true);

        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }

    public static void startCustomNight(int[] dif){
        SoundManager.stop("fnaf-music-box-109");
        stopMenuEffects();
        startRandomSounds();
        menuFrame.dispose();
        menuFrame = null;
        running = true;
        nightStartTime = System.currentTimeMillis();
        cam = false;
        light_left = false;
        light_right = false;
        left_door_close = false;
        right_door_close = false;
        cam_id = 0;
        position = 0;
        power_usage = 1;
        power = 1000 * 60;
        powerOut = false;
        powerOutStart = 0;
        can_play=true;
        SoundManager.loop("Eerie ambience largesca");
        L_a = new L_animatronics();
        L_a.L_animatronics_Builder_custom(dif);

        rg = new Rooms_Graph();
        rg.Rooms_Graph_Builder();
        gameFrame = new JFrame("FNAF");
        gameFrame.setSize(800, 500);
        gameFrame.setUndecorated(true);
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        panel = new GamePanel();
        gameFrame.add(panel);
        gameFrame.setVisible(true);
        gameFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (canUse("ESC", 500)) {
                        returnToMenu();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!canUse("CAM", 500)) return;
                    if (cam) remove_cam();
                    else {
                        put_cam();
                        if(light_left||light_right){
                            power_usage--;
                        }
                        remove_light(0);
                        remove_light(1);
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_Q|| e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (!canUse("LEFT", 300)) return;
                    if (cam) switch_cam_left();
                    else {
                        turn_left();
                        if (light_right) {
                            remove_light(1);
                            put_light();
                        }
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (!canUse("RIGHT", 300)) return;
                    if (cam) switch_cam_right();
                    else {
                        turn_right();
                        if (light_left) {
                            remove_light(0);
                            put_light();
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (!rewindKeyDown && !cam) {
                        rewindKeyDown = true;
                        getAnimatronics().get_puppet().rewind();
                    }
                    return;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_UP)
                    && rewindKeyDown) {

                    rewindKeyDown = false;
                    getAnimatronics().get_puppet().end_rewind();
                }
            }
        });
        gameFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !cam) {
                    if (canUse("LIGHT", 150)) {put_light(); power_usage++;}
                }
                else if (SwingUtilities.isRightMouseButton(e) && !cam) {
                    if (canUse("DOOR", 200)) door(rg);
                }
                if (cam){
                    Point p = e.getPoint();
                    for (Map.Entry<String, Rectangle> entry : CameraMap.cameras.entrySet()) {
                        if (entry.getValue().contains(p)) {
                            switchCamera(entry.getKey());
                            break;
                        }
                    }
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)&&(light_left||light_right)) {
                    remove_light(position); power_usage--;
                }
            }
        });
        new Thread(Main::run).start();
        gameFrame.setFocusable(true);
        gameFrame.requestFocusInWindow();
    }

    private static Timer randomSoundTimer;

    public static void startRandomSounds() {
        randomSoundTimer = new Timer(1000, e -> {
            if (!can_play || powerOut) return;

            if (Math.random() < 0.03) {
                SoundManager.playRandomAmbience();
            }
        });
        randomSoundTimer.start();
    }

    public static void stopRandomSounds() {
        if (randomSoundTimer != null) {
            randomSoundTimer.stop();
            randomSoundTimer = null;
        }
    }


}

