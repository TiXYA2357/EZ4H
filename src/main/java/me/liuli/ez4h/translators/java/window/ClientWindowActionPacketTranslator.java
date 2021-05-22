package me.liuli.ez4h.translators.java.window;

import com.github.steveice10.mc.protocol.packet.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.inventory.InventoryActionData;
import com.nukkitx.protocol.bedrock.data.inventory.InventorySource;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InteractPacket;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import me.liuli.ez4h.EZ4H;
import me.liuli.ez4h.minecraft.Client;
import me.liuli.ez4h.translators.JavaTranslator;
import me.liuli.ez4h.translators.converters.ItemConverter;
import me.liuli.ez4h.translators.java.play.ClientPlayerActionPacketTranslator;

public class ClientWindowActionPacketTranslator implements JavaTranslator {
    @Override
    public void translate(Packet inPacket, Client client) {
        ClientWindowActionPacket packet = (ClientWindowActionPacket) inPacket;
        //TODO:Rewrite
        switch (packet.getMode()) {
            case 0: {
                if (packet.getSlot() == -999) return;
                if (client.getData().getItemInHand() == 0) {
                    client.getData().setItemInHand(packet.getSlot());
                } else {
                    moveItem(client.getData().getItemInHand(), packet.getSlot(), client);
                    client.getData().setItemInHand(0);
                }
                break;
            }
            case 1: {
                //TODO:Click with shift
                break;
            }
            case 2: {
                moveItem(packet.getSlot(), packet.getButton() + 36, client);
                break;
            }
            case 4: {
                if (packet.getSlot() == -999) return;
                int count = 1;
                ItemData item = client.getData().getInventory().getBedrockItem(packet.getSlot());
                if (packet.getButton() == 1) {
                    count = item.getCount();
                }
                ((ClientPlayerActionPacketTranslator) EZ4H.getTranslatorManager().getJavaTranslator(ClientPlayerActionPacket.class)).dropItem(client, count, item);
                break;
            }
        }
    }

    public void moveItem(int fromSlot, int toSlot, Client client) {
        if (!client.getInventory().isOpen()) {
            InteractPacket interactPacket = new InteractPacket();
            interactPacket.setAction(InteractPacket.Action.OPEN_INVENTORY);
            interactPacket.setRuntimeEntityId(client.getPlayer().getEntityId());
            interactPacket.setMousePosition(Vector3f.ZERO);
            client.sendPacket(interactPacket);
        }
        ItemConverter itemConverter = EZ4H.getConverterManager().getItemConverter();
        ItemData fromItem = client.getInventory().getBedrockItem(fromSlot),
                toItem = client.getInventory().getBedrockItem(toSlot);

        if (fromItem == null) {
            fromItem = ItemData.AIR;
        }
        if (toItem == null) {
            toItem = ItemData.AIR;
        }

        fromItem.setUsingNetId(false);
        toItem.setUsingNetId(false);

        InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
        inventoryTransactionPacket.setTransactionType(TransactionType.NORMAL);
        inventoryTransactionPacket.setLegacyRequestId(0);
        inventoryTransactionPacket.setActionType(0);
        inventoryTransactionPacket.setRuntimeEntityId(0);
        inventoryTransactionPacket.setBlockFace(0);
        inventoryTransactionPacket.setHotbarSlot(0);
        inventoryTransactionPacket.setBlockRuntimeId(0);
        InventoryActionData inventoryActionData = new InventoryActionData(InventorySource.fromContainerWindowId(0), itemConverter.inventoryIndex(fromSlot, true), fromItem, toItem);
        inventoryTransactionPacket.getActions().add(inventoryActionData);
        inventoryActionData = new InventoryActionData(InventorySource.fromContainerWindowId(0), itemConverter.inventoryIndex(toSlot, true), toItem, fromItem);
        inventoryTransactionPacket.getActions().add(inventoryActionData);
        client.sendPacket(inventoryTransactionPacket);

        client.getInventory().updateItem(fromItem, toSlot, true);
        client.getInventory().updateItem(toItem, fromSlot, true);
    }

    @Override
    public Class<? extends MinecraftPacket> getPacketClass() {
        return ClientWindowActionPacket.class;
    }
}
