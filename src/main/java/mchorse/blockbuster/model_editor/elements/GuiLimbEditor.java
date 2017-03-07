package mchorse.blockbuster.model_editor.elements;

import java.util.ArrayList;
import java.util.List;

import mchorse.blockbuster.client.gui.widgets.buttons.GuiCirculate;
import mchorse.blockbuster.model_editor.GuiModelEditor;
import mchorse.blockbuster.model_editor.elements.GuiThreeInput.IThreeListener;
import mchorse.metamorph.api.models.Model;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.client.config.GuiCheckBox;

/**
 * Limb editor GUI view
 *
 * This thing is going to be responsible for editing current selected limb,
 * its data and also current pose's transformations.
 */
public class GuiLimbEditor implements IThreeListener, GuiResponder
{
    /* Field IDs */

    /* Meta */
    private static final int NAME = 0;
    private static final int PARENT = 1;

    /* Visual properties */
    private static final int MIRROR = 2;
    private static final int TEXTURE = 3;
    private static final int SIZE = 4;
    private static final int ANCHOR = 4;

    /* Gameplay properties */
    private static final int LOOKING = 6;
    private static final int IDLE = 7;
    private static final int SWINGING = 8;
    private static final int SWIPING = 9;
    private static final int HOLDING = 10;

    /* Pose properties */
    private static final int TRANSLATE = 11;
    private static final int SCALE = 12;
    private static final int ROTATE = 13;

    /* Data */

    /**
     * Currently editing limb
     */
    public Model.Limb limb;

    /**
     * Current pose
     */
    public Model.Pose pose;

    /* GUI fields */

    /**
     * Parent screen
     */
    private GuiModelEditor editor;

    /**
     * List of buttons to be handled
     */
    private List<GuiButton> buttons = new ArrayList<GuiButton>();

    /* Buttons for changing stuff */
    private GuiButton name;
    private GuiButton parent;

    /* Visual properties */
    private GuiCheckBox mirror;
    private GuiThreeInput size;
    private GuiThreeInput anchor;

    /* Gameplay features */
    private GuiCheckBox looking;
    private GuiCheckBox idle;
    private GuiCheckBox swinging;
    private GuiCheckBox swiping;
    private GuiCirculate holding;

    /* Poses */
    private GuiThreeInput translate;
    private GuiThreeInput scale;
    private GuiThreeInput rotate;

    private int thing;

    /**
     * Initiate all GUI fields here
     *
     * I don't understand why Minecraft's GUI create new fields every time in
     * iniGui (only makes sense for {@link InitGuiEvent}, but then you still can
     * create buttons only once, update their positions and add them to
     * buttonList).
     */
    public GuiLimbEditor(GuiModelEditor editor)
    {
        this.editor = editor;

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        final int width = 100;

        /* Buttons */
        this.name = new GuiButton(NAME, 0, 0, width, 20, "Change name");
        this.parent = new GuiButton(PARENT, 0, 0, width, 20, "Change parent");

        /* Initiate inputs */
        this.mirror = new GuiCheckBox(MIRROR, 0, 0, "Mirror", false);
        this.size = new GuiThreeInput(SIZE, font, 0, 0, 0, this);
        this.anchor = new GuiThreeInput(ANCHOR, font, 0, 0, 0, this);

        /* Gameplay */
        this.looking = new GuiCheckBox(LOOKING, 0, 0, "Looking", false);
        this.idle = new GuiCheckBox(IDLE, 0, 0, "Idle", false);
        this.swinging = new GuiCheckBox(SWINGING, 0, 0, "Swinging", false);
        this.swiping = new GuiCheckBox(SWIPING, 0, 0, "Swiping", false);
        this.holding = new GuiCirculate(HOLDING, 0, 0, width, 20);

        this.holding.addLabel("No hands");
        this.holding.addLabel("Right");
        this.holding.addLabel("Left");

        /* Poses */
        this.translate = new GuiThreeInput(TRANSLATE, font, 0, 0, width, this);
        this.scale = new GuiThreeInput(SCALE, font, 0, 0, width, this);
        this.rotate = new GuiThreeInput(ROTATE, font, 0, 0, width, this);

        /* Attach buttons */
        this.buttons.add(this.name);
        this.buttons.add(this.parent);

        this.buttons.add(this.mirror);

        this.buttons.add(this.looking);
        this.buttons.add(this.idle);
        this.buttons.add(this.swinging);
        this.buttons.add(this.swiping);
        this.buttons.add(this.holding);
    }

    /**
     * Set currently editing limb
     */
    public void setLimb(Model.Limb limb)
    {
        this.limb = limb;

        /* Visual */
        this.mirror.setIsChecked(limb.mirror);
        this.size.a.setText(String.valueOf(limb.size[0]));
        this.size.b.setText(String.valueOf(limb.size[1]));
        this.size.c.setText(String.valueOf(limb.size[2]));
        this.anchor.a.setText(String.valueOf(limb.anchor[0]));
        this.anchor.b.setText(String.valueOf(limb.anchor[1]));
        this.anchor.c.setText(String.valueOf(limb.anchor[2]));

        /* Gameplay */
        this.looking.setIsChecked(limb.looking);
        this.idle.setIsChecked(limb.idle);
        this.swinging.setIsChecked(limb.swinging);
        this.swiping.setIsChecked(limb.swiping);
        this.holding.setValue(limb.holding.isEmpty() ? 0 : (limb.holding.equals("right") ? 1 : 2));

        this.updatePoseFields();
    }

    /**
     * Set currently used pose
     */
    public void setPose(Model.Pose pose)
    {
        this.pose = pose;

        this.updatePoseFields();
    }

