

#  NIO基础

non-blocking io 非阻塞IO

## 三大组件

## Channel&Buffer

channel有一点类似与stream，它就是读写数据的双向通道，可以从channel将数据读入buffer，也可以将buffer的数据写入channel，而之前的stream要么是输入，要么输出，channel比stream更为底层

1. 常见的channel：
   1. FileChannel:文件的传输通道
   2. DatagramChannel:UDP网络传输通道
   3. SocketChannel:TCP客户端网络通道
   4. ServerSocketChannel:TCP服务端网络通道
2. 常见的Buffer(用来缓冲读写数据):
   1. ByteBuffer
      1. MappedByteBuffer
      2. DirectByteBuffer
      3. HeapByteBuffer
   2. ShortBuffer
   3. IntBuffer
   4. LongBUffer
   5. FloatBuffer
   6. DoubleBUffer
   7. CharBuffer 

### Selector(选择器)

selector需要结合服务器的设计演化来理解它的用途

#### 多线程版设计

一个线程对应一个socket

#### 多线程版缺点

- 占用内存高
- 线程上下文切换成本高
- 只适合连接数少的场景

#### 线程池版设计

可以实现线程的复用

#### 线程池版缺点(无法解决连接被长期占用的问题)

- 阻塞模式下，线程仅能处理一个socket连接
- 仅适合短连接场景
- 线程的利用率不高

### selector版设计(非阻塞的设计模式)

selector的作用就是配合一个线程来管理多个channel，获取这些channel发生的事件，这些channel工作在非阻塞模式下，不会让线程吊在一个channel上。==**适合连接数特别多，但流量低的场景(low traffic)**==。调用selector的select()会阻塞知道channel发生了读写就绪事件，这些时间发生，select方法就会返回这些事件交给thread处理

## ByteBuffer的使用

1. 向buffer写入数据，例如调用channel.read(buffer)
2. 调用flip()切换至读模式
3. 从buffer读取数据，例如调用buffer.get()
4. 调用clear()或compact()切换至写模式
5. 重复1~4步骤

##### ByteBUffer结构

1. ByteBuffer有以下属性

   1. capacity（容量）
   2. position（写入位置/读取位置）
   3. limit（写入限制/读取限制）
   4. celar（切换模式，位置还有容量初始化）
   5. compact（切换模式，但是如果有数据没有读完，会将未读的向后压缩，初始化位置为未读数据的后面）
   6. ==**clear和compact的方法不能紧随其后使用，不然会导致位置错乱**==

2. ByteBuffer常见方法

   1. 分配空间
      1. ByteBuffer.allocate(分配的空间)  --分配的空间不能调整,使用的是java的堆内存，读写效率较低，受到GC的影响
      2. ByteBuffer.allocateDirect(分配的空间) --使用的是直接内存，读写效率高（少一次数据的拷贝），不会受到GC的影响，分配内存的效率比较低下，需要合理的释放，不然会出现内存泄漏
   2. 向ByteBuffer写入数据
      1. 调用channel的read方法
      2. 调用buffer自己的put方法
   3. 从buffer读取数据
      1. 调用channel的write方法
      2. 调用buffer的get方法
      3. get方法会将position读指针向后走，如果想要重复读取数据
         1. 可以调用rewind方法将position重新置为0
         2. 或者调用get（int i）方法获取索引i的内容，他不会移动读指针
      4. mark和reset
         1. mark 做一个标记,记录position的位置,reset是将position重置到mark的位置

3. ByteBuffer和String的互转

   ```java
   public class ByteBuferSwitchString {
       public static void main(String[] args) {
           //ByteBuffer和String的相互转换
           //使用String转换为ByteBuffer
           ByteBuffer buffer1 = ByteBuffer.allocate(16);
           buffer1.put("hello".getBytes());
           debugAll(buffer1);
   
           //使用chartset方法
           ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
           debugAll(buffer2);
   
           //使用nio的方法
           ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
           debugAll(buffer3);
   
           //ByteBuffer转换为String
           String str2 = StandardCharsets.UTF_8.decode(buffer2).toString();
           System.out.println("转换的数据是:"+str2);
   
           String str3 = StandardCharsets.UTF_8.decode(buffer3).toString();
           System.out.println("转换的数据是:"+str3);
   
           //转换第一个方法会出现问题,他的下标还是在写入时候的位置,需要把position的位置初始化
           buffer1.flip();
           String str1 = StandardCharsets.UTF_8.decode(buffer1).toString();
           System.out.println("转换的数据是:"+str1);
   
       }
   }
   ```

   

4. Scattering Reads(分散读取)

   ```java
   public class TestScatteringReads {
       public static void main(String[] args) {
           //分散读,将一个文件里面的数据,读取到多个bytebuffer中,读取文件的时候使用inputStream来读取文件里面的数据,如果使用的是outputSream就会导致读取的数据为空
           try (FileChannel channel = new FileInputStream("words.txt").getChannel()) {
               ByteBuffer b1 =ByteBuffer.allocate(3);
               ByteBuffer b2 =ByteBuffer.allocate(3);
               ByteBuffer b3 =ByteBuffer.allocate(5);
               channel.read(new ByteBuffer[]{b1,b2,b3});
               b1.flip();
               b2.flip();
               b3.flip();
               debugAll(b1);
               debugAll(b2);
               debugAll(b3);
           } catch (IOException e) {
           }
       }
   }
   ```

   

5. Gathering Writes(集中写入)

   ```java
   public class TestGatteringWirter {
       public static void main(String[] args) {
           //集中写,将多组数据一次性全部写入到一个文件中,减少数据的拷贝过程
           try (FileChannel channel = new FileOutputStream("words2.txt").getChannel()) {
               ByteBuffer b1 = StandardCharsets.UTF_8.encode("hello");
               ByteBuffer b2 = StandardCharsets.UTF_8.encode("world");
               ByteBuffer b3 = StandardCharsets.UTF_8.encode("你好,世界");
               channel.write(new ByteBuffer[]{b3,b1,b2});
           } catch (IOException e) {
           }
       }
   }
   ```

   

6. 黏包和半包

   ```java
   public class ByteBufferProjcet {
       /**
        * 网络上有多条数据发送到服务端,数据之间使用\n进行分隔,但由于某种原因
        * 这些数据在接收时,被进行了重新组合,例如原始的三条为
        * hello,world\n
        * I'm zhangsan\n
        * How are you?\n
        * 变成下面的两个bytebuffer(黏包,半包)
        * 黏包:一次发送多条数据,导致数据可能黏在一起
        * 半包:超过接收容量,导致数据到了下一个的容器里面
        * hello,world\nI'm zhangsan\nHo
        * w are you?\n
        * 要求编写程序,将错乱的数据恢复成原始的按\n分隔数据
        */
       public static void main(String[] args) {
           //创建两个bytebuffer.模拟出现的错乱的数据
           ByteBuffer source = ByteBuffer.allocate(32);
           source.put("hello,world\nI'm zhangsan\nHo".getBytes());
           split(source);
           source.put("w are you ?\n".getBytes());
           split(source);
   
       }
   
       private static void split(ByteBuffer source){
   //        debugAll(source);
           //切换为读模式,将position初始化
           source.flip();
           //将bytebuffer循环获取字符
           for(int i = 0 ; i < source.limit() ; i++){
               //在字符等于\n的时候表示是一条完整的数据
               if(source.get(i) == '\n') {
                   //获取他的长度,用于创建bytebuffer
                   int length = i+1-source.position();
                   //创建一个byteBuffer获取数据
                   ByteBuffer target = ByteBuffer.allocate(length);
                   for(int j = 0 ; j< target.limit();j++){
                       target.put(source.get());
                   }
                   debugAll(target);
               }
           }
           source.compact();
       }
   
   }
   ```

7. ByteBuffer控制台工具类

   ```java
   public class ByteBufferUtil {
       private static final char[] BYTE2CHAR = new char[256];
       private static final char[] HEXDUMP_TABLE = new char[256 * 4];
       private static final String[] HEXPADDING = new String[16];
       private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
       private static final String[] BYTE2HEX = new String[256];
       private static final String[] BYTEPADDING = new String[16];
   
       static {
           final char[] DIGITS = "0123456789abcdef".toCharArray();
           for (int i = 0; i < 256; i++) {
               HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
               HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
           }
   
           int i;
   
           // Generate the lookup table for hex dump paddings
           for (i = 0; i < HEXPADDING.length; i++) {
               int padding = HEXPADDING.length - i;
               StringBuilder buf = new StringBuilder(padding * 3);
               for (int j = 0; j < padding; j++) {
                   buf.append("   ");
               }
               HEXPADDING[i] = buf.toString();
           }
   
           // Generate the lookup table for the start-offset header in each row (up to 64KiB).
           for (i = 0; i < HEXDUMP_ROWPREFIXES.length; i++) {
               StringBuilder buf = new StringBuilder(12);
               buf.append(StringUtil.NEWLINE);
               buf.append(Long.toHexString(i << 4 & 0xFFFFFFFFL | 0x100000000L));
               buf.setCharAt(buf.length() - 9, '|');
               buf.append('|');
               HEXDUMP_ROWPREFIXES[i] = buf.toString();
           }
   
           // Generate the lookup table for byte-to-hex-dump conversion
           for (i = 0; i < BYTE2HEX.length; i++) {
               BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
           }
   
           // Generate the lookup table for byte dump paddings
           for (i = 0; i < BYTEPADDING.length; i++) {
               int padding = BYTEPADDING.length - i;
               StringBuilder buf = new StringBuilder(padding);
               for (int j = 0; j < padding; j++) {
                   buf.append(' ');
               }
               BYTEPADDING[i] = buf.toString();
           }
   
           // Generate the lookup table for byte-to-char conversion
           for (i = 0; i < BYTE2CHAR.length; i++) {
               if (i <= 0x1f || i >= 0x7f) {
                   BYTE2CHAR[i] = '.';
               } else {
                   BYTE2CHAR[i] = (char) i;
               }
           }
       }
   
       /**
        * 打印所有内容
        * @param buffer
        */
       public static void debugAll(ByteBuffer buffer) {
           int oldlimit = buffer.limit();
           buffer.limit(buffer.capacity());
           StringBuilder origin = new StringBuilder(256);
           appendPrettyHexDump(origin, buffer, 0, buffer.capacity());
           System.out.println("+--------+-------------------- all ------------------------+----------------+");
           System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), oldlimit);
           System.out.println(origin);
           buffer.limit(oldlimit);
       }
   
       /**
        * 打印可读取内容
        * @param buffer
        */
       public static void debugRead(ByteBuffer buffer) {
           StringBuilder builder = new StringBuilder(256);
           appendPrettyHexDump(builder, buffer, buffer.position(), buffer.limit() - buffer.position());
           System.out.println("+--------+-------------------- read -----------------------+----------------+");
           System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), buffer.limit());
           System.out.println(builder);
       }
   
       private static void appendPrettyHexDump(StringBuilder dump, ByteBuffer buf, int offset, int length) {
           if (MathUtil.isOutOfBounds(offset, length, buf.capacity())) {
               throw new IndexOutOfBoundsException(
                       "expected: " + "0 <= offset(" + offset + ") <= offset + length(" + length
                               + ") <= " + "buf.capacity(" + buf.capacity() + ')');
           }
           if (length == 0) {
               return;
           }
           dump.append(
                   "         +-------------------------------------------------+" +
                           StringUtil.NEWLINE + "               |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" +
                           StringUtil.NEWLINE + "+--------+-------------------------------------------------+----------------+");
   
           final int startIndex = offset;
           final int fullRows = length >>> 4;
           final int remainder = length & 0xF;
   
           // Dump the rows which have 16 bytes.
           for (int row = 0; row < fullRows; row++) {
               int rowStartIndex = (row << 4) + startIndex;
   
               // Per-row prefix.
               appendHexDumpRowPrefix(dump, row, rowStartIndex);
   
               // Hex dump
               int rowEndIndex = rowStartIndex + 16;
               for (int j = rowStartIndex; j < rowEndIndex; j++) {
                   dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
               }
               dump.append(" |");
   
               // ASCII dump
               for (int j = rowStartIndex; j < rowEndIndex; j++) {
                   dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
               }
               dump.append('|');
           }
   
           // Dump the last row which has less than 16 bytes.
           if (remainder != 0) {
               int rowStartIndex = (fullRows << 4) + startIndex;
               appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);
   
               // Hex dump
               int rowEndIndex = rowStartIndex + remainder;
               for (int j = rowStartIndex; j < rowEndIndex; j++) {
                   dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
               }
               dump.append(HEXPADDING[remainder]);
               dump.append(" |");
   
               // Ascii dump
               for (int j = rowStartIndex; j < rowEndIndex; j++) {
                   dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
               }
               dump.append(BYTEPADDING[remainder]);
               dump.append('|');
           }
   
           dump.append(StringUtil.NEWLINE +
                   "+--------+-------------------------------------------------+----------------+");
       }
   
       private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
           if (row < HEXDUMP_ROWPREFIXES.length) {
               dump.append(HEXDUMP_ROWPREFIXES[row]);
           } else {
               dump.append(StringUtil.NEWLINE);
               dump.append(Long.toHexString(rowStartIndex & 0xFFFFFFFFL | 0x100000000L));
               dump.setCharAt(dump.length() - 9, '|');
               dump.append('|');
           }
       }
   
       public static short getUnsignedByte(ByteBuffer buffer, int index) {
           return (short) (buffer.get(index) & 0xFF);
       }
   }
   ```

   

