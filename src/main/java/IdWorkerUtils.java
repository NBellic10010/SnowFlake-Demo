import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IdWorkerUtils {

    private final long beginEpoch = 1489111610226L;
    private final long workerIdBits = 5L;
    private final long dataCenterIdBits = 5L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long dataCenterIdShift = sequenceBits + workerIdBits;

    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long workerId;
    private long dataCenterId;
    private long sequence = 0L;
    private long lastTimeStamp = -1L;
    private static IdWorkerUtils idWorker;

    public IdWorkerUtils(long workerId, long dataCenterId) {
        if(workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId cant be greater than %d or less than 0", maxWorkerId));
        }
        if(dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenterId cant be greater than %d or less than 0", maxDataCenterId));
        }
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }

    long tillNextMillis(long lastTimeStamp) {
        long timestamp = timeGen();
        while(timestamp <= lastTimeStamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if(timestamp < lastTimeStamp) {
            throw new RuntimeException(String.format("Clock moved backwards, refusing to generate id for %d milliseconds", lastTimeStamp - timestamp));
        }

        if(lastTimeStamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;

            if(sequence == 0) {
                timestamp = tillNextMillis(lastTimeStamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimeStamp = timestamp;

        return ((timestamp - beginEpoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

//    private static int[] toCodePoints(String str) {
//        int[] result = new int[str.length()];
//        for(int i = 0; i < str.length(); i++) {
//            result[i] = str.charAt(i);
//        }
//        return result;
//    }

    private static Long getWorkId() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            int[] ints = StringUtils.toCodePoints(hostAddress);
            int sums = 0;
            for(int b: ints) {
                sums += b;
            }
            return (long) sums % 32;
        } catch (UnknownHostException e) {
            return RandomUtils.nextLong(0, 31);
        }
    }

    private static Long getDataCenterId() {
        int[] ints = StringUtils.toCodePoints(SystemUtils.getHostName());
        int sums = 0;
        for (int i: ints) {
            sums += i;
        }
        return (long) (sums % 32);
    }

    static {
        idWorker = new IdWorkerUtils(getWorkId(), getDataCenterId());
    }

    public static long generateId() {
        return idWorker.nextId();
    }
}