    private void updatePoseFields()
    {
        if (this.limb == null)
        {
            return;
        }

        Model.Transform trans = this.pose.limbs.get(this.limb.name);

        if (trans != null)
        {
            this.translate.a.setText(String.valueOf(trans.translate[0]));
            this.translate.b.setText(String.valueOf(trans.translate[1]));
            this.translate.c.setText(String.valueOf(trans.translate[2]));

            this.scale.a.setText(String.valueOf(trans.scale[0]));
            this.scale.b.setText(String.valueOf(trans.scale[1]));
            this.scale.c.setText(String.valueOf(trans.scale[2]));

            this.rotate.a.setText(String.valueOf(trans.rotate[0]));
            this.rotate.b.setText(String.valueOf(trans.rotate[1]));
            this.rotate.c.setText(String.valueOf(trans.rotate[2]));
        }
    }

    /**
     * Initiate here all your GUI stuff
     */
    public void initiate(int x, int y)
    {
        int width = 100;

        this.name.xPosition = x;
        this.name.yPosition = y;
        y += 25;
        this.parent.xPosition = x;
        this.parent.yPosition = y;
        y += 35;
        this.mirror.xPosition = x;
        this.mirror.yPosition = y;
        y += 15;
        this.size.update(x, y, width);
        y += 20;
        this.anchor.update(x, y, width);
        y += 33;
        this.looking.xPosition = x;
        this.looking.yPosition = y;
        y += 20;
        this.idle.xPosition = x;
        this.idle.yPosition = y;
        y += 20;
        this.swinging.xPosition = x;
        this.swinging.yPosition = y;
        y += 20;
        this.swiping.xPosition = x;
        this.swiping.yPosition = y;
        y += 18;
        this.holding.xPosition = x;
        this.holding.yPosition = y;
        y += 35;
        this.translate.update(x, y, width);
        y += 20;
        this.scale.update(x, y, width);
        y += 20;
        this.rotate.update(x, y, width);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.limb == null)
        {
            return;
        }

        this.size.mouseClicked(mouseX, mouseY, mouseButton);
        this.anchor.mouseClicked(mouseX, mouseY, mouseButton);

        this.checkButtons(mouseX, mouseY, mouseButton);

        if (this.pose == null)
        {
            return;
        }

        this.translate.mouseClicked(mouseX, mouseY, mouseButton);
        this.scale.mouseClicked(mouseX, mouseY, mouseButton);
        this.rotate.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Check whether buttons were clicked
     *
     * This method is a pretty much copy paste from {@link GuiScreen}'s method
     * mouseClicked.
     */
    private void checkButtons(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttons.size(); ++i)
            {
                GuiButton button = this.buttons.get(i);

                if (button.mousePressed(this.editor.mc, mouseX, mouseY))
                {
                    button.playPressSound(this.editor.mc.getSoundHandler());
                    this.actionPerformed(button);
                }
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        if (this.limb == null)
        {
            return;
        }

        this.size.keyTyped(typedChar, keyCode);
        this.anchor.keyTyped(typedChar, keyCode);

        if (this.pose == null)
        {
            return;
        }

        this.translate.keyTyped(typedChar, keyCode);
        this.scale.keyTyped(typedChar, keyCode);
        this.rotate.keyTyped(typedChar, keyCode);
    }

    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        FontRenderer font = this.editor.mc.fontRendererObj;

        if (this.limb == null)
        {
            font.drawStringWithShadow("No limb selected", this.name.xPosition, this.name.yPosition - 15, 0xffffff);
            return;
        }

        font.drawStringWithShadow(this.limb.name, this.name.xPosition, this.name.yPosition - 15, 0xffffff);

        this.size.draw();
        this.anchor.draw();

        for (GuiButton button : this.buttons)
        {
            button.drawButton(this.editor.mc, mouseX, mouseY);
        }

        if (this.pose == null)
        {
            return;
        }

        this.translate.draw();
        this.rotate.draw();
        this.scale.draw();
    }

    /* Methods for changing values */

    /**
     * Action performed
     *
     * This method is responsible for changing values based on the button
     * clicks. Works with {@link GuiCheckBox} and {@link GuiCirculate}.
     *
     * This method is also responsible for triggering name and parent changing
     * modals.
     */
    private void actionPerformed(GuiButton button)
    {
        if (this.limb == null)
        {
            return;
        }

        if (button.id == MIRROR)
        {
            this.limb.mirror = this.mirror.isChecked();
        }
        if (button.id == LOOKING)
        {
            this.limb.looking = this.looking.isChecked();
        }
        if (button.id == IDLE)
        {
            this.limb.idle = this.idle.isChecked();
        }
        if (button.id == SWINGING)
        {
            this.limb.swinging = this.swinging.isChecked();
        }
        if (button.id == SWIPING)
        {
            this.limb.swiping = this.swiping.isChecked();
        }
        if (button.id == HOLDING)
        {
            this.holding.toggle();

            int value = this.holding.getValue();
            this.limb.holding = value == 0 ? "" : (value == 1 ? "right" : "left");
        }
    }

    @Override
    public void setValue(int id, int subset, String value)
    {
        if (this.limb == null)
        {
            return;
        }

        if (id == ANCHOR)
        {

        }
        if (id == SIZE)
        {

        }

        if (this.pose == null)
        {
            return;
        }

        try
        {
            Model.Transform trans = this.pose.limbs.get(this.limb.name);
            float val = Float.parseFloat(value);

            if (id == TRANSLATE)
            {
                trans.translate[subset] = val;
            }
            if (id == SCALE)
            {
                trans.scale[subset] = val;
            }
            if (id == ROTATE)
            {
                trans.rotate[subset] = val;
            }
        }
        catch (NumberFormatException e)
        {}
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
        if (this.limb == null)
        {
            return;
        }
    }
}