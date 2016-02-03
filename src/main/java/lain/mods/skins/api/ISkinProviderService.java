package lain.mods.skins.api;

public interface ISkinProviderService extends ISkinProvider
{

    void clear();

    void register(ISkinProvider provider);

}
