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

    private static long getWorkId() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            //TODO
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static long getDataCenterId() {
        //TODO
    }

    static {
        idWorker = new IdWorkerUtils(getWorkId(), getDataCenterId());
    }
}
