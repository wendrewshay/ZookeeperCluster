package com.xwq.zk.recipes.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
 * @ClassName: ZNodeName   
 * @Description: 一个临时节点名称，该节点有序列号且可排序 
 * @author: XiaWenQiang
 * @date: 2017年7月21日 上午10:22:58   
 *      
 */
public class ZNodeName implements Comparable<ZNodeName>{

	private static final Logger LOG = LoggerFactory.getLogger(ZNodeName.class);
	
	private final String name;  //节点名称
	private String prefix;      //节点名称前缀
	private int sequence = -1;  //节点名称序列号
	
	public ZNodeName(String name) {
		if(name == null) {
			throw new NullPointerException("id cannot be null");
		}
		this.name = name;
		this.prefix = name;
		int idx = name.lastIndexOf("-");
		if(idx >= 0) {
			this.prefix = name.substring(0, idx);
			try {
				this.sequence = Integer.parseInt(name.substring(idx + 1));
			} catch (NumberFormatException e) {
				LOG.info("Number format exception for " + idx, e);
			}
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode() + 37;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || getClass() != obj.getClass()) return false;
		
		ZNodeName sequence = (ZNodeName) obj;
		if(!name.equals(sequence.name)) return false;
		return true;
	}

	@Override
	public String toString() {
		return name.toString();
	}

	/**   
	 * @Title: compareTo  
	 * @Description:   
	 * @param o
	 * @return   
	 * @see java.lang.Comparable#compareTo(java.lang.Object)   
	 */
	@Override
	public int compareTo(ZNodeName o) {
		int answer = this.prefix.compareTo(o.prefix);
		if(answer == 0) {
			int s1 = this.sequence;
			int s2 = o.sequence;
			if(s1 == -1 && s2 == -1) {
				return this.name.compareTo(o.name);
			}
			answer = s1 == -1 ? 1 : s2 == -1 ? -1 : s1 - s2; 
		}
		return answer;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public int getSequence() {
		return sequence;
	}
	
}
