package info.danbecker.ss.graph;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public class WindowOpener {
    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 400;

    public static final int DEFAULT_COUNT = 5;

    public static void main( String[] args) {
        launchAWT( DEFAULT_COUNT );
        launchSwing( DEFAULT_COUNT );
        // Manual GUI exit
    }

    public static void launchAWT( int count ) {
        // AWT event queue
        for (int i = 0; i < count; i++) {
            int finalI = i;
            EventQueue.invokeLater(new Runnable() { // AWT-EventQuueue
                public void run() {
                    System.out.println("Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
                    JFrame frame = new JFrame("AWT JFrame " + finalI);
                    frame.setLocation( 10 * finalI, 30 * finalI );
                    frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Parent thread calls exit
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Any thread will cause exit
                    frame.setVisible(true);
                }
            });
        }
    }

    public static void launchSwing( int count ) {
        // Swing utilities
        for (int i = 0; i < count; i++) {
            int finalI = i;
            SwingUtilities.invokeLater(new Runnable() { // AWT-EventQuueue
                public void run() {
                    System.out.println( "Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
                    JFrame frame = new JFrame("Swing JFrame " + finalI);
                    frame.setLocation( DEFAULT_WIDTH + 10 * finalI, 30 * finalI );
                    frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // All threads must close to exit
                    frame.setVisible(true);
                }
            });
        }
    }
}
