package dec.core.context.runtime;

import dec.core.context.model.AccessOperation;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** TESTDESIGN-P2-R32 DEV-04 exact value-oracle tests. */
class P2ContextNeutralValueExactTest {
    @Test
    void runtimeFactValueHasExactlyFrozenR32DomainAndDeepImmutability(){assertEquals(Arrays.asList("NULL","BOOL","INTEGER","DECIMAL","STRING","LIST","OBJECT"),enumNames(RuntimeFactValue.Kind.values()));List<RuntimeFactValue> list=new ArrayList<RuntimeFactValue>();list.add(RuntimeFactValue.integerValue(7));Map<String,RuntimeFactValue> object=new LinkedHashMap<String,RuntimeFactValue>();object.put("z",RuntimeFactValue.listValue(list));object.put("a",RuntimeFactValue.decimalValue(new BigDecimal("1.2300")));RuntimeFactValue frozen=RuntimeFactValue.objectValue(object);list.add(RuntimeFactValue.integerValue(8));object.clear();assertEquals("{\"a\":1.23,\"z\":[7]}",frozen.canonicalForm());assertThrows(UnsupportedOperationException.class,()->frozen.objectValue().put("x",RuntimeFactValue.nullValue()));assertThrows(UnsupportedOperationException.class,()->frozen.objectValue().get("z").listValue().add(RuntimeFactValue.nullValue()));}
    @Test
    void targetPathAndAccessRuleKeepEveryIdentityDimensionExact(){TargetKey target=TargetKey.of(new ViewKey("Order"));ModelPath path=ModelPath.of("user.authInfo");ModelAccessRuleKey read=ModelAccessRuleKey.of(new SystemKey("Trade"),target,path,AccessOperation.READ);ModelAccessRuleKey write=ModelAccessRuleKey.of(new SystemKey("Trade"),target,path,AccessOperation.WRITE);assertEquals(Arrays.asList("user","authInfo"),path.segments());assertNotEquals(read,write);assertNotEquals(TargetKey.of(new ViewKey("Order")),TargetKey.of(new ViewKey("order")));assertThrows(IllegalArgumentException.class,()->ModelPath.of("user.*"));assertThrows(IllegalArgumentException.class,()->ModelPath.of(" user"));}
    @Test
    void opaqueIdsRejectBlankAndRemainCaseSensitive(){assertEquals(RuntimeObjectId.of("A-01"),RuntimeObjectId.of("A-01"));assertNotEquals(RuntimeObjectId.of("A-01"),RuntimeObjectId.of("a-01"));assertThrows(IllegalArgumentException.class,()->RuntimeObjectId.of(" "));assertThrows(IllegalArgumentException.class,()->RuntimeExecutionFrameId.of(" x "));}
    private static List<String> enumNames(Enum<?>[] values){List<String> result=new ArrayList<String>();for(Enum<?> value:values)result.add(value.name());return result;}
}
