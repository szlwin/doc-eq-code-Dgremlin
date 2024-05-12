package dec.demo.bean.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SourceObject {
    private Long id = 1l;

    private String name = "test";

    private Date date = new Date();

    private byte aByte=2;

    private Integer m_id=3;

    private BigDecimal amount = BigDecimal.valueOf(10000);

    private char test = 'r';

    public boolean isExist = true;

    private SourceObject sourceObject;

    private Map<String,Object> sourceObjectMap;
    public List<SourceObject> getSourceObjects() {
        return sourceObjects;
    }

    public void setSourceObjects(List<SourceObject> sourceObjects) {
        this.sourceObjects = sourceObjects;
    }

    private List<SourceObject> sourceObjects;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public byte getaByte() {
       return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public Integer getM_id() {
        return m_id;
    }

    public void setM_id(Integer m_id) {
        this.m_id = m_id;
    }

    public BigDecimal getAmount() {
        return amount;
   }

   public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public char getTest() {
        return test;
    }

    public void setTest(char test) {
        this.test = test;
    }

    public boolean getIsExist() {
        return isExist;
    }

    public void setIsExist(boolean exist) {
        isExist = exist;
    }

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }

    public SourceObject getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(SourceObject sourceObject) {
        this.sourceObject = sourceObject;
    }
}
