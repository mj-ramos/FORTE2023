import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr] 
// Data race when inserting new account in the bank
// Data race in totalBalance

class Bank {

    private static class Account {
        public ReentrantLock accLock = new ReentrantLock();
        private int balance;
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }
    private final ReentrantLock bankLock = new ReentrantLock(); 
    private final Map<Integer, Account> map = new HashMap<>();
    private int nextId = 0;

    // create account and return account ID
    public int createAccount(int balance) {
        Account c = new Account(balance);
        bankLock.lock(); // write lock
        int id = nextId;
        nextId += 1;
        bankLock.unlock(); // unlock bank
        map.put(id, c); // Data race!
        return id;
    }

    // close account and return balance, or 0 if no such account.
    public int closeAccount(int id) {
        bankLock.lock(); // write lock
        Account c;
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.accLock.lock();
        } finally {
            bankLock.unlock(); // unlock bank
        }

        try {
            return c.balance;
        } finally {
            c.accLock.unlock();
        }

    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        bankLock.lock(); // read lock
        try {
            c = map.get(id);

            if (c == null)
                return 0;

            c.accLock.lock();
        } finally {
            bankLock.unlock();
        }

        try{
            return c.balance();
        } finally {
            c.accLock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        bankLock.lock(); // read lock
        Account c;
        try {
            c = map.get(id);
            if (c == null)
                return false;
            //Thread.sleep(100);
            c.accLock.lock();
        } /*catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/ finally {
            bankLock.unlock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.accLock.unlock();
        }

    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        bankLock.lock();    // read lock
        Account c;
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.accLock.lock();
        } finally {
            bankLock.unlock();  // unlock bank
        }

        try {
            return c.withdraw(value);
        } finally {
            c.accLock.unlock();
        }

    }

    // fails if an account does not exist;
    // or insufficient balance.
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        bankLock.lock();                                                            // read lock
        try {
            cfrom = map.get(from);
            cfrom.accLock.lock();
            cto = map.get(to);
            cto.accLock.lock();

            if (cfrom == null || cto ==  null)
                return false;
        } finally {
            bankLock.unlock();                                                     // unlock bank
        }

        try {
            return cfrom.withdraw(value) && cto.deposit(value);
        } finally {
            cfrom.accLock.unlock();
            cto.accLock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist.
    public int totalBalance(int[] ids) {
        int total = 0;

        bankLock.lock();    // read lock bank

        try {
            for(int i: ids){
                Account c = map.get(i);     // ensures locks of all accounts
                c.accLock.lock();
            }
        } finally {
            bankLock.unlock();  // unlock banco
        }

        for (int i : ids) {
            Account c = map.get(i); // another data race!

            if (c == null)
                return 0;
            total += c.balance();

            c.accLock.unlock(); 
        }

        return total;
    }

}
