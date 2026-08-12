package dec.core.context.runtime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** P2 封闭的运行时事实值。只允许七种不可变值域，禁止携带任意 live Object。 */
public final class RuntimeFactValue {
    public enum Kind { NULL, BOOL, INTEGER, DECIMAL, STRING, LIST, OBJECT }
    private final Kind kind; private final Object value;
    private RuntimeFactValue(Kind kind,Object value){this.kind=Objects.requireNonNull(kind,"kind");this.value=value;}
    public static RuntimeFactValue nullValue(){return new RuntimeFactValue(Kind.NULL,null);} public static RuntimeFactValue boolValue(boolean value){return new RuntimeFactValue(Kind.BOOL,Boolean.valueOf(value));} public static RuntimeFactValue integerValue(long value){return new RuntimeFactValue(Kind.INTEGER,Long.valueOf(value));}
    public static RuntimeFactValue decimalValue(BigDecimal value){return new RuntimeFactValue(Kind.DECIMAL,normalizeDecimal(value));}
    public static RuntimeFactValue stringValue(String value){return new RuntimeFactValue(Kind.STRING,Objects.requireNonNull(value,"value"));}
    public static RuntimeFactValue listValue(List<RuntimeFactValue> values){Objects.requireNonNull(values,"values");List<RuntimeFactValue> copy=new ArrayList<RuntimeFactValue>(values.size());for(RuntimeFactValue v:values)copy.add(Objects.requireNonNull(v,"values contains null"));return new RuntimeFactValue(Kind.LIST,Collections.unmodifiableList(copy));}
    public static RuntimeFactValue objectValue(Map<String,RuntimeFactValue> values){Objects.requireNonNull(values,"values");TreeMap<String,RuntimeFactValue> sorted=new TreeMap<String,RuntimeFactValue>();for(Map.Entry<String,RuntimeFactValue> e:values.entrySet()){sorted.put(requireKey(e.getKey()),Objects.requireNonNull(e.getValue(),"values contains null"));}return new RuntimeFactValue(Kind.OBJECT,Collections.unmodifiableMap(new LinkedHashMap<String,RuntimeFactValue>(sorted)));}
    public Kind kind(){return kind;}
    @SuppressWarnings("unchecked") public List<RuntimeFactValue> listValue(){requireKind(Kind.LIST);return (List<RuntimeFactValue>)value;}
    @SuppressWarnings("unchecked") public Map<String,RuntimeFactValue> objectValue(){requireKind(Kind.OBJECT);return (Map<String,RuntimeFactValue>)value;}
    public String canonicalForm(){switch(kind){case NULL:return "null";case BOOL:return ((Boolean)value).booleanValue()?"true":"false";case INTEGER:return String.valueOf(value);case DECIMAL:return ((BigDecimal)value).toPlainString();case STRING:return quote((String)value);case LIST:return canonicalList(listValue());case OBJECT:return canonicalObject(objectValue());default:throw new IllegalStateException("unsupported kind: "+kind);}}
    @Override
    public boolean equals(Object other){if(this==other)return true;if(!(other instanceof RuntimeFactValue))return false;RuntimeFactValue that=(RuntimeFactValue)other;return kind==that.kind&&Objects.equals(value,that.value);}
    @Override
    public int hashCode(){return Objects.hash(kind,value);}
    @Override
    public String toString(){return canonicalForm();}
    private void requireKind(Kind expected){if(kind!=expected)throw new IllegalStateException("expected "+expected+" but was "+kind);}
    private static BigDecimal normalizeDecimal(BigDecimal value){Objects.requireNonNull(value,"value");BigDecimal normalized=value.stripTrailingZeros();return normalized.scale()<0?normalized.setScale(0):normalized;}
    private static String requireKey(String value){Objects.requireNonNull(value,"key");if(value.isEmpty())throw new IllegalArgumentException("object key must not be empty");return value;}
    private static String canonicalList(List<RuntimeFactValue> values){StringBuilder r=new StringBuilder("[");for(int i=0;i<values.size();i++){if(i>0)r.append(',');r.append(values.get(i).canonicalForm());}return r.append(']').toString();}
    private static String canonicalObject(Map<String,RuntimeFactValue> values){StringBuilder r=new StringBuilder("{");int i=0;for(Map.Entry<String,RuntimeFactValue> e:values.entrySet()){if(i++>0)r.append(',');r.append(quote(e.getKey())).append(':').append(e.getValue().canonicalForm());}return r.append('}').toString();}
    private static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
}
