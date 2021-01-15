package com.wencaixu;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class CreateFictitiousApplication {

    private static final String first = "./test.txt";

    private static final String second = "./temp.txt";

    private static final String feedLine = "\n";

    private void createFile(String params) {
        Path path = Paths.get(params);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Path file = Files.createFile(path);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private List<Record> splitFilesAndSort() {
        Path path = Paths.get(first);
        int count = 1;
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(first))) {
            String temp = "";
            int index = 0;
            while ((temp = reader.readLine()) != null) {
                Record[] records = new Record[5000];
                Record record = JSON.parseObject(temp, Record.class);
                records[index++] = record;
                if (index == 5000) {
                    sort(records, 0, 4999);
                    index = 0;
                }
                // 获取前128行即可
                try (FileWriter writer = new FileWriter(second, true)) {
                    for (int i = 0; i < 128; i++) {
                        writer.write(JSON.toJSONString(record) + feedLine);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        // 内部排序
        try (BufferedReader reader = new BufferedReader(new FileReader(second))) {
            String temp = "";
            List<Record> records = new ArrayList<>();
            while ((temp = reader.readLine()) != null) {
                Record record = JSON.parseObject(temp, Record.class);
                records.add(record);
            }
            return records.parallelStream().sorted(Comparator.comparing(Record::getId)).limit(128).collect(Collectors.toList());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return new ArrayList<>();
    }

    public void sort(Record[] A, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        sort(A, mid + 1, right);
        sort(A, left, mid);
        merge(A, left, mid, right);
    }

    public void merge(Record[] A, int left, int mid, int right) {
        Record[] TMP = new Record[A.length];
        int r = mid + 1;
        int t = left, c = left;

        while (left <= mid && r <= right) {
            if (Objects.nonNull(A[left]) && Objects.nonNull(A[left])) {
                if (A[left].id <= A[r].id) {
                    TMP[t++] = A[left++];
                } else {
                    TMP[t++] = A[r++];
                }
            }
        }

        while (left <= mid) {
            TMP[t++] = A[left++];
        }

        while (r <= right) {
            TMP[t++] = A[r++];
        }

        while (c <= right) {
            A[c] = TMP[c];
            c++;
        }
    }

    private void create() {
        createFile(first);
        createFile(second);
        try (FileWriter writer = new FileWriter(first, true)) {
            for (int i = 0; i < 100000; i++) {
                Record record = new Record((int) (Math.random() * 100000), (byte) 1, (short) 12, 20);
                String recordString = JSON.toJSONString(record) + feedLine;
                writer.append(recordString);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        // 4M
        // System.out.println(44 * 100000 / 1024 / 1024);
        // System.out.println("{\"age\":20,\"depart\":12,\"gender\":1,\"id\":18974}".getBytes().length);
        CreateFictitiousApplication createFictitiousApplication = new CreateFictitiousApplication();
        createFictitiousApplication.create();
        List<Record> records = createFictitiousApplication.splitFilesAndSort();
        for (Record record : records) {
            System.out.println(record.id);
        }
    }
}
