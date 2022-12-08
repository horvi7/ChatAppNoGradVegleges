/**
 * This class is instantiating the App class and running its thread to get the functionalities of the chat application.
 */
public class Main {
    /**
     * App is instantiated and its thread is started.
     * @param args
     */
    public static void main(String[] args) {
        App app = new App();

        Thread appThread = new Thread(app);
        appThread.start();

    }
}