package com.softsec.editor;

import com.softsec.editor.decode.AXMLDoc;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainAxmlEditor {
    public static boolean change(String inputFile, String outputFile, String newPackageName, String newAppLabel) {
        try {
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(inputFile));
            //doc.print();

            ChannelEditor editor = new ChannelEditor(doc);
            editor.commit(newPackageName, newAppLabel);
            //doc.print();
            doc.build(new FileOutputStream(outputFile));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//	public static void main(String[] args) {
//		change("/sdcard/z.xml", "/sdcard/z.xml", "com.zic.test", "");
//	}
}
