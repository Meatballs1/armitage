package msf;

import java.io.*;
import java.util.*;

public class RpcAsync implements RpcConnection, Async {
	protected RpcQueue queue;
	protected RpcConnection connection;
	protected boolean connected = true;

	public boolean isConnected() {
		return connected;
	}

	public RpcAsync(RpcConnection connection) {
		this.connection = connection;
	}

	public void disconnect() {
		connected = false;
	}

	public void execute_async(String methodName) {
		execute_async(methodName, new Object[]{}, null);
	}

	public void execute_async(String methodName, Object[] args) {
		execute_async(methodName, args, null);
	}

	public void execute_async(String methodName, Object[] args, RpcCallback callback) {
		if (queue == null) {
			queue = new RpcQueue(connection);
		}
		queue.execute(methodName, args, callback);
	}

	public Object execute(String methodName) throws IOException {
		return connection.execute(methodName);
	}

	protected Map cache = new HashMap();

	public Object execute(String methodName, Object[] params) throws IOException {
		if (methodName.equals("module.info") || methodName.equals("module.options") || methodName.equals("module.compatible_payloads")) {
			StringBuilder keysb = new StringBuilder(methodName);

			for(int i = 0; i < params.length; i++)
				keysb.append(params[i].toString());

			String key = keysb.toString();
			Object result = cache.get(key);

			if(result != null) {
				return result;
			}

			result = connection.execute(methodName, params);
			cache.put(key, result);
			return result;
		}
		else {
			return connection.execute(methodName, params);
		}
	}
}
