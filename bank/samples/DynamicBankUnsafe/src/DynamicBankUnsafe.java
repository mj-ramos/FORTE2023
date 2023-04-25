import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr, sem]

/*
 * Bank with an unsynchronized method that mutates the accounts map
 * Accesses to accounts are not synchronized
 */

class Bank {
    private Map<Integer, Account> accounts;
    private Lock lock;
    private int nextId;

    public class Account {
        int balance;
        Lock lock;

        public Account(int balance) {
            this.balance = balance;
            this.lock = new ReentrantLock();
        }

        public int balance() {
            return this.balance;
        }

        public void deposit(int quant) {
            balance += quant;
        }
        
        public boolean withdraw(int quant) {
            if (quant > balance)
                return false;
            balance -= quant;
            return true;
        }

        public void lock() {
            this.lock.lock();
        }

        public void unlock() {
            this.lock.unlock();
        }
    }

    public Bank() {
        this.lock = new ReentrantLock();
        this.accounts = new HashMap<>();
    }
    public int balance(int acc) {
        return this.accounts.get(acc).balance();
    }

    public void deposit(int acc, int quant) {
        this.accounts.get(acc).deposit(quant);
    }

    public boolean withdraw(int acc, int quant) {
        return this.accounts.get(acc).withdraw(quant);
    }

    public boolean transfer(int from, int to, int quant) {
        if (withdraw(from, quant)) {
            deposit(to, quant);
            return true;
        }
        return false;
    } 

    public int createAccount(int balance) {
        this.lock.lock();
        try {
            int id = this.nextId;
            this.nextId += 1;
            this.accounts.put(id, new Account(balance));
            return id;
        } finally {
            this.lock.unlock();
        }
    }

    public void closeAccount(int acc) { // Unsynchronized mutation of the map
        this.accounts.remove(acc);
    }

        public int totalBalance(int[] ids) {
        int total = 0; Account c;
        this.lock.lock();
        try{
            for (int i : ids) {
                c = accounts.get(i);
                if (c == null) return 0;
                total += c.balance();
            }
            return total;
        } finally {
            this.lock.unlock();
        }
    }
}