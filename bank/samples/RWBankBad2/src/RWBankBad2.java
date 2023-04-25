import java.util.*;
import java.util.concurrent.locks.*;

// ConcTest [dr]
// Accesses to accounts are synchronized.
// Method for closing an account uses a read lock instead of a write lock

class Bank {

    private static class Account {
        Lock key = new ReentrantLock();
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

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantReadWriteLock globalRWL = new ReentrantReadWriteLock();
    private Lock rl = globalRWL.readLock();
    private Lock wl = globalRWL.writeLock();
    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        wl.lock();
        try {
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            wl.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        rl.lock(); // Uses wrong lock type!
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.key.lock();
        }
        finally {
            rl.unlock();
        }
        try {
            return c.balance();
        }finally {
            c.key.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        rl.lock(); 
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.key.lock();
        }finally {
            rl.unlock();
        }try {
            return c.balance();
        }finally {
            c.key.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        rl.lock();
        try {
            c = map.get(id);
            if (c==null)
                return false;
            c.key.lock();
        }finally{
            rl.unlock();
        }try {
            return c.deposit(value);
        }finally {
            c.key.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        rl.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.key.lock();
        }finally{
            rl.unlock();
        }try {
            return c.withdraw(value);
        }finally {
            c.key.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto; 
        rl.lock(); 
        try { 
            cfrom = map.get(from);//
            cto = map.get(to);//
            if (cfrom == null || cto ==  null)
                return false;
                if(from < to){
                    cfrom.key.lock();
                    cto.key.lock();
                }else{
                    cto.key.lock();
                    cfrom.key.lock();
                }
        }finally{
            rl.unlock();
        }try{
                try {
                    if(!cfrom.withdraw(value))
                    return false;
                }finally{
                    cfrom.key.unlock();
                }
            return cto.deposit(value);
        }finally{
            cto.key.unlock();
        }
 
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        Account acs[] = new Account[ids.length];
        ids = ids.clone(); 
        Arrays.sort(ids); 
        rl.lock();
        try {
            for (int i = 0; i < ids.length; i++){
                acs[i] = map.get(ids[i]); 
                if(acs[i] == null){
                    return 0;
                }
            }
            for(Account c: acs){
                c.key.lock();
            }
        }finally{
            rl.unlock();
        }
        int total = 0;
        for(Account c: acs){
            total += c.balance();
            c.key.unlock();
        }
        return total;
  }

}
