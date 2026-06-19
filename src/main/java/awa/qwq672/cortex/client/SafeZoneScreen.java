package awa.qwq672.cortex.client;

import awa.qwq672.cortex.SafeZoneManager;
import awa.qwq672.cortex.SafeZoneManager.HeightMode;
import awa.qwq672.cortex.SafeZoneManager.ZoneShape;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SafeZoneScreen extends Screen {
    private TextFieldWidget nameField;
    private TextFieldWidget size1Field;
    private TextFieldWidget sizeYField;
    private TextFieldWidget sizeZField;
    private SafeZoneListWidget listWidget;
    private ButtonWidget shapeBtn;
    private ButtonWidget heightModeBtn;
    private ZoneShape currentShape = ZoneShape.SPHERE;
    private HeightMode currentHeightMode = HeightMode.FULL;

    private static final int LIST_TOP = 45;
    private static final int LIST_BOTTOM_OFFSET = 130;
    private static final int FIELD_Y = 0;
    private static final int BUTTON_ROW_Y = 0;

    protected SafeZoneScreen() {
        super(Text.literal("Cortex - 安全区管理"));
    }

    @Override
    protected void init() {
        int listBottom = this.height - LIST_BOTTOM_OFFSET;
        this.listWidget = new SafeZoneListWidget(this.client, this.width, this.height, LIST_TOP, listBottom, 32);
        this.addSelectableChild(this.listWidget);

        int fieldStartY = this.height - 115;

        this.nameField = new TextFieldWidget(this.textRenderer, 10, fieldStartY, 120, 20, Text.literal("名称"));
        this.nameField.setMaxLength(32);
        this.addSelectableChild(this.nameField);

        this.size1Field = new TextFieldWidget(this.textRenderer, 140, fieldStartY, 50, 20, Text.literal("半径"));
        this.size1Field.setText("5.0");
        this.addSelectableChild(this.size1Field);

        this.sizeYField = new TextFieldWidget(this.textRenderer, 195, fieldStartY, 50, 20, Text.literal("Y"));
        this.sizeYField.setText("5.0");
        this.addSelectableChild(this.sizeYField);

        this.sizeZField = new TextFieldWidget(this.textRenderer, 250, fieldStartY, 50, 20, Text.literal("Z"));
        this.sizeZField.setText("5.0");
        this.addSelectableChild(this.sizeZField);

        updateFieldVisibility();

        this.shapeBtn = ButtonWidget.builder(Text.literal("形状: 球形"), button -> {
            currentShape = ZoneShape.values()[(currentShape.ordinal() + 1) % ZoneShape.values().length];
            updateShapeButtonText();
            updateFieldVisibility();
        }).dimensions(10, fieldStartY + 25, 100, 20).build();
        this.addDrawableChild(this.shapeBtn);

        this.heightModeBtn = ButtonWidget.builder(Text.literal("高度: 全范围"), button -> {
            currentHeightMode = HeightMode.values()[(currentHeightMode.ordinal() + 1) % HeightMode.values().length];
            updateHeightModeButtonText();
        }).dimensions(120, fieldStartY + 25, 100, 20).build();
        this.addDrawableChild(this.heightModeBtn);

        ButtonWidget addBtn = ButtonWidget.builder(Text.literal("添加当前位置"), button -> {
            if (this.client != null && this.client.player != null) {
                BlockPos pos = this.client.player.getBlockPos();
                String name = this.nameField.getText().trim();
                if (name.isEmpty()) name = "zone_" + System.currentTimeMillis();

                double radius = 5.0, sx = 5.0, sy = 5.0, sz = 5.0;
                try {
                    double size1 = Double.parseDouble(this.size1Field.getText());
                    if (currentShape == ZoneShape.CUBOID) {
                        sx = size1;
                    } else {
                        radius = size1;
                    }
                } catch (NumberFormatException ignored) {}
                try { sy = Double.parseDouble(this.sizeYField.getText()); } catch (NumberFormatException ignored) {}
                try { sz = Double.parseDouble(this.sizeZField.getText()); } catch (NumberFormatException ignored) {}

                SafeZoneManager.addZone(name, currentShape, currentHeightMode,
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        radius, sx, sy, sz);
                refreshList();
            }
        }).dimensions(this.width / 2 - 110, fieldStartY + 25, 100, 20).build();
        this.addDrawableChild(addBtn);

        ButtonWidget delBtn = ButtonWidget.builder(Text.literal("删除选中"), button -> {
            SafeZoneListWidget.SafeZoneEntry selected = this.listWidget.getSelectedOrNull();
            if (selected != null) {
                SafeZoneManager.removeZone(selected.zoneName);
                refreshList();
            }
        }).dimensions(this.width / 2, fieldStartY + 25, 80, 20).build();
        this.addDrawableChild(delBtn);

        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("关闭"), button -> this.close())
                .dimensions(this.width - 60, 5, 50, 20).build();
        this.addDrawableChild(closeBtn);
    }

    private void updateShapeButtonText() {
        switch (currentShape) {
            case SPHERE:
                shapeBtn.setMessage(Text.literal("形状: 球形"));
                size1Field.setPlaceholder(Text.literal("半径"));
                break;
            case CYLINDER:
                shapeBtn.setMessage(Text.literal("形状: 圆柱"));
                size1Field.setPlaceholder(Text.literal("半径"));
                break;
            case CUBOID:
                shapeBtn.setMessage(Text.literal("形状: 长方体"));
                size1Field.setPlaceholder(Text.literal("X"));
                break;
        }
    }

    private void updateHeightModeButtonText() {
        switch (currentHeightMode) {
            case FULL: heightModeBtn.setMessage(Text.literal("高度: 全范围")); break;
            case ABOVE: heightModeBtn.setMessage(Text.literal("高度: 仅上方")); break;
            case BELOW: heightModeBtn.setMessage(Text.literal("高度: 仅下方")); break;
        }
    }

    private void updateFieldVisibility() {
        boolean isCuboid = currentShape == ZoneShape.CUBOID;
        this.sizeYField.visible = isCuboid;
        this.sizeZField.visible = isCuboid;
    }

    private void refreshList() {
        this.listWidget.refresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.listWidget.render(context, mouseX, mouseY, delta);

        int fieldStartY = this.height - 115;
        context.drawTextWithShadow(this.textRenderer, "名称:", 10, fieldStartY - 10, 0xAAAAAA);
        if (currentShape == ZoneShape.CUBOID) {
            context.drawTextWithShadow(this.textRenderer, "尺寸X:", 140, fieldStartY - 10, 0xAAAAAA);
        } else {
            context.drawTextWithShadow(this.textRenderer, "半径:", 140, fieldStartY - 10, 0xAAAAAA);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "点击列表项选中后可按删除", this.width / 2, 22, 0x666666);
        super.render(context, mouseX, mouseY, delta);
    }
}