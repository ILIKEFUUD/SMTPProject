import java.util.*;

public class FIFOQueue<T> {
    // Queue data
    private ArrayList<T> queue = new ArrayList<T>();

    public T dequeue() throws FIFOQueueException {
        if(queue.size() == 0)
            throw new FIFOQueueException("Dequeue of an empty queue");
        T head = queue.get(0);
        queue.remove(0);
        return head;
    }

    public T peek() throws FIFOQueueException {
        if(queue.size() == 0)
            throw new FIFOQueueException("Peek of an empty queue");
        return queue.get(0);
    }

    public void enqueue(T newValue) {
        queue.add(newValue);
    }

    public boolean empty() {
        return queue.size() == 0;
    }

}

class FIFOQueueException extends Exception {
    public FIFOQueueException(String msg) {
        super(msg);
    }
}