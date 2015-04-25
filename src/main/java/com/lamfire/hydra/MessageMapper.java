package com.lamfire.hydra;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * 消息处理器映射
 * @author lamfire
 *
 */
public class MessageMapper{

	private final Map<Integer,Action> maps = new ConcurrentHashMap<Integer,Action>();

	public void registAction(int id, Action action) {
		maps.put(id, action);
	}

	public Action getAction(int id) {
		return maps.get(id);
	}

}
