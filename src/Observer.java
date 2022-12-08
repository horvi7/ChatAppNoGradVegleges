import javax.swing.*;

/**
 * Based on the observer pattern, the observer perform some action after it is triggered by an observed (observable) object
 */
public interface Observer {
    /**
     *This action will be fired after observer is triggered by an observed object
     * @param username
     * @param contact
     * @param msg
     */
    void report (String username, String contact, String msg);
}
