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

    /**
     * Счетчики
     */
    protected final List<Long> lastNumbers;

    protected final List<Long> firstUnwrittens;

    public Counter(int number, ReentrantLock locker, List<Long> lastNumbers, List<Long> firstUnwrittens) {
        this.number = number;
        this.locker = locker;
        this.lastNumbers = lastNumbers;
        this.firstUnwrittens = firstUnwrittens;
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
        for (long i = 2; i <= Math.sqrt(value); i++) {
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
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(value);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Long getMinUnwritten() {
        Long min = null;
        if (firstUnwrittens.size() > 1) {
            for (int i = 0; i < firstUnwrittens.size(); i++) {
                if (i != number && firstUnwrittens.get(i) != null && (min == null || min > firstUnwrittens.get(i))) {
                    min = firstUnwrittens.get(i);
                }

            }
        }
        return min;
    }

    @Override
    public void run() {
        File localFile = new File(String.format("Thread%d.txt", number));
        File commonFile = new File("Result.txt");
        boolean needToStop = false;
        boolean isFirstValue = true;
        Long value;
        List<Long> buffer = new ArrayList<>();
        String s = "";
        while (!needToStop) {
            locker.lock();
            try {
                if (!buffer.isEmpty()) {
                    List<Long> tmp = new ArrayList<>();
                    Long min = getMinUnwritten();
                    if (min != null) {
                        tmp = buffer.stream().filter(a -> a <= min).collect(Collectors.toList());
                    } else {
                        tmp.addAll(buffer);
                    }
                    if (!tmp.isEmpty()) {
                        s = tmp.stream().map(String::valueOf).collect(Collectors.joining(" ")) + " ";
                        writeToFile(s, commonFile);
                        buffer.removeAll(tmp);
                    }
                }
                if (isFirstValue) {
                    value = lastNumbers.get(number);
                    isFirstValue = false;
                } else {
                    value = lastNumbers.stream().max(Long::compareTo).get() + 1;
                }
                if (value > CounterService.maxNumber) {
                    if (buffer.isEmpty()) {
                        needToStop = true;
                    }
                    firstUnwrittens.set(number, buffer.isEmpty() ? null : buffer.get(0));
                } else {
                    lastNumbers.set(number, value);
                    firstUnwrittens.set(number, buffer.isEmpty() ? value : buffer.get(0));
                }
            } finally {
                locker.unlock();
            }
            if (!"".equals(s)) {
                writeToFile(s, localFile);
                s = "";
            }
            if (!needToStop && value <= CounterService.maxNumber && isPrimeNumber(value)) {
                buffer.add(value);
            }
        }
        System.out.println(String.format("Finish Thread %d", number));
    }

}
