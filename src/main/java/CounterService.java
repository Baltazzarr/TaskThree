import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Сервис для управления потоками
 */
public class CounterService {

    /**
     * Служебная ообщая переменная для проверяемых чисел
     */
    protected static volatile long lastNumber = 1;

    /**
     * Максимальное число
     */
    protected static final long maxNumber = 250000;

    public void start() {
        ReentrantLock locker = new ReentrantLock();
        File file = new File("Result.txt");
        try {
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            Files.createFile(file.toPath());

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        Counter counter1 = new Counter(1, locker);
        Counter counter2 = new Counter(2, locker);
        counter1.start();
        counter2.start();
    }

}
