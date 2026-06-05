package com.zurrtum.create.client.ponder.foundation.ui;

import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.catnip.gui.widget.BoxWidget;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.catnip.layout.LayoutHelper;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderTag;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PonderTagIndexScreen extends AbstractPonderScreen {

    protected List<Map.Entry<String, List<PonderTag>>> sortedModTags = List.of();
    protected @UnknownNullability ModTagsEntry currentEntry;
    protected int currentPage;

    @Nullable
    protected PonderButton pageNext;
    @Nullable
    protected PonderButton pagePrev;

    @Nullable
    private PonderTag hoveredItem;

    // The main ponder entry point from menus.
    public PonderTagIndexScreen() {
    }

    @Override
    protected void init() {
        super.init();

        sortedModTags = PonderIndex.getTagAccess().getListedTags().stream()
            .collect(Collectors.groupingBy(tag -> tag.getId().getNamespace(), TreeMap::new, Collectors.toList()))
            .entrySet().stream().toList();
        int size = sortedModTags.size();
        if (size == 0) {
            return;
        }
        setupModTagEntries();
        if (size == 1) {
            return;
        }

        int xOffset = (int) (width * 0.5);

        addRenderableWidget(pagePrev = new PonderButton(
            xOffset - 120,
            height - 27
        ).showing(PonderGuiTextures.ICON_PONDER_LEFT).withCallback(() -> {
            currentPage--;
            updateAfterPaginationChange();
        }).setActive(false));

        pagePrev.updateGradientFromState();

        addRenderableWidget(pageNext = new PonderButton(
            xOffset + 100,
            height - 27
        ).showing(PonderGuiTextures.ICON_PONDER_RIGHT).withCallback(() -> {
            currentPage++;
            updateAfterPaginationChange();
        }).setActive(true));

    }

    protected void setupModTagEntries() {
        removeWidgets(children().stream().filter(widget -> {
            if (widget instanceof PonderButton ponderButton) {
                PonderTag tag = ponderButton.tag;
                if (tag != null) {
                    ponderButton.clear();
                    return true;
                }
            }
            return false;
        }).toList());

        int yOffset = 158;
        int xOffset = (int) (width * 0.5) + 4;

        Map.Entry<String, List<PonderTag>> entry = sortedModTags.get(currentPage);
        String modName = FabricLoader.getInstance().getModContainer(entry.getKey())
            .map(mod -> mod.getMetadata().getName()).orElseGet(entry::getKey);
        //TODO
        //                .orElse(ConfigScreen.toHumanReadable(modId));
        List<PonderTag> tags = entry.getValue();

        int size = tags.size();
        int row = size / 11.0d > 1 ? 2 : 1;
        LayoutHelper layout = LayoutHelper.centeredHorizontal(size, row, 28, 28, 8);
        int left = layout.getX();
        int top = layout.getY();
        if (row != 1) {
            yOffset = yOffset - 14 - top;
        }
        Rect2i layoutArea = new Rect2i(left, top, left * -2, top * -2);

        for (PonderTag tag : tags) {
            PonderButton button = new PonderButton(xOffset + layout.getX(), yOffset + layout.getY()).showingTag(tag)
                .withCallback((mouseX, mouseY) -> {
                    centerScalingOn(mouseX, mouseY);
                    ScreenOpener.transitionTo(new PonderTagScreen(tag));
                });
            addRenderableWidget(button);
            layout.next();
        }

        currentEntry = new ModTagsEntry(modName, size, layoutArea);
    }

    protected void updateAfterPaginationChange() {
        setupModTagEntries();

        pagePrev.<PonderButton>setActive(currentPage != 0).animateGradientFromState();
        pageNext.<PonderButton>setActive(currentPage < sortedModTags.size() - 1).animateGradientFromState();
    }

    @Override
    protected void initBackTrackIcon(BoxWidget backTrack) {
        backTrack.showing(PonderGuiTextures.ICON_PONDER_IDENTIFY);
    }

    @Override
    public void tick() {
        super.tick();
        PonderUI.ponderTicks++;

        hoveredItem = null;
        Window w = minecraft.getWindow();
        double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
        for (GuiEventListener child : children()) {
            if (child == backTrack) {
                continue;
            }
            if (child instanceof PonderButton button) {
                if (button.isMouseOver(mouseX, mouseY)) {
                    hoveredItem = button.getTag();
                }
            }
        }
    }

    @Override
    protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWindow(graphics, mouseX, mouseY, partialTicks);
        Matrix3x2fStack poseStack = graphics.pose();

        poseStack.pushMatrix();
        poseStack.translate(width / 2.0f, 30);

        //title, box for icon and streak
        poseStack.pushMatrix();
        poseStack.translate(-120, 0);

        String title = Ponder.lang().translate(WELCOME).string();

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(PonderUI.COLOR_IDLE).at(0, 0, 0)
            .withBounds(30, 30).render(graphics);

        PonderGuiTextures.LOGO.render(graphics, -1, -1);

        //34 = 30 bounds + 2 padding + 2 box width
        //-3 = 2 padding + 1 pixel of the box
        poseStack.translate(34, -3);

        int streakHeight = 36;
        UIRenderHelper.streak(graphics, 0, 0, streakHeight / 2, streakHeight, 280);

        poseStack.scale(2.0f, 2.0f);
        graphics.text(font, title, 3, 5, UIRenderHelper.COLOR_TEXT.getFirst().getRGB(), false);

        poseStack.popMatrix();
        poseStack.translate(0, 50);
        poseStack.pushMatrix();
        //at the middle, 80px from the top now

        int maxWidth = (int) (width * 0.5f);
        String desc = Ponder.lang().translate(DESCRIPTION).string();

        int descWidth = font.width(desc);
        if (descWidth + 2 < maxWidth) {
            maxWidth = descWidth + 2;
        }

        int descHeight = font.wordWrapHeight(Component.literal(desc), maxWidth);

        poseStack.translate(-maxWidth / 2.0f, 0);

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(PonderUI.COLOR_IDLE).at(-3, -3, 0)
            .withBounds(maxWidth + 6, descHeight + 5).render(graphics);

        ClientFontHelper.submitSplitString(
            graphics,
            font,
            desc,
            0,
            0,
            maxWidth,
            UIRenderHelper.COLOR_TEXT.getFirst().getRGB()
        );
        poseStack.popMatrix();

        poseStack.translate(0, 60);
        String categories = Ponder.lang().translate(CATEGORIES, currentEntry.modName).string();
        int stringWidth = font.width(categories);
        poseStack.pushMatrix();
        poseStack.translate(-stringWidth / 2.0f, -20);

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(PonderUI.COLOR_IDLE).at(-3, -1, 0)
            .withBounds(stringWidth + 6, 10).render(graphics);

        graphics.text(font, categories, 0, 0, UIRenderHelper.COLOR_TEXT.getFirst().getRGB(), false);

        poseStack.popMatrix();

        Rect2i layoutArea = currentEntry.layoutArea;
        int layoutWidth = layoutArea.getWidth();
        int layoutHeight = layoutArea.getHeight();
        int extraLength = Mth.clamp(currentEntry.tagCount, 2, 8);
        UIRenderHelper.streak(graphics, 0, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);
        UIRenderHelper.streak(graphics, 180, 0, layoutHeight / 2, layoutHeight + 6, layoutWidth / 2 + extraLength * 15);
        poseStack.popMatrix();
    }

    @Override
    protected void renderWindowForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (hoveredItem != null) {
            List<Component> list = FontHelper.cutStringTextComponent(hoveredItem.getDescription(), Palette.ALL_GRAY);
            list.addFirst(Component.literal(hoveredItem.getTitle()));
            graphics.setComponentTooltipForNextFrame(font, list, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void removed() {
        super.removed();
        hoveredItem = null;
        for (GuiEventListener child : children()) {
            if (child instanceof PonderButton button) {
                button.clear();
            }
        }
    }

    public record ModTagsEntry(String modName, int tagCount, Rect2i layoutArea) {
    }

}
