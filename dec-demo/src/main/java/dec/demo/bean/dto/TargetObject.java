package dec.demo.bean.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class TargetObject {
    private Long id;

    private String name;

    private Date date;

    private byte aByte;

    private Integer m_id;

    private BigDecimal amount;

    private char test;

    private boolean isExist;

    private TargetObject targetObject;

    private List<TargetObject> targetObjects;

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

    public TargetObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(TargetObject targetObject) {
        this.targetObject = targetObject;
    }

    public List<TargetObject> getTargetObjects() {
        return targetObjects;
    }

    public void setTargetObjects(List<TargetObject> targetObjects) {
        this.targetObjects = targetObjects;
    }
}
