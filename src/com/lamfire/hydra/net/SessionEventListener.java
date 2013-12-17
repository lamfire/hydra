package com.lamfire.hydra.net;



public interface SessionEventListener {

	public void onClosed(Context context,Session session);

	public void onConnected(Context context,Session session);

	public void onDisconnected(Context context,Session session);

	public void onOpen(Context context,Session session);
	
	public void onHeatbeat(Context context,Session session,HeatbeatType heatbeat);

	public void onExceptionCaught(Context context,Session session,Throwable throwable);
	
}
