package com.lamfire.hydra;


public class IdentitySnake extends Snake {
	private MessageMapper mapper;
	
	public IdentitySnake(MessageMapper mapper, String host, int port) {
		super(host, port);
		this.mapper = mapper;
	}

	@Override
	protected void handleMessage(MessageContext context, Message message) {
		int id = message.getId();
		final Action action = mapper.getAction(id);
		if (action == null) {
			return;
		}
		action.execute(context,message);
	}

}
