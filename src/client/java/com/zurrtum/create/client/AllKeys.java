package com.zurrtum.create.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class AllKeys {
    public static final List<KeyMapping> ALL = new ArrayList<>();
    public static final Category CATEGORY = Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "binding"));
    public static final KeyMapping TOOL_MENU = register("toolmenu", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping TOOLBELT = register("toolbelt", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping ROTATE_MENU = register("rotate_menu", GLFW.GLFW_KEY_UNKNOWN);

    private static KeyMapping register(String name, int code) {
        KeyMapping key = new KeyMapping("create.keyinfo." + name, code, CATEGORY);
        ALL.add(key);
        return key;
    }

    public static void register() {
    }
}
