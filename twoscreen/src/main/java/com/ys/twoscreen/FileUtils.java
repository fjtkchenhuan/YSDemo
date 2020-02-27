package com.ys.twoscreen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RYX on 2016/6/23.
 */
public class FileUtils {
    public static List<String> getPaths(String path) {
        List<String> paths = new ArrayList<>();
        File file = new File(path);
        File[] fs = null;
        if (file.exists() && file != null && file.isDirectory()) {
            fs = file.listFiles();
        }
        if (fs != null && fs.length > 0) {
            for (int i = 0; i < fs.length; i++) {
                paths.add(fs[i].getAbsoluteFile().toString());
            }
        }
        return paths;
    }

}