## 文件编程

### FileChannel简单使用

FileChannel只能工作在阻塞模式下

### 获取

不能直接打开FileChannel，必须通过FileInputStream，FileOutPutStream或者RandomAccessFile来获取FileChannel，他们都有getChannel方法

- 通过FileInputStream获取channel只能读
- 通过FileOutPutStream获取只能写
- 通过RandomAccessFile是否能读写更具构造RandomAccessFile是的读写模式决定

#### 读取

会从channel读取数据填充ByteBuffer，返回值表示读到了多少字节，-1表示到达了文件末尾

```java
int readBytes = channel.read(buffer)
```

#### 写入

写入的正确姿势如下==**channel有写的上限**==

```java
ByteBuffer buffer = ......;
buffer.put(...); //存入数据
buffer.flip(); //切换读模式
while(buffer.hasRemaining()){
channel.write(buffer);
}
```

在while中调用channel.write是因为write方法并不能保证一次将buffer中的内容全部写入到channel中

#### 关闭

channel必须关闭，不过调用了FileInputStream，FileOutPutStream或者RandomAccessFile的close方法会间接的调用channel的close方法

#### 位置

获取当前位置

```java
long pos = channle.position();
```

设置当前位置

```java
long newPos = .....;
channel.position(newPos)
```

设置当前位置时，如果设置为文件末尾

- 这时读取会返回-1
- 这时写入会追加内容，但是注意如果position超过了文件末尾，再写入时在新内容和原末尾之间会有空洞(00);

#### 大小

使用size方法获取文件的大小

#### 强制写入

操作系统处于性能的考虑，会将数据缓存，不是立刻写入磁盘，可以调用froce(true)方法将文件内容和元数据(文件的权限等信息)立刻写入磁盘

### 两个channel传输数据

```java
public class TestFileChannleTransferTo {
    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("words2.txt").getChannel();
                FileChannel to = new FileOutputStream("words3.txt").getChannel()
            ) {
            //效率高,底层会利用操作系统的零拷贝进行优化,最多传2G的数据
//            from.transferTo(0,from.size(),to);
            //传输高于2G的文件方式
            //获取传输文件的大小
            long size = from.size();
            for(long left = size;left>0;){
                left =left - from.transferTo((size-left),left,to);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

### Path

jdk7引入Path和Paths类

- Path用来表示文件路径
- Paths是工具类，用于获取Path实例

```java
Path source = Path.get("1.txt"); //相对路径，使用user.dir环境变量来定位1.txt
Path source = Paths.get("d:\\1.txt"); //绝对路径代表了d:\1.txt
Path source = Paths.get("d:/1.txt"); //绝对路径代表了d:\1.txt
Path project = Paths.get("d:\\data","projects"); //代表了 d:\data\project
```

- ==**.**==代表了当前路径
- ==**..**==代表了上一级路径

例如目录结构如下所示

```
d:
		|- data
			|- projects
					|- a
					|- b
