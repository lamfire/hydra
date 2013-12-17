package com.lamfire.hydra.net;

import java.nio.ByteBuffer;

public interface MessageHandler {

	public void onMessageReceived(Context context,Session session,ByteBuffer buffer);
}
