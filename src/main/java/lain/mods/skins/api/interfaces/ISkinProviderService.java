package lain.mods.skins.api.interfaces;

public interface ISkinProviderService extends ISkinProvider {

    /**
     * Clears all registered providers.
     */
    void clearProviders();

    /**
     * @param provider the provider to register.
     * @return true if successful.
     */
    boolean registerProvider(ISkinProvider provider);

}
