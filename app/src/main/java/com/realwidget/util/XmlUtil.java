package com.realwidget.util;

import android.os.Environment;
import android.util.Xml;
import com.realwidget.Constants;
import com.realwidget.db.Button;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtil {
    private static final String TAG_WIDGET = "widget";
    private static final String TAG_BUTTON = "button";

    /**
     * 将配置转换成xml的字符串
     *
     * @param widgets
     * @return
     */
    public static boolean writeWidgetXml(Map<Long, List<Button>> widgets, String fileName) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        String result = "";

        try {
            serializer.setOutput(writer);
            serializer.startDocument(Constants.DEFAULT_ENCODING, true);
            serializer.startTag("", Constants.APP_NAME);

            for (Long key : widgets.keySet()) {
                serializer.startTag("", TAG_WIDGET);
                serializer.attribute("", "time", key + "");

                for (Button btn : widgets.get(key)) {
                    serializer.startTag("", TAG_BUTTON);
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_BACK_COLOR, btn.backColor + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_BACK_FILE, btn.backFile);
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_BUTTON_ID, btn.btnId + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, btn.size + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, btn.type + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_ICON_COLOR, btn.iconColor + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_ICON_FILE, btn.iconFile);
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_LABEL, btn.label);
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_LABEL_COLOR, btn.labelColor + "");
                    serializer.attribute("", Constants.TABLE_WIDGET.COLUMN_INTENT, btn.intent);
                    serializer.endTag("", TAG_BUTTON);
                }
                serializer.endTag("", TAG_WIDGET);
            }

            serializer.endTag("", Constants.APP_NAME);
            serializer.endDocument();
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return saveToFile(result.getBytes(), fileName);
    }

    private static boolean saveToFile(byte[] msgs, String fileName) {
        try {
            // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File dir = new File(sdCardDir.getPath() + File.separator + Constants.DIR_NAME);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                File saveFile = new File(dir.getPath(), fileName);
                FileOutputStream outStream = new FileOutputStream(saveFile);
                outStream.write(msgs);
                outStream.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    /**
     * @return 当读取文件出错时返回Size为0的集合
     */
    public static Map<Long, List<Button>> parseWidgetCfg(String fileName) {
        InputStream is = readFile(fileName);
        HashMap<Long, List<Button>> xmlEntitys = null;

        if (is == null) {
            return new HashMap<Long, List<Button>>();
        }

        XmlPullParser parser = Xml.newPullParser();

        try {
            // auto-detect the encoding from the stream
            parser.setInput(is, null);
            int eventType = parser.getEventType();

            Long time = null;
            List<Button> currentWidget = null;
            Button currentButton = null;
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        xmlEntitys = new HashMap<Long, List<Button>>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_WIDGET)) {
                            currentWidget = new ArrayList<Button>();
                            time = Long.parseLong(parser.getAttributeValue("", "time"));
                        } else if (name.equalsIgnoreCase(TAG_BUTTON)) {
                            currentButton = new Button();
                            currentButton.backColor = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_BACK_COLOR));
                            currentButton.backFile = parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_BACK_FILE);
                            currentButton.btnId = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_BUTTON_ID));
                            currentButton.iconColor = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_ICON_COLOR));
                            currentButton.iconFile = parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_ICON_FILE);
                            currentButton.label = parser.getAttributeValue("", Constants.TABLE_WIDGET.COLUMN_LABEL);
                            currentButton.labelColor = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_LABEL_COLOR));
                            currentButton.size = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE));
                            currentButton.type = Integer.parseInt(parser.getAttributeValue("",
                                    Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE));
                            currentButton.intent = parser.getAttributeValue("", Constants.TABLE_WIDGET.COLUMN_INTENT);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_WIDGET) && currentWidget != null) {
                            xmlEntitys.put(time, currentWidget);
                        } else if (name.equalsIgnoreCase(TAG_BUTTON) && currentButton != null) {
                            currentWidget.add(currentButton);
                        } else if (name.equalsIgnoreCase(Constants.APP_NAME)) {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlEntitys;
    }

    /**
     * 读取配置文件，当出现错误时返回null
     *
     * @param fileName
     * @return
     */
    private static FileInputStream readFile(String fileName) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir.getPath() + File.separator + Constants.DIR_NAME + File.separator
                        + fileName);

                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    return fis;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
