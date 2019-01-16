package lain.mods.skins.impl;

public class ConfigOptions
{

    public boolean useCrafatar;
    public boolean useCustomServer;
    public String hostCustomServer;

    /**
     * @return self with all options revert to default.
     */
    public ConfigOptions defaultOptions()
    {
        useCrafatar = true;
        useCustomServer = false;
        hostCustomServer = "http://example.com";
        return this;
    }

    /**
     * @return true if changed.
     */
    public boolean validate()
    {
        boolean any = false;

        if (hostCustomServer == null)
        {
            hostCustomServer = "http://example.com";
            any = true;
        }

        return any;
    }

}
