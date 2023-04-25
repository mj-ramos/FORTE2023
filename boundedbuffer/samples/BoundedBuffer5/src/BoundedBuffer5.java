import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr, dl]

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
        while (queue.isEmpty())
            notEmpty.await();
        notFull.signal();
        lock.unlock();
        return queue.remove(0);
        // race if return is after unlock (common without try/finally)
        // deadlock if producers wakes up before remove
    }

    public void push(T value) throws InterruptedException {
        try {
            lock.lock();
            while (queue.size()>=max)
                notFull.await();
            notEmpty.signal();
            queue.add(value);
        } finally {
            lock.unlock();
        }
    }
}
