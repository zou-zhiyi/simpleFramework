import com.alibaba.fastjson.JSONObject;
import com.zzy.utils.MeFileUtils;

import java.io.IOException;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MeFileTEst.java
 * @Description TODO
 * @createTime 2022年04月14日 10:57:00
 */
public class MeFileTest {
    public static void main(String[] args) throws IOException {
        String filepath = "test.json";
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("test",1);
        MeFileUtils.wirteJSONObject(filepath, jsonObject);
    }
}
