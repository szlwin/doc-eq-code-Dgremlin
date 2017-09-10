package com.orm.model.execute.tran.advance.back;

import java.io.Serializable;

public class TranQueue implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -46027894690075216L;

	private TranQueueNode first = new TranQueueNode();
	
	private TranQueueNode end = new TranQueueNode();

	private int size;
	
	private Object lock = new Object();
	
	public TranQueue(){
		first.setNext(end);
		end.setPre(first);
	}
	
	public void add(TranQueueNode node){
		synchronized(lock){
			
			TranQueueNode preNode = end.getPre();
			
			preNode.setNext(node);
			node.setPre(preNode);
			
			end.setPre(node);
			node.setNext(end);
			size++;
		}
	}
	
	public TranQueueNode get(){
		TranQueueNode currentNode = first.getNext();
		if(currentNode != end){
			first.setNext(currentNode.getNext());
			currentNode.getNext().setPre(first);
			size--;
			return currentNode;
		}else{
			return end;
		}
	}

	public TranQueueNode getEnd(){
		return end;
	}
	
	public int getSize() {
		return size;
	}
	
	
}
