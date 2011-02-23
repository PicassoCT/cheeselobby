package net.cheesecan.cheeselobby.mapviewer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

/**
 * 
 * @author jahziah
 */
public class JGameEngine extends Thread {
    // Constants

    private final String title = "Preview";
    public final static int screenWidth = 384;
    public final static int screenHeight = 384;
    // Fields
    private MapPreview game;
    private boolean wireFrame = false;
    private boolean paused = false;
    private boolean mouseIsInsideWindow = true;
    // Tasks
    final Queue<Runnable> tasks;

    /**
     * Public constructor.
     * Not much is done here because creation must happen on its own thread after invoking start() because of GL-Context.
     */
    public JGameEngine() {
        tasks = new LinkedBlockingQueue<Runnable>();
    }

    public void giveTask(Runnable task) {
        tasks.add(task);
    }

    private void initGame() {
        this.game = new MapPreview();
    }

    public MapPreview getGame() {
        return game;
    }

    public void setMouseIsInsideWindow(boolean value) {
        mouseIsInsideWindow = value;
    }

    /**
     * Initializes the display.
     * @throws LWJGLException
     */
    private void initDisplay() throws LWJGLException {
        Display.setParent(GraphicsPanel.canvas);

        Display.setDisplayMode(new DisplayMode(screenWidth, screenHeight));
        Display.setTitle(title);
        Display.setFullscreen(false);
        // Display.setVSyncEnabled(true);
        Display.create();
    }

    private void initCamera() {
        // 2D
        glEnable(GL_TEXTURE_2D);
        glClear(GL_COLOR_BUFFER_BIT);
        glClear(GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0f, screenWidth, 0.0f, screenHeight, -9000.0f, 9000.0f);
        glPushMatrix();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glShadeModel(GL_SMOOTH);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        glPushMatrix();
    }

    private void initInput() {
        try {
            Keyboard.create();
        } catch (LWJGLException ex) {
            Logger.getLogger(JGameEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Initialized input.");
    }

    /**
     * Initializes and starts all subsystems.
     */
    private void initGL() {
        try {
            initDisplay();
            initCamera();

            //2D Initialization
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_LIGHTING);

        } catch (LWJGLException ex) {
            Logger.getLogger(JGameEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Initialized OpenGL.");
    }

    /**
     * Processes all user input.
     */
    private void processInput() {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            System.out.println("Test");
            System.exit(0);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            System.out.println(game.camrotation);
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                game.camrotation = (game.camrotation + 1) % 360;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                game.camrotation = (game.camrotation - 1) % 360;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            game.camy -= 10;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            game.camy += 10;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            game.camx += 10;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            game.camx -= 10;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            if (!wireFrame) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                wireFrame = true;
            } else {
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                wireFrame = false;
            }
        }

        // If cursor is in the display window
        if (mouseIsInsideWindow) {
            // If mouse is on right edge
            if (Mouse.getX() > screenWidth * 0.8) {
                game.camx--;
            }
            // If mouse is on left edge
            if (Mouse.getX() < screenWidth * 0.2) {
                game.camx++;
            }
            // If mouse is on bottom edge
            if (Mouse.getY() < screenHeight * 0.2) {
                game.camy++;
            }
            // If mouse is on top edge
            if (Mouse.getY() > screenHeight * 0.8) {
                game.camy--;
            }

            // Zoom
            int delta = Mouse.getDWheel();

            if (delta > 0) {
                game.zoom -= 0.04;
                System.out.println("++");
            } else if (delta < 0) {
                game.zoom += 0.04;
                System.out.println("--");
            }
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void run() {
        // Initialize when thread starts
        initGL();
        initInput();
        initGame();

        // Start game loop
        while (true) {
            // Execute tasks
            while (!tasks.isEmpty()) {
                tasks.peek().run();
                tasks.remove();
            }

            // Pause thread by letting it sleep 3s at a time
            if (paused) {
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JGameEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {    // Render
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                processInput();
                game.paint();
                Display.update();
            }
        }
    }

    private void doCleanup() {
        Keyboard.destroy();
        Display.destroy();
        System.out.println("Cleaned up.");
    }
}
