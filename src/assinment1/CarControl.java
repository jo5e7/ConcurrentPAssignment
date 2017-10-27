//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2017

//Hans Henrik Lovengreen     Oct 9, 2017


import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;




class Gate {
    
    Semaphore g = new Semaphore(0);
    Semaphore e = new Semaphore(1);
    boolean isopen = false;

    public void pass() throws InterruptedException {
        g.P(); 
        g.V();
    }

    public void open() {
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition


    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to
    
    Semaphore [][] smatrix;
    Semaphore [][] Nsmatrix;
    Semaphore mutex;
    
    Alley alley;

    public Car(int no, CarDisplayI cd, Gate g) {

        this.no = no;
        this.cd = cd;
        mygate = g;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use

        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int) Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }

   public void run() {
       
       //Create a matrix of semaphore
       
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) { 
                sleep(speed());
  
                if (atGate(curpos)) { 
                    mygate.pass(); 
                    speed = chooseSpeed();
                }
                
                newpos = nextPos(curpos);
                
                
                //Check for alley enter
                if (!alley.isAlley(curpos) && alley.isAlley(newpos)) {
                    alley.enter(no);
                }
               smatrix[newpos.row][newpos.col].P();
                //  Move to new position 
                cd.clear(curpos);
                cd.mark(curpos,newpos,col,no);
                sleep(speed());
                cd.clear(curpos,newpos);
                cd.mark(newpos,col,no);
                
                smatrix[curpos.row][curpos.col].V();
                
                
                if ( alley.isAlley(curpos) && !alley.isAlley(newpos)) {
                    alley.leave(no);
                }
                
                curpos = newpos;
            }

        } catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

}

class Alley{

    
    
    Pos a10 = new Pos(1, 0); Pos a11 = new Pos(1, 1);Pos a12 = new Pos(1, 2);
    Pos a20 = new Pos(2, 0);
    Pos a30 = new Pos(3, 0);
    Pos a40 = new Pos(4, 0);
    Pos a50 = new Pos(5, 0);
    Pos a60 = new Pos(6, 0);
    Pos a70 = new Pos(7, 0);
    Pos a80 = new Pos(8, 0);
    Pos a90 = new Pos(9, 0); 
    Pos alleyPos[] = {a10, a20, a30, a40, a50, a60, a70, a80, a90, a11, a12};
    
   
    
    /*Pos a23 = new Pos(1, 3);
    Pos a33 = new Pos(2, 3);
    Pos a100 = new Pos(10, 0);
    Pos alleyTopPos[] = {a23, a33};
    Pos alleyButtonPos[] = {a100};*/
    
    int one2four = 0;
    int five2eight = 0;
            
    
    Semaphore mutex = new Semaphore(1);
    Semaphore mutex2 = new Semaphore(1);
    Semaphore e = new Semaphore(1);
   public void enter(int i) throws InterruptedException{
       
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           mutex.P();
           one2four += 1;
           if (one2four == 1) {
               //block
               e.P();
               Util.println("Block Alley" + String.valueOf(i));
               Util.println("e.P()");
           }
           mutex.V();
       } 
       if(i == 5|| i == 6 || i == 7|| i == 8) {
           mutex2.P();
           five2eight += 1;
           if (five2eight == 1) {
               //block
               e.P();
               Util.println("Block Alley" + String.valueOf(i));
               Util.println("e.P()");
           }
           mutex2.V();
       }
       
   } 

   public void leave(int i) throws InterruptedException{
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           mutex.P();
           one2four -= 1;
           Util.println(String.valueOf(one2four));
           if (one2four == 0) {
               //unblock
               e.V();
               Util.println("e.V()");
           }
           mutex.V();
       } 
       if (i == 5|| i == 6 || i == 7|| i == 8){
           mutex2.P();
           five2eight -= 1;
           if (five2eight == 0) {
               //unblock
               e.V();
               Util.println("e.v()");
           }
           mutex2.V();
       }
       
   }
   
   public boolean isAlley(Pos nextPos){
       for (Pos alleyPo : alleyPos) {
           if (nextPos.equals(alleyPo)) {
               return true;
           }
       }
       return false;
   }
   
   public boolean isAlleyEmpty(int i,Pos nextPos){
       if (i == 1|| i == 2 || i == 3|| i == 4 ) {
           if (five2eight == 0) {
               return true;
           }
       } else {
           if (one2four == 0) {
               return true;
           }
        }
       return false;
   }

}

public class CarControl implements CarControlI{

    CarDisplayI cd;           // Reference to GUI
    Car[]  car;               // Cars
    Gate[] gate;              // Gates
    
    Semaphore [][] smatrix = new Semaphore[11][12];
    Alley alley = new Alley();
    Semaphore mutex = new Semaphore(1);
       
    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car  = new  Car[9];
        gate = new Gate[9];

        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 11; j++) {
                smatrix [i][j] = new Semaphore(1);
            }
        }
        
        
        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            
            car[no] = new Car(no,cd,gate[no]);
            car[no].smatrix = this.smatrix;
            car[no].alley = this.alley;
            car[no].mutex = mutex;
            car[no].start();
        } 
        
    }

   public void startCar(int no) {
        gate[no].open();
    }

    public void stopCar(int no) {
        gate[no].close();
    }

    public void barrierOn() { 
        cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() { 
        cd.println("Barrier Off not implemented in this version");
    }

    public void barrierShutDown() { 
        cd.println("Barrier shut down not implemented in this version");
        // This sleep is for illustrating how blocking affects the GUI
        // Remove when shutdown is implemented.
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
        // Recommendation: 
        //   If not implemented call barrier.off() instead to make graphics consistent
    }

    public void setLimit(int k) { 
        cd.println("Setting of bridge limit not implemented in this version");
    }

    public void removeCar(int no) { 
        cd.println("Remove Car not implemented in this version");
    }

    public void restoreCar(int no) { 
        cd.println("Restore Car not implemented in this version");
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}






