package me.jamiethompson.forge;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Jamie on 19/10/2017.
 */

public class AutoFillNode {

    public static final int PASSWORD = 0;
    public static final int USERNAME = 1;
    public static final int EMAIL = 2;
    public static final int FIRST_NAME = 3;
    public static final int MIDDLE_NAME = 4;
    public static final int LAST_NAME = 5;
    public static final int FULL_NAME = 6;

    private AccessibilityNodeInfo accessiblityNode;
    private int type;

    public AutoFillNode(AccessibilityNodeInfo accessiblityNode, int type) {
        this.accessiblityNode = accessiblityNode;
        this.type = type;
    }

    public AccessibilityNodeInfo getAccessiblityNode() {
        return accessiblityNode;
    }

    public void setAccessiblityNode(AccessibilityNodeInfo accessiblityNode) {
        this.accessiblityNode = accessiblityNode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
