# 大文件数据排序（基于归并排序）

##### 开发工具及环境
  
  IntelliJ IDEA;
  Java 8

##### 启动说明
本机电脑内存大小为8G，因为Java创建对象在堆内分配内存，此时在启动脚本上设置-xms，来控制堆的大小，
因为模拟数据大概在4MB左右，此处设置堆内存为1MB。
```jshelllanguage
    -Xms = 1M # 初始化堆内存大小
    -Xmx = 1M # 最大堆内存
    eg:
    VM Operation : -Xmx1M -Xms1M
```
启动过程中会创建模拟数据，将近10万条，一条数据大概50Byte，所以将近5MB,此处分割*5000条为一个分割单元*(需要考虑建立的临时变量)
此时会有100000/5000=20个临时文件（外部排序）

排序后选出每个临时文件的前128条，保存，此时有128 * 20 = 2560 条，此时一定小于1MB,可以全局读入到内存排序，做内部排序

##### 排序（归并排序算法）
```java
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
               if (A[left].id <= A[r].id) {
                   TMP[t++] = A[left++];
               } else {
                   TMP[t++] = A[r++];
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
```
##### 小于1MB内部排序（灵活使用Java8特性效率更高）
```java
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
```

详情请查看源代码，谢谢

### 待优化