```

代码

```java
Path path = Paths.get("d:\\data\\project\\a..\\b");
System.out.println(path);
System.out.println(path.normalize()); //正常化路径
```

会输出

```
d:\data\project\a..\b
d:\data\project\b
```

### Files

检查文件是否存在

```java
Path path = Paths.get("helloworld/d1");
System.out.println(Files.existis(path)); //检查文件是否存在
```

创建一级目录

```java
Path path = Paths.get("helloworld/d1");
Files.createDirectory(path);
```

- 如果目录已经存在，会抛异常FileAlreadyExistsException
- 不能一次创建多级目录，否则会抛异常NoSuchFileException

创建多级目录

```
Path path = Paths.get("helloworld/d1/d2");
Files.createDirectories(path);
```

拷贝文件

```java
Path source = Paths.get("helloworld/d1/data.txt");
Path target = Paths.get("helloworld/d1/target.txt");
Files.copy(source,target);
```

- 如果文件已存在，会抛异常FileAlreadyExistsException
- 如果希望source覆盖target，需要用StandardCopyOption来控制

```java
Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);
```

移动文件

```java
Path source = Paths.get("helloworld/d1/data.txt");
Path target = Paths.get("helloworld/d1/data.txt");
Files.move(source,target,StandardCopyOption.ATOMIC_MOVE);
```

- StandardCopyOption.ATOMIC_MOVE 保证文件移动的原子性

删除文件

```java
Path target = Paths.get("helloworld/d1/data.txt");
Files.delete(target);
```

删除目录

```java
Path path = Paths.get("helloworld/d1");
Files.delete(target);
```

- 如果目录还有内容，会抛异常DirectoryNotEmptyException

遍历文件

```java
public class TestFilesWalkFileTree {
    public static void main(String[] args) throws IOException {
        //创建两个原子性的参数,用于计数
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        AtomicInteger jarCount = new AtomicInteger();

        //遍历目录
        Files.walkFileTree(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_321.jdk"),new SimpleFileVisitor<Path>(){
            //进入文件夹前的操作
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dirCount.incrementAndGet();
                System.out.println("=======>"+dir);
                return super.preVisitDirectory(dir, attrs);
            }

            //对文件的操作
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fileCount.incrementAndGet();
                System.out.println("=========>"+file);
                if((file.toString()).endsWith(".jar")){
                    System.out.println(file);
                    jarCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }

            //遍历文件失败的操作
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            //遍历文件之后的操作
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return super.postVisitDirectory(dir, exc);
            }
        });

        //输出参数
        System.out.println("dir count:"+dirCount);
        System.out.println("file count:"+fileCount);
        System.out.println("jar count:"+jarCount);
    }
}
```

逐级删除文件

```java
public class TestFilesWalkTreeDelete {
    public static void main(String[] args) throws IOException {
//        Files.deleteIfExists(Paths.get("/Users/pengjing/Downloads/nacos-2.2.1-RC"));
        //逐级删除目录
        Files.walkFileTree(Paths.get("/Users/pengjing/Downloads/nacos-2.2.1-RC"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                Files.deleteIfExists(file);
                System.out.println("删除了======>"+file.toString());
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("得到的文件夹是:"+dir);
                Files.deleteIfExists(dir);
                System.out.println("删除的文件夹是:"+dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
```

多级目录的拷贝

```java
public class TestFilesWalkTreeCopy {
    public static void main(String[] args) throws IOException {
        //复制多级目录的文件
        String source = "/Users/pengjing/Downloads/文档";
        String target = "/Users/pengjing/Downloads/文档1";

            Files.walk(Paths.get(source)).forEach(path -> {
        try {
                //相当于讲前面的替换，然后在后面如果是文件夹就创建文件夹，如果是文件就执行复制
                String targetName = path.toString().replace(source, target);
                //创建目标文件夹
                if(Files.isDirectory(path)){
                    Files.createDirectory(Paths.get(targetName));
                }else if(Files.isRegularFile(path)){
                    //执行拷贝
                    Files.copy(path,Paths.get(targetName));
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
            });
    }
}
```

## 网络编程

### 阻塞VS非阻塞

共用的客户端

```java
public class Client {
    public static void main(String[] args) throws IOException {
        //创建客户端对象
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",8080));
        System.out.println("waiting.......");
//        sc.write(StandardCharsets.UTF_8.encode("123456789"));
    }
}
```

阻塞模式的服务器端

```java
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        //使用nio理解阻塞模式 单线程
        //0 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        //2 绑定端口
        ssc.bind(new InetSocketAddress(8080));

        //3 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while(true){
            //accept 建立客户端连接 SocketChannel 用来与客户端之间通信
            log.debug("connecting.......");
            //阻塞方法,线程停止运行
            SocketChannel channel = ssc.accept();

            log.debug("connected.....{}",channel);
            channels.add(channel);

            //遍历集合,获取消息
            channels.stream().forEach(a->{
                try {
                    //阻塞方法,线程停止运行
                    a.read(buffer);
                    log.debug("berfer read.......{}",a);
                    buffer.flip();
                    debugRead(buffer);
                    //切换到写模式,重置postiion
                    buffer.clear();
                    log.debug("after read....{}",a);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
```

非阻塞模式的服务器端

```java
@Slf4j
public class ServerNoBlocking {
    public static void main(String[] args) throws IOException {
        //非阻塞模式的展示
        //0 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //开启ServerSocketChannel的非阻塞模式,影响的是ssc.accept()的代码
        ssc.configureBlocking(false);
        //2 绑定端口
        ssc.bind(new InetSocketAddress(8080));

        //3 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while(true){
            //accept 建立客户端连接 SocketChannel 用来与客户端之间通信
            SocketChannel sc = ssc.accept();

            if(Optional.ofNullable(sc).isPresent()) {
                log.debug("connected.....{}", sc);

                //将SocketChannel设置以为非阻塞模式,影响的是读取数据的方法a.read(buffer);
                sc.configureBlocking(false); //非阻塞模式 线程还会继续运行,如果没有链接建立,但sc是null;

                channels.add(sc);
            }
            //遍历集合,获取消息
            channels.stream().forEach(a->{
                try {
                    int read = a.read(buffer);//非阻塞,线程仍会继续运行,如果没有读到数据,read会返回0
                    if(read >0) {
                        buffer.flip();
                        debugRead(buffer);
                        //切换到写模式,重置postiion
                        buffer.clear();
                        log.debug("after read....{}", a);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
```

1. 阻塞模式对资源的运用不是太大，会造成项目完成的效率
2. 非阻塞模式，因为会一直空转，就会导致cpu的占用率很高，服务器压力就会变得很大
3. 非阻塞模式的开启
   1. ssc.configureBlocking(false); //开启就收客户端连接的非阻塞模式
   2. sc.configureBlocking(false); //开启读取消息的非阻塞模式

### Selector

### 处理accept

==**要点**==

- 使用selector的时候,需要将ServerSocketChannel开启非阻塞模式
- 事件到来需要处理，不处理的话会一直循环，直到事件处理完毕
- 使用==**key.cancel()**==; 来取消事件

```java
@Slf4j
public class Server2Selector {
    public static void main(String[] args) throws IOException {
        //1 创建 Selector 管理多个channel
        Selector selector = Selector.open();


        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //使用selector的时候,需要将ServerSocketChannel开启非阻塞模式
        ssc.configureBlocking(false);
        //2 建立Selector 和channel的联系(注册)
        /**
         * 事件有多个类型
         * 1.accpet  --会在有连接请求时触发
         * 2.connect --是客户端连接建立后触发
         * 3.read --读取客户端发送的信息后触发
         * 4.write --可写事件
         */
        SelectionKey ssckey = ssc.register(selector, 0, null);
        //key只关注accept事件
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("regeister key:{}",ssckey);

        ssc.bind(new InetSocketAddress(8080));

        while(true){
           //3select方法 没有事件发生,线程阻塞,有事件发生,线程恢复运行
          //select 在事件未处理时，他不会阻塞，事件发生后要么处理，要么取消，不能置之不理
            selector.select();
            //4 处理事件 selectedKeys 内部包含了所有发生的事件 获取所有的可读可写的事件的集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("key,{}",key);
                ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                SocketChannel accept = channel.accept();
                accept.configureBlocking(false);
                log.debug("{}",accept);
                //取消任务
                key.cancel();
            }
        }
    }
}
```

#### 处理read

==**要点**==

- 创建一个selector对象，里面装的是selectorkey
- 发生事件之后，==**selector.select()**==;会将selector中的selectorkey添加到selector.selectedKeys()集合中，对象是一样的，但是存在不同的集合。但是它只会添加对象，不会删除。==**如果事件被处理，只会删除相关的value值，但是不会删除他的key，就会导致能进入if的分支，但是会出现空指针异常**==

```java
@Slf4j
public class Server2SelectorUseRead {
    public static void main(String[] args) throws IOException {
        //获取Selector
        Selector selector = Selector.open();

        //创建ServerSocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //开启他的非阻塞模式
        ssc.configureBlocking(false);

        //将ServerSocketChannel注册到selector中
        SelectionKey ssckey = ssc.register(selector, 0, null);
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("key:{}",ssckey);
        //绑定ServerSocketChannel的端口号
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            selector.select();
            //获取selector中的key
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("迭代器里面的key是:{}",key);
                //selector会在事件后,向selectedKeys集合中添加key,但是不删除,如果不删除,就会导致后续的循环一直在accept的if分支里面,从而会出现空指针异常
                iterator.remove();
                //判断key是属于什么事件
                if(key.isAcceptable()){
                    log.debug("========开始,sc.accept");
                    ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                  //在连接事件中将SockerChannel注册到选择器中
                    sc.register(selector,SelectionKey.OP_READ,null);
                    log.debug("{}",sc);
                }else if(key.isReadable()){
                    log.debug("========开始,sc.read");
                    SocketChannel sc = (SocketChannel)key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    sc.read(buffer);
                    log.debug("{}",sc);
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                }
            }
        }
    }
}
```

客户端断开问题解决

```java
try {
  log.debug("========开始,sc.read");
  SocketChannel sc = (SocketChannel)key.channel();
  ByteBuffer buffer = ByteBuffer.allocate(16);
  int read = sc.read(buffer);
  //如果是正常断开,read的返回值就是-1
  if(read == -1){
    key.cancel();
  }else {
    log.debug("{}", sc);
    buffer.flip();
    debugRead(buffer);
    buffer.clear();
  }
} catch (IOException e) {
  e.printStackTrace();
  //因为客户端断开,需要将key取消(从selector的key中真正删除key)
  key.cancel();
}
```

消息边界问题

- 固定消息长度，数据包大小一样，服务器按照预定长度读取，缺点浪费宽带
- 按照分隔符拆分，缺点效率低
- TLV格式，即Type类型，Length长度，Value数据，类型和长度已知的情况下，就可以方便获取消息大小，分配合适的buffer，缺点buffer需要提前分配，如果内容过大，则影响server吞吐量
  - Http1.1是TLV格式
  - Http2.0是LTV格式

##### 使用\n等特定的分隔符会出现的问题

- 如果接收的bytebuffer是一个局部变量，会导致接收的数据一直是超过的容量的最后的数据
- 如果将bytebuffer设置为全局变量，会导致读取的数据都是前面的最大容量的数据,单使用bytebuffer的模拟，是会报错的，但是在SockerChannel中不会报错，显示的是能接收到的最大的数据
- 在sockerchannel中如果数据超过接收的最大值，会进入两次循环读取
- 在将bytebuffer不放在局部变量时，需要考虑的是bytebuffer的共用问题，可以使用选择器的附件模式

##### 要点

- sockerchannle的附件使用方式
- 接收数据的Bytebuffer的使用范围

```java
@Slf4j
public class Server2SelecrotByRead {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        log.debug("得到的ssc为{}",ssc);
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        SelectionKey selectionKey = ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        log.debug("得到的selectionkey是:{}",selectionKey);
        while(true){
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("d得到的key是:{}",key);
                iterator.remove();
                if(key.isAcceptable()){
                    ServerSocketChannel ssckey =(ServerSocketChannel) key.channel();
                    ssckey.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(1<<4);
                  
                    //第三个参数,表示的是附件,作为附件关联到selectionkey上
                    SocketChannel sc = ssckey.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ,buffer);
                  
                }else if(key.isReadable()){
                    try{
                    SocketChannel sckey =(SocketChannel) key.channel();
                      
                    //获取selectiokey上关联的附件
                    ByteBuffer buffer =(ByteBuffer) key.attachment();
                    int read = sckey.read(buffer);
                      
                    debugAll(buffer);
                    if(read == -1){
                        key.cancel();
                    }
                    splite(buffer);
                      
                    //如果buffer的position和limite长度一样,就表示需要扩容
                        if(buffer.position() == buffer.limit()){
                            //扩容为之前的两倍
                            ByteBuffer newBytebuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                            buffer.flip();
                            newBytebuffer.put(buffer);
                            key.attach(newBytebuffer);
                        }

                } catch (Exception e){
                    e.printStackTrace();
                    key.cancel();
                    }
                }
            }
        }
    }

    private static void splite(ByteBuffer buffer) {
        buffer.flip();
        int limit = buffer.limit();
        for(int i=0;i<limit;i++){
            if(buffer.get(i)=='\n'){
                //这是一条完整的数据
                int length = i+1-buffer.position();
                ByteBuffer targer = ByteBuffer.allocate(length);
                for(int j = 0 ; j< targer.limit();j++){
                    targer.put(buffer.get());
                }
                debugAll(targer);
            }
        }
        buffer.compact();
    }

}
```

##### BytebuBuffer的扩容问题

- 每个channel都需要记录可能被切分消息，因为ByteBuffer不是线程安全的，因此需要为每个channel维护一个独立的ByteBuffer
- ByteBuffer不能太大，比如一个ByteBuffer为1MB的话，要支持百万连接就需要1TB内存，因此需要设计大小可变的ByteBuffer
  - 首先的分配较小的buffer，例如4K，如果发现数据不够，在分配8K的buffer，将4Kbuffer内容拷贝至8K buffer，优点是消息连续容易处理，缺点是数据拷贝消耗性能，参考实现：https://jenkov.com/tutorials/java-performance/resizable-array.html
  - 第二种:使用多个数据组成buffer，一个数组不够，把多出来的内容写到新的数组，与前面的区别是消息存储不连续解析复杂，优点是避免了拷贝引起的性能损耗

#### 处理写事件

##### 会出现的问题

```java
public class Server2SelectorByWrite {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel ssc  = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT,null);
        while(true){
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while(iterator.hasNext()){
                SelectionKey next = iterator.next();
                iterator.remove();
                if(next.isAcceptable()){
                    ServerSocketChannel ssckey =(ServerSocketChannel) next.channel();
                    SocketChannel sc = ssckey.accept();
                    sc.configureBlocking(false);
                    //向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());
                    while(buffer.hasRemaining()){
                        int writer = sc.write(buffer);
                        System.out.println(writer);
                    }
                }
            }
        }
    }
}
/**
 * 控制台打印的结果:
 * 261676
 * 719752
 * 0
 * 0
 * 0
 * 588992
 * 810228
 * 196036
 * 0
 * 40960
 * 382356
 * 会导致不是非阻塞,在写缓冲区满的时候会在等待,需要给他优化
 */
```

##### 优化

```java
public class Server2SelectorByWrite {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel ssc  = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT,null);


        while(true){
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){

                    ServerSocketChannel ssckey =(ServerSocketChannel) key.channel();
                    SocketChannel sc = ssckey.accept();

                    sc.configureBlocking(false);

                    SelectionKey sckey = sc.register(selector, 0, null);

                    //向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }

                    ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());
                        int writer = sc.write(buffer);
                        System.out.println(writer);
                        //判断是否还有数据没有写完
                        if(buffer.hasRemaining()){
                            //关注可写事件 读事件是1 写事件是2
                            sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                            sckey.attach(buffer);
                        }
                }else if(key.isWritable()){
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    int write = socketChannel.write(buffer);
                    System.out.println(write);

                    //清理操作
                    if(!buffer.hasRemaining()){
                        //清除buffer
                        key.attach(null);
                        //数据写完不需要关注可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}

/**
 * 控制台打印的结果:
 * 482340
 * 220820
 * 679468
 * 802036
 * 637884
 * 177452
 */
```

#### 网络编程小结

##### 阻塞

- 阻塞模式下，相关方法会导致线程暂停
  - ServerSocketChannel.accept()会在没有连接建立时让线程暂停
  - SocketChannel.read会在没有数据可读时让线程暂停
  - 阻塞的表现其实就是线程暂停，暂停期间不会占用cup，但线程相当于闲置
- 单线程下，阻塞方法之间相互影响，几乎不能正常工作，需要多线程支持
- 但多线程下，有新的问题，体现在以下几个方面
  - 32为JVM一个线程320k，64位JVM一个线程1024k，如果连接数过多，不然导致OOM，并且线程太多，反而会因为频繁上下文切换导致性能降低
  - 可以采用线程池技术来减少线程数和线程上下文切换，但治标不治本，如果有很多建立连接，但长时间inactive，会阻塞线程池中所有线程，因此不适合长连接，只适合短连接

##### 多路复用

单线程可以配合selector完成多个Channel可读可写事件的监控，这称之为多路复用

- 多路复用仅针对网络IO，普通文件没法利用多路复用
- 如果不用Selector的非阻塞模式，线程大部分时间都在做无用功，而Selector能够保证
  - 有连接事件时才回去连接
  - 有可读事件才去读取
  - 有可写事件才去写入
    - 限于网络传输能力，Channel未必时时可写，一但Channel可写，会触发Selector的可写事件

监听Channel事件

可以通过下面三种方法来监听事件是否发生吗，方法的返回值代表有多少Channel发生了事件

1. 阻塞知道绑定事件发生

   ```java
   int count = selecor.select();
   ```

2. 阻塞知道绑定事件发生，或是超时（时间单位为ms）

   ```java
   int count = selecor.select(long timeout);
   ```

3. 不会阻塞，也就是不管有没有事件发生，立刻返回，自己根据返回值检查是否有事件

   ```java
   int count = selector.selectorNpw();
   ```

##### selector何时不阻塞

- 事件发生
  - 客户端发起连接诶请求，会触发accept事件
  - 客户端发送数据过来，客户端正常，异常关闭时，都会触发read事件，另外如果发送的数据大于buffer缓冲区，会触发多次读取事件
  - channel可写，会触发wroter事件
  - 在linux下nio bug时
- 调用selector.wakeup()
- 调用selector.close()
- selector所在贤臣interrupt	

#### NIO多线程编程

##### 使用多线程优化

现在都是多核CPU，设计时需要充分考虑别让CPU的力量白白浪费

- 单线程配一个选择器，专门处理accpet事件
- 创建cpu核心线程数，每一个线程配一个选择器，轮流处理read事件

##### 多线程版的实现

```java
@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);

        Selector boss = Selector.open();

        ssc.register(boss, SelectionKey.OP_ACCEPT,null);
        Worker worker = new Worker("worker-01");
        worker.regest();
        while(true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    //如果是一个连接事件
                    ServerSocketChannel  ssc_1 = (ServerSocketChannel) key.channel();
                    SocketChannel sc = ssc_1.accept();
                    sc.configureBlocking(false);
                    log.debug("connected........{}",sc.getRemoteAddress());
                    sc.register(worker.selector,SelectionKey.OP_READ,null);
                }
            }
        }
    }

    //创建一个worker对象,用于读取数据
    static class Worker implements Runnable{
        private Selector selector;
        private Thread thread;
        private String name;
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private volatile boolean start = false;

        public Worker(String name){
            this.name = name;
        }

        public void regest() throws IOException {
            if(!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }
          /*  queue.add(()->{
                try {
                    sc.register(selector,SelectionKey.OP_READ,null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();*/
        }

        @Override
        public void run() {
            while(true) {
                try {
                   //设置过期时间
                    selector.select(10);
                    Runnable poll = queue.poll();
                    if(poll != null){
                        poll.run();
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            log.debug("开始读取数据.......");
                            sc.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

1. 可能出现的问题
   1. 使用两个线程，可能会因为在不一样的线程中出现两个线程相互影响，导致读取不到数据
   2. 出现的原因
      1. 在selector遍历select（）的时候，worker的任务并没有注册进去，导致一直将线程阻塞，然后那个注册的方法也一直不能执行
   3. 解决方式
      1. 增加一个队列，在队列中添加任务，然后再worker注册的方法中，将SockerChannel的注册进selector的方法放入到队列中，然后在selector遍历需要执行的任务的时候，执行队列中的方法

##### 线程数的选择

```java
//获取核心线程数
Runtime.getRuntime().availableProcessors()
```

1. 问题
   1. Runtime.getRuntime().availableProcessors()如果工作在docker容器下，因为容器不是物理隔离的，会拿到物理cpu个数，不是容器申请的个数
   2. 这个问题直到jdk10才修复，使用jvm参数UserContainerSupport配置，默认开启

## NIO vs BIO

### stream vs channel

- stream不会自动缓冲数据，channel会利用系统提供的发送缓冲区，接收缓冲区(更为底层)
- stream仅支持阻塞API，channel同时支持阻塞、非阻塞API，网络channel可配置selector实现多路复用
- 二者均为全双工，即读写可以同时进行

### IO模型

#### 同步阻塞、同步非阻塞、多路复用、异步阻塞（没有这个情况）、异步非阻塞

当调用一次channel.read或stream.read后，会切换至操作系统内核态来完成真正数据读取，而读取又分为两个阶段，分别为：

- 等待数据阶段
- 复制数据阶段



- 阻塞IO:用户线程发起一次read，会将用户空间切换到内核空间，网络上可能没有数据读取，这个时候read就会是阻塞状态，用户线程被阻塞（同步）
- 非阻塞IO:用户调用一次read方法，如果没有读到数据会继续进行一次读操作，一直在轮询，看有没有数据，有数据之后，就会做复制数据的操作，这个时候还是会阻塞线程（只是等待数据阶段是非阻塞的）（同步）
- 多路复用:首先调用select方法，select是一个阻塞的方法，在复制数据的阶段使用read方法，也是阻塞的方法。（同步）

```
阻塞io和多路复用的区别
阻塞io一次只能处理一件事
多路复用可以在同一时间处理多个不同事件
```

- 信号驱动:
- 异步IO:通知内核系统进行读数操作，然后另外一个线程返回真正的结果，关键的实现是回调方法(异步)
- 同步:现成自己去获取结果(一个线程)
- 异步:线程自己不去获取结果，而是由其他线程送结果(至少两个线程)

参考书籍:UNIX网络编程 卷一

#### 零拷贝

##### 内部工作流程

1. java本身并不具备IO读写能力，因此read方法调用后，要从java程序的用户态切换到内核态，去调用操作系统(Kernel)能力，并将数据读入内核缓冲区，这期间用户线程阻塞，操作系统使用DMA(Direct Menory Access)来实现文件读，期间也不会使用cpu
   1. DMA也可以理解为硬件单元，用来解放cpu完成文件IO
2. 从内核态切换回用户态，将数据从内个缓冲区读入用户缓冲区(即Byte[] buf)，这期间cpu会参与拷贝，无法利用DMA
3. 调用writer方法，这时将数据从用户缓冲区(byte[] buf)写入socket缓冲区，cpu参与拷贝
4. 接下来要从网卡写数据，这项能力java又不具备，因此又得从用户态切换至内核态，调用操作系统的写能力，使用DMA将SOCket缓冲区的数据写入网卡，不会使用cpu

中间的环节较多，java的IO实际不是物理设备级别的读写，而是缓存的复制，底层的真正读写是操作系统来完成的

- 用户态与内核态的切换发生了3次，这个操作比较重量级
- 数据拷贝共4次

##### NIO优化

通过DirectByteBuf

- ByteBuffer.allocate(10) heapByteBuffer:使用的还是java内存
- ByteBuffer.allocateDirect(10) DirectBytebuffer :使用的是操作系统内存

大部分步骤与优化前相同，唯一一点：java可以使用DirectByteuffer讲堆外内存映射到jvm内存中来直接访问使用

- 这一块内存不受JVM垃圾回收的影响，因此内存地址固定，有助于IO读写
- java中的DirectByteBuffer对象仅维护了此内存的虚引用，内存回收分成两步
  - DirectByteBuf对象呗垃圾回收，将虚引用家督引用队列
  - 通过专门线程访问引用队列，根据虚引用加入引用队列
- 减少了一个数据拷贝，用户态与内核态的切换次数没有减少

:exclamation:进一步优化(底层采用linux2.1后提供的sendfile方法)，java中对应着两个channel调用transferto/transferFrom方法拷贝数据

1. java调用transferTo方法后，要从java程序的用户态切换至内核态，使用DMA将数据读入内核缓冲区，不会使用cpu
2. 将数据从内核缓冲区传输到socket缓冲区，cpu会参与拷贝
3. 最后使用DMA将socket缓冲区的数据写入网卡，不会使用cpu

- 只使用了一次用户态与内核态的切换
- 数据拷贝了3次

:exclamation:进一步优化

- java调用transferTo方法后，要从java程序到用户态切换至内核态，使用DMA将数据读入内核缓冲区，不会使用cpu
- 只会将一些offset和length信息拷贝到socket缓冲区，几乎没有消耗
- 使用DMA将内核缓冲区的数据写入网卡，不会使用cpu

整个过程仅只发生了一次用户态与内核态的切换，数据拷贝了2次，所谓的【零拷贝】,并不是真正拷贝，而是不会拷贝重复数据到jvm中，零拷贝的优点

- 更少的用户态与内核态的切换
- 不利用cpu计算，减少cpu缓存伪共享
- 零拷贝上个小文件传输

### AIO

AIO用来解决数据复制阶段的阻塞问题

- 同步意味着，在进行读写操作时，现成要等待结果，还是相当于闲置
- 异步意味着，在进行读写操作时，线程不必等待结果，而是将来由操作系统来通过调用方式由另外的线程来获取结果
- 异步模型需要底层操作系统(Kernel)提供支持
  - windows系统通过IOCP实现真正的异步IO
  - Linux系统异步IO在2.6版本引入，但其底层实现还是多路复用模拟了异步IO，性能没有优势

# Netty入门学习

## Hollw World入门

#### Hello World代码

```java
//服务器端
public class HelloServer {
    public static void main(String[] args) {
        //1.启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                //2.BossEventLoop,WorkerEventLoop(包含线程和选择器)
                .group(new NioEventLoopGroup())
                //3.选择服务器的serverSocketChannel实现
                //oioserverSocketChannel是阻塞的io实现
                .channel(NioServerSocketChannel.class)
                //4.boss，负责处理连接，worker负责处理读写，
                //决定将来能做什么事（能执行哪些操作（handle））
                .childHandler(
                        //5.代表和客户端进行数据读写的通道
                        new ChannelInitializer<NioSocketChannel>() {
                    //6.初始化器，负责添加别的处理器
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //7.添加具体的handle
                        nioSocketChannel.pipeline().addLast(new StringDecoder());//将传来的ByteBuffer转化为字符串
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){//自定义的业务处理
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                super.channelRead(ctx, msg);
                                //打印上一步转换好的字符串
                                System.out.println(msg);
                            }
                        });
                    }
                }).bind(8080);//绑定的监听端口
    }
}

//客户端
public class ServerClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类
        new Bootstrap()
                //2.添加EvenLoop
                .group(new NioEventLoopGroup())
                //3.添加选择客户端，channel实现
                .channel(NioSocketChannel.class)
                //4.添加处理器，初始化处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //将字符串转为bytebuf
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                //连接到服务器
                .connect(new InetSocketAddress("127.0.0.1",8080))
                .sync().channel().writeAndFlush("hello world");

    }
}
```

#### Hello World执行顺序

1. 启动服务器的==**ServerBootstrap**==
2. 组装服务器的==**group**==
3. 选择服务器的==**serverSocketChannel**==实现
4. 服务器的==**(childHandle)**==负责连接,使用的对象都是
5. 服务器创建==**(ChannelInitializer)**==初始化(和客户端进行数据读写通道)==**(NioSocketChannel)**==
6. 绑定监听端口==**bind**==
7. 客户端启动器创建==**Bootstrap**==
8. 客户端组装==**group添加EventLoop**==
9. 选择客户端的==**channel**==实现
10. 客户端添加处理器==**(ChannelInitializer)**==
11. 连接服务器==**(connect)**==
12. 客户端连接服务器时==**(调用初始化通道和服务器的初始化通道)**==
13. 客户端使用==**sync()**==阻塞方法,直到连接建立,后接连接对象
14. 客户端发送数据==**writeAndFlush**==
15. 在客户端的初始化通道中,将数据转化为byteBuf
16. 服务器中由某个==**EventLoop处理read事件,接收ByteBuf**==
17. 在服务器的初始化通道中,将byteBuf转化为字符串
18. 服务器中执行自定义的方法,==**处理需要处理的逻辑**==

:bulb:提示

- 把channel理解为数据的通道
- 把msg理解为流动的数据,最开始输入的时ByteBuf,但是经过==**pipeline(流水线)**==加工,会变成其他类型对象,最后输出又变成ByteBuf
- 把handle理解为数据的处理工序
  - 工序有多道,合在一起时pipeline,==**pipeline负责发布事件(读,读取完成.....)**==传播给每个handle,==**handle对自己感兴趣的事件进行处理(重写了相对应事件处理方法)**==
  - handle分==**Inbound(入站)**==和==**OutBound(出站)**==
- 把Eventloop理解为处理数据的工人
  - 工人可以管理多个channel的io操作,并且一旦工人负责了某个channel,就要负责到底(绑定,处理完之后,后续如果还过来,还是同一个工人处理)
  - 工人既可以执行io操作,也可以进行任务处理,每个工人有任务队列,队列里可以堆放多个channel的待处理任务,任务分为普通任务,定时任务
  - 工人按照pipeline顺序,依次按照handle的规划(代码)处理数据,可以为每道工序制定不同的工人

### 组件

#### Eventloop

EventLoop本质是一个单线程执行器(同时维护一个Selector),里面run方法处理Channel上源源不断的io事件

它的继承关系比较复杂

- 一条线是集成自j.u.c.ScheduledExcecutorService因此包含了线程池所有方法
- 另一条线是继承自netty自己的OrderdEventExecutor
  - 提供了boolean inEventLoop(Thread thread)方法判断一个线程是否属于此EventLoop
  - 提供了parent方法来看看自己属于哪个EventLoopGroup

EventLoopGroup是一组EventLoop,Channel一般会调用EventLoopGroup的register方法来绑定其中一个EventLoop,后续这个Channel上的io事件由此EventLoop来处理(保证了io事件处理时的线程安全)

- 继承自netty自己的EventLoopGroup
  - 实现了Iterable接口提供遍历EventLoop的能力
  - 另有next方法获取集合中下一个EventLoop

:bulb:细节

1. NIOEventLoop的默认线程数为服务器的线程数*2
2. 在处理多线程任务的时候,需要将断点方式从ALL修改为Thread
3. 如果在处理的时间需要耗费的时间很长,可以另外添加一个Group,用于处理耗时很长的事件,一般使用的是默认的group.

```java
//服务端代码
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        //todo 进一步细化,在NioEvevtLoop需要处理很大的操作的时候,可以给他指定一个group来操作
        DefaultEventLoopGroup group = new DefaultEventLoopGroup(2);
        new ServerBootstrap()
                //两个EventLoop分别是 案例中的boss和worker
                //第一个作用是负责accept的连接事件
                //第二个作用是负责数据的读写事件
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                log.debug(buffer.toString(Charset.forName(StandardCharsets.UTF_8.name())));
                                ctx.fireChannelRead(msg);
                            }
                        }).addLast(group,"Handle_2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                log.debug(buffer.toString(Charset.forName(StandardCharsets.UTF_8.name())));
                            }
                        });
                    }
                }).bind(8080);
    }
}

//客户端代码:
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        Channel channel = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 8080))
                .sync()
                .channel();

        System.out.println("");
    }
}
```

:bulb:handle执行中如何换人

关键代码io.netty.channel.ChannelHandlerContext#fireChannelRead

```java
static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
        final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
    //下一个handle的事件循环是否与当前事件循环是同一个线程
        EventExecutor executor = next.executor();//返回下一个handle的eventLoop
    //是 ,直接调用
        if (executor.inEventLoop()) {
            next.invokeChannelRead(m);
        } else {
            //不是,将要执行的代码作为任务提交给下一个事件循环处理(换人)
            executor.execute(new Runnable() {//下一个handle线程
                @Override
                public void run() {
                    next.invokeChannelRead(m);
                }
            });
        }
    }
```

:bulb:总结:

- 如果两个handle绑定的是同一个线程,那么就直接调用
- 如果两个handle绑定的不是同一个线程,把要调用的代码封装为一个任务对象,由下一个handle的线程来调用

#### Channel

##### channel的主要作用

- close()可以用来关闭channel
- closeFuture()用来处理channel的关闭
  - sync方法作用是同步等待channel关闭
  - addListener方法是异步等待channel关闭
- peline()方法添加处理器
- write()方法将数据写入
- writeAndFlush()方法将数据写入并刷出

##### ChannelFuture 

```java
@Slf4j
public class ChannelClient {
    public static void main(String[] args) throws InterruptedException {
        //2 带有Future Promise的类型都是和异步方法配套使用,用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                //1.连接到服务器
                //异步非阻塞,main发起了作用,真正执行connect是nio线程
                .connect(new InetSocketAddress("127.0.0.1", 8080));

        //2.1使用sync()方法,同步处理结果
        //阻塞当前线程,直到nio线程连接建立完毕
        /*channelFuture.sync();
        Channel channel = channelFuture.channel();
        log.debug("{}",channel);
        channel.writeAndFlush("1111");*/

        //2.2使用addListener方法,异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            //nio在线程建立好之后,会调用operationComplate
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("{}",channel);
                channel.writeAndFlush("我是方法2");
            }
        });
    }
}
```

:bulb:重点

- 一般带有Future Promise的类型都是和异步方法配套使用,用来处理结果

- 连接服务器的步骤，不应该是main线程发起连接，而是应该Nio的线程发起连接
- 如果没有使用sync，就不会同步等待连接结果，会一直运行下去，导致连接使用的是main线程，从而出现接收不到数据的结果
- 在没有sync方法的情况下，可以使用addListener来异步处理结果，在addListener连接之后，使用operationComplete方法来运行连接建立完之后运行的方法

```java
@Slf4j
public class ChannelFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                //netty自带的日志记录器
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast(new StringEncoder());
            }
        }).connect(new InetSocketAddress("127.0.0.1", 8080)).channel();

        //创建一个新的线程用于发送消息
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.nextLine();
                if ("q".equals(msg)) {
                    //在用户输入q之后,退出聊天
                    channel.close();
//  q_1.1              log.debug("后续收尾的工作......");
                    break;
                }
                channel.writeAndFlush(msg);
            }
        }, "input").start();
// q_1.1       log.debug("后续收尾的工作......");

        //q_1.2 channel.closeFuture().sync();是一个同步的阻塞方法,会将main线程阻塞住,在执行完channel的关闭动作之后,开始执行后面的代码,可以实现后续收尾工作
       /* ChannelFuture future = channel.closeFuture().sync();
        log.debug("后续收尾的工作......");*/

        //q_1.3 channel.closeFuture().addListener使用的是异步的操作,收尾的工作是在nio的线程中完成,可以保证他的执行顺序
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("后续收尾的工作......");
                group.shutdownGracefully();
                channel.writeAndFlush("bbbbb");
            }
        });

    }
}
```

:diamonds:注释

q_1:如果需要再channel退出后做一些收尾工作,应该怎么操作
 * 项目中有q_1.1的注释,表示的是将后续收尾的工作都放在这里并不合适,通过日志可以知道他们不是在一个线程中的,所以无法保证他们执行的顺序
 * 应该使用的是channel自带的方法来进行后续收尾
 * q_1.2 channel.closeFuture().sync();是一个同步的阻塞方法,会将main线程阻塞住,在执行完channel的关闭动作之后,开始执行后面的代码,可以实现后续收尾工作
 * q_1.3 channel.closeFuture().addListener使用的是异步的操作,收尾的工作是在nio的线程中完成,可以保证他的执行顺序

q_2:为什么退出后,项目并没有退出,依旧在运行?应该怎么操作让它随用户的退出一起关闭?

 * 需要关闭的还有NioEventLoopGroup,将这个资源关闭之后,系统就会停止运行
 *  使用group.shutdownGracefully();方法,表示优雅关闭,注释为在关闭期间不接受任务,如果接受了任务就会将任务执行完,并重新计算是时间,2s内接受到任务就重新计时15s为超时时间

##### 为什么使用异步

:bulb:要点

- 单线程没法异步提高效率，必须配合多线程，多核cpu才能发挥异步的优势
- 异步并没有缩短响应时间，反而有所增加
- 合理进行任务拆分，也是利用异步的关键

#### Future&promise

- 异步处理时，经常用到两个接口

首先需要说明netty中的future与jdk中的future同名，但是两个接口，netty的future继承自jdk的future，而promise又对future进行了拓展

- jdkFuture只能同步等待任务结束（或成功、或失败）才能得到结果

- netty Future可以同步等待任务结束得到结果，也可以异步方式得到结果，但都是要等任务结束

- netty Promise不仅有netty Future的功能，而且脱离了任务独立存在，只作为两个线程间传递结果的容器

- |  功能/名称   |            jdk Future            |                         netty Future                         |   Promise    |
  | :----------: | :------------------------------: | :----------------------------------------------------------: | :----------: |
  |    cancel    |             取消任务             |                              -                               |      -       |
  |  isCanceled  |           任务是否取消           |                              -                               |      -       |
  |    isDone    | 任务是否完成，不能够区分成功失败 |                              -                               |      -       |
  |     get      |      获取任务结果，阻塞等待      |                              -                               |      -       |
  |    getNow    |                -                 |        获取任务结果，非阻塞，还未产生结果时，返回null        |      -       |
  |    await     |                -                 | 等待任务结束，如果任务失败，不会抛异常，而是通过isSuccess判断 |      -       |
  |     sync     |                -                 |             等待任务结束，如果任务失败，抛出异常             |      -       |
  |  isSuccess   |                -                 |                       判断任务是否成功                       |      -       |
  |    cause     |                -                 |         获取失败信息，非阻塞，如果没有失败，返回null         |      -       |
  | addLinstener |                -                 |                    添加回调，异步接收结果                    |      -       |
  |  setSuccess  |                -                 |                              -                               | 设置成功结果 |
  |  setFailure  |                -                 |                              -                               | 设置失败结果 |

#### handle&Pipeline

ChannelHandler用来处理Channel上的各种事件，分为入站、出站两种，所有ChannelHandler被连成一串，就是Pipeline

- 入站处理器通常是ChannelInboundHandlerAdapter的子类，主要用来读取客户端数据，写回结果
- 出站处理器通常是ChannelOutboundHandlerAdapter的子类，主要对写回结果进行加工

相当于每个Channel是一个产品的加工车间，Pipeline是车间中的流水线，ChannelHandler就是流水线上的各道工序，而后面要讲的ByteBuf是原材料，经过很多工序的加工路线经过一道道入站工序，在经过一道道出站工序最终变成产品

服务端pipeline出发的原始流程，图中数字代表了处理步骤的先后次序

head--->ln_1--->ln_2--->ln_3--->out_4--->out_5--->out_6--->tail

- 入站的执行顺序为lin_1到ln_2到ln_3

- 在遇到tail.writeAndFlush()方法之后就会执行出站的head

- 所以在ln_1到ln_3这个入站的顺序中如果调用了tail.writeAndFlush()方法，就会使pipeline从执行tail.writeAndFlush()方法的最开始向后执行，判断那个是出站操作，并执行相关head

  ```java
  @Slf4j
  public class TestHandle {
      public static void main(String[] args) throws InterruptedException {
          new ServerBootstrap()
                  .group(new NioEventLoopGroup())
                  .channel(NioServerSocketChannel.class)
                  .childHandler(new ChannelInitializer<NioSocketChannel>() {
                      @Override
                      protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                          //获取
                          ChannelPipeline pipeline = nioSocketChannel.pipeline();
                          pipeline.addLast(new StringDecoder());
                          //添加处理器head->h1->h2->h3->h4->h5->h6->tail
                          pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                              @Override
                              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                  log.debug("1,{}",msg);
                                  String name = msg.toString();
                                  super.channelRead(ctx, name);
                              }
                          });
                          pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                              @Override
                              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                  log.debug("2");
                                  Student student = new Student(msg.toString());
                                  super.channelRead(ctx, student);
                              }
                          });
                          pipeline.addLast("h7",new ChannelOutboundHandlerAdapter(){
                              @Override
                              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                  log.debug("7");
                                  super.write(ctx, msg, promise);
                              }
                          });
                          pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                              @Override
                              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                  log.debug("3,结果是{},class:{}",msg,msg.getClass());
                                  //将处理权交给下一个入站处理器,如果后续没有可以不加channelRead
  //                                super.channelRead(ctx, msg);
  //                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("server.....".getBytes()));
                                  /**
                                   * 使用ctx的writeAndFlush方法会导致后面的出站操作不能被读取的到
                                   * 会在有writeAndFlush方法执行后向前执行,判断前面的方法时候存在出站的head
                                   * 添加了h7之后就会因为h7在h3的前面,然后又是出站的head,就会使他运行
                                   */
                                  ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server.....".getBytes()));
                              }
                          });
                          pipeline.addLast("h4",new ChannelOutboundHandlerAdapter(){
                              @Override
                              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                  log.debug("4");
                                  super.write(ctx, msg, promise);
                              }
                          });
                          pipeline.addLast("h5",new ChannelOutboundHandlerAdapter(){
                              @Override
                              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                  log.debug("5");
                                  super.write(ctx, msg, promise);
                              }
                          });
                          pipeline.addLast("h6",new ChannelOutboundHandlerAdapter(){
                              @Override
                              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                  log.debug("6");
                                  super.write(ctx, msg, promise);
                              }
                          });
                      }
                  }).bind(8080).sync().channel().read();
      }
  }
  ```

- ##### EmbeddedChannel（）：用于调试，不需要启动客户端和服务端

  ```java
  @Slf4j
  public class TestEmbeddedChannel {
      public static void main(String[] args) {
          ChannelInboundHandlerAdapter h1 = new ChannelInboundHandlerAdapter(){
              @Override
              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  log.debug("h1");
                  super.channelRead(ctx, msg);
              }
          };
          
          ChannelInboundHandlerAdapter h2 = new ChannelInboundHandlerAdapter(){
              @Override
              public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  log.debug("h2");
                  super.channelRead(ctx, msg);
  //                ctx.writeAndFlush(msg);
              }
          };
  
          ChannelOutboundHandlerAdapter h3 = new ChannelOutboundHandlerAdapter(){
              @Override
              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                  log.debug("h3");
                  super.write(ctx, msg, promise);
              }
          };
  
          ChannelOutboundHandlerAdapter h4 = new ChannelOutboundHandlerAdapter(){
              @Override
              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                  log.debug("h4");
                  super.write(ctx, msg, promise);
              }
          };
          EmbeddedChannel channel = new EmbeddedChannel(h1,h2,h3,h4);
          //模拟入站操作
  //        channel.writeOneInbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello".getBytes()));
  
          //模拟出站操作
          channel.writeOneOutbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello".getBytes()));
      }
  
  }
  ```

#### ByteBuf

1. ##### 直接内存 VS 堆内存

   可以使用下面的代码来创建池化基于堆的Bytebuf

   ```java
   Bytebuf buffer = ByteBufAllocator.DEFFAULT.heapBuffer(10);
   ```

   也可以使用下面的代码创建池化基于内存的ByteBuf

   ```java
   Bytebuf buffer = ByteBufAllocator.DEFFAULT.directBuffer(10);
   ```

   - 直接内存创建和销毁的代价昂贵，但读写性能高(少一次内存复制)，适合配合池化功能使用
   - 直接内存对GC压力小，因为这部分内存不受JVM垃圾回收的管理，但也要注意及时主动释放

2. ##### 池化 vs 非池化

   池化的最大意义在于可以重用ByteBuf，优点有

   - 没有池化，则每次都得创建新的ByteBuf实例，这个操作对直接内存代价昂贵，就算是堆内存，也会增加GC压力
   - 有了池化，则可以重用池中ByteBuf实例，并且采用与jemalloc类似的内存分配算法提升分配效率
   - 高并发时，池化功能更节约内存，减少内存溢出的问题

3. ##### 池化功能是否开启，可以通过下面的系统环境变量来设置

   ```java
   -Dio.netty.allocator.type={unpooled|pooled}
   //4.1以后，非Android平台默认启用池化实现，Android平台启用非池化实现
   //4.1之前，池化功能还不成熟，默认是非池化实现
   ```

4. ##### 组成

   Bytebuf由四个部分组成

   - capacity：容量
   - max capacity :最大容量
   - read index:读取位置
   - write index:写入位置
   - 最大容量和容量之间为可扩容字节

5. ##### 写入

   |          方法签名           |         含义         |                   备注                   |
   | :-------------------------: | :------------------: | :--------------------------------------: |
   | writeBoolean(boolean value) |    写入boolean值     |     用一个字节01\|00表示true\|false      |
   |    writeByte(int value)     |      写入byte值      |                                          |
   |    writeShort(int value)    |     写入short值      |                                          |
   |     writeInt(int value)     |      写入int值       |  Big Endian，即0x250，写入后00 00 02 50  |
   |    writeIntLE(int value)    |      写入int值       | Little Endian,即0x250，写入后50 02 00 00 |
   |    writeLong(long value)    |      写入long值      |                                          |
   |    writeChar(int value)     |      写入char值      |                                          |
   |   writeFloat(float value)   |     写入float值      |                                          |
   |  writeDouble(double value)  |     写入double值     |                                          |
   |   writeBytes(ByteBuf src)   | 写入netty的Bytebuf值 |                                          |
   |   writeBytes(byte[] src)    |      写入byte[]      |                                          |

   :bulb:注意

   - 这些方法的未指明返回值的，其返回值都是ByteBuf，意味着可以链式调用
   - 网络传输，默认习惯是Big Endian

6. ##### 扩容

   - 扩容规则
   - 如果写入的数据大小未超过512，则选择下一个16的整数倍，例如写入后大小为12，则扩容后capacity是16
   - 如果写入后数据大小超过512，则选择下一个2^n,例如写入后为513，则扩容后capacity是2^10=1024(2^9=512已经不够了)
   - 扩容不能超过max capacity，会报错

7. ##### 读取

   读过的内容，就属于废弃部分，再读只能读那些尚未读取的部分

   如果需要重复读取一个位置的数据，可以在read前先做个标记mark(buffer.markReaderIndex())，这时要重复读取的话，重置到标记位置rest(buffer.resetReaderIndex())

8. ##### Retain & release

   由于netty中有堆外内存的ByteBuf实现，堆外内存最好是手动来释放，而不是等GC垃圾回收

   - UnpooledHeapByteBuf使用的是JVM内存，只需要等GC回收内存即可
   - UnpooledDireByteBuf使用的是直接内存，需要特殊的方法来回收内存
   - PooledByteBuf和它的子类使用了池化机制，需要更复杂的规则来回收内存

   :bulb:回收内存的源码实现，请关注下面方法的不同实现

   ```java
   protected abstract void deallocate()
   ```

   netty这里采用了引用计数的方法控制回收内存，每个ByteBuf都实现了ReferenceCounted接口

   - 每个ByteBuf对象的初始化计数为1
   - 调用release方法计数减一，如果计数为0，ByteBuf内存被回收
   - 调用retain方法计数加1，表示调用者没有用完之前，其他handler即使调用了release也不会造成回收
   - 当计数为0，底层内存会被回收，这时即使ByteBuf对象还在，其他方法均无法正常使用

   基本规则(==**谁最后使用谁就释放**==)

   视频进度:https://www.bilibili.com/video/BV1py4y1E7oA/?p=86&spm_id_from=pageDriver&vd_source=000766059912952028e3af1ddb9f2463

9. ##### slice

   【零拷贝】的体现之一，对原始ByteBuf进行切片成多个ByteBuf，切片后的ByteBuf并没有发生复制，还是使用原始ByteBuf的内存,切片后的ByteBuf维护独立的read，write指针

   - 切片过程中，没有发生数据复制
   - 切片后的数据,slince容量不会在增加
   - 如果对原始数据做了一次release(释放原有数据)，切片后的数据也不能显示

10. ##### duplicate

    【零拷贝】的体现之一，就好比截取原始ByteBuf所有内容，并且没有max capacity的限制，也是与原始ByteBuf使用同一块底层地址，只是读写指针是独立的

11. ##### copy

    会将底层的数据进行深度拷贝，因此无论读写，都与原始ByteBuf无关

12. ##### compositeBuf

    和slice相反，是将多个ByteBuf组合为一个ByteBuf，也需要考虑应用计数的问题

    ```java
    CompositeByteBuf bufs = ByteBufAllocator.DEFAULT.compositeBuffer();
            bufs.addComponents(true,f1,f2);
            log(bufs);
    ```

13. Unpooled
    - 是一个工具类，提供了非池化的ByteBuf创建、组合、复制等操作
    - 当包装ByteBuf个数超过一个时，底层使用了CompositeByteBuf

**:bulb:ByteBuf优势**

- 池化-可以重用池中ByteBuf实例，更节约内存，减少内存溢出的可能
- 读写指针分离，不需要像ByteBuffer一样切换读写模式
- 可以自动扩容
- 支持链式调用，使用更加流畅
- 很多地方体现零拷贝、例如slice、duplicate、CompositeByteBuf

#### 问题双向通信

```java
//服务端代码
@Slf4j
public class WorkServer {
    public static void main(String[] args) throws InterruptedException {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //首先将数据格式化
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        //创建出站和入站处理
                        pipeline.addLast("l1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("读取的消息是:"+msg.toString());
//                                ctx.writeAndFlush(msg.toString());
                                String s = msg.toString();
                                super.channelRead(ctx, s);
                            }
                        });
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("发送的消息是:"+msg);
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(msg.toString().getBytes()));
                            }
                        });
                        /*pipeline.addLast("w1",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                                super.write(ctx, msg, promise);
                                log.debug("发送的消息是:"+msg);
                                ctx.writeAndFlush(msg);
                            }
                        });*/
                    }
                }).bind(8080).sync().channel().read();
    }
}
```

```java
//客户端代码
@Slf4j
public class WorkerClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new StringEncoder());
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                    super.channelRead(ctx, msg);
                        ByteBuf byteBuf = (ByteBuf) msg;
                        log(byteBuf);
                        WorkerClient.log.debug("客户端读取的消息是:"+byteBuf.toString());
                        super.channelRead(ctx, msg);
                    }
                });
            }
        }).connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while(true) {
                String s = scanner.nextLine();
                if ("q".equals(s)) {
                    channel.writeAndFlush(s);
//                    优雅关闭客户端
                    channel.close();
                    break;
                }else {
                    channel.writeAndFlush(s);
                }
            }
        },"write_1").start();

        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                group.shutdownGracefully();
            }
        });

    }

}
```



# Netty进阶

### 黏包与半包

1. 滑动窗口：TCP以一个段(segment)为单位，每发送一个段需要进行一次确认应答(ack)处理，但如果这么做，缺点是包的往返时间越长性能越差
   1. 窗口实际就起到一个缓冲区的作用，同时也能起到流量控制的作用
   2. 图中深色的部分既要发送的数据，高亮的部分即窗口
   3. 窗口内的数据才允许被发送，当应答未到达前，窗口必须停止滑动
   4. 如果1001-2000这个段的数据ack回来了，窗口就可以向前滑动
   5. 接收方也会维护一个窗口，只要落在窗口内的数据才允许被接受
2. 现象分析
   - 黏包
     - 现象：发送abc def，接收abcdef
     - 原因：
       - 应用层:接收方ByteBuf设置太大(netty默认1024)
       - 滑动窗口:假设发送方256Bytes表示一个完整报文，但由于接收方处理不及时且窗口大小足够大，这256bytes字节就会缓冲在接收方的滑动窗口中，当滑动窗口缓冲了多个报文就会黏包
       - Nagle算法:会造成黏包
   - 半包
     - 现象: 发送abcdef 接收abc def
     - 原因: 
       - 应用层:接收方ByteBuf小于实际发送数据量
       - 滑动窗口：假设接收方的窗口只剩了128bytes，发送方的报文大小是256bytes，这时放不下了，只能先发送前128bytes，等待ack后才能发送剩余部分，这就造成了半包
       - MSS限制:当发送的数据超过MSS限制后，会将数据切分发送，就会造成半包
3. 本质是因为TCP是流式协议，消息无边界

### 解决方案

- #### 创建短连接

```java
//短连接使用的就是在每一次发送的完之后就将连接断开，在下一次发送的时候再连接，这样会导致连接消耗的资源很多
//关键代码
Bootstrap bootstrap= new Bootstrap().group(clientEvent).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        /*会在连接建立的时候出发active事件*/
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer(16);
                            buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,});
                            ctx.writeAndFlush(buf);
                        }
                    });
                }
            });
             connect = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
             connect.close();
             clientEvent.shutdownGracefully();
```



- #### 使用netty自带的方法，配置定长截取，需要注意的是需要规定的是最大的数据的大小

```java
//关键代码

                        //设置缓冲区的大小
//                        socketChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(10));
                         socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(10));
```

- #### 使用换行符来进行消息的界定

```java
@Slf4j
public class DelimiterClient {
    public static void main(String[] args) throws InterruptedException {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //打印日志
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                Random r =new Random();
                                ByteBuf buf = ctx.alloc().buffer();
                                String message = "";
                                for (int i = 0; i < 10; i++) {
                                     message +=  createMessage(r.nextInt(256) + 1, i + "");
//                                     buf.writeBytes(message.getBytes());
                                }
                                //如果字符组不是Bytebuf会导致发送不出去
                                buf.writeBytes(message.getBytes());
                                ctx.writeAndFlush(buf);
//                                super.channelActive(ctx);
                            }
                        });
                    }
                }).connect(new InetSocketAddress("127.0.0.1",8080)).sync().channel().read();
    }

    //创建消息发
    public static String createMessage(int leng,String s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leng; i++) {
            sb.append(s);
        }
        sb.append("\n");
        return sb.toString();
    }
}
```



- #### LengthFieldBasedFrameDecoder

  - lengthFieldOffset:记录长度的偏移量
  - lengthFieldLength :  长度本身占用的字节数
  -  lengthAdjustment :长度的调整
  -   initialBytesToStrip : 舍弃的字节位数

```java
public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                /**
                 * 接收的最大数
                 * 记录长度的偏移量,偏移几个字节是长度域
                 * 长度所占用的字节数
                 * 长度的调整
                 * 需要跳过的字节数
                 */
                new LoggingHandler(LogLevel.DEBUG),
                new LengthFieldBasedFrameDecoder(1024,8,4,0,12),
                new LoggingHandler(LogLevel.DEBUG)
        );

        //4个字节的内容长度 实际内容
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        send(buf,"Hello, World");
        send(buf,"Hi");
        channel.writeInbound(buf);
    }

    public static void send(ByteBuf buf,String content){
        byte[] bytes = content.getBytes();
        //如果在消息的长度前面添加了相对应的长度或者数据,就需要使用,lengthFieldOffset调整接收的消息的长度
        buf.writeBytes("pengjing".getBytes());
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
```

- #### 协议的解析与设计

- redis协议

```java
public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入redis服务器地址（回车直接本地服务器：localhost）");
        String hostname = scanner.nextLine();
        if (hostname.equals("")){
            hostname = "localhost";
        }
        int port;
        System.out.println("请输入redis端口(回车直接端口默认：6379)");
        while (true){
            try {
                String str = scanner.nextLine();
                if (str.equals("")){
                    port = 6379;
                }else {
                    port = Integer.parseInt(scanner.nextLine());
                }
                break;
            }catch (NumberFormatException e){
                System.out.println("请输入合法端口:"+e.getMessage());
            }
        }
        NioEventLoopGroup boss = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(boss)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel){
                        Scanner sc = new Scanner(System.in);
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                System.out.println("连接成功，请输入redis的命令指令");
                                String commend = sc.nextLine();
                                sendCommend(ctx, commend.trim());
                            }
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String resp = byteBuf.toString(StandardCharsets.UTF_8);
//                                System.out.println("正常响应："+resp);
                                resp = resp.replace("\r\n","")
                                        .replace("+", "")
                                        .replace("$-1", "");
                                if (resp.contains("$")){
                                    resp = resp.substring(2);
                                }
                                System.out.println(resp);
                                if (resp.equals("-NOAUTH Authentication required.")){
                                    System.out.println("监测到连接该redis没有验证密码，请输入密码验证：");
                                    String password = sc.nextLine();
                                    String verifyCommend = "auth "+password;
                                    sendCommend(ctx, verifyCommend);
                                }else {
                                    String commend = sc.nextLine();
                                    sendCommend(ctx, commend.trim());
                                }
                            }
                        });
                    }
                });
        try {
            ChannelFuture connect = bootstrap.connect(new InetSocketAddress(hostname, port)).sync();
            connect.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
        }
    }

    private static void sendCommend(ChannelHandlerContext ctx, String commend) {
        //        空格+换行
        final byte[] LINE = {13,10};
        if (commend.equals("q")){
            System.out.println("正在退出程序...");
            ctx.close();
        }
        List<String> words = Arrays.asList(commend.split(" "));
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(("*" + words.size()).getBytes(StandardCharsets.UTF_8));
        buffer.writeBytes(LINE);
        for (String word : words) {
//            判断是不是中文汉字，如果是汉字，则每个汉字在utf-8 编码中占三个字节
            if (checkChinese(word)){
//                所以每个汉字的字节数*3
                buffer.writeBytes(("$" + word.length()*3).getBytes(StandardCharsets.UTF_8));
            }else {
                buffer.writeBytes(("$" + word.length()).getBytes(StandardCharsets.UTF_8));
            }
            buffer.writeBytes(LINE);
            buffer.writeBytes(word.getBytes(StandardCharsets.UTF_8));
            buffer.writeBytes(LINE);
        }
        ctx.writeAndFlush(buffer);
    }

    public static boolean checkChinese(String name)
    {
        int n;
        for(int i = 0; i < name.length(); i++) {
            n = name.charAt(i);
            if(!(19968 <= n && n <40869)) {
                return false;
            }
        }
        return true;
    }
}
```

- http协议

```java
@Slf4j
public class TestHttp {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workEvent = new NioEventLoopGroup(2);
        Channel channel =  new ServerBootstrap()
                .group(bossEvent,workEvent)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        //表示http的编解码,既是入栈处理器,也是出站处理器
                        socketChannel.pipeline().addLast(new HttpServerCodec());
                        //在HttpServerCodec的返回中会存在DefaultHttpRequest和EmptyLastHttpContent两个对象,无论是什么请求都会有两个
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
                                //可以使用SimpleChannelInboundHandler方法来接收自己需要的请求,减少if else的出现
                                //可以返回给浏览器数据使用
                                DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
                                //让浏览器知道内容发送完毕,需要给出返回体的长度
                                byte[] bytes = "<h1>hello ,world</h1>".getBytes();
                                defaultFullHttpResponse.headers().setInt(CONTENT_LENGTH,bytes.length);
                                //返回给浏览器的内容
                                defaultFullHttpResponse.content().writeBytes(bytes);

                                channelHandlerContext.writeAndFlush(defaultFullHttpResponse);

                            }
                        });
                        /*socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("{}",msg);
                            }
                        });*/
                    }
                }).bind(8080).sync().channel();

//        channel.close();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossEvent.shutdownGracefully();
                workEvent.shutdownGracefully();
            }
        });
    }
}
```

- #### 自定义协议要素

  - 魔数:用来在第一时间判定是否是无效数据包
  - 版本号:可以支持协议的升级
  - 序列化算法:消息正文到底采用那种序列化反序列化方式，可以由此拓展，例如:json，protobuf，hessian，jdk
  - 指令类型:是登录、注册、单聊、群聊等业务相关
  - 请求序号;为了双工通信，提供异步能力
  - 正文长度
  - 消息正文

- #### 编码

  - 编码对象

    ```java
    @Data
    public abstract class Message implements Serializable {
    
        public static Class<?> getMessageClass(int messageType) {
            return messageClasses.get(messageType);
        }
    
        private int sequenceId;
    
        private int messageType;
    
        public abstract int getMessageType();
    
        public static final int LoginRequestMessage = 0;
    
    
        /**
         * 请求类型 byte 值
         */
        public static final int RPC_MESSAGE_TYPE_REQUEST = 101;
        /**
         * 响应类型 byte 值
         */
        public static final int  RPC_MESSAGE_TYPE_RESPONSE = 102;
    
        private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();
    
        static {
            messageClasses.put(LoginRequestMessage, LoginRequestMessage.class);
            /*messageClasses.put(LoginResponseMessage, LoginResponseMessage.class);
            messageClasses.put(ChatRequestMessage, ChatRequestMessage.class);
            messageClasses.put(ChatResponseMessage, ChatResponseMessage.class);
            messageClasses.put(GroupCreateRequestMessage, GroupCreateRequestMessage.class);
            messageClasses.put(GroupCreateResponseMessage, GroupCreateResponseMessage.class);
            messageClasses.put(GroupJoinRequestMessage, GroupJoinRequestMessage.class);
            messageClasses.put(GroupJoinResponseMessage, GroupJoinResponseMessage.class);
            messageClasses.put(GroupQuitRequestMessage, GroupQuitRequestMessage.class);
            messageClasses.put(GroupQuitResponseMessage, GroupQuitResponseMessage.class);
            messageClasses.put(GroupChatRequestMessage, GroupChatRequestMessage.class);
            messageClasses.put(GroupChatResponseMessage, GroupChatResponseMessage.class);
            messageClasses.put(GroupMembersRequestMessage, GroupMembersRequestMessage.class);
            messageClasses.put(GroupMembersResponseMessage, GroupMembersResponseMessage.class);
            messageClasses.put(RPC_MESSAGE_TYPE_REQUEST, RpcRequestMessage.class);
            messageClasses.put(RPC_MESSAGE_TYPE_RESPONSE, RpcResponseMessage.class);*/
        }
    }
    ```

  - 相关子类

    ```java
    /**
     * 登录使用的对象
     */
    @Data
    @ToString(callSuper = true)
    public class LoginRequestMessage extends Message {
        private String username;
        private String password;
    
        public LoginRequestMessage() {
        }
    
        public LoginRequestMessage(String username, String password) {
            this.username = username;
            this.password = password;
        }
    
        @Override
        public int getMessageType() {
            return LoginRequestMessage;
        }
    }
    ```

  - 编解码对象

    ```java
    /**
     * 1.魔数,用于在第一时间判定数据是否有效
     * 2.版本号,可以支持协议的升级
     * 3.序列化算法,消息正文所采用的序列化和反序列化的方式0:jdk,1:json,2:protoubuf,3:hessian
     * 4.指令类型,是登录,注册,单聊等
     * 5.请求序号,为了双工通信,提供异步能力
     * 6.正文长度
     * 7.消息正文
     */
    @Slf4j
    public class MessageDecodec extends ByteToMessageCodec<Message> {
        //自定义的编码操作,需要的相关数据为,固定的字节数最好是2的整数倍
        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            //1.魔数,java使用的是cafeebabe
            out.writeBytes("pengjing".getBytes());
            //2.版本号
            out.writeByte(1);
            //3.序列化算法
            out.writeByte(0);
            //4.指令类型
            out.writeByte(msg.getMessageType());
            //5.请求序号
            out.writeInt(msg.getSequenceId());
            //无意义,对齐使用
            out.writeByte(0xff);
            //获取对象字节
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(msg);
            //对象
            byte[] bytes = bos.toByteArray();
            //6.正文长度
            out.writeInt(bytes.length);
    
            //消息正文
            out.writeBytes(bytes);
    
    
    
        }
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            Message message = null;
            //1.魔数
            int magic_num = in.readInt();
            //版本号
            byte version = in.readByte();
            //序列化算法
            byte serializerType = in.readByte();
            //指令
            byte  messageType= in.readByte();
            //请求序号
            int sequenceId = in.readInt();
            //无意义数据
            in.readByte();
            //对象长度
            int lenth = in.readInt();
            byte[] bytes = new byte[lenth];
            in.readBytes(bytes,0,lenth);
            //反序列化为对象
            if(serializerType == 0){
                //使用jdk转对象
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                 message =(Message) ois.readObject();
            }
            log.debug("{},{},{},{},{},{}",magic_num,version,serializerType,messageType,sequenceId,lenth);
            log.debug("{}",message);
            out.add(message);
        }
    }
    ```

  - 测试类

    ```java
    public class EmbeddedTestMessage {
        public static void main(String[] args) {
            EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                    new LoggingHandler(),
                    new MessageDecodec()
            );
            LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
            embeddedChannel.writeOutbound(loginRequestMessage);
        }
    }
    ```

  - #### 解码器的使用
  
    - 使用的是入栈的操作，在入栈的时候会进行解码
    - 解码器的使用需要防止因为网络的波动产生半包的问题，需要使用到的是帧解码器
    - 在提取公共的handle的时候，需要注意的是有的handle是不允许再多线程下使用的（@@Sharable表示可以公用）
  
    ```java
    public class EmbeddedTestMessage {
        public static void main(String[] args) throws Exception {
    
            //LengthFieldBasedFrameDecoder handle是不能共用的,在多线程下可能会将不同的数据拼接在一起
            //能否支持多线程使用的handle.添加有@Sharable表示可以公用
            EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                    new LoggingHandler(),
                    //防止半包问题,添加帧解码器
                    new LengthFieldBasedFrameDecoder(1024,16,4,0,0),
                    new MessageDecodec()
            );
    
            //encode
            LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
            embeddedChannel.writeOutbound(loginRequestMessage);
    
            //decode 解码器
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1024);
            new MessageDecodec().encode(null,loginRequestMessage,buffer);
    
            //设置切片
            ByteBuf slice = buffer.slice(0, 100);
            ByteBuf slice_1 = buffer.slice(100, buffer.readableBytes()-100);
            //入栈操作会激活解码器
            embeddedChannel.writeInbound(slice); //会调用release方法,将buff应用计数-1
            buffer.retain(); //将应用计数+1
            embeddedChannel.writeInbound(slice_1);
    
        }
    }
    ```
  
  - ####  @Shable的使用
  
    ```java
    @Slf4j
    @ChannelHandler.Sharable
    /**
     * 必须和帧解码器一起使用:LengthFieldBasedFrameDecoder ,确保接收的bytebuf的消息是完整的
     */
    public class MessageDecodecSharble extends MessageToMessageCodec<ByteBuf,Message> {
    
        //设置魔数
        private static  final  byte[] magic_num = "pengjing".getBytes();
    
        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
            ByteBuf out = ctx.alloc().buffer();
            //1.魔数,java使用的是cafeebabe
            out.writeBytes(magic_num);
            //2.版本号
            out.writeByte(1);
            //3.序列化算法
            out.writeByte(0);
            //4.指令类型
            out.writeByte(msg.getMessageType());
            //5.请求序号
            out.writeInt(msg.getSequenceId());
            //无意义,对齐使用
            out.writeByte(0xff);
            //获取对象字节
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(msg);
            //对象
            byte[] bytes = bos.toByteArray();
            //6.正文长度
            out.writeInt(bytes.length);
    
            //消息正文
            out.writeBytes(bytes);
    
            outList.add(out);
        }
    
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    
            Message message = null;
            byte[] bytes_magic = new byte[magic_num.length];
            //1.魔数
            in.readBytes(bytes_magic, 0, magic_num.length);
    
            ByteBuf buffer =  ByteBufAllocator.DEFAULT.buffer(bytes_magic.length);
            buffer.writeBytes(bytes_magic);
            //版本号
            byte version = in.readByte();
            //序列化算法
            byte serializerType = in.readByte();
            //指令
            byte  messageType= in.readByte();
            //请求序号
            int sequenceId = in.readInt();
            //无意义数据
            in.readByte();
            //对象长度
            int lenth = in.readInt();
            byte[] bytes = new byte[lenth];
            in.readBytes(bytes,0,lenth);
            //反序列化为对象
            if(serializerType == 0){
                //使用jdk转对象
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                message =(Message) ois.readObject();
            }
            log.debug("魔数是:{},版本号:{},序列化算法:{},:请求序号:{},{},对象长度:{}", StandardCharsets.UTF_8.decode(buffer.nioBuffer()),version,serializerType,messageType,sequenceId,lenth);
            log.debug("{}",message);
            out.add(message);
        }
    }
    ```
  
  - #### 连接假死
  
    - 原因
      - 网络设备出现故障，例如网卡，机房等，底层的TCP链接已经断开，但应用程序没有感知到，任然占用着资源
      - 公网网络不稳定，出现丢包，如果连续出现丢包，这时现象就是客户端数据发不出去，服务端也一直收不到数据，就这么一直耗着
      - 应用程序线程阻塞，无法进行数据读写
    - 问题
      - 假死的连接占用资源不能释放
      - 向假死的连接发送数据，得到的反馈是发送超时
  
    ```java
    //使用空闲检测器 new IdleStateHandler() 用来判断是不是读空闲时间过长或者写空闲时间过长
    、/表示超过5S没有读到消息就会触发一个连接超时的事件
                        socketChannel.pipeline().addLast(new IdleStateHandler(5,0,0));
                        //既可以做为入栈处理器,也可以作为出栈处理器
                        socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                //响应用户的特殊事件
    //                            super.userEventTriggered(ctx, evt);
                                //IdleState#READER_IDLE
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.READER_IDLE) {
                                    System.out.println("超过了5S没有读到数据");
                                    ctx.close();
                                }
                            }
                        });
    ```
  
    https://www.bilibili.com/video/BV1py4y1E7oA/?p=118&spm_id_from=pageDriver&vd_source=000766059912952028e3af1ddb9f2463

# 优化与源码

1. ## 优化

   1. ### 扩展序列化算法

      - 序列化，反序列化主要用在消息正文的转换上

        - 序列化时，需要讲java对象变为要传输的数据（可以是byte[]或json等，最终都需要变成byte[]）
        - 反序列化需要将传入的数据还原成java对象，便于处理

      - 目前的代码仅支持java自带的序列化，反序列化机制，核心代码如下

      - ```java
        //反序列化
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        message =(Message) ois.readObject();
        
        //序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        //对象
        byte[] bytes = bos.toByteArray();
        //6.正文长度
        out.writeInt(bytes.length);
        
        //消息正文
        out.writeBytes(bytes);
        ```

        为了支持序列化算法

        抽象出一个接口用户实现序列化

        ```java
        public interface Serial {
        
            //序列化
            <T> byte[] decode(T object);
        
            //反序列化
              <T> T encode(Class<T> clazz, byte[] bytes);
        
             enum decodec implements Serial{
                 //使用jdk自带的序列化
                 Java{
                     @Override
                     public <T> byte[] decode(T object) {
                         try {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(object);
                             return bos.toByteArray();
                         } catch (IOException e) {
                             throw new RuntimeException("序列化失败");
                         }
                     }
        
                     @Override
                     public <T> T encode(Class<T> clazz, byte[] bytes) {
                         try {
                             ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                             ObjectInputStream ois = new ObjectInputStream(bis);
                             return (T)ois.readObject();
                         } catch (Exception e) {
                             throw new RuntimeException("反序列化失败");
                         }
                     }
                 },
                 Json{
                     @Override
                     public <T> byte[] decode(T object) {
        
                         try {
                             Gson gson = new Gson();
                             return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
                         } catch (Exception e) {
                             throw new RuntimeException("序列化失败");
                         }
                     }
        
                     @Override
                     public <T> T encode(Class<T> clazz, byte[] bytes) {
                         try {
                             Gson gson = new Gson();
                             String json = new String(bytes, StandardCharsets.UTF_8);
                             return gson.fromJson(json,clazz);
                         } catch (JsonSyntaxException e) {
                             throw new RuntimeException("反序列化失败");
                         }
                     }
                 }
             }
        }
        ```
        
        优化代码
        
        ```java
        @Slf4j
        @ChannelHandler.Sharable
        /**
         * 必须和帧解码器一起使用:LengthFieldBasedFrameDecoder ,确保接收的bytebuf的消息是完整的
         */
        public class MessageDecodecSharble extends MessageToMessageCodec<ByteBuf, Message> {
        
            //设置魔数
            private static  final  byte[] magic_num = "P".getBytes();
        
            @Override
            protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
                ByteBuf out = ctx.alloc().buffer();
                //1.魔数,java使用的是cafeebabe
                out.writeBytes(magic_num);
                //2.版本号
                out.writeByte(1);
                //3.序列化算法
                out.writeByte(Config.getSerialDecodec().ordinal());
                //4.指令类型
                out.writeInt(msg.getMessageType());
                //5.请求序号
                out.writeInt(msg.getSequenceId());
                //无意义,对齐使用
                out.writeByte(0xff);
                //获取对象字节
                /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(msg);
                //对象
                byte[] bytes = bos.toByteArray();*/
                byte[] bytes = Serial.decodec.values()[Config.getSerialDecodec().ordinal()].decode(msg);
                //6.正文长度
                out.writeInt(bytes.length);
        
                //消息正文
                out.writeBytes(bytes);
        
                outList.add(out);
            }
        
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        
                Message message = null;
                byte[] bytes_magic = new byte[magic_num.length];
                //1.魔数
                in.readBytes(bytes_magic, 0, magic_num.length);
        
                ByteBuf buffer =  ByteBufAllocator.DEFAULT.buffer(bytes_magic.length);
                buffer.writeBytes(bytes_magic);
                //版本号
                byte version = in.readByte();
                //序列化算法
                byte serializerType = in.readByte();
                //指令
                int  messageType= in.readInt();
                //请求序号
                int sequenceId = in.readInt();
                //无意义数据
                in.readByte();
                //对象长度
                int lenth = in.readInt();
                byte[] bytes = new byte[lenth];
                in.readBytes(bytes,0,lenth);
                //反序列化为对象
                /*if(serializerType == 0){
                    //使用jdk转对象
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    message =(Message) ois.readObject();
                }*/
        
                Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        
                message = Serial.decodec.values()[serializerType].encode(messageClass, bytes);
                log.debug("魔数是:{},版本号:{},序列化算法:{},:请求序号:{},{},对象长度:{}", StandardCharsets.UTF_8.decode(buffer.nioBuffer()),version,serializerType,messageType,sequenceId,lenth);
                log.debug("{}",message);
                out.add(message);
            }
        }
        ```

### 优化

##### 参数调优

##### CONNECT_TIMEOUT_MILLIS

- 用在客户端建立连接时，如果在指定毫秒内无法连接，会抛出timeout异常

- SO_TIMEOUT只要用在阻塞io，阻塞IO中accept，read等都是无限等待的，如果不希望永远阻塞，使用它调整超时时间

- 超时时间的设置java连接超时时间为2S

- 连接超时的本质上是一个定时任务，在到达连接的超时时间就会报一个连接超时的异常，其中的future的对象和连接使用的超时对象是一个对象

  ```java
  @Slf4j
  public class ConnectTomeOut {
      public static void main(String[] args) {
          try {
              Bootstrap bootstrap = new Bootstrap();
              bootstrap.group(new NioEventLoopGroup());
              bootstrap.channel(NioSocketChannel.class);
              bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000);
              bootstrap.handler(new LoggingHandler());
              ChannelFuture future = bootstrap.connect(new InetSocketAddress("124.221.132.142", 8081));
              future.sync().channel().closeFuture().sync();
          } catch (InterruptedException e) {
              e.printStackTrace();
              log.debug(e.getMessage());
          }
      }
  }
  ```

- #### 源码

  ```java
  //connectPromise,两个线程之间数据交互
  if (connectTimeoutMillis > 0) {
                          connectTimeoutFuture = eventLoop().schedule(new Runnable() {
                              @Override
                              public void run() {
                                  ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
                                  if (connectPromise != null && !connectPromise.isDone()
                                          && connectPromise.tryFailure(new ConnectTimeoutException(
                                                  "connection timed out: " + remoteAddress))) {
                                      close(voidPromise());
                                  }
                              }
                          }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
                      }
  ```

  

##### :bulb:option和childOption的差别

1. 在ServerBootstrap()中它的option()，是给ServerSocketChannel配置参数
2. 在ServerBootstrap()中它的childOption()，是给SocketChannel配置参数
3. 在BootStrap()他的option(),是给SockrtChannel配置参数	

##### SO_BACKLOG

- 属于ServerSocketChannel参数

##### 三次握手

- 首先是数据准备阶段
  - client端：在数据准备好之后开始connect
  - server端：配置好bind，listen数据
  - syns queue： 半连接队列
  - accept queue：全连接队列
  - 第一次握手，client发送SYN到server，状态修改为SYN_SEND，server收到，状态修改为SYN_REVD，并将该请求放入sync_queue队列
  - 第二次握手，server回复SYN+ACK给client，client收到，状态修改为ESTABLISHED，并发送ACK给server
  - 第三次握手，server收到ACK，状态修改为ESTABLISHED，将该请求从sync queue放入accept queue

:bulb:在相关设置

- 在linux2.2之前，backlog大小包括两个队列的大小，在2.2之后，分别用下面两个参数来控制
- sync queue --半连接队列
  - 大小通过/proc/sys/net/ipv4/tcp_max_syn_backlog指定，在syncookies启动的情况下，逻辑上没有最大值限制，这个设置便被忽略
- accept queue--全连接队列
  - 其大小通过/proc/sys/net/core/somaxconn指定，在使用函数listen时，内核会根据传入的backlog参数与系统参数，取二者的较小值
  - 如果accept queue队列满了，server将会发送一个拒绝连接的错误信息到client
- netty通过ChannelOption.SO_BACKLOG来设置大小

```java
public class BlockLogServer {
    public static void main(String[] args) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(new NioEventLoopGroup());
            serverBootstrap.option(ChannelOption.SO_BACKLOG,3);//设置连接为两个
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new LoggingHandler());
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            channelFuture.sync().channel().read();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

##### Ulinit-n:限制一个进程同时打开的文件描述符的数据量 

- 属于操作系统参数

##### TCP_NODELAY:是否开启nagle算法 false开启 ，true关闭，建议关闭

- nagle算法:让数据尽可能多的一起发送，会导致数据延迟送达

- 属于SocketChannel参数

##### SO_SNDBUF&SO_RCVBUF:参数不建议调整

- SO_SNDBUF属于SocketChannel参数
- SO_RCVBUF即可用于SocketChannel参数，也可以用于ServerSocketChannel参数（建议设置到ServerSocketChannel）

##### ALLOCATOR:分配器

- 属于SocketChannel参数
- 用来分配ByteBuf，ctx.alloc();

```java
//设置系统参数，需要知道怎么找到相关参数，并修改
-Dio.netty.allocator.type=unpooled  
-Dio.netty.noPreferDirect=true
```

##### RCVBUF_ALLOCATOR:

- 属于SocketChannel参数
- 负责入栈数据的分配，决定入栈缓冲区的大小(并可以动态调整)，廷议采用direct直接内存，具体池化还是还是非池化由allocator决定

![image-20230830224319839](image/image-20230830224319839.png)

- 设置了ALLOCATOR参数之后，得到的消息还是存在直接内存中：是为了在通信的过程中提高效率
- 在不设置初始值的时候系统会给与它初始值最大不会超过65532，如果不超过默认的1024的话会动态的减少，最少得话是64

##### RCVBUF_ALLOCATOR VS ALLOCATOR

- RCVBUF_ALLOCATOR可以动态的设置缓冲区的大小，并且规定使用的不会是堆内存而是直接内存
- 池化还是非池化是由ALLOCATOR控制的
- 二者共同协作完成对bytBbuf的初始化

## RPC框架

#### 在原来的聊天项目的基础上新增Rpc请求和响应的消息

```java
@Data
public abstract class Message implements Serializable {

    /**
     * 根据消息类型字节，获得对应的消息 class
     * @param messageType 消息类型字节
     * @return 消息 class
     */
    public static Class<? extends Message> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    private int sequenceId;

    private int messageType;

    public abstract int getMessageType();

    public static final int LoginRequestMessage = 0;
    public static final int LoginResponseMessage = 1;
    public static final int ChatRequestMessage = 2;
    public static final int ChatResponseMessage = 3;
    public static final int GroupCreateRequestMessage = 4;
    public static final int GroupCreateResponseMessage = 5;
    public static final int GroupJoinRequestMessage = 6;
    public static final int GroupJoinResponseMessage = 7;
    public static final int GroupQuitRequestMessage = 8;
    public static final int GroupQuitResponseMessage = 9;
    public static final int GroupChatRequestMessage = 10;
    public static final int GroupChatResponseMessage = 11;
    public static final int GroupMembersRequestMessage = 12;
    public static final int GroupMembersResponseMessage = 13;
    public static final int PingMessage = 14;
    public static final int PongMessage = 15;
    /**
     * 请求类型 byte 值
     */
    public static final int RPC_MESSAGE_TYPE_REQUEST = 101;
    /**
     * 响应类型 byte 值
     */
    public static final int  RPC_MESSAGE_TYPE_RESPONSE = 102;

    public static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(LoginRequestMessage, LoginRequestMessage.class);
        messageClasses.put(LoginResponseMessage, LoginResponseMessage.class);
        messageClasses.put(ChatRequestMessage, ChatRequestMessage.class);
        messageClasses.put(ChatResponseMessage, ChatResponseMessage.class);
        messageClasses.put(GroupCreateRequestMessage, GroupCreateRequestMessage.class);
        messageClasses.put(GroupCreateResponseMessage, GroupCreateResponseMessage.class);
        messageClasses.put(GroupJoinRequestMessage, GroupJoinRequestMessage.class);
        messageClasses.put(GroupJoinResponseMessage, GroupJoinResponseMessage.class);
        messageClasses.put(GroupQuitRequestMessage, GroupQuitRequestMessage.class);
        messageClasses.put(GroupQuitResponseMessage, GroupQuitResponseMessage.class);
        messageClasses.put(GroupChatRequestMessage, GroupChatRequestMessage.class);
        messageClasses.put(GroupChatResponseMessage, GroupChatResponseMessage.class);
        messageClasses.put(GroupMembersRequestMessage, GroupMembersRequestMessage.class);
        messageClasses.put(GroupMembersResponseMessage, GroupMembersResponseMessage.class);
        messageClasses.put(PingMessage, PingMessage.class);
        messageClasses.put(PongMessage, PongMessage.class);
        messageClasses.put(RPC_MESSAGE_TYPE_REQUEST, RpcRequestMessage.class);
        messageClasses.put(RPC_MESSAGE_TYPE_RESPONSE, RpcResponseMessage.class);
    }

}
```

#### 请求消息

```java
@Getter
@ToString(callSuper = true)
public class RpcRequestMessage extends Message {

    /**
     * 调用的接口全限定名，服务端根据它找到实现
     */
    private String interfaceName;
    /**
     * 调用接口中的方法名
     */
    private String methodName;
    /**
     * 方法返回类型
     */
    private Class<?> returnType;
    /**
     * 方法参数类型数组
     */
    private Class[] parameterTypes;
    /**
     * 方法参数值数组
     */
    private Object[] parameterValue;

    public RpcRequestMessage(int sequenceId, String interfaceName, String methodName, Class<?> returnType, Class[] parameterTypes, Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
```

#### 响应消息

```java
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
```

上面实现的是服务器端和客户端的通信,需要实现简单rpc的框架需要用到知识:

- 类的动态代理
- jdk中的反射
- 锁的使用

#### rpc连接端的实现

```java
public static void main(String[] args) {
        HelloService helloService = getProxyService(HelloService.class);
        System.out.println(helloService.sayHello("zhangsan"));
        System.out.println(helloService.sayHello("lisi"));
        System.out.println(helloService.sayHello("wangwu"));
    }

    public static <T> T getProxyService(Class<T> serviceClass){
        //使用代理的方式发送数据
        ClassLoader classloader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        //1.将方法调用转化为消息对象
        Object o = Proxy.newProxyInstance(classloader, interfaces, (proxy, method, args) -> {

            int sequenceId = SequenceIdGenerator.nextId();
            //创建一个获取序列号的方式
            RpcRequestMessage msg = new RpcRequestMessage(sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);

            //将消息发送
            createChannel().writeAndFlush(msg);
            DefaultPromise promise = new DefaultPromise(createChannel().eventLoop());
            RpcResponseMessageHandler.promises.put(sequenceId,promise);

            //等待结果的返回,无论有没有都会返回并且不会报异常,sync会抛异常
            promise.await();
            //对象的返回使用的是一个promise方式
            if(promise.isSuccess()){
                System.out.println(".........................");
                return promise.getNow();
            }else {
                throw new RuntimeException(promise.cause());
            }
//            return null;
        });
        return (T) o;
    }

    //创建一个代理类,实现远程调用的接口

    private static Channel channel = null;
    //添加一个锁
    private static  final  Object LOCK = new Object();
    //获取channel方法
    public static Channel createChannel() {
        if(channel != null){
            return channel;
        }
        synchronized(LOCK){
            if(channel != null){
                return  channel;
            }else {
                 initChannel();
                 return channel;
            }
        }
    }

    private static void initChannel() {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        //日志打印
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        //消息的编解码
        MessageDecodecSharble MESSAGE_HANDLER = new MessageDecodecSharble();
        //rpc请求处理
        RpcResponseMessageHandler RPC_RESPONSE = new RpcResponseMessageHandler();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //解决消息黏包半包
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                //打印日志
                ch.pipeline().addLast(LOGGING_HANDLER);
                //消息编解码
                ch.pipeline().addLast(MESSAGE_HANDLER);
                //处理rpc消息
                ch.pipeline().addLast(RPC_RESPONSE);
            }
        });
        try {
             channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            channel.closeFuture().addListener(future->{
                eventExecutors.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("client error",e.getMessage());
        }
    }
```

#### RPC回复消息处理器

```java
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //创建一个map用于存储请求方发送的消息
    //保证线程的安全性
    public static final Map<Integer, Promise<Object>> promises = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.debug("{}",msg);

        //根据返回的对象填充promise对象
        //获取promise
        Promise<Object> promise = promises.get(msg.getSequenceId());

        if(promise != null){
            //成功的对象
            Object returnValue = msg.getReturnValue();
            //异常对象
            Exception exception = msg.getExceptionValue();
            if(exception != null){
                promise.setFailure(exception);
            }else {
                promise.setSuccess(returnValue);
            }
        };
    }
}
```

- 将客户端的连接封装成一个方法,在这个方法里面可以将空的channel赋值为带有连接信息的channel对象
- 创建一个获取channel对象的方法,为了防止在多线程中重复获取channel对象(**==在创建channel的方法里面使用双重锁,在一开始的时候判断一次公用的channel对象是否为null,然后在添加锁的代码中在添加一个是否为null的判断==**)
- 对于消息的发送,为了不破坏调用者的习惯,使用的方式
  - 首先获取一个对象
    - 对象的获取使用的是动态代理的方式,然后在这个动态代理的方法实现中,将消息发送出去
    - 对于消息的返回使用的是一个泛型为promise的map对象,在发送消息之前将带有这个线程的执行器创建一个空的promise
    - 然后使用promise的await()方法等待结果的返回(如果使用的是sync同步器,那么在出现异常的时候会抛出异常,但是await()不会)
  - 使用获取的对象调用方法
- 在相应处理器上的做修改
  - 创建一个带有sequenceId和promise的map对象(这个对象全局可见,可在多线程上使用)
  - 直接根据服务端的返回对象获取所携带的sequenceId,在map中获取promise对象
  - 如果对象存在,然后就是在服务器端的对象获取ReturnValue和ExceptionValue
  - 根据得到的两个对象放入promise中的不同字段中(setSuccess或者setFailure)

#### rpc服务端的实现

```java
@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        //创建两个工作对象
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workerEvent = new NioEventLoopGroup(2);
        //创建日志
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        //创建消息处理
        MessageDecodecSharble MESSAGE_CODEC = new MessageDecodecSharble();
        //rpc消息处理
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEvent,workerEvent);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //黏包半包处理
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //打印日志
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    //消息的编解码
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    //使用rpc请求
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });

            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error",e.getMessage());
//            e.printStackTrace();
        }finally {
            bossEvent.shutdownGracefully();
            workerEvent.shutdownGracefully();
        }
    }
}
```

#### 服务器端的请求消息处理

```java
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage rpcRequestMessage) throws Exception {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        try {
            //使用反射的方法获取请求的对象和方法
            HelloService service = (HelloService)ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            //根据class获取方法名
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(service, rpcRequestMessage.getParameterValue());
            rpcResponseMessage.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponseMessage.setExceptionValue(e);
        }
        rpcResponseMessage.setSequenceId(rpcRequestMessage.getSequenceId());
        ctx.writeAndFlush(rpcResponseMessage);
    }
  }
```

- 服务器端的处理和普通的处理差不多,建立连接,消息处理
- 在消息的处理上使用的是反射的方式
- 根据全限定类型,方法名,方法返回值,方法参数等使用反射来获取请求的结果
- 最后将得到的对象通过ctx写入到通道中发送给客户端

#### Gson自定义序列化类型

- ==**Gson的序列化中没有包含Class类型,所以需要添加**==

```java
public static class ClassTypeAdapater implements JsonSerializer<Class>, JsonDeserializer<Class>{

        //反序列化
        @Override
        public Class deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                String asString = jsonElement.getAsString();
                return Class.forName(asString);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        //序列化
        @Override
        public JsonElement serialize(Class aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
//            return null;
        }
    }


//调用方式,
Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Config.ClassTypeAdapater()).create();
```

https://www.bilibili.com/video/BV1py4y1E7oA/?p=136&spm_id_from=pageDriver&vd_source=15cac809b169713f965c1032f507b775

# Netty源码分析

