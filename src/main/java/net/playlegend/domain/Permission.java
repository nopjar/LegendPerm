package net.playlegend.domain;

import java.util.Objects;

public final class Permission {

    private final String node;
    private boolean mode;

    /**
     * @param node the permission as string
     * @param mode if granted or not
     */
    public Permission(String node, boolean mode) {
        this.node = node;
        this.mode = mode;
    }

    public String getNode() {
        return node;
    }

    public boolean getMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Permission) obj;
        return Objects.equals(this.node, that.node) &&
               this.mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, mode);
    }

    @Override
    public String toString() {
        return "Permission[" +
               "node=" + node + ", " +
               "mode=" + mode + ']';
    }


}
