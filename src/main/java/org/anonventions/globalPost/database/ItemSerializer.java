package org.anonventions.globalPost.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class ItemSerializer {

    private static final Gson gson = new GsonBuilder().create();

    public static String serializeItems(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.size());

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }
    }

    public static List<ItemStack> deserializeItems(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            List<ItemStack> items = new java.util.ArrayList<>();

            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) dataInput.readObject();
                items.add(item);
            }

            dataInput.close();
            return items;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize items", e);
        }
    }
}