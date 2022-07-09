import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzy.core.CoreManagerImpl;
import com.zzy.core.bean.StatusRequest;
import com.zzy.core.interfaces.BeanController;
import com.zzy.core.interfaces.CoreManager;
import com.zzy.http.request.interfaces.RequestHandler;
import com.zzy.rpc.client.ProxyFactory;
import com.zzy.rpc.server.loadbalance.ClusterBeanController;
import org.objectweb.asm.ClassVisitor;
import com.zzy.rpc.client.RpcNettyClient;
import com.zzy.rpc.test.HelloService;
import com.zzy.rpc.test.HelloServiceImpl;

import java.util.List;
import java.util.Map;

import static com.zzy.http.utils.HttpConstances.SIMPLE_PARAMETERS;
import static com.zzy.http.utils.HttpConstances.URI;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcClientTest.java
 * @Description TODO
 * @createTime 2022年04月16日 23:08:00
 */
public class RpcClientTest {
    public static void main(String[] args) throws Exception {
//        CoreManager coreManager = ProxyFactory.create(CoreManager.class, CoreManagerImpl.class);
//        StatusRequest statusRequest = new StatusRequest();
//        statusRequest.setIsRoot(true);
//        statusRequest.setComponentStr("");
//        coreManager.getComponentStatus(true,"");

//        CoreManager coreManager = ProxyFactory.create(CoreManager.class, CoreManagerImpl.class.getName());
//        List<String> beanList = coreManager.getBeanList();
//        System.out.println(JSON.toJSONString(beanList));
        HelloService helloService = ProxyFactory.create(HelloService.class, HelloServiceImpl.class.getName(),
                "127.0.0.1", 8892);
        helloService.hello("test hello");

//        BeanController beanController = ProxyFactory.create(BeanController.class, ClusterBeanController.class.getName(),
//                "127.0.0.1", 8891);
//        List<String> beanList = beanController.getBeanList();
//        System.out.println(JSONObject.toJSONString(beanList));

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(URI, "GET:/core/getStatus");
//        jsonObject.put(SIMPLE_PARAMETERS, new JSONObject());
//        RequestHandler requestHandler = ProxyFactory.create(RequestHandler.class, "com.nzzy.http.MyCoreMultiRequestHandler");
//        requestHandler.doRequest(jsonObject);
    }
}
