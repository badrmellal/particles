package particleUniverse;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class ParticleUniverse extends JPanel implements ActionListener, MouseMotionListener, MouseListener, KeyListener {
    private ArrayList<Particle> particles;
    private ArrayList<ForceField> forceFields;
    private Timer timer;
    private Random random;
    private Point mousePos;
    private boolean mousePressed;
    private final Dimension PANEL_SIZE = new Dimension(800, 600);

    // Effects and modes
    private boolean vortexMode = false;
    private boolean explosionMode = false;
    private boolean rainbowMode = false;
    private boolean trailMode = false;
    private float globalHue = 0;
    private int particleSize = 4;
    private float particleSpeed = 1.0f;

    public ParticleUniverse() {
        setPreferredSize(PANEL_SIZE);
        setBackground(Color.BLACK);
        setFocusable(true);

        particles = new ArrayList<>();
        forceFields = new ArrayList<>();
        random = new Random();
        mousePos = new Point(0, 0);

        // Initialize particles
        for (int i = 0; i < 1000; i++) {
            particles.add(new Particle());
        }

        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create fade effect for trails
        if (trailMode) {
            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw force fields
        for (ForceField field : forceFields) {
            field.draw(g2d);
        }

        // Draw particles
        for (Particle particle : particles) {
            particle.draw(g2d);
        }

        // Draw controls info
        drawControls(g2d);

        // Draw current force field if mouse is pressed
        if (mousePressed) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.drawOval(mousePos.x - 100, mousePos.y - 100, 200, 200);
        }
    }

    private void drawControls(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = 20;
        int lineHeight = 20;

        g2d.drawString("Click & Drag: Create Force Fields", 10, y);
        g2d.drawString("V - Vortex Mode: " + (vortexMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("E - Explosion Mode: " + (explosionMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("R - Rainbow Mode: " + (rainbowMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("T - Trail Mode: " + (trailMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("+/- : Adjust Particle Size (" + particleSize + ")", 10, y += lineHeight);
        g2d.drawString("←/→ : Adjust Speed (" + String.format("%.1f", particleSpeed) + ")", 10, y += lineHeight);
        g2d.drawString("SPACE: Reset Particles", 10, y += lineHeight);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        globalHue += 0.005f;
        if (globalHue > 1) globalHue = 0;

        // Update particles
        for (Particle particle : particles) {
            particle.update();
        }

        // Remove old force fields
        forceFields.removeIf(field -> field.strength <= 0);

        // Update force fields
        for (ForceField field : forceFields) {
            field.update();
        }

        repaint();
    }

    private class Particle {
        private float x, y;
        private float vx, vy;
        private float hue;
        private int size;

        public Particle() {
            reset();
        }

        public void reset() {
            x = random.nextInt(PANEL_SIZE.width);
            y = random.nextInt(PANEL_SIZE.height);
            vx = (random.nextFloat() - 0.5f) * 2;
            vy = (random.nextFloat() - 0.5f) * 2;
            hue = random.nextFloat();
            size = particleSize;
        }

        public void update() {
            // Apply force fields
            for (ForceField field : forceFields) {
                double dx = field.x - x;
                double dy = field.y - y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < field.radius) {
                    float force = (float)(field.strength / distance);
                    if (vortexMode) {
                        // Create swirling effect
                        vx += (-dy / distance) * force * 0.1f;
                        vy += (dx / distance) * force * 0.1f;
                    } else if (explosionMode) {
                        // Push particles away
                        vx -= (dx / distance) * force * 0.1f;
                        vy -= (dy / distance) * force * 0.1f;
                    } else {
                        // Pull particles
                        vx += (dx / distance) * force * 0.1f;
                        vy += (dy / distance) * force * 0.1f;
                    }
                }
            }

            // Update position
            x += vx * particleSpeed;
            y += vy * particleSpeed;

            // Bounce off walls
            if (x < 0 || x > getWidth()) vx *= -0.8f;
            if (y < 0 || y > getHeight()) vy *= -0.8f;

            // Keep particles in bounds
            x = Math.max(0, Math.min(x, getWidth()));
            y = Math.max(0, Math.min(y, getHeight()));

            // Add some random movement
            vx += (random.nextFloat() - 0.5f) * 0.1f;
            vy += (random.nextFloat() - 0.5f) * 0.1f;

            // Dampen velocity
            vx *= 0.99f;
            vy *= 0.99f;
        }

        public void draw(Graphics2D g2d) {
            Color color;
            if (rainbowMode) {
                color = Color.getHSBColor((globalHue + x * 0.001f + y * 0.001f) % 1.0f, 0.8f, 1.0f);
            } else {
                float speed = (float)Math.sqrt(vx * vx + vy * vy);
                color = Color.getHSBColor(0.5f, 0.5f, Math.min(1.0f, speed));
            }

            // Draw glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g2d.fillOval((int)x - size, (int)y - size, size * 2, size * 2);

            // Draw core
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setColor(color);
            g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }
    }

    private class ForceField {
        private float x, y;
        private float strength;
        private float radius;

        public ForceField(float x, float y) {
            this.x = x;
            this.y = y;
            this.strength = 5.0f;
            this.radius = 100;
        }

        public void update() {
            strength *= 0.95f;
        }

        public void draw(Graphics2D g2d) {
            int alpha = (int)(strength * 50);
            if (alpha > 0) {
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.drawOval((int)(x - radius), (int)(y - radius),
                        (int)(radius * 2), (int)(radius * 2));
            }
        }
    }

    // Mouse event handlers
    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos = e.getPoint();
        if (mousePressed) {
            forceFields.add(new ForceField(e.getX(), e.getY()));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        forceFields.add(new ForceField(e.getX(), e.getY()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    // Key event handlers
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_V:
                vortexMode = !vortexMode;
                explosionMode = false;
                break;
            case KeyEvent.VK_E:
                explosionMode = !explosionMode;
                vortexMode = false;
                break;
            case KeyEvent.VK_R:
                rainbowMode = !rainbowMode;
                break;
            case KeyEvent.VK_T:
                trailMode = !trailMode;
                break;
            case KeyEvent.VK_SPACE:
                for (Particle p : particles) p.reset();
                forceFields.clear();
                break;
            case KeyEvent.VK_EQUALS:
                particleSize = Math.min(10, particleSize + 1);
                break;
            case KeyEvent.VK_MINUS:
                particleSize = Math.max(2, particleSize - 1);
                break;
            case KeyEvent.VK_LEFT:
                particleSpeed = Math.max(0.2f, particleSpeed - 0.1f);
                break;
            case KeyEvent.VK_RIGHT:
                particleSpeed = Math.min(3.0f, particleSpeed + 0.1f);
                break;
        }
    }

    // Required interface methods
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
