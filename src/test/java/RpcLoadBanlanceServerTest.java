import com.zzy.rpc.server.LoadBalanceNettyServer;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcLoadBanlanceServerTest.java
 * @Description TODO
 * @createTime 2022年04月19日 18:03:00
 */
public class RpcLoadBanlanceServerTest {
    public static void main(String[] args) throws Exception {

        LoadBalanceNettyServer loadBalanceNettyServer = new LoadBalanceNettyServer();
        loadBalanceNettyServer.start(8890);

    }
}
