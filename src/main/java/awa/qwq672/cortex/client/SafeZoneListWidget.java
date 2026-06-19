package awa.qwq672.cortex.client;

import awa.qwq672.cortex.SafeZoneManager;
import awa.qwq672.cortex.SafeZoneManager.SafeZone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;

import java.util.List;

public class SafeZoneListWidget extends EntryListWidget<SafeZoneListWidget.SafeZoneEntry> {
    public SafeZoneListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        refresh();
    }

    public void refresh() {
        this.clearEntries();
        List<String> names = SafeZoneManager.getZoneNames();
        for (String name : names) {
            SafeZone zone = SafeZoneManager.getZone(name);
            this.addEntry(new SafeZoneEntry(name, zone != null ? zone.getDescription() : "", this));
        }
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    public class SafeZoneEntry extends Entry<SafeZoneEntry> {
        public final String zoneName;
        public final String description;
        private final SafeZoneListWidget parent;

        public SafeZoneEntry(String name, String description, SafeZoneListWidget parent) {
            this.zoneName = name;
            this.description = description;
            this.parent = parent;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(client.textRenderer, zoneName, x + 5, y + 5, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, description, x + 5, y + 16, 0xAAAAAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            parent.setSelected(this);
            return true;
        }
    }
}