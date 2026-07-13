package com.axsaafe.sheetlab.util;

import android.content.Context;
import android.os.Environment;

import com.axsaafe.sheetlab.model.SheetFile;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileManager {

    private final File sheetsDir;

    public FileManager(Context context) {
        // Save files in app-specific external storage (no permission needed on API 29+)
        sheetsDir = new File(context.getExternalFilesDir(null), "SheetLab");
        if (!sheetsDir.exists()) {
            sheetsDir.mkdirs();
        }
    }

    public List<SheetFile> getAllFiles() {
        List<SheetFile> list = new ArrayList<>();
        File[] files = sheetsDir.listFiles(f -> f.getName().endsWith(".xlsx"));
        if (files != null) {
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (File f : files) {
                list.add(new SheetFile(f.getName(), f.getAbsolutePath(), f.lastModified()));
            }
        }
        return list;
    }

    public List<SheetFile> searchFiles(String query) {
        List<SheetFile> all = getAllFiles();
        List<SheetFile> result = new ArrayList<>();
        String lower = query.toLowerCase();
        for (SheetFile sf : all) {
            if (sf.getName().toLowerCase().contains(lower)) {
                result.add(sf);
            }
        }
        return result;
    }

    public SheetFile createNewFile(String name) throws IOException {
        if (!name.endsWith(".xlsx")) name = name + ".xlsx";
        File newFile = new File(sheetsDir, name);
        int counter = 1;
        while (newFile.exists()) {
            String baseName = name.replace(".xlsx", "");
            newFile = new File(sheetsDir, baseName + "_" + counter + ".xlsx");
            counter++;
        }

        // Create empty XLSX
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("Sheet1");
        FileOutputStream fos = new FileOutputStream(newFile);
        wb.write(fos);
        fos.close();
        wb.close();

        return new SheetFile(newFile.getName(), newFile.getAbsolutePath(), newFile.lastModified());
    }

    public boolean deleteFile(SheetFile sf) {
        return sf.toFile().delete();
    }

    public boolean renameFile(SheetFile sf, String newName) {
        if (!newName.endsWith(".xlsx")) newName = newName + ".xlsx";
        File newFile = new File(sheetsDir, newName);
        boolean success = sf.toFile().renameTo(newFile);
        if (success) {
            sf.setName(newName);
            sf.setPath(newFile.getAbsolutePath());
        }
        return success;
    }

    public File getDownloadTarget(SheetFile sf) {
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(downloads, sf.getName());
    }

    public File getSheetsDir() {
        return sheetsDir;
    }
}
