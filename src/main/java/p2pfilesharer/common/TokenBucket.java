package p2pfilesharer.common;


import java.util.concurrent.TimeUnit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Edward
 */
public interface TokenBucket {
    
    /**
     * @return max number of tokens bucket can hold. Determines max burst rate
     */
    long getCapacity();
    
    /**
     * @param num max number of tokens bucket can hold.
     */
    void setCapacity(long num);
    
    /**
     * 
     * @param num
     * @param timeUnit 
     */
    void setFillRate(long num, TimeUnit timeUnit);
    
    /**
     * 
     * @param num
     * @throws InterruptedException 
     */
    void takeBlocking(int num) throws InterruptedException;
    
    /**
     * 
     * @param num
     * @return 
     */
    boolean tryTake(int num);
    
}
