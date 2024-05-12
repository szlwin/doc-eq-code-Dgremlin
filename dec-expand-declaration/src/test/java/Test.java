import java.util.Arrays;


import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.executer.conume.Conumer;
import dec.expand.declare.service.DefaultServiceDeclare;

public class Test {

	public static void main(String[] args) {
		test();

	}

	public static void test(){
		
		Conumer<String> conumer = new Conumer<String>();
		
		conumer.setName("s-data-strings");
		
		
		conumer.setFun((str)->{
			System.out.println("wee:"+str);
			
			return ExecuteResult.success();
		});
		
		DefaultServiceDeclare serviceDeclareManager = new DefaultServiceDeclare();
		
		serviceDeclareManager
			.addEntity("data-string", "test")
			.addEntitys("data-stringArray",  Arrays.asList(new String[]{"a","b","c"}))
			.subscribe("s-data-string",(str)->{
				System.out.println(str.get("data-string"));
				
				return new ExecuteResult();
			})
			.subscribe("s-data-stringArr", (strs)->{
				

				System.out.println(strs.get("data-stringArray"));

				
				return new ExecuteResult();
			}).subscribe("s-data-strings", (strs)->{
				
				String str = (String) strs.get("data-string");
				System.out.println(str);

				
				return ExecuteResult.success();
			}).consume("s-data-strings", (str)->{
				System.out.println("one:"+str.get("s-data-strings"));
				
				return ExecuteResult.success();
			}).consume("s-data-strings", String.class, (str)->{
				
				System.out.println("last:"+str);
				
				return ExecuteResult.success();
			}).consume(conumer)
			.execute();
	}
}
