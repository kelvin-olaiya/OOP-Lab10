package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This is a first example on how to realize a reactive GUI.
 */
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final JButton reset = new JButton("reset");
    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        panel.add(reset);
        up.setEnabled(false);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        final ExecutorService es = Executors.newFixedThreadPool(2);
        es.submit(agent);
        es.submit(new StoppingAgent());
        /*new Thread(agent).start();
        new Thread(new StoppingAgent(agent)).start();*/
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(e -> {
                agent.stopCounting();
                down.setEnabled(true);
                up.setEnabled(true);
                reset.setEnabled(true);
                stop.setEnabled(false);
        });
        up.addActionListener(e -> {
            agent.countUp();
            up.setEnabled(false);
            down.setEnabled(true);
            stop.setEnabled(true);
            reset.setEnabled(false);
        });
        down.addActionListener(e -> {
            agent.countDown();
            up.setEnabled(true);
            down.setEnabled(false);
            stop.setEnabled(true);
            reset.setEnabled(false);
        });
        reset.addActionListener(e -> {
            agent.reset();
            this.display.setText(Integer.toString(agent.counter));
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private int counter;
        private volatile boolean countDown;

        @Override
        public void run() {
            while (true) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(Integer.toString(this.counter)));
                    if (!this.stop) {
                        this.counter += this.countDown ? -1 : 1; 
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void startCounting() {
            this.stop = false;
        }

        public void reset() {
            this.counter = 0;
        }

        public void countUp() {
            this.countDown = false;
            this.startCounting();
        }

        public void countDown() {
            this.countDown = true;
            this.startCounting();
        }
    }
    private class StoppingAgent implements Runnable {

        private static final int MILLIS_IN_SECOND = 1000;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(3 * MILLIS_IN_SECOND);
                    stop.doClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
