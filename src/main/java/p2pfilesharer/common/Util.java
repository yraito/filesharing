/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharer.common;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.Field;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.p2p.Shutdown;
import net.tomp2p.peers.Number160;

/**
 *
 * @author Edward
 */
public class Util {

    public static Object getValue(Field f, Object k) {
        try {
            return f.get(k);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ListenableFuture<Void> toFuture(BaseFuture bf) {
        SettableFuture<Void> sf = SettableFuture.create();
        bf.addListener(new BaseFutureListener() {
            @Override
            public void operationComplete(BaseFuture f) throws Exception {
                sf.set(null);
            }

            @Override
            public void exceptionCaught(Throwable thrwbl) throws Exception {
                sf.setException(thrwbl);
            }
        });
        return sf;
    }
}
