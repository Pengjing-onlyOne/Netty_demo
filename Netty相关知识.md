# NIO基础

non-blocking io 非阻塞IO

## 三大组件

## Channel&Buffer

channel有一点雷速与stream，它就是读写数据的双向通道，可以从channel将数据读入buffer，也可以将buffer的数据写入channel，而之前的stream要么是输入，要么输出，channel比stream更为底层

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

视频进度:https://www.bilibili.com/video/BV1py4y1E7oA/?p=24&spm_id_from=pageDriver&vd_source=000766059912952028e3af1ddb9f2463

# Netty入门学习

# Netty进阶学习

# Netty常见参数学习以及优化

# Netty源码分析