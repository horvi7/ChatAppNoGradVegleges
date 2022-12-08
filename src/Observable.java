import java.util.ArrayList;

/**
 * Implementing observer pattern.
 * This pattern is used to send the message from the application to the server.
 * App will be Observable, which reports to the class which implements Observer (ServerCommunication) to make some action.
 */
public interface Observable {
    /**
     * Based on the rules of observer pattern, <code>observers ArrayList</code> includes the observers of the observed objects.
     */
    ArrayList<Observer> observers = new ArrayList<>();

    /**
     * Observers can be assigned to observable objects
     * @param observer Observer who should observ the observable objects
     */
    void register(Observer observer);

    /**
     * Observer can be removed from the obervable's observer list
     * @param observer Observer which should be removed from the observer list of the observable
     */
    void unregister(Observer observer);

    /**
     * On a defined action the observed (observable) object signals the observer, and the observers will perform
     * the defined action.
     */
    void reportToObservers();

}
