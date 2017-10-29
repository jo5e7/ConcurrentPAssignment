
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import assinment1.Semaphore;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/**
 *
 * @author jdmaestre
 */
public class Alley{
    
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
