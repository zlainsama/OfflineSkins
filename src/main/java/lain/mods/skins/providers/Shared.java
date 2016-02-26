package lain.mods.skins.providers;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Shared
{

    protected static final ExecutorService pool = Executors.newCachedThreadPool();
    protected static final BufferedImage dummy = new BufferedImage(1, 1, 2);

}
