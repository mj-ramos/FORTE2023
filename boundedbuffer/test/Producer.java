import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable{
    BoundedBuffer<Integer> b;

    public Producer(BoundedBuffer<Integer> b) {this.b=b;}

    public void run(){
        try{         
            int item = ThreadLocalRandom.current().nextInt(1, 10);       
            b.push(item);
            System.out.println("Push " + item);
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}