package dec.demo.directory;


import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.data.ModelData;
import dec.core.directory.container.DirectoryContainer;
import dec.core.model.utils.DataUtil;
import dec.demo.directory.dom.UserData;
import santr.common.context.LexerUtil;
import santr.v4.parser.ExpressParser;
import santr.view.parser.TreeViewer;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("mysql-it")
public class DirectoryTest {
    @Test
    void legacyDirectoryScenario() throws Exception {
        main(new String[0]);
    }

    public static void main(String args[]) throws Exception {
        init();
        testFind();
        /*LexerUtil.load("mySqlExpr", DirectoryTest.class
                .getClassLoader().getResourceAsStream("ExprCSQL.ls"));
        ExpressParser expressParser = new ExpressParser();
        expressParser.parser("mySqlExpr","select * from user where status=1  or status=2");
        TreeViewer treeViewer = new TreeViewer(expressParser.getTree());
        treeViewer.open();*/
    }

    public static void init() throws Exception {
        ConfigInit.init();
    }

    public static void testFind() throws DataNotDefineException {
        ModelData userInfo = DataUtil.createViewData("UserInfo");
        DirectoryContainer directoryContainer = new DirectoryContainer(userInfo);
        List<UserData> userDataList = directoryContainer.find("user").start("register").end("auth").invoke()
                .getFindData(UserData.class);
        System.out.println(userDataList.size()+":"+userDataList.get(0).getName());
    }
}
