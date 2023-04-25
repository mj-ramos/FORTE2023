import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr, sem]

class Bank {

    private static class Account {
        private int balance;
        private ReentrantLock l;

        Account(int balance) { this.balance = balance; l = new ReentrantLock();}

        int balance() {
            l.lock();
            try{
                return balance;
            } finally {
                l.unlock();
            }
        }

        boolean deposit(int value) {
            l.lock();
            try {
                balance += value;
                return true;
            } finally {
                l.unlock();
            }
        }

        boolean withdraw(int value) {
            if (value > balance)
                return false;
            l.lock();
            try {
                balance -= value;
                return true;
            }finally {
                l.unlock();
            }
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantLock l= new ReentrantLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        l.lock();
        try {
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            l.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        l.lock();
        try {
            c = map.remove(id);
        }finally {
            l.unlock();
        }
        if (c == null)
            return 0;
        return c.balance();
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        l.lock();
        try{
            c = map.get(id);
        } finally {
            l.unlock();
        }
        if (c == null)
            return 0;
        return c.balance();
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        l.lock();
        try {
            c = map.get(id);
        } finally {
            l.unlock();
        }
        if (c == null)
            return false;
        return c.deposit(value);
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        l.lock();
        try {
            c = map.get(id);
        } finally {
            l.unlock();
        }
        if (c == null)
            return false;
        return c.withdraw(value);
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        l.lock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto ==  null)
                return false;
            cfrom.l.lock();
            cto.l.lock();
        }finally {
            l.unlock();
        }
        try {
            try {
                if(!cfrom.withdraw(value)) return false;
            } finally {
                cfrom.l.unlock();
            }
            return cto.deposit(value);
        }finally {
            cto.l.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0; Account c;
        l.lock();
        try {
            for (int i : ids) {
                c = map.get(i);
                if (c == null)
                    return 0;//
                total += c.balance();
            }
            return total;
            }finally {
                l.unlock();
            }
  }

}
