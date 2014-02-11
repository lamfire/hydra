package com.lamfire.hydra.net;

import java.nio.ByteBuffer;

import com.lamfire.logger.Logger;




public class SessionUtils {
	static final Logger LOGGER = Logger.getLogger(SessionUtils.class);
	
	static void onClosed(Context context,SessionEventListener l ,Session s){
		if(l != null && s != null){
			l.onClosed(context,s);
			s.clearAttributes();
		}
	}

	static void onConnected(Context context,SessionEventListener l ,Session s){
		if(l != null && s != null){
			l.onConnected(context,s);
		}
	}

	static void onDisconnected(Context context,SessionEventListener l ,Session s){
		if(l != null && s != null){
			l.onDisconnected(context,s);
		}
	}

	static void onOpen(Context context,SessionEventListener l ,Session s){
		if(l != null && s != null){
			l.onOpen(context,s);
		}
	}
	
	static void onHeatbeat(Context context,SessionEventListener l ,Session s,HeatbeatType heatbeat){
		if(l != null && s != null){
			l.onHeatbeat(context,s,heatbeat);
		}
	}

	static void onExceptionCaught(Context context,SessionEventListener l ,Session s,Throwable t){
		if(l != null && s != null){
			l.onExceptionCaught(context,s,t);
		}
	}
	
	static void onMessageReceived(Context context,MessageHandler handler ,Session s,ByteBuffer buffer){
		if(handler != null && s != null){
			handler.onMessageReceived(context,s,buffer);
		}
	}
}
