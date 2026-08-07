package dec.demo.directory;

import dec.core.context.data.ModelData;
import dec.core.directory.container.DirectoryContainer;
import dec.core.model.utils.DataUtil;
import dec.demo.directory.dom.UserData;
import dec.demo.support.DemoMySqlTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 基于 dev_all DirectoryTest，验证目录状态范围会转换为 SQL 并查询真实 MySQL 数据。
 */
@Tag("mysql-it")
public class DirectoryTest {

    @Test
    void directoryRangeReturnsOnlyRegisterAndAuthUsers() throws Exception {
        try (DemoMySqlTestSupport mysql =
                DemoMySqlTestSupport.load("directory/orm-config.xml")) {
            seedUsers(mysql);

            ModelData userInfo = DataUtil.createViewData("UserInfo");
            List<UserData> users = new DirectoryContainer(userInfo)
                    .find("user")
                    .start("register")
                    .end("auth")
                    .invoke()
                    .getFindData(UserData.class);

            Set<String> names = new HashSet<String>();
            for (UserData user : users) {
                names.add(user.getName());
            }
            assertEquals(2, users.size());
            assertEquals(new HashSet<String>(java.util.Arrays.asList("register-user", "auth-user")), names);

            mysql.recordExecution("DirectoryTest.directoryRangeReturnsOnlyRegisterAndAuthUsers");
        }
    }

    private void seedUsers(DemoMySqlTestSupport mysql) throws Exception {
        try (Connection connection = mysql.primaryConnection();
                Statement clear = connection.createStatement();
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO user_info(u_name, u_status, u_password) VALUES (?, ?, ?)")) {
            clear.executeUpdate("DELETE FROM user_info");
            insertUser(insert, "register-user", 1);
            insertUser(insert, "auth-user", 2);
            insertUser(insert, "outside-range-user", 3);
        }
    }

    private void insertUser(PreparedStatement insert, String name, int status) throws Exception {
        insert.setString(1, name);
        insert.setInt(2, status);
        insert.setString(3, "directory-password");
        insert.executeUpdate();
    }
}
