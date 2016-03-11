package com.realwidget.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Button implements Parcelable, Comparable<Button> {
    // 实例化静态内部对象CREATOR实现接口Parcelable.Creator
    public static final Parcelable.Creator<Button> CREATOR = new Creator<Button>() {
        @Override
        public Button[] newArray(int size) {
            return new Button[size];
        }

        // 将Parcel对象反序列化，字段顺序要跟序列化时相同
        @Override
        public Button createFromParcel(Parcel source) {
            Button button = new Button();
            button.btnId = source.readInt();
            button.type = source.readInt();
            button.size = source.readInt();
            button.backColor = source.readInt();
            button.iconColor = source.readInt();
            button.labelColor = source.readInt();
            button.label = source.readString();
            button.intent = source.readString();
            button.iconFile = source.readString();
            button.backFile = source.readString();
            return button;
        }
    };
    public int btnId;
    public int type;
    public int size;
    public int backColor;
    public int iconColor;
    public int labelColor;
    public String label = "";
    public String intent = "";
    public String iconFile = "";
    public String backFile = "";
    public Button() {
    }

    public Button(int btnId, int type, int size, String label, int backColor, int iconColor, int lableColor) {
        this.btnId = btnId;
        this.type = type;
        this.size = size;
        this.label = label;
        this.backColor = backColor;
        this.iconColor = iconColor;
        this.labelColor = lableColor;
    }

    @Override
    public int compareTo(Button paramT) {
        return btnId - paramT.btnId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + btnId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Button other = (Button) obj;
        if (btnId != other.btnId)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel paramParcel, int paramInt) {
        paramParcel.writeInt(btnId);
        paramParcel.writeInt(type);
        paramParcel.writeInt(size);
        paramParcel.writeInt(backColor);
        paramParcel.writeInt(iconColor);
        paramParcel.writeInt(labelColor);
        paramParcel.writeString(label);
        paramParcel.writeString(intent);
        paramParcel.writeString(iconFile);
        paramParcel.writeString(backFile);
    }
}
