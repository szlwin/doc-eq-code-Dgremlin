package com.orm.model.execute.tran.advance.back;

import java.io.Serializable;

public class TranQueueNode implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9020934668881941186L;

	private TranNode tranNode;
	
	private TranQueueNode next;
	
	private TranQueueNode pre;

	public TranQueueNode getNext() {
		return next;
	}

	public void setNext(TranQueueNode next) {
		this.next = next;
	}

	public TranQueueNode getPre() {
		return pre;
	}

	public void setPre(TranQueueNode pre) {
		this.pre = pre;
	}

	public TranNode getTranNode() {
		return tranNode;
	}

	public void setTranNode(TranNode tranNode) {
		this.tranNode = tranNode;
	}
	
	
}
