/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jdmaestre
 */
public class Barrier {

    Semaphore[][] smatrix;
    public Barrier(Semaphore[][] smatrix) {
       this.smatrix = smatrix;
    }
    
    Pos p54 = new Pos(5, 4);
    Pos p55 = new Pos(5, 5);
    Pos p56 = new Pos(5, 6);
    Pos p57 = new Pos(5, 7);
    
    Pos p68 = new Pos(6, 8);
    Pos p69 = new Pos(6, 9);
    Pos p610 = new Pos(6, 10);
    Pos p611 = new Pos(6, 11);
    
    boolean isOn = false;
    
    
    Pos barrierPos[] = {p54, p55, p56, p57, p68, p69, p610, p611};
    
   public void sync() {  }  // Wait for others to arrive (if barrier active)

   public void on() throws InterruptedException {
       // Activate barrier
       for (Pos pos : barrierPos) {
           smatrix[pos.row][pos.col].P(); 
       }
       isOn = true;
   }    

   public void off() { 
       // Deactivate barrier
       for (Pos pos : barrierPos) {
           smatrix[pos.row][pos.col].V(); 
       }
       isOn = false;
   }   
}
