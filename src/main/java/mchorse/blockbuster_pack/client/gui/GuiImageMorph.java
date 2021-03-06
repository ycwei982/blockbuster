package mchorse.blockbuster_pack.client.gui;

import mchorse.blockbuster.client.gui.dashboard.GuiDashboard;
import mchorse.blockbuster.client.textures.GifTexture;
import mchorse.blockbuster_pack.morphs.ImageMorph;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiTexturePicker;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiImageMorph extends GuiAbstractMorph<ImageMorph>
{
    public GuiImageMorphPanel general;

    public GuiImageMorph(Minecraft mc)
    {
        super(mc);

        this.defaultPanel = this.general = new GuiImageMorphPanel(mc, this);
        this.registerPanel(this.general, GuiDashboard.GUI_ICONS, I18n.format("blockbuster.morph.image"), 48, 0, 48, 16);
    }

    @Override
    public boolean canEdit(AbstractMorph morph)
    {
        return morph instanceof ImageMorph;
    }

    public static class GuiImageMorphPanel extends GuiMorphPanel<ImageMorph, GuiImageMorph>
    {
        public GuiTexturePicker picker;
        public GuiButtonElement<GuiButton> texture;
        public GuiTrackpadElement scale;
        public GuiButtonElement<GuiCheckBox> shaded;
        public GuiButtonElement<GuiCheckBox> lighting;
        public GuiButtonElement<GuiCheckBox> billboard;

        public GuiTrackpadElement left;
        public GuiTrackpadElement right;
        public GuiTrackpadElement top;
        public GuiTrackpadElement bottom;

        public GuiImageMorphPanel(Minecraft mc, GuiImageMorph editor)
        {
            super(mc, editor);

            this.texture = GuiButtonElement.button(mc, I18n.format("blockbuster.gui.builder.pick_texture"), (b) ->
            {
                this.picker.refresh();
                this.picker.fill(this.morph.texture);
                this.picker.setVisible(true);
            });

            this.scale = new GuiTrackpadElement(mc, I18n.format("blockbuster.gui.model_block.scale"), (value) ->
            {
                this.morph.scale = value;
            });

            this.shaded = GuiButtonElement.checkbox(mc, I18n.format("blockbuster.gui.me.limbs.shading"), false, (b) ->
            {
                this.morph.shaded = b.button.isChecked();
            });

            this.lighting = GuiButtonElement.checkbox(mc, I18n.format("blockbuster.gui.me.limbs.lighting"), false, (b) ->
            {
                this.morph.lighting = b.button.isChecked();
            });

            this.billboard = GuiButtonElement.checkbox(mc, I18n.format("blockbuster.gui.billboard"), false, (b) ->
            {
                this.morph.billboard = b.button.isChecked();
            });

            this.picker = new GuiTexturePicker(mc, (rl) ->
            {
                this.morph.texture = rl;
            });
            this.picker.setVisible(false);

            this.left = new GuiTrackpadElement(mc, I18n.format("blockbuster.gui.image.left"), (value) -> this.morph.cropping.x = value.intValue());
            this.left.setLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            this.right = new GuiTrackpadElement(mc, I18n.format("blockbuster.gui.image.right"), (value) -> this.morph.cropping.w = value.intValue());
            this.right.setLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            this.top = new GuiTrackpadElement(mc, I18n.format("blockbuster.gui.image.top"), (value) -> this.morph.cropping.y = value.intValue());
            this.top.setLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            this.bottom = new GuiTrackpadElement(mc, I18n.format("blockbuster.gui.image.bottom"), (value) -> this.morph.cropping.h = value.intValue());
            this.bottom.setLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            this.texture.resizer().parent(this.area).set(10, 10, 115, 20);
            this.scale.resizer().relative(this.texture.resizer()).set(0, 25, 115, 20);
            this.shaded.resizer().relative(this.scale.resizer()).set(0, 25, 80, 11);
            this.lighting.resizer().relative(this.shaded.resizer()).set(0, 16, 80, 11);
            this.billboard.resizer().relative(this.lighting.resizer()).set(0, 16, 80, 11);
            this.picker.resizer().parent(this.area).set(10, 10, 0, 0).w(1, -20).h(1, -20);

            this.left.resizer().relative(this.billboard.resizer()).set(0, 11 + 30, 115, 20);
            this.right.resizer().relative(this.left.resizer()).set(0, 25, 115, 20);
            this.top.resizer().relative(this.right.resizer()).set(0, 25, 115, 20);
            this.bottom.resizer().relative(this.top.resizer()).set(0, 25, 115, 20);

            this.children.add(this.texture, this.scale, this.shaded, this.lighting, this.billboard, this.left, this.right, this.top, this.bottom, this.picker);
        }

        @Override
        public void fillData(ImageMorph morph)
        {
            super.fillData(morph);

            this.scale.setValue(morph.scale);
            this.shaded.button.setIsChecked(morph.shaded);
            this.lighting.button.setIsChecked(morph.lighting);
            this.billboard.button.setIsChecked(morph.billboard);

            this.left.setValue(morph.cropping.x);
            this.right.setValue(morph.cropping.w);
            this.top.setValue(morph.cropping.y);
            this.bottom.setValue(morph.cropping.h);
        }

        @Override
        public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
        {
            this.font.drawStringWithShadow(I18n.format("blockbuster.gui.image.crop"), this.left.area.x, this.left.area.y - 12, 0xffffff);

            GifTexture.bindTexture(this.morph.texture);
            int w = this.morph.getWidth();
            int h = this.morph.getHeight();

            this.font.drawStringWithShadow(I18n.format("blockbuster.gui.image.dimensions", w, h), this.bottom.area.x, this.bottom.area.y + 25, 0xaaaaaa);

            super.draw(tooltip, mouseX, mouseY, partialTicks);
        }
    }
}