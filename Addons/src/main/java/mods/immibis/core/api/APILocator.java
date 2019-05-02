package mods.immibis.core.api;

import mods.immibis.core.api.net.INetworkingManager;

/**
 * Contains methods to get instances of various API interfaces.
 * 
 * Mods using any APIs should specify "after:ImmibisCore" in their dependency list, so that
 * if the mod includes an outdated API then the newer one will take precedence.
 * 
 * @deprecated TODO: figure out the best replacement for this.
 */
@Deprecated
public final class APILocator {
	
	private static class Ref<T> {public T v; public boolean initialized;}
	
	@SuppressWarnings("unchecked")
	private static <T> T getField(String name, Ref<T> ref) {
		if(ref.initialized)
			return ref.v;
		try {
			ref.v = (T)Class.forName("mods.immibis.core.ImmibisCore").getField(name).get(null);
		} catch(ClassNotFoundException e) {
			ref.v = null;
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw (AssertionError)new AssertionError("Should not happen").initCause(e);
		}
		ref.initialized = true;
		return ref.v;
	}
	
	private static Ref<INetworkingManager> r_net_manager = new Ref<>();

	
	/**
	 * Returns the networking manager interface, used to send and listen
	 * for packets.
	 * 
	 * Null if Immibis Core is not installed.
	 */
	public static INetworkingManager getNetManager() {
		return getField("networkingManager", r_net_manager);
	}
}
