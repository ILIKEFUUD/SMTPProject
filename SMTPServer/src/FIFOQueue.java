import java.util.*;

public class FIFOQueue<T> {
    // Queue data
    private ArrayList<T> queue = new ArrayList<T>();

    /** dequeue
     * Remove the head element of the stack, and return it.
     */
    public T dequeue() throws FIFOQueueException {
        if(queue.size() == 0)
            throw new FIFOQueueException("Dequeue of an empty queue");
        T head = queue.get(0);
        queue.remove(0);
        return head;
    }

    /** peek
     * Return the head element of the queue without removing it
     */
    public T peek() throws FIFOQueueException {
        if(queue.size() == 0)
            throw new FIFOQueueException("Peek of an empty queue");
        return queue.get(0);
    }

    /** enqueue
     * Enqueue a new value at the tail of the queue
     */
    public void enqueue(T newValue) {
        queue.add(newValue);
    }

    /** empty
     * Test to see if the queue is empty
     */
    public boolean empty() {
        return queue.size() == 0;
    }
}

/**
 * Class to implement exceptions for ALStacks
 */
class FIFOQueueException extends Exception {
    public FIFOQueueException(String msg) {
        super(msg);
    }
}