package dev.minixr9k.types;

import java.util.UUID;

public class Entity {

    private int entityId;
    private UUID uuid;
    private String entityType;

    private double x, y, z = 0;
    private float yaw, pitch = 0;
    private boolean isSneaking;

    private double prevX, prevY, prevZ;
    private float prevYaw, prevPitch;

    private boolean onGround = false;

    private boolean lookingAtPlayer;

    public void updatePrevPosition() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public double getPrevX() {
        return prevX;
    }

    public double getPrevY() {
        return prevY;
    }

    public double getPrevZ() {
        return prevZ;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public double getVelocityX() {
        return this.x - this.prevX;
    }

    public double getVelocityY() {
        return this.y - this.prevY;
    }

    public double getVelocityZ() {
        return this.z - this.prevZ;
    }

    public double getHorizontalVelocity() {
        double velX = getVelocityX();
        double velZ = getVelocityZ();
        return Math.sqrt(velX * velX + velZ * velZ);
    }

    public short getNetworkVelocityX() {
        double velX = getVelocityX();
        return (short) (clamp(velX, -3.9, 3.9) * 8000.0);
    }

    public short getNetworkVelocityY() {
        double velY = getVelocityY();
        return (short) (clamp(velY, -3.9, 3.9) * 8000.0);
    }

    public short getNetworkVelocityZ() {
        double velZ = getVelocityZ();
        return (short) (clamp(velZ, -3.9, 3.9) * 8000.0);
    }

    // Вспомогательный метод, чтобы значение не выходило за границы безопасности short
    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public void setSneaking(boolean sneaking) {
        isSneaking = sneaking;
    }

}
