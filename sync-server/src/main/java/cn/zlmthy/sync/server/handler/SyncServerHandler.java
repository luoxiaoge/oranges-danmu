package cn.zlmthy.sync.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;

/**
 * @author zengliming
 * @date 2019/5/26
 * @since 1.0.0
 */
@Log4j2
public class SyncServerHandler extends SimpleChannelInboundHandler<String> {

    public static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        String user = channel.remoteAddress().toString();
        log.info("收到推送[{}]", msg);
        channelGroup.forEach(ch->{
            if(ch == channel){
                ch.writeAndFlush("[myself]:"+msg+"\n");
                return;
            }
            ch.writeAndFlush("["+user+"]:" + msg+"\n");
        });
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.forEach(ch -> {
            ch.writeAndFlush("用户[" + channel.remoteAddress() + "]加入，当前在线"+(channelGroup.size()+1)+"人\n");
        });
        channelGroup.add(channel);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("用户["+ctx.channel().remoteAddress()+"]上线");
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("用户["+ctx.channel().remoteAddress()+"]下线");
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelGroup.forEach(ch->{
            ch.writeAndFlush("用户["+ ctx.channel().remoteAddress()+"]离开，当前在线"+channelGroup.size()+"人\n");
        });
    }
}
