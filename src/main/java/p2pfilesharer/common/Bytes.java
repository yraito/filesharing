package p2pfilesharer.common;


/**
 * 
 * @author Nick
 */
public class Bytes {

    public enum Unit {
        
                
        BYTES("B", 1),
        KILOBYTES("KB", 1024),
        MEGABYTES("MB", 1024 * 1024),
        GIGABYTES("GB", 1024 * 1024 * 1024);
        
        public static Unit getBestUnit(long numBytes) {
            if (numBytes == 0) {
                return BYTES;
            }
            double pow = Math.log(numBytes) / Math.log(1024);
            int powInt = (int) Math.abs(pow);
            switch (powInt) {
                case 0: 
                    return BYTES;
                case 1: 
                    return KILOBYTES;
                case 2: 
                    return MEGABYTES;
                default: 
                    return GIGABYTES;
                    
            }
        }


        private final String abbreviation;
        private final long bytesPerUnit;

        private Unit(String abbreviation, int bytesPerUnit) {
            this.abbreviation = abbreviation;
            this.bytesPerUnit = bytesPerUnit;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public long getBytesPerUnit() {
            return bytesPerUnit;
        }

        public long toBytesNumber(long num) {
            return num * bytesPerUnit;
        }
        
        public long fromBytesNumber(long num) {
            return (long) (num / bytesPerUnit);
        }
    }

    private final long bytes;
    private final long num;
    private final Unit unit;
    
    public Bytes(Unit unit, long number) {
        this.bytes = unit.toBytesNumber(number);
        this.unit = unit;
        this.num = number;
    }
    
    public Bytes(long number) {
        this.bytes = number;
        this.unit = Unit.getBestUnit(number);
        this.num = unit.fromBytesNumber(number);
    }

    public Unit getUnit() {
        return unit;
    }

    public long getNumber() {
        return num;
    }

    public Bytes plus(Bytes other) {
        long bytesSum = getBytes() + other.getBytes();
        return new Bytes(bytesSum);
    }

    public Bytes minus(Bytes other) {
        long bytesSum = getBytes() - other.getBytes();
        return new Bytes(bytesSum);
    }
    
    public Bytes dividedBy(long divisor) {
        if (divisor == 0) {
            return new Bytes(Long.MAX_VALUE);
        }
        long bytesQuotient = getBytes() / divisor;
        return new Bytes(bytesQuotient);
    }
    
    public long dividedBy(Bytes other) {
        if (other.getBytes() == 0) {
            return Long.MAX_VALUE;
        }
        return getBytes() / other.getBytes();
    }
    
    public long getBytes() {
        return bytes;
    }
    
    @Override
    public String toString() {
        return num + " " + unit.getAbbreviation();
    }

}
