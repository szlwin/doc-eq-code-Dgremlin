package test.business;

import java.math.BigDecimal;

public class Order {

	private Long id;

	private String productName;
	
	private BigDecimal amount;

	private OrderOne orderOne;

	private Integer status;

	public OrderOne getOrderOne() {
		return orderOne;
	}

	public void setOrderOne(OrderOne orderOne) {
		this.orderOne = orderOne;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
