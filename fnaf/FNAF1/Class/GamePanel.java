package Class;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import Class.animatronics.abstrac_animatronic;
import Class.animatronics.Chica;
import Class.animatronics.Bonnie;
import Class.animatronics.Freddy;
import Class.animatronics.Freddy_Eyes;
import Class.animatronics.Foxy;
import Class.animatronics.Golden_Freddy;
import Class.animatronics.Puppet;
import Class.animatronics.L_animatronics;

public class GamePanel extends JPanel {
    private Image camOverlay;
    private Image image;
    private Image guardLeft;
    private Image guardRight;
    private Map<String, Image> cameraImages = new HashMap<>();
    private Image flashlightLeft;
    private Image flashlightRight;
    private Map<String, Image> animImages = new HashMap<>();
    private java.util.Map<abstrac_animatronic, String> lastSeenRooms = new java.util.HashMap<>();
    private volatile boolean blackoutActive = false;
    private volatile long blackoutStart = 0L;
    private volatile long blackoutDuration = 0L;
    private boolean slideActive = false;
    private long slideStart = 0;
    private static final int SLIDE_DURATION = 600;
    private abstrac_animatronic slideAnim = null;

    private boolean flashActive = false;
    private long flashStart = 0;
    private static final int FLASH_DURATION = 120;

    private boolean shakeActive = false;
    private long shakeStart = 0;
    private static final int SHAKE_DURATION = 300;
    private static final int SHAKE_INTENSITY = 18;

    private boolean powerOutAnim = false;
    private float darkness = 0f;

    private float eyesGlow = 0.0f;
    private boolean glowUp = true;

    private boolean eyesVisible = true;
    private long lastBlink = System.currentTimeMillis();

    public void startCameraBlackout(int ms) {
        blackoutDuration = Math.max(0, ms);
        blackoutStart = System.currentTimeMillis();
        blackoutActive = true;
        new Thread(() -> {
            long end = blackoutStart + blackoutDuration;
            while (System.currentTimeMillis() < end) {
                try { Thread.sleep(16); } catch (InterruptedException ignored) {}
                repaint();
            }
            blackoutActive = false;
            repaint();
        }).start();
        repaint();
    }

    public void drawAnimAtGuard(Graphics g, abstrac_animatronic a, int x, int y, int targetW, int targetH, boolean leftSide) {
        if (a == null) return;
        String baseName;
        if (a instanceof Chica) baseName = "Chica";
        else if (a instanceof Bonnie) baseName = "Bonnie";
        else if (a instanceof Freddy) baseName = "Freddy";
        else if (a instanceof Foxy) baseName = "Foxy";
        else if (a instanceof Golden_Freddy) baseName = "Golden_Freddy";
        else if (a instanceof Freddy_Eyes) baseName = "Freddy_Eyes";
        else if(a instanceof Puppet) baseName = "Puppet";
        else baseName = null;

        Graphics2D g2 = (Graphics2D) g.create();
        int drawW = targetW * 2 / 3;
        int drawH = targetH * 2 / 3;
        int dx = x - drawW/2 + (leftSide ? -targetW/6 : targetW/6);
        int dy = y - drawH/2;

        if (baseName != null) {
            String side = !leftSide ? "_Left" : "_Right";
            String key = baseName + side;
            Image animImg = animImages.get(key);
            if (animImg == null) {
                try {
                    java.io.File f = new java.io.File("FNAF1/Pictures/" + key + ".png");
                    if (f.exists()) animImg = ImageIO.read(f);
                } catch (IOException ignored) {}
                if (animImg != null) animImages.put(key, animImg);
            }
            if (animImg != null) {
                int iw = animImg.getWidth(this);
                int ih = animImg.getHeight(this);
                int drawHH = (iw > 0 && ih > 0) ? (int) ((double) drawW * ih / iw) : drawW;
                g2.drawImage(animImg, dx, dy - (drawHH - drawH)/2, drawW, drawHH, this);
            }
        }
        g2.dispose();
    }

