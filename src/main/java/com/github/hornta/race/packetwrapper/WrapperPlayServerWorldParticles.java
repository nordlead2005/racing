package com.github.hornta.race.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;

public class WrapperPlayServerWorldParticles extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.WORLD_PARTICLES;

	public WrapperPlayServerWorldParticles() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerWorldParticles(PacketContainer packet) {
		super(packet, TYPE);
	}

  @SuppressWarnings("all")
	public WrappedParticle getParticle() {
		return handle.getNewParticles().read(0);
	}

  @SuppressWarnings("all")
	public void setParticleType(WrappedParticle value) {
		handle.getNewParticles().write(0, value);
	}

	public double getX() {
		return handle.getDoubles().read(0);
	}

	public void setX(double value) {
		handle.getDoubles().write(0, value);
	}

	public double getY() {
		return handle.getDoubles().read(1);
	}

	public void setY(double value) {
		handle.getDoubles().write(1, value);
	}

	public double getZ() {
		return handle.getDoubles().read(2);
	}

	public void setZ(double value) {
		handle.getDoubles().write(2, value);
	}

	public float getOffsetX() {
		return handle.getFloat().read(0);
	}

	public void setOffsetX(float value) {
		handle.getFloat().write(0, value);
	}

	public float getOffsetY() {
		return handle.getFloat().read(1);
	}

	public void setOffsetY(float value) {
		handle.getFloat().write(1, value);
	}

	public float getOffsetZ() {
		return handle.getFloat().read(2);
	}

	public void setOffsetZ(float value) {
		handle.getFloat().write(2, value);
	}

	public float getParticleData() {
		return handle.getFloat().read(3);
	}

	public void setParticleData(float value) {
		handle.getFloat().write(3, value);
	}

	public int getNumberOfParticles() {
		return handle.getIntegers().read(0);
	}

	public void setNumberOfParticles(int value) {
		handle.getIntegers().write(0, value);
	}

	public boolean getLongDistance() {
		return handle.getBooleans().read(0);
	}

	public void setLongDistance(boolean value) {
		handle.getBooleans().write(0, value);
	}
}
