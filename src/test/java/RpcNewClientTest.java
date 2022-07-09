import com.zzy.rpc.client.RpcNettyDistributeClient;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcNewClientTest.java
 * @Description TODO
 * @createTime 2022年04月20日 09:56:00
 */
public class RpcNewClientTest {
    public static void main(String[] args) throws InterruptedException {
        RpcNettyDistributeClient rpcClient = new RpcNettyDistributeClient("127.0.0.1", 8890);
        rpcClient.connect();
        rpcClient.heartBeat();
    }
}
