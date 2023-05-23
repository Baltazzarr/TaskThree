import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Сервис для управления потоками
 */
public class CounterService {

    /**
     * Служебный общий массив для проверяемых чисел
     */
    protected List<Long> lastNumbers = new ArrayList<>();

    protected final List<Long> firstUnwrittens = new ArrayList<>();

    /**
     * Список потоков
     */
    protected final List<Counter> counterList = new ArrayList<>();

    /**
     * Максимальное число
     */
    protected static final long maxNumber = 200000;

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
        createCounters(4, locker);
        Date startDate = new Date();
        counterList.forEach(Counter::start);
        while (isAnyAlive()) {
        }
        Date endDate = new Date();
        System.out.println((endDate.getTime() - startDate.getTime()));
        System.out.println(isRight("Result.txt"));
    }

    public boolean isAnyAlive() {
        for (int i = 0; i < counterList.size(); i++) {
            if (counterList.get(i).isAlive()) {
                return true;
            }
        }
        return false;
    }

    public void createCounters(int number, ReentrantLock locker) {
        for (int i = 0; i < number; i++) {
            lastNumbers.add(Long.valueOf(i + 2));
            firstUnwrittens.add(lastNumbers.get(i));
            counterList.add(new Counter(i, locker, lastNumbers, firstUnwrittens));
        }
    }

    public boolean isRight(String fileName) {
        String line = "";
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (line == null || "".equals(line)) {
            return false;
        }
        List<String> list = Arrays.asList(line.split(" "));
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size() - 1; i++) {
                if (Long.valueOf(list.get(i + 1)) <= Long.valueOf(list.get(i))) {
                    System.out.println(String.format("Wrong number = %s", list.get(i + 1)));
                    return false;
                }
            }
        }
        return true;
    }

}
