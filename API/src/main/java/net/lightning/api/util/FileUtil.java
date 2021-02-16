package net.lightning.api.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class FileUtil {

    public void checkFilePersistence(File file) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
    }

    public void checkDirPersistence(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        else if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
    }

    public Collection<File> getFilesRecursively(File dir) {
        List<File> files = Lists.newArrayList();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(getFilesRecursively(file));
            }
            else {
                files.add(file);
            }
        }

        return files;
    }

    public Collection<String> getFileNamesRecursively(File dir) {
        List<String> files = Lists.newArrayList();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                Collection<String> subFiles = getFileNamesRecursively(dir);
                files.addAll(subFiles.stream().map(name -> file.getName() + "/" + name).collect(Collectors.toList()));
            }
            else {
                files.add(file.getName());
            }
        }

        return files;
    }

}