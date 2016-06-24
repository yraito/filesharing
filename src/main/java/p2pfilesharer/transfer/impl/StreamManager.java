package p2pfilesharer.transfer.impl;


import p2pfilesharer.common.BasicTokenBucket;
import p2pfilesharer.common.TokenBucket;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import p2pfilesharer.transfer.BandwidthThrottler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Edward
 */
public class StreamManager implements BandwidthThrottler {

    //static int TOKEN_SIZE_BYTES = 8092;
    private static class ThrottledInputStream extends FilterInputStream {

        private final InputStream wrappedIs;
        private final TokenBucket tokenBucket;
        private final AtomicBoolean toggler;
        // byte[] buf = new byte[TOKEN_SIZE_BYTES];

        ThrottledInputStream(InputStream wrappedIs, TokenBucket tokenBucket, AtomicBoolean toggler) {
            super(wrappedIs);
            this.wrappedIs = wrappedIs;
            this.tokenBucket = tokenBucket;
            this.toggler = toggler;
        }

        @Override
        public int read() throws IOException {
            if (!toggler.get()) {
                return wrappedIs.read();
            }

            try {
                int r = wrappedIs.read();
                if (r != -1) {
                    tokenBucket.takeBlocking(1);
                }
                return r;
            } catch (InterruptedException e) {
                throw new InterruptedIOException(e.getMessage());
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (!toggler.get()) {
                return wrappedIs.read(b, off, len);
            }

            try {
                int lim0 = (int) Math.min(Integer.MAX_VALUE, tokenBucket.getCapacity());
                int lim1 = Math.min(len, lim0);
                int r = wrappedIs.read(b, off, lim1);
                if (r > 0) {
                    tokenBucket.takeBlocking(r);
                }
                return r;
            } catch (InterruptedException e) {
                throw new InterruptedIOException(e.getMessage());
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
    }

    private class ThrottledOutputStream extends FilterOutputStream {

        private final OutputStream wrappedOs;
        private final TokenBucket tokenBucket;
        private final AtomicBoolean toggler;

        public ThrottledOutputStream(OutputStream wrappedOs, TokenBucket tokenBucket, AtomicBoolean toggler) {
            super(wrappedOs);
            this.wrappedOs = wrappedOs;
            this.tokenBucket = tokenBucket;
            this.toggler = toggler;
        }

        @Override
        public void write(int b) throws IOException {
            if (!toggler.get()) {
                wrappedOs.write(b);
                return;
            }

            try {
                wrappedOs.write(b);
                tokenBucket.takeBlocking(1);
            } catch (InterruptedException e) {
                throw new InterruptedIOException(e.getMessage());
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            if (!toggler.get()) {
                wrappedOs.write(b, off, len);
                return;
            }

            try {

                while (len > 0) {
                    int lim0 = (int) Math.min(Integer.MAX_VALUE, tokenBucket.getCapacity());
                    int lim1 = Math.min(len, lim0);
                    wrappedOs.write(b, off, lim1);
                    tokenBucket.takeBlocking(lim1);
                    off += lim1;
                    len -= lim1;
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException(e.getMessage());
            }
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
    }

    private final AtomicBoolean throttledOutput = new AtomicBoolean(false);
    private final AtomicBoolean throttledInput = new AtomicBoolean(false);
    private final TokenBucket outputBucket = new BasicTokenBucket(1000000, 1000000, TimeUnit.SECONDS);
    private final TokenBucket inputBucket = new BasicTokenBucket(1000000, 1000000, TimeUnit.SECONDS);

    @Override
    public void setMaxUploadSpeed(long bytesPerSec) {
        if (bytesPerSec < 0) {
            throw new IllegalArgumentException("Negative upload speed");
        }
        if (bytesPerSec == 0) {
            throttledOutput.set(false);
        } else {
            throttledOutput.set(true);
            outputBucket.setFillRate(bytesPerSec, TimeUnit.SECONDS);
            outputBucket.setCapacity(bytesPerSec);
        }

    }

    @Override
    public void setMaxDownloadSpeed(long bytesPerSec) {
        if (bytesPerSec < 0) {
            throw new IllegalArgumentException("Negative download speed");
        }
        if (bytesPerSec == 0) {
            throttledInput.set(false);
        } else {
            throttledInput.set(true);
            inputBucket.setFillRate(bytesPerSec, TimeUnit.SECONDS);
            inputBucket.setCapacity(bytesPerSec);
        }
    }

    public InputStream createThrottledDownload(InputStream is) {
        return new ThrottledInputStream(is, inputBucket, throttledInput);
    }

    public OutputStream createThrottledUpload(OutputStream os) {
        return new ThrottledOutputStream(os, outputBucket, throttledOutput);
    }

}
