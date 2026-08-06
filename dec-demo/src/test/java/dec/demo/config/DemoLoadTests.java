package dec.demo.config;

import dec.core.context.data.BaseData;
import dec.core.model.utils.DataUtil;
import dec.demo.support.DemoMySqlTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * 保留 dev_all DemoLoadTests 的数据构造语义，并用测试专用夹具替代已退役的全局 Starter 工具。
 */
public abstract class DemoLoadTests {
    protected DemoMySqlTestSupport mysql;

    @BeforeEach
    void initializeModelRuntime() throws Exception {
        mysql = DemoMySqlTestSupport.load("model/orm-config.xml");
    }

    @AfterEach
    void closeModelRuntime() {
        if (mysql != null) {
            mysql.close();
        }
    }

    protected BaseData createProductData(String name, int count, double price) throws Exception {
        BaseData productData = DataUtil.createBaseData("product");
        productData.setValue("name", name);
        productData.setValue("count", count);
        productData.setValue("price", price);
        return productData;
    }

    protected BaseData createUserData(String name, String password) throws Exception {
        BaseData userData = DataUtil.createBaseData("user");
        userData.setValue("name", name);
        userData.setValue("password", password);
        return userData;
    }
}
