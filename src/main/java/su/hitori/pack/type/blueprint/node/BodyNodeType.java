package su.hitori.pack.type.blueprint.node;

/*
    "head": 0.0, "right_arm": -1024.0, "right_forearm": -6144.0,
    "left_arm": -2048.0, "left_forearm": -7168.0, "torso": -3072.0,
    "waist": -8192.0, "right_leg": -4096.0, "lower_right_leg": -9216.0,
    "left_leg": -5120.0, "lower_left_leg": -10240.0
*/
public enum BodyNodeType {

    HEAD(0),

    RIGHT_ARM(1),
    LEFT_ARM(2),

    TORSO(3),

    RIGHT_LEG(4),
    LEFT_LEG(5),

    RIGHT_FOREARM(6),
    LEFT_FOREARM(7),

    WAIST(8),

    LOWER_RIGHT_LEG(9),
    LOWER_LEFT_LEG(10);

    public final int negativeVerticalOffset;

    BodyNodeType(int negativeVerticalOffsetStep) {
        this.negativeVerticalOffset = negativeVerticalOffsetStep * -1024;
    }

}
