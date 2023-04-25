public class Consumer implements Runnable{
    BoundedBuffer<Integer> b;

    public Consumer(BoundedBuffer<Integer> b) {this.b=b;}
    
    public void run(){
        try{
            Integer r=b.pop();
            System.out.println("Pop " + r);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
