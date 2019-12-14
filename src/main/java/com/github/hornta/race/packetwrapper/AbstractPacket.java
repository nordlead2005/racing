package com.github.hornta.race.packetwrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

public abstract class AbstractPacket {
	protected PacketContainer handle;

	protected AbstractPacket(PacketContainer handle, PacketType type) {
		if (handle == null) {
			throw new IllegalArgumentException("Packet handle cannot be null.");
		}

		if (!Objects.equals(handle.getType(), type)) {
			throw new IllegalArgumentException(handle.getHandle() + " is not a packet of type " + type);
		}

		this.handle = handle;
	}

	public PacketContainer getHandle() {
		return handle;
	}

	public void sendPacket(Player receiver) {
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, getHandle());
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Cannot send packet.", e);
		}
	}

	public void broadcastPacket() {
		ProtocolLibrary.getProtocolManager().broadcastServerPacket(getHandle());
	}

	public void receivePacket(Player sender) {
		try {
			ProtocolLibrary.getProtocolManager().recieveClientPacket(sender,
					getHandle());
		} catch (Exception e) {
			throw new RuntimeException("Cannot receive packet.", e);
		}
	}
}
