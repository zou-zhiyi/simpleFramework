import com.alibaba.fastjson.JSONObject;
import com.zzy.core.ServiceCoreComponent;
import com.zzy.utils.MeFileUtils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ServerTest.java
 * @Description TODO
 * @createTime 2022年02月13日 19:23:00
 */
public class ServerCoreTest {
    public static void main(String[] args) throws Exception {
        ServiceCoreComponent instance = ServiceCoreComponent.getInstance();
        instance.serverStart();
    }
}
