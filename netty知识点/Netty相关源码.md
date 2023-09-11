# Netty相关源码

## Netty启动流程

### nio的处理

```java
//1.netty中使用NioEventLoopGroup(简称nio boss线程) 用来封装线程和selector
Selector selector = Selector.opne();
//2 创建NioServerSocketChannel,同时会初始化它关联的handler,以及为原生ssc存储config
NioServerSocketChannel attachment = new NioServerSocketChannel();
//3 创建NioServerSocketChannel时,创建了java原生的ServerSocketChannel
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.configureBlocking(false);

//4 启动nio boss线程执行接下来的操作

//5 注册(仅关联selector和NioServerSocketChannel),未关注事件
SelectionKey selectionKey = serverSocketChannel.register(selector,0,attachment);

//6 head-->初始化容器-->ServerBootstrapAcceptor-->tail,初始化器是一次性的,只为添加acceptor

//7 绑定端口
serverSocketChannel.bind(new InetSocketAddress(8080));

//8 触发channel active事件,在head中关注op_accept事件
selectionkey.interesOps(Selectionkey.OP_ACCEPT);
```



1. init & register regFuture 处理
   1. init mian
      1. 创建NioServerSocketChannel  ==**main线程**==
      2. 添加NioServerSocketChannel 初始化handler  ==**main**==
         1. 初始化handler等待调用
   2. register
      1. 启动nio boss线程 ==**main线程**==
      2. 原生ssc注册至selector未关注事件 ==**nio-thread**==
      3. 执行NioServerSocketChannel 初始化 handler ==**nio-thread**==
2. regFuture等待回调 doBind0  ==**nio-thread**==
   1. 原生ServerSocketChannel 绑定   ==**nio-thread**==
   2. 出发NioServerSocketChannel active事件   ==**nio-thread**==  
