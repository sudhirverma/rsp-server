package org.jboss.tools.ssp.server.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.api.beans.SSPAttributes;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.launching.LaunchingCore;
import org.jboss.tools.ssp.server.model.internal.Server;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;

public class ServerModel {
	private HashMap<String, IServerType> factories;
	private HashMap<String, IServer> servers;
	private HashMap<String, IServerDelegate> serverDelegates;
	private Set<Class> approvedAttributeTypes;
	
	public ServerModel() {
		factories = new HashMap<String, IServerType>();
		servers = new HashMap<String, IServer>();
		serverDelegates = new HashMap<String, IServerDelegate>();
		
		
		// Server attributes must be one of the following types
		approvedAttributeTypes = new HashSet<Class>();
		approvedAttributeTypes.add(Integer.class);
		approvedAttributeTypes.add(Boolean.class);
		approvedAttributeTypes.add(String.class);
		// List must be List<String>
		approvedAttributeTypes.add(List.class);
		// Map must be Map<String, String>
		approvedAttributeTypes.add(Map.class);
		
		// TODO load / save of servers?
	}
	
	public void addServerFactory(IServerType fact) {
		if( fact != null && fact.getServerTypeId() != null ) {
			factories.put(fact.getServerTypeId(), fact);
		}
	}
	
	public void removeServerFactory(IServerType fact) {
		if( fact != null && fact.getServerTypeId() != null ) {
			factories.remove(fact.getServerTypeId());
		}
	}
	
	public IStatus createServer(String serverType, String id, Map<String, Object> attributes) {
		IServerType fact = factories.get(serverType);
		if( fact != null ) {
			IStatus valid = validateAttributes(fact, attributes);
			if( !valid.isOK()) {
				return valid;
			}
			IServer server = createServer2(serverType, id, attributes);
			IServerDelegate del = fact.createServerDelegate(server);

			valid = del.validate();
			if( !valid.isOK()) {
				return valid;
			}
			addServer(server, del);
			return Status.OK_STATUS;
		} else {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server", "Server Type " + serverType + " not found");
		}
	}
	
	private IStatus validateAttributes(IServerType type, Map<String, Object> attrs) {
		SSPAttributes a = type.getRequiredAttributes();
		Set<String> required = a.listAttributes();
		for( String attrKey : required ) {
			if( attrs.get(attrKey) == null ) {
				return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server", "Attribute " + attrKey + " must not be null");
			}
			Object v = attrs.get(attrKey);
			Class actual = v.getClass();
			Class expected = a.getAttributeType(attrKey);
			if( !actual.equals(expected)) {
				// Something's different than expectations based on json transfer
				// Try to convert it
				Object converted = convertJSonTransfer(v, expected);
				if( converted == null ) {
					return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server", 
							"Attribute " + attrKey + " must be of type " + expected.getName() 
							+ " but is of type " + actual.getName());
				} else {
					attrs.put(attrKey, converted);
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	
	private Object convertJSonTransfer(Object value, Class expected) {
		// TODO check more things here for errors in the transfer
		if( Integer.class.equals(expected) && Double.class.equals(value.getClass())) {
			return new Integer(((Double)value).intValue());
		}
		return null;
	}
	
	private Server createServer2(String serverType, String id, Map<String, Object> attributes) {
		File data = LaunchingCore.getDataLocation();
		File servers = new File(data, "servers");
		if( !servers.exists()) {
			servers.mkdirs();
		}
		// TODO check for duplicates
		File thisServer = new File(servers, id);
		Server s = new Server(thisServer, serverType);
		s.setAttribute("id", id);
		
		Set<String> keys = attributes.keySet();
		for( String k : keys) {
			Object val = attributes.get(k);
			if( val instanceof Integer) {
				s.setAttribute(k, ((Integer)val).intValue());
			} else if( val instanceof Boolean) {
				s.setAttribute(k, ((Boolean)val).booleanValue());
			} else if( val instanceof String ) {
				s.setAttribute(k, (String)val);
			} else if( val instanceof List) {
				s.setAttribute(k, (List)val);
			} else if( val instanceof Map) {
				s.setAttribute(k, (Map)val);
			}
		}
		
		
		
		return s;
	}
	

	private void addServer(IServer server, IServerDelegate del) {
		servers.put(server.getId(), server);
		serverDelegates.put(server.getId(), del);
		// TODO fire events?
	}
	public void removeServer(String serverId) {
		servers.remove(serverId);
		serverDelegates.remove(serverId);
		// TODO fire events?
	}
	
	public ServerHandle[] getServerHandles() {
		Set<String> s = servers.keySet();
		ArrayList<ServerHandle> handles = new ArrayList<>();
		for( String s1 : s ) {
			String id = s1;
			String type = servers.get(id).getTypeId();
			handles.add(new ServerHandle(id,  type));
		}
		return (ServerHandle[]) handles.toArray(new ServerHandle[handles.size()]);
	}
	
	public String[] getServerTypes() {
		Set<String> types = factories.keySet();
		return (String[]) types.toArray(new String[types.size()]);
	}
	
	public SSPAttributes getRequiredAttributes(String type) {
		IServerType t = factories.get(type);
		SSPAttributes ret = t == null ? null : t.getRequiredAttributes();
		return validateAttributes(ret, type);
	}
	
	private SSPAttributes validateAttributes(SSPAttributes ret, String serverType) {
		if( ret != null ) {
			Set<String> all = ret.listAttributes();
			for( String all1 : all ) {
				Class attrType = ret.getAttributeType(all1);
				if( !approvedAttributeTypes.contains(attrType)) {
					LaunchingCore.log("Extension for servertype " + serverType + " is invalid and requires an attribute of an invalid class.");
				}
			}
		}
		return ret;
	}
	
	public SSPAttributes getOptionalAttributes(String type) {
		IServerType t = factories.get(type);
		SSPAttributes ret = t == null ? null : t.getOptionalAttributes();
		return validateAttributes(ret, type);
	}
}
