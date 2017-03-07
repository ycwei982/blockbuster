package mchorse.blockbuster.model_editor.elements;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;

/**
 * "Three inputs in one" GUI widget
 */
public class GuiThreeInput implements GuiResponder
{
    public GuiTextField a;
    public GuiTextField b;
    public GuiTextField c;

    private int id;
    private IThreeListener listener;

    public GuiThreeInput(int id, FontRenderer font, int x, int y, int width, IThreeListener listener)
    {
        int w = (width - 6) / 3;

        this.id = id;
        this.listener = listener;

        this.a = new GuiTextField(id, font, x + 1, y + 1, w - 2, 16);
        this.b = new GuiTextField(id + 1, font, x + w + 3 + 1, y, w - 2, 16);
        this.c = new GuiTextField(id + 2, font, x + width - w + 1, y, w - 2, 16);

        this.a.setGuiResponder(this);
        this.b.setGuiResponder(this);
        this.c.setGuiResponder(this);
    }

    public void update(int x, int y, int width)
    {
        int w = (width - 6) / 3;

        this.a.xPosition = x + 1;
        this.b.xPosition = x + w + 3 + 1;
        this.c.xPosition = x + width - w + 1;

        this.a.yPosition = this.b.yPosition = this.c.yPosition = y + 1;
        this.a.width = this.b.width = this.c.width = w - 2;
    }

    @Override
    public void setEntryValue(int id, boolean value)
    {}

    @Override
    public void setEntryValue(int id, float value)
    {}

    @Override
    public void setEntryValue(int id, String value)
    {
        this.listener.setValue(this.id, id - this.id, value);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.a.mouseClicked(mouseX, mouseY, mouseButton);
        this.b.mouseClicked(mouseX, mouseY, mouseButton);
        this.c.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        this.a.textboxKeyTyped(typedChar, keyCode);
        this.b.textboxKeyTyped(typedChar, keyCode);
        this.c.textboxKeyTyped(typedChar, keyCode);
    }

    public void draw()
    {
        this.a.drawTextBox();
        this.b.drawTextBox();
        this.c.drawTextBox();
    }

    public static interface IThreeListener
    {
        public void setValue(int id, int subset, String value);
    }
}