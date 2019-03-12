package org.termite.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.termite.http.Request;

public class TermiteServer {
	// 线程池
	ExecutorService pool = Executors.newFixedThreadPool(50);
	// 通道管理器
	private Selector selector;

	/**
	 * 启动服务端测试
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TermiteServer server = new TermiteServer();
		server.initServer(8000);
		server.listen();
	}

	/**
	 * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
	 * 
	 * @param port 绑定的端口号
	 * @throws IOException
	 */
	public void initServer(int port) throws IOException {
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务端启动成功！");
	}

	/**
	 * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void listen() throws IOException {
		
		// 轮询访问selector
		while (true) {
			// 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
			selector.select();
			// 获得selector中选中的项的迭代器，选中的项为注册的事件
			Iterator ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理
				ite.remove();
				// 客户端请求连接事件
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					// 获得和客户端连接的通道
					SocketChannel channel = server.accept();
					// 设置成非阻塞
					channel.configureBlocking(false);
					// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
					channel.register(this.selector, SelectionKey.OP_READ);
					// 获得了可读的事件
				} else if (key.isReadable()) {
					// 取消可读触发标记
					key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
					// 加入线程池
					pool.execute(new ThreadHandlerChannel(key));
				}
			}
		}
	}
}

/**
 * 用多线程处理读取客户端发来的信息的事件
 * 
 * @param SelectionKey
 * @throws IOException
 */
class ThreadHandlerChannel extends Thread {
	private SelectionKey key;

	ThreadHandlerChannel(SelectionKey key) {
		this.key = key;
	}

	@Override
	public void run() {
		// 服务器可读取消息:得到事件发生的Socket通道
		
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		// 把读取到的缓冲区内容存入到流内，到最后一次性取出来，建议定义自定义协议先传内容长度然后再传内容，就可以根据长度建立对应长度缓冲区然后读取数据
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int size = 0;
			while ((size = channel.read(buffer)) > 0) {
				buffer.flip();
				baos.write(buffer.array(), 0, size);
				buffer.clear();
			}
			baos.close();
			if(baos.size()!=0) {
				Request req = new Request(baos.toString());}
//			System.out.println("服务器当前处理线程：" + Thread.currentThread().getName() + " 服务端收到信息：" + baos.toString());
			
			byte[] content = baos.toByteArray();
			ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
			writeBuf.put(content);
			writeBuf.flip();
			channel.write(writeBuf);// 将消息回送给客户端
			if (size == -1) {
				// System.out.println("客户端断开。。。");
				channel.close();
			} else {
				// 没有可用字节,继续监听OP_READ
				key.interestOps(key.interestOps() | SelectionKey.OP_READ);
				key.selector().wakeup();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
