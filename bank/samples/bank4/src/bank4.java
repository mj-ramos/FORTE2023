import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// ConcTest [sem]

class Bank {

    private static class Account {
        private int balance;
        private ReentrantLock lockConta = new ReentrantLock();

        Account(int balance) { this.balance = balance; }

        int balance() {
            return balance;
        }

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

        void lock(){
            lockConta.lock();
        }

        void unlock(){
            lockConta.unlock();
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantLock lockBanco = new ReentrantLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        int id;

        lockBanco.lock();
        try{
            id = nextId;
            nextId += 1;
            map.put(id, c);
        }finally {
            lockBanco.unlock();
        }

        return id;
    }

    // close account and return balance, or -1 if no such account
    public int closeAccount(int id) {
        Account conta;

        lockBanco.lock();
        try{
            conta = map.remove(id);

            if (conta == null)
                return -1;

            conta.lock();
        }finally {
            lockBanco.unlock();
        }

        try{
            return conta.balance();
        }finally {
            conta.unlock();
        }

    }

    // account balance; -1 if no such account
    public int balance(int id) {
        Account conta;

        lockBanco.lock();
        try{
            conta = map.get(id);
            if (conta == null)
                return -1;
            conta.lock();
        }finally {
            lockBanco.unlock();
        }

        try{
            return conta.balance();
        }finally {
            conta.unlock();
        }

    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account conta ;

        lockBanco.lock();
        try{
            conta = map.get(id);
            if (conta == null)
                return false;
            conta.lock();
        }finally {
            lockBanco.unlock();
        }
        try {
            return conta.deposit(value);
        }finally {
            conta.unlock();
        }

    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account conta;
        lockBanco.lock();
        try{
            conta = map.get(id);
            if (conta == null)
                return false;
            conta.lock();
        }finally {
            lockBanco.unlock();
        }
        try{
            return conta.withdraw(value);
        }finally {
            conta.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        int aux;

        if(from > to){
            aux = to;
            to = from;
            from = aux;
        }
        lockBanco.lock();
        try{
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto ==  null || cfrom == cto)
                return false;
            cfrom.lock();
            cto.lock();
        }finally {
            lockBanco.unlock();
        }

        try{
            return cfrom.withdraw(value) && cto.deposit(value);
        }finally {
            cfrom.unlock();
            cto.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        int total = 0;
        Account c;
        for (int i : ids) {
            lockBanco.lock();

            try{
                c = map.get(i);
                if (c == null)
                    return 0;
                c.lock();
            }finally {
                lockBanco.unlock();
            }

            try{
                total += c.balance();
            }finally {
                c.unlock();
            }
        }
        return total;
    }

}
