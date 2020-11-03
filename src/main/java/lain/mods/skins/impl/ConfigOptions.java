package lain.mods.skins.impl;

public class ConfigOptions {

    public boolean useMojang;
    public boolean useCrafatar;
    public boolean useCustomServer;
    public String hostCustomServer;
    public boolean useCustomServer2;
    public String hostCustomServer2Skin;
    public String hostCustomServer2Cape;

    /**
     * @return self with all options revert to default.
     */
    public ConfigOptions defaultOptions() {
        useMojang = true;
        useCrafatar = true;
        useCustomServer = false;
        hostCustomServer = "http://example.com";
        useCustomServer2 = false;
        hostCustomServer2Skin = "http://example.com/skins/%auto%";
        hostCustomServer2Cape = "http://example.com/capes/%auto%";
        return this;
    }

    /**
     * @return true if changed.
     */
    public boolean validate() {
        boolean any = false;

        if (hostCustomServer == null) {
            hostCustomServer = "http://example.com";
            any = true;
        }
        if (hostCustomServer2Skin == null) {
            hostCustomServer2Skin = "http://example.com/skins/%auto%";
            any = true;
        }
        if (hostCustomServer2Cape == null) {
            hostCustomServer2Cape = "http://example.com/capes/%auto%";
            any = true;
        }

        return any;
    }

}
