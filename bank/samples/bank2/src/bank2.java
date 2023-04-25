import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [dr, sem]

class Bank {
    private Map<Integer, Account> contas= new HashMap<>();;
    private int nextId = 0;
    private ReentrantLock lock_banco= new ReentrantLock();;

    private static class Account {
        private int balance;
        private ReentrantLock lockConta = new ReentrantLock();
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
        public void lock(){
            this.lockConta.lock();
        }

        public void unlock(){
            this.lockConta.unlock();
        }
    }



    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        lock_banco.lock();
        int id = nextId;
        try{
            nextId += 1;
            contas.put(id, c);
        } finally {
            lock_banco.unlock();
        }

        return id;
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c = contas.remove(id);
        lock_banco.lock();
        try {
            c = contas.remove(id);
            if (c == null)
                return 0;
            c.lockConta.lock();
        } finally {
            lock_banco.unlock();
        }

        try {
            return c.balance();
        } finally {
            c.lockConta.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        lock_banco.lock();
        try {
            c = contas.get(id);
            if (c == null)
                return 0;
            c.lockConta.lock();
        }
        finally {
            lock_banco.unlock();
        }

        try{
            return c.balance();
        }
        finally {
            c.lockConta.unlock();
        }

    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        lock_banco.lock();
        Account c;
        try{
            c = contas.get(id); 
            if (c == null) return false;
            c.lockConta.lock();
        }
        finally {
            lock_banco.unlock();
        }
        try{
            return c.deposit(value);
        }
        finally {
            c.lockConta.unlock();
        }

    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        lock_banco.lock();
        Account c;
        try{
            c = contas.get(id); 
        }
        finally {
            lock_banco.unlock();
        }
        if (c == null)
            return false;
        return c.withdraw(value);
    }



    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        lock_banco.lock();
        try{
            cfrom = contas.get(from);
            cto = contas.get(to);
        }
        finally {
            lock_banco.unlock();
        }

        //Temos de dar lock as contas que estamos a usar
       

        if (cfrom == null || cto ==  null)
            return false;
        if(from<to){
            cfrom.lockConta.lock();
            cto.lockConta.lock();
        }
        else{
            cto.lockConta.lock();
            cfrom.lockConta.lock();
        }
        try{
            return cfrom.withdraw(value) && cto.deposit(value);
        }
        finally {  
            cfrom.lockConta.unlock();
            cto.lockConta.unlock();
        }

    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        List<Account> valid_accounts = new ArrayList<>();
        int i = 0;
        this.lock_banco.lock();
        for(int c : ids)
            if( this.contas.containsKey(c) )
                valid_accounts.add(this.contas.get(c));
        valid_accounts.forEach(Account::lock);
        this.lock_banco.unlock();

        int sum = valid_accounts.stream().mapToInt(Account::balance).sum();

        valid_accounts.forEach(Account::unlock);

        return sum;
    }

}