    private void drawCameraOverlay(Graphics g) {
        String camId = Main.getCurrentCamera();
        BufferedImage camOverlay2 = loadImage("Cam_commun_overlay.png");
        if (camOverlay != null) {
            g.drawImage(camOverlay2, 0, 0, getWidth(), getHeight(), this);
        }
        camOverlay = loadImage(camId+"_interface.png");
        if (camOverlay != null) {
            g.drawImage(camOverlay, 0, 0, getWidth(), getHeight(), this);
        }
        g.setColor(new Color(0,0,0,40));
        for (int y = 0; y < getHeight(); y += 4) {
            g.fillRect(0, y, getWidth(), 1);
        }
    }

    private BufferedImage loadImage(String name) {
        try {
            var url = getClass().getResource("/images/" + name);
            if (url != null) return ImageIO.read(url);
        } catch (IOException ignored) {}

        try {
            File f = new File("FNAF1/Pictures/" + name);
            if (f.exists()) return ImageIO.read(f);
        } catch (IOException ignored) {}

        return null;
    }

    public GamePanel() {        
        try {
            java.io.File f = new java.io.File("FNAF1/Pictures/room_You.png");
            if (f.exists()) {
                image = ImageIO.read(f);
            }
        } catch (IOException e) {}

        try {
            java.net.URL urlL = getClass().getResource("/images/Guard_Left.png");
            if (urlL != null) guardLeft = ImageIO.read(urlL);
        } catch (IOException ignored) {}
        try {
            java.net.URL urlR = getClass().getResource("/images/Guard_Right.png");
            if (urlR != null) guardRight = ImageIO.read(urlR);
        } catch (IOException ignored) {}
        try {
            java.io.File fL = new java.io.File("FNAF1/Pictures/Guard_Left.png");
            if (guardLeft == null && fL.exists()) guardLeft = ImageIO.read(fL);
        } catch (IOException ignored) {}
        try {
            java.io.File fR = new java.io.File("FNAF1/Pictures/Guard_Right.png");
            if (guardRight == null && fR.exists()) guardRight = ImageIO.read(fR);
        } catch (IOException ignored) {}

        try {
            java.net.URL urlFL = getClass().getResource("/images/Flashlight_Left.png");
            if (urlFL != null) flashlightLeft = ImageIO.read(urlFL);
        } catch (IOException ignored) {}
        try {
            java.net.URL urlFR = getClass().getResource("/images/Flashlight_Right.png");
            if (urlFR != null) flashlightRight = ImageIO.read(urlFR);
        } catch (IOException ignored) {}
        try {
            java.io.File ffL = new java.io.File("FNAF1/Pictures/Flashlight_Left.png");
            if (flashlightLeft == null && ffL.exists()) flashlightLeft = ImageIO.read(ffL);
        } catch (IOException ignored) {}
        try {
            java.io.File ffR = new java.io.File("FNAF1/Pictures/Flashlight_Right.png");
            if (flashlightRight == null && ffR.exists()) flashlightRight = ImageIO.read(ffR);
        } catch (IOException ignored) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int shakeX = 0;
        int shakeY = 0;

        if (shakeActive) {
            long elapsed = System.currentTimeMillis() - shakeStart;
            if (elapsed < SHAKE_DURATION) {
                shakeX = (int) (Math.random() * SHAKE_INTENSITY - SHAKE_INTENSITY / 2);
                shakeY = (int) (Math.random() * SHAKE_INTENSITY - SHAKE_INTENSITY / 2);
            } else {
                shakeActive = false;
            }
        }
        g2d.translate(shakeX, shakeY);
        L_animatronics la = Main.getAnimatronics();
        java.util.List<abstrac_animatronic> list = la.get_L();
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
        try {
            int pos = Main.getPosition();
            Image guard = (pos == 0) ? guardLeft : guardRight;
            if (guard != null) {
                int targetW = getWidth() / 10;
                int iw = guard.getWidth(this);
                int ih = guard.getHeight(this);
                int x = (getWidth() - targetW) / 2;
                int y = (getHeight() - targetW) / 2;
                int targetH = targetW;
                if (iw > 0 && ih > 0) {
                    targetH = (int) ((double) targetW * ih / iw);
                    y = (getHeight() - targetH) / 2;
                }
                g.drawImage(guard, x, y, targetW, targetH, this);
                try {
                    if (la != null) {
                        if (Main.isLightLeft()) {
                            for (abstrac_animatronic a : list) {
                                if (a == null) continue;
                                if ("Door_Left".equals(a.get_id_room())) {
                                    drawAnimAtGuard(g, a, x - targetW/4-200, y+20, (int) (targetW*1.5),(int) (targetH*1.5), true);
                                    break;
                                }
                            }
                        }
                        if (Main.isLightRight()) {
                            for (abstrac_animatronic a : list) {
                                if (a == null) continue;
                                if ("Door_Right".equals(a.get_id_room())) {
                                    drawAnimAtGuard(g, a, x + targetW/4+300, y+20, (int) (targetW*1.5),(int) (targetH*1.5), false);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        
        try {
            if (Main.isCam()) {
                String camId = Main.getCurrentCamera();
                if (camId != null) { 
                    Image camImg = cameraImages.get(camId);
                    if (camImg == null) {
                        try {
                            java.net.URL url = getClass().getResource("/images/" + camId + ".png");
                            if (url != null) camImg = ImageIO.read(url);
                        } catch (IOException ignored) {}
                        if (camImg == null) {
                            try {
                                java.io.File f = new java.io.File("FNAF1/Pictures/" + camId + ".png");
                                if (f.exists()) camImg = ImageIO.read(f);
                            } catch (IOException ignored) {}
                        }
                        if (camImg != null) cameraImages.put(camId, camImg);
                    }
                    if (camImg != null) {
                        g.drawImage(camImg, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(new Color(0, 0, 0, 150));
                        g.fillRect(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
                        g.setColor(Color.WHITE);
                        g.drawString("Camera " + camId + " (image not found)", getWidth() / 2 - 60, getHeight() / 2);
                    }

                    if (blackoutActive) {
                        long now = System.currentTimeMillis();
                        long elapsed = now - blackoutStart;
                        float alpha = 1.0f;
                        if (blackoutDuration > 0) {
                            alpha = 1.0f - Math.min(1.0f, (float) elapsed / (float) blackoutDuration);
                        } else {
                            alpha = 0f;
                        }

                        if (alpha <= 0f) {
                            blackoutActive = false;
                        } else {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                            g2.setColor(Color.BLACK);
                            g2.fillRect(0, 0, getWidth(), getHeight());
                            g2.dispose();
                            drawCameraOverlay(g);
                            return;
                        }
                    }

                    try {
                        if (!"CAM6".equals(camId)) {
                            if (la != null) {

                                try {
                                    String currentCam = camId;
                                    for (abstrac_animatronic a : list) {
                                        if (a == null) continue;
                                        String prev = lastSeenRooms.get(a);
                                        String nowRoom = a.get_id_room();
                                        if (prev != null && prev.equals(currentCam) && !currentCam.equals(nowRoom) && Main.isCam()) {
                                            try { 
                                                Main.blinkCamera(400); 
                                                GamePanel.camNoiseStrength = 0.7f;
                                                GamePanel.camSwitching = true;    
                                            } catch (Throwable ignored) {}
                                        }
                                        lastSeenRooms.put(a, nowRoom);
                                    }
                                } catch (Throwable ignored) {}
                                java.util.List<abstrac_animatronic> present = new java.util.ArrayList<>();
                                for (abstrac_animatronic a : list) {
                                    if (a != null && camId.equals(a.get_id_room())) {
                                        if (a instanceof Chica || a instanceof Bonnie || a instanceof Freddy || a instanceof Foxy|| a instanceof Golden_Freddy|| a instanceof Freddy_Eyes ||a instanceof Puppet) {
                                            present.add(a);
                                        }
                                    }
                                }
                                if (!present.isEmpty()) {
                                    int n = present.size();
                                    for (int i = 0; i < n; i++) {
                                        abstrac_animatronic a = present.get(i);
                                        String baseName;
                                        if (a instanceof Chica) baseName = "Chica";
                                        else if (a instanceof Bonnie) baseName = "Bonnie";
                                        else if (a instanceof Freddy) baseName = "Freddy";
                                        else if (a instanceof Foxy) baseName = "Foxy";
                                        else if (a instanceof Golden_Freddy) baseName = "Golden_Freddy";
                                        else if (a instanceof Freddy_Eyes) baseName = "Freddy_Eyes";
                                        else if(a instanceof Puppet)baseName = "Puppet";
                                        else baseName = null;

                                        int w = getWidth();
                                        int h = getHeight();
                                        int markerSize = Math.max(20, Math.min(w, h) / 8);
                                        boolean verticalLayout = "CAM2A".equals(camId) || "CAM4A".equals(camId);
                                        int x, y;
                                        double marginFrac = 0.12;
                                        if (verticalLayout) {
                                            marginFrac = -0.12;
                                            x = (int) (w * 0.5 - markerSize / 2.0);
                                            double start = h * marginFrac;
                                            double range = h * (1.0 - 2 * marginFrac);
                                            y = (int) ((i + 1) * (range / (n + 1)) + start - markerSize / 2.0);
                                        } else {
                                            double startX = w * marginFrac;
                                            double rangeX = w * (1.0 - 2 * marginFrac);
                                            x = (int) ((i + 1) * (rangeX / (n + 1)) + startX - markerSize / 2.0);
                                            y = (int) (h * 0.65) - markerSize / 2;
                                        }

                                        if (baseName != null) {
                                            String side ="_Left" ;
                                            String key = baseName + side;
                                            Image animImg = animImages.get(key);
                                            if (animImg == null) {
                                                try {
                                                    java.io.File f = new java.io.File("FNAF1/Pictures/" + key + ".png");
                                                    if (f.exists()) animImg = ImageIO.read(f);
                                                } catch (IOException ignored) {}
                                                if (animImg != null) animImages.put(key, animImg);
                                            }

                                            if (animImg != null) {
                                                int iw = animImg.getWidth(this);
                                                int ih = animImg.getHeight(this);
                                                int drawW = markerSize * 2;
                                                int drawH = (iw > 0 && ih > 0) ? (int) ((double) drawW * ih / iw) : drawW;
                                                int dx = x - (drawW - markerSize) / 2;
                                                int dy = y - (drawH - markerSize) / 2;
                                                g.drawImage(animImg, dx, dy, drawW, drawH, this);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (Main.isCam()) {
                            drawStatic(g);
                            applyOldCameraFilter(g);
                            drawCameraSnow(g);
                        }
                        if (Main.staticActive) {
                            drawStatic(g);

                            if (System.currentTimeMillis() - Main.staticStartTime > Main.STATIC_DURATION) {
                                Main.staticActive = false;
                            }
                        }
                    } catch (Throwable ignored) {}
                    drawCameraOverlay(g);
                }
            }
        } catch (Throwable ignored) {}
        try {
            boolean leftClose = Main.left_door_close();
            boolean rightClose = Main.right_door_close();
            int panelW = getWidth();
            int panelH = getHeight();

            if (leftClose && !Main.isCam()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g2.setColor(Color.GRAY);
                g2.fillRect(panelW/12+90, panelH/2 - 100, panelW/30, 200);
                g2.dispose();
            }

            if (rightClose && !Main.isCam()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g2.setColor(Color.GRAY);
                g2.fillRect(panelW - panelW/3+90, panelH/2 - 100, panelW/30, 200);
                g2.dispose();
            }
        } catch (Throwable ignored) {}

        try {
            boolean leftOn = Main.isLightLeft();
            boolean rightOn = Main.isLightRight();
            int panelW = getWidth();
            int panelH = getHeight();

            if (leftOn && !Main.isCam()) {
                if (flashlightLeft != null) {
                    int w = panelW / 3;
                    int iw = flashlightLeft.getWidth(this);
                    int ih = flashlightLeft.getHeight(this);
                    int h = (iw > 0 && ih > 0) ? (int) ((double) w * ih / iw) : w;
                    int x = panelW / 8 - w/2;
                    int y = panelH/2 - h/2;
                    g.drawImage(flashlightLeft, x, y, w, h, this);
                } else {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(panelW/12, panelH/2 - 120, panelW/4, 240);
                    g2.dispose();
                }
            }

            if (rightOn && !Main.isCam()) {
                if (flashlightRight != null) {
                    int w = panelW / 3;
                    int iw = flashlightRight.getWidth(this);
                    int ih = flashlightRight.getHeight(this);
                    int h = (iw > 0 && ih > 0) ? (int) ((double) w * ih / iw) : w;
                    int x = panelW - panelW/8 - w/2;
                    int y = panelH/2 - h/2;
                    g.drawImage(flashlightRight, x, y, w, h, this);
                } else {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(panelW - panelW/3, panelH/2 - 120, panelW/4, 240);
                    g2.dispose();
                }
            }
        } catch (Throwable ignored) {}

        if (glowUp) {
            eyesGlow += 0.30f;
            if (eyesGlow >= 1.2f) glowUp = false;
        } else {
            eyesGlow -= 0.30f;
            if (eyesGlow <= 0.3f) glowUp = true;
        }

        long now = System.currentTimeMillis();
        if (eyesVisible && Math.random() < 0.005) {
            eyesVisible = false;
            lastBlink = now;
        }

        if (!eyesVisible && now - lastBlink > 120) {
            eyesVisible = true;
        }

        if (powerOutAnim) {
            if (eyesVisible && freddyEyesActive) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setComposite(
                    AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, eyesGlow * 0.8f
                    )
                );
                int targetW = getWidth() / 10;
                int x = (getWidth() - targetW) / 2;
                int y = (getHeight() - targetW) / 2;
                int targetH = targetW;
                g2.setColor(new Color(255, 255, 180));

                abstrac_animatronic a=new Freddy_Eyes("Door_Left", 20, 0);
                abstrac_animatronic b=new Freddy("Door_Left", 20, 0);
                drawAnimAtGuard(g2, b, x - targetW / 4 - 200, y + 20, (int) (targetW * 1.5), (int) (targetH * 1.5), true);
                drawAnimAtGuard(g2, a, x - targetW / 4 - 200, y + 20, (int) (targetW * 1.5), (int) (targetH * 1.5), true);
                g2.setComposite(AlphaComposite.SrcOver);
                g2.dispose();
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(
                AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, darkness
                )
            );
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        try {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int power = Main.getPower();
            int powerPercent = Math.max(0, power / 600);

            if (powerPercent > 30) {
                g2.setColor(Color.GREEN);
            } else if (powerPercent > 10) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(Color.RED);
            }

            g2.setFont(new Font("Arial", Font.BOLD, 16));

            String text = "Power: " + powerPercent + "%";

            int x = 15;
            int y = getHeight() - 50;

            g2.drawString(text, x, y);

            int power_usage = Main.getPowerUsage();
            power_usage = Math.max(1, power_usage);

            g2.setColor(Color.WHITE);
            String text2 = "Power Usage: " + power_usage;
            g2.drawString(text2, x, y+20);


            g2.dispose();
        } catch (Throwable ignored) {}

        try {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setFont(new Font("Arial", Font.BOLD, 32));
            g2.setColor(Color.WHITE);

            String hourText = Main.getHourString();

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(hourText);

            int x = getWidth() - textWidth - 30;
            int y =50;

            g2.drawString(hourText, x, y);
            g2.dispose();
        } catch (Throwable ignored) {}
        if (flashActive) {
            long elapsed = System.currentTimeMillis() - flashStart;
            float alpha = 1f - Math.min(1f, elapsed / (float) FLASH_DURATION);

            if (alpha <= 0f) {
                flashActive = false;
            } else {
                Graphics2D fg = (Graphics2D) g.create();
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                fg.setColor(Color.WHITE);
                fg.fillRect(0, 0, getWidth(), getHeight());
                fg.dispose();
                return;
            }
        }

        g2d.translate(-shakeX, -shakeY);
        if (slideActive && slideAnim != null) {
            jumpscare(g, slideAnim);
            repaint();
        }
        for (abstrac_animatronic a : list) {
            if (a instanceof Golden_Freddy && !Main.isCam()&& a.get_is_here()) {
                String side ="_Right";
                String key = "Golden_Freddy" + side;
                Image animImg = animImages.get(key);
                if (animImg == null) {
                    try {
                        java.io.File f = new java.io.File("FNAF1/Pictures/" + key + ".png");
                        if (f.exists()) animImg = ImageIO.read(f);
                    } catch (IOException ignored) {}
                    if (animImg != null) animImages.put(key, animImg);
                }

                if (animImg != null) {
                    int targetW = getWidth() / 10;
                    int targetH = targetW;
                    int dx = 300;
                    int dy = getHeight()-160;
                    drawAnimAtGuard(g, a, dx, dy, (int)(targetW * 1.5), (int)(targetH * 1.5), true);
                }
            }else if (a instanceof Puppet && !Main.isCam()&& a.get_is_here()) {
                String side ="_Left";
                String key = "Puppet" + side;
                Image animImg = animImages.get(key);
                if (animImg == null) {
                    try {
                        java.io.File f = new java.io.File("FNAF1/Pictures/" + key + ".png");
                        if (f.exists()) animImg = ImageIO.read(f);
                    } catch (IOException ignored) {}
                    if (animImg != null) animImages.put(key, animImg);
                }

                if (animImg != null) {
                    int targetW = getWidth() / 10;
                    int targetH = targetW;
                    int dx = 500;
                    int dy = getHeight()-160;
                    drawAnimAtGuard(g, a, dx, dy, (int)(targetW * 1.5), (int)(targetH * 1.5), false);
                    drawGift(g,a,430,getHeight()-280,(int)(getWidth()/4.5),(int)(getWidth()/4.5));
                }
            }
        }
    }

    public void jumpscare(Graphics g, abstrac_animatronic a) {
        if (!slideActive || a == null) return;
        Main.remove_cam();
        Main.cant_play();
        long elapsed = System.currentTimeMillis() - slideStart;
        float t = Math.min(1f, elapsed / (float) SLIDE_DURATION);

        t = t * t;

        int targetW = getWidth() / 10;
        int targetH = targetW;
        boolean fromLeft = a.get_coter() == 0;

        int finalX = (getWidth() - targetW) / 2 + (fromLeft? targetW/2+15:-targetW/2+50);
        int finalY = (getHeight() - targetH) / 2+20;


        int startX = fromLeft
                ? finalX - targetW / 4 - 200 
                : finalX + targetW / 4 + 300;
        int startY=finalY;

        if(a instanceof Golden_Freddy){
            startX=300;
            startY=getHeight()-160;
        }else if(a instanceof Puppet){
            startX=500;
            startY=getHeight()-160;
        }

        int x = (int) (startX + (finalX - startX) * t);
        int y = (int) (startY + (finalY - startY) * t);

        drawAnimAtGuard(
            g,
            a,
            x,
            y,
            (int)(targetW * 1.5),
            (int)(targetH * 1.5),
            fromLeft
        );
        if(a instanceof Puppet){
            drawGift(g,a,430,getHeight()-280,(int)(getWidth()/4.5),(int)(getWidth()/4.5));
        }

        if (t >= 1f) {
            slideActive = false;
                flashActive = true;
                flashStart = System.currentTimeMillis();

                shakeActive = true;
                shakeStart = System.currentTimeMillis();
            new javax.swing.Timer(300, e -> {
                ((javax.swing.Timer)e.getSource()).stop();
                Main.gameOver();
            }).start();
        }
    }

    public void startSlideJumpscare(abstrac_animatronic a) {
        slideAnim = a;
        slideActive = true;
        slideStart = System.currentTimeMillis();
    }

    public void startPowerOutAnimation() {
        powerOutAnim = true;
        darkness = 0f;

        new Timer(50, e -> {
            darkness += 0.05f;
            if (darkness >= 0.8f) {
                darkness = 0.8f;
                ((Timer)e.getSource()).stop();
            }
            repaint();
        }).start();
    }


    private boolean freddyEyesActive = false;

    public void enableFreddyEyes(boolean enable) {
        freddyEyesActive = enable;
        repaint();
    }

    private static Image starImg;

    public static Image loadStar() {
        if (starImg != null) return starImg;
        try {
            File f = new File("FNAF1/Pictures/404-4043726_cartoon-gold-star-clip-arts-cartoon-gold-star.png");
            if (f.exists()) return starImg = ImageIO.read(f);
        } catch (Exception ignored) {}

        return null;
    }

    private void drawGift(Graphics g, abstrac_animatronic a, int dx, int dy, int w, int h){
        Image Gift = loadImage("Gift_"+a.get_etape()+".png");
        g.drawImage(Gift,dx, dy, w, h,this);
    }
  
    public class CameraMap {
    
        public static Map<String, Rectangle> cameras = new HashMap<>();
    
        static {
            cameras.put("CAM1A", new Rectangle(587, 286, 42, 28));
            cameras.put("CAM1B", new Rectangle(574, 318, 42, 28));
            cameras.put("CAM1C", new Rectangle(554, 362, 42, 28));
            cameras.put("CAM2A", new Rectangle(588, 432, 42, 28));
            cameras.put("CAM2B", new Rectangle(587, 453, 42, 28));
            cameras.put("CAM3",  new Rectangle(533, 419, 42, 28));
            cameras.put("CAM4A",  new Rectangle(656, 431, 42, 28));
            cameras.put("CAM4B",  new Rectangle(655, 455, 42, 28));
            cameras.put("CAM5",  new Rectangle(506, 333, 42, 28));
            cameras.put("CAM6",  new Rectangle(718, 410, 42, 28));
            cameras.put("CAM7",  new Rectangle(725, 333, 42, 28));
        }

    }

    private void drawStatic(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        float alpha = 0.2f + (float)Math.random() * 0.4f;
        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, alpha
        ));

        for (int i = 0; i < 2500; i++) {
            int x = (int)(Math.random() * w);
            int y = (int)(Math.random() * h);
            int gray = (int)(Math.random() * 255);

            g2.setColor(new Color(gray, gray, gray));
            g2.fillRect(x, y, 1, 1);
        }
        g2.setColor(new Color(255, 255, 255, 40));
        int y = (int)(Math.random() * h);
        g2.fillRect(0, y, w, 2);
        g2.dispose();


    }

    private float camFlicker = 1f;
    private long lastFlickerTime = 0;

    public void applyOldCameraFilter(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        if (System.currentTimeMillis() - lastFlickerTime > 80) {
            camFlicker = 0.85f + (float)Math.random() * 0.25f;
            lastFlickerTime = System.currentTimeMillis();
        }

        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, camFlicker
        ));

        g2.setColor(new Color(120, 120, 120, 40));
        g2.fillRect(0, 0, w, h);

        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.15f
        ));
        g2.setColor(Color.BLACK);

        for (int y = 0; y < h; y += 3) {
            g2.drawLine(0, y, w, y);
        }

        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.25f
        ));

        for (int i = 0; i < 800; i++) {
            int x = (int)(Math.random() * w);
            int y = (int)(Math.random() * h);
            int gray = 100 + (int)(Math.random() * 100);

            g2.setColor(new Color(gray, gray, gray));
            g2.fillRect(x, y, 1, 1);
        }

        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.25f
        ));
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w - 1, h - 1);

        g2.dispose();
    }

    public static float camNoiseStrength = 0f;
    public static boolean camSwitching = false;


    private void drawCameraSnow(Graphics g) {
        if (camNoiseStrength <= 0f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        int slices = (int)(camNoiseStrength * 25);
        for (int i = 0; i < slices; i++) {
            int sliceY = (int)(Math.random() * h);
            int sliceH = 5 + (int)(Math.random() * 25);
            int offset = (int)((Math.random() - 0.5) * 80 * camNoiseStrength);

            g2.copyArea(0, sliceY, w, sliceH, offset, 0);
        }

        int blocks = (int)(camNoiseStrength * 80);
        for (int i = 0; i < blocks; i++) {
            int bw = 10 + (int)(Math.random() * 80);
            int bh = 5 + (int)(Math.random() * 30);
            int x = (int)(Math.random() * w);
            int y = (int)(Math.random() * h);

            int gray = (int)(Math.random() * 255);
            g2.setColor(new Color(gray, gray, gray, 180));
            g2.fillRect(x, y, bw, bh);
        }
        if (Math.random() < 0.6) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    camNoiseStrength * 0.7f
            ));
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
        }

        g2.dispose();

        camNoiseStrength -= 0.25f;
        if (camNoiseStrength <= 0f) {
            camNoiseStrength = 0f;
            camSwitching = false;
        }
    }


}
