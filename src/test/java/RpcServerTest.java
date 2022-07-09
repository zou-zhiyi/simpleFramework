import com.zzy.rpc.server.SimpleRPCNettyServer;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcServerTest.java
 * @Description TODO
 * @createTime 2022年04月16日 23:07:00
 */
public class RpcServerTest {
    public static void main(String[] args) throws Exception {
        SimpleRPCNettyServer simpleRPCNettyServer = new SimpleRPCNettyServer();
        simpleRPCNettyServer.start(8888);

    }
}
