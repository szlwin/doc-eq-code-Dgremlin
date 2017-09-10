package test.common;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXid;


public class TestMysqlXA {
	
	public static void main(String[] args) throws Exception {
		test();
	}
	public static void test() throws Exception {
		
		Connection conn1 = null;
		Connection conn2 = null;
		MysqlXADataSource suspXaDs = new MysqlXADataSource();
		suspXaDs.setUrl("jdbc:mysql://127.0.0.1:3306/orm-test?useUnicode=true&amp;characterEncoding=utf8");
		//suspXaDs.setPinGlobalTxToPhysicalConnection(true);
		//suspXaDs.setRollbackOnPooledClose(true);
		
		XAConnection xaConn1 = null;
		XAConnection xaConn2 = null;
		Xid xid = createXid();
		Xid xid1 = createXid();
		Statement stmt = null;
		try {
			
			xaConn1 = suspXaDs.getXAConnection("root", "1234");
			xaConn2 = suspXaDs.getXAConnection("root", "1234");
			XAResource xaRes1 = xaConn1.getXAResource();
			conn1 = xaConn1.getConnection();
			
			
			XAResource xaRes2 = xaConn1.getXAResource();
			conn2 = xaConn2.getConnection();
			conn2.setAutoCommit(true);
			//stmt = conn1.createStatement();
			
			
			xaRes1.start(xid, XAResource.TMNOFLAGS);
			conn1.createStatement().executeUpdate("update orderinfo set o_count=50 where o_count=100");
			//conn1.createStatement().executeQuery("SELECT 1");
			xaRes1.end(xid, XAResource.TMSUCCESS);
			xaRes1.commit(xid, true);
			//xaRes1.prepare(xid);
			//conn1.createStatement().executeUpdate("update orderinfo set o_count=200 where o_count=100");
			
			//xaRes2.start(xid, XAResource.TMNOFLAGS);
			//conn1.createStatement().executeUpdate("update orderinfo set o_count=200 where o_count=100");
			//xaRes2.end(xid, XAResource.TMSUCCESS);
			//xaRes2.commit(xid, true);
			
			
			xaRes1.start(xid1, XAResource.TMJOIN);
			conn1.createStatement().executeUpdate("update orderinfo set o_count=50 where o_count=90");
			//conn1.createStatement().executeQuery("SELECT 1");
			xaRes1.end(xid1, XAResource.TMSUCCESS);
			
			
			xaRes1.commit(xid1, true);
			xaConn1.close();
			
			
		} finally {
			if (xaConn1 != null) {
				xaConn1.close();
			}
		}
	}

	private static Xid createXid() throws IOException {
		ByteArrayOutputStream gtridOut = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(gtridOut);
		new UID().write(dataOut);
		
		final byte[] gtrid = gtridOut.toByteArray();
		
		ByteArrayOutputStream bqualOut = new ByteArrayOutputStream();
		dataOut = new DataOutputStream(bqualOut);
		
		new UID().write(dataOut);
		
		final byte[] bqual = bqualOut.toByteArray();
		
		Xid xid = new MysqlXid(gtrid, bqual, 3306);
		return xid;
	}

	private Xid createXid(Xid xidToBranch) throws IOException {
		ByteArrayOutputStream bqualOut = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bqualOut);
		
		new UID().write(dataOut);
		
		final byte[] bqual = bqualOut.toByteArray();
		
		Xid xid = new MysqlXid(xidToBranch.getGlobalTransactionId(), bqual, 3306);
		
		return xid;
	}
}
