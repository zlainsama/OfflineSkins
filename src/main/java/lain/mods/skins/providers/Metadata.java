package lain.mods.skins.providers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Metadata
{

    protected String etag;
    protected long validtime;

    public String getEtag()
    {
        return etag == null ? "" : etag;
    }

    public long getValidtime()
    {
        return validtime;
    }

    public boolean hasEtag()
    {
        return etag != null && !etag.isEmpty();
    }

    public boolean isValid()
    {
        return validtime > 0 && System.currentTimeMillis() < validtime;
    }

    public void readFromFile(File file) throws IOException
    {
        DataInputStream s = null;
        try
        {
            s = new DataInputStream(new FileInputStream(file));
            setEtag(s.readUTF());
            setValidtime(s.readLong());
        }
        finally
        {
            if (s != null)
                s.close();
        }
    }

    public void setEtag(String newEtag)
    {
        etag = newEtag == null ? "" : newEtag;
    }

    public void setValidtime(long newValidtime)
    {
        validtime = newValidtime;
    }

    public void writeToFile(File file) throws IOException
    {
        DataOutputStream s = null;
        try
        {
            s = new DataOutputStream(new FileOutputStream(file));
            s.writeUTF(getEtag());
            s.writeLong(getValidtime());
        }
        finally
        {
            if (s != null)
                s.close();
        }
    }

}
