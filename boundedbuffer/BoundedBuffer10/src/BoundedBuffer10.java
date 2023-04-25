import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ContTest [dr, sem, dl]

class BoundedBuffer<T> {
    private final int max;
    private List<T> queue = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    
    private Condition notFull = lock.newCondition(), notEmpty = lock.newCondition();

    public BoundedBuffer(int size) {
        this.max = size;
    }

    public T pop() throws InterruptedException {
        lock.lock();
        if (queue.isEmpty())
            // race if there are multiple consumers
            notEmpty.await();
        notFull.signalAll();
        lock.unlock();
        return queue.remove(0);
        // race if return is done after unlock (common without try/finally)
        // deadlock, producers are awake before the item is removed, which can lead to consumers waiting
    }

    public void push(T value) throws InterruptedException {
        lock.lock();
        if (queue.size()>=max)
            // race if there are multiple producers
            notFull.await();
        notEmpty.signalAll();
        lock.unlock();
        queue.add(value);
        // race if add is done after unlock
        // deadlock, consumers are awake before the item is added, which can lead to producers waiting

    }
}
