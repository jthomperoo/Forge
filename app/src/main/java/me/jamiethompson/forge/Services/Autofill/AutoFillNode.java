package me.jamiethompson.forge.Services.Autofill;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Jamie on 19/10/2017.
 * Represents an accessibility node that can be auto filled by the auto fill service, includes
 * the auto fill input type
 * REMOVED IN 1.4 DUE TO CHANGE IN ANDROID GUIDELINES
 */

public class AutoFillNode {
    // Auto fill input types
    public static final int PASSWORD = 0;
    public static final int USERNAME = 1;
    public static final int EMAIL = 2;
    public static final int FIRST_NAME = 3;
    public static final int MIDDLE_NAME = 4;
    public static final int LAST_NAME = 5;
    public static final int FULL_NAME = 6;
    // Accessibility node that can be auto filled
    private AccessibilityNodeInfo accessibilityNode;
    // This node's auto fill input type
    private int type;

    /**
     * @param accessibilityNode the accessibility node that is valid for auto fill
     * @param type              the input type of the valid accessibility node, as defined in AutoFillNode
     */
    public AutoFillNode(AccessibilityNodeInfo accessibilityNode, int type) {
        this.accessibilityNode = accessibilityNode;
        this.type = type;
    }

    /**
     * Returns the accessibility node that is valid for auto fill
     *
     * @return the node that is valid for auto fill
     */
    public AccessibilityNodeInfo getAccessibilityNode() {
        return accessibilityNode;
    }

    /**
     * Returns the input type of the auto fill node
     *
     * @return the input type of the auto fill node, as defined in AutoFillNode
     */
    public int getType() {
        return type;
    }

}
