import com.unicom.channel.TcpChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
/**
 * Created by unicom on 2017/11/9.
 */
public class NettyServer {
   // private static final Logger logger = Logger.getLogger(NettyServer.class);
    private static final String IP = "127.0.0.1";
    private static final int PORT = 9999;
    /**用于分配处理业务线程的线程组个数 */
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2;	//默认
    /** 业务出现线程大小*/
    protected static final int BIZTHREADSIZE = 1000;
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);
    protected static void run(){
        //设置启动辅助类
        ServerBootstrap b=new ServerBootstrap();
        b.group(bossGroup,workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childOption(ChannelOption.RCVBUF_ALLOCATOR,
                new AdaptiveRecvByteBufAllocator(64, 2048, 65536));
        b.childHandler(new childChannelHandler());
        //阻塞等待服务器完全启动
        ChannelFuture channelFuture;
        try {
            channelFuture = b.bind(PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

    static class childChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // TODO Auto-generated method stub
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
            pipeline.addLast(workerGroup,new TcpChannelHandler());
        }
    }
    public static void main(String[] args){
       // logger.info("开始启动TCP服务器...");
        NettyServer.run();
//	TcpServer.shutdown();
    }
}
