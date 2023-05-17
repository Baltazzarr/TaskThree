import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Counter extends Thread{

    /**
     * Номер потока для идентфикации
     */
    protected int number;

    /**
     * Блокировка
     */
    protected ReentrantLock locker;

    public Counter(int number, ReentrantLock locker) {
        this.number = number;
        this.locker = locker;
        File file = new File(String.format("Thread%d.txt", number));
        try {
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            Files.createFile(file.toPath());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Проверить, что число простое
     * @param value число для проверки
     * @return true, если число простое; false в противном случае
     */
    public boolean isPrimeNumber(long value) {
        if (value == 2) {
            return true;
        }
        for (long i = 2; i <= value / 2; i++) {
            if (value % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Записать значение в конец файла
     * @param value значение
     * @param file дескриптор файла
     */
    public void writeToFile(String value, File file) {
        try (FileWriter commonFileWriter = new FileWriter(file, true)) {
            commonFileWriter.write(value);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        File localFile = new File(String.format("Thread%d.txt", number));
        File commonFile = new File("Result.txt");
        boolean needToStop = false;
        boolean needToFlush = false;
        long value = 1;
        String bufferVal = "";
        List<Long> buffer = new ArrayList<>();
        while (!needToStop) {
            locker.lock();
            try {
                if (!buffer.isEmpty()) {
                    if (value < CounterService.lastNumber || value >= CounterService.maxNumber) {
                        needToFlush = true;
                        writeToFile(bufferVal, commonFile);
                    }
                }
                if (CounterService.lastNumber < CounterService.maxNumber) {
                    CounterService.lastNumber++;
                    value = CounterService.lastNumber;;
                } else {
                    needToStop = true;
                }
            } finally {
                locker.unlock();
            }
            if (needToFlush) {
                writeToFile(bufferVal, localFile);
                buffer.clear();
                bufferVal = "";
                needToFlush = false;
            }
            if (needToStop) {
                break;
            }
            if (isPrimeNumber(value)) {
                buffer.add(value);
                bufferVal = buffer.stream().map(a -> a.toString()).collect(Collectors.joining(" ")) + " ";
            }
        }
        System.out.println(String.format("Finish Thread %d", number));
    }

}
