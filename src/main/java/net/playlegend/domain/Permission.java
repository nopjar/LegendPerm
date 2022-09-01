package net.playlegend.domain;

/**
 * @param node the permission as string
 * @param mode if granted or not
 */
public record Permission(String node, boolean mode) {

}
