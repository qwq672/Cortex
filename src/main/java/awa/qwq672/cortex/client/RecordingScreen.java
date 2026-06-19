package awa.qwq672.cortex.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class RecordingScreen extends Screen {
    private TextFieldWidget nameField;
    private TextFieldWidget subgoalField;
    private boolean isRecording = false;
    private ButtonWidget pvpBtn;
    private ButtonWidget minigameBtn;
    private ButtonWidget survivalBtn;

    protected RecordingScreen() {
        super(Text.literal("Cortex - 录制控制"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 20, 200, 20, Text.literal("录制名称"));
        this.nameField.setMaxLength(32);
        this.nameField.setPlaceholder(Text.literal("输入录制名称..."));
        this.addSelectableChild(this.nameField);

        isRecording = ClientDataCollector.isRecording();
        if (isRecording) {
            this.nameField.setText(ClientDataCollector.getCurrentName());
            this.nameField.setEditable(false);
        }

        ButtonWidget toggleBtn = ButtonWidget.builder(
                Text.literal(isRecording ? "停止录制" : "开始录制"),
                button -> {
                    if (isRecording) {
                        ClientDataCollector.stopRecording();
                        isRecording = false;
                        this.nameField.setEditable(true);
                        this.subgoalField.setEditable(true);
                        button.setMessage(Text.literal("开始录制"));
                        updateModeButtonsEnabled(true);
                    } else {
                        String name = this.nameField.getText().trim();
                        if (name.isEmpty()) name = "record_" + System.currentTimeMillis();
                        ClientDataCollector.startRecording(name);
                        isRecording = true;
                        this.nameField.setEditable(false);
                        this.nameField.setText(name);
                        this.subgoalField.setEditable(false);
                        button.setMessage(Text.literal("停止录制"));
                        updateModeButtonsEnabled(false);
                    }
                })
                .dimensions(centerX - 100, centerY + 10, 200, 20).build();
        this.addDrawableChild(toggleBtn);

        int modeBtnY = centerY + 38;
        int modeBtnWidth = 60;

        this.pvpBtn = ButtonWidget.builder(Text.literal("PVP"), button -> {
            ClientDataCollector.setMode(0);
            updateModeButtonStyles();
        }).dimensions(centerX - 100, modeBtnY, modeBtnWidth, 20).build();
        this.addDrawableChild(this.pvpBtn);

        this.minigameBtn = ButtonWidget.builder(Text.literal("Minigame"), button -> {
            ClientDataCollector.setMode(1);
            updateModeButtonStyles();
        }).dimensions(centerX - 30, modeBtnY, modeBtnWidth, 20).build();
        this.addDrawableChild(this.minigameBtn);

        this.survivalBtn = ButtonWidget.builder(Text.literal("Survival"), button -> {
            ClientDataCollector.setMode(2);
            updateModeButtonStyles();
        }).dimensions(centerX + 40, modeBtnY, modeBtnWidth, 20).build();
        this.addDrawableChild(this.survivalBtn);

        this.subgoalField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY + 90, 200, 20, Text.literal("子目标"));
        this.subgoalField.setMaxLength(5);
        this.subgoalField.setPlaceholder(Text.literal("子目标ID（-1=无）..."));
        this.subgoalField.setText(String.valueOf(ClientDataCollector.getSubgoal()));
        this.subgoalField.setChangedListener(text -> {
            try {
                ClientDataCollector.setSubgoal(Integer.parseInt(text.trim()));
            } catch (NumberFormatException ignored) {
                ClientDataCollector.setSubgoal(-1);
            }
        });
        this.addSelectableChild(this.subgoalField);

        if (isRecording) {
            this.subgoalField.setEditable(false);
        }

        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("关闭"), button -> this.close())
                .dimensions(centerX - 30, centerY + 115, 60, 20).build();
        this.addDrawableChild(closeBtn);

        updateModeButtonStyles();
        updateModeButtonsEnabled(!isRecording);
    }

    private void updateModeButtonStyles() {
        int currentMode = ClientDataCollector.getMode();
        this.pvpBtn.active = currentMode != 0;
        this.minigameBtn.active = currentMode != 1;
        this.survivalBtn.active = currentMode != 2;
    }

    private void updateModeButtonsEnabled(boolean enabled) {
        this.pvpBtn.active = enabled && ClientDataCollector.getMode() != 0;
        this.minigameBtn.active = enabled && ClientDataCollector.getMode() != 1;
        this.survivalBtn.active = enabled && ClientDataCollector.getMode() != 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        String status;
        if (isRecording) {
            status = "正在录制: " + ClientDataCollector.getCurrentName();
        } else {
            status = "未在录制";
        }
        context.drawCenteredTextWithShadow(this.textRenderer, status, this.width / 2, this.height / 2 - 40,
                isRecording ? 0x55FF55 : 0xAAAAAA);

        context.drawCenteredTextWithShadow(this.textRenderer,
                "当前模式: " + ClientDataCollector.getModeName(),
                this.width / 2, this.height / 2 - 55, 0xCCCCCC);

        context.drawCenteredTextWithShadow(this.textRenderer,
                "子目标: " + ClientDataCollector.getSubgoal(),
                this.width / 2, this.height / 2 + 70, 0xCCCCCC);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}