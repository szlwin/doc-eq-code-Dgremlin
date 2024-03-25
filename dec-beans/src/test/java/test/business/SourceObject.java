package test.business;

import java.math.BigDecimal;
import java.util.Date;

public class SourceObject {
    public Long id = 1l;

    public String name = "test";

    public Date date = new Date();

    //private byte aByte=2;

    public Integer m_id=3;

    //private BigDecimal amount = BigDecimal.valueOf(10000);

    public char test = 'r';

    public boolean isExist = true;

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

    //public byte getaByte() {
      //  return aByte;
    //}

   // public void setaByte(byte aByte) {
   //     this.aByte = aByte;
   // }

    public Integer getM_id() {
        return m_id;
    }

    public void setM_id(Integer m_id) {
        this.m_id = m_id;
    }

    //public BigDecimal getAmount() {
    //    return amount;
   // }

   // public void setAmount(BigDecimal amount) {
    //    this.amount = amount;
    //}

    public char getTest() {
        return test;
    }

    public void setTest(char test) {
        this.test = test;
    }

    public boolean getExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }
}
