package noname.blockbuster.client.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import noname.blockbuster.client.gui.elements.GuiCast;
import noname.blockbuster.network.Dispatcher;
import noname.blockbuster.network.common.director.PacketDirectorReset;

/**
 * Director block (the one for machinimas) GUI
 */
public class GuiDirector extends GuiScreen
{
    /* Cached localized strings */
    protected String title = I18n.format("blockbuster.director.title");
    protected String noCast = I18n.format("blockbuster.director.no_cast");

    /* Input data */
    private BlockPos pos;

    /* GUI fields */
    protected GuiCast cast;
    protected GuiButton done;
    protected GuiButton reset;

    public GuiDirector(BlockPos pos)
    {
        this.pos = pos;
    }

    public void setCast(List<String> actors, List<String> cameras)
    {
        if (actors == null || cameras == null || (actors.isEmpty() && cameras.isEmpty()))
        {
            this.cast = null;
            return;
        }

        this.cast = new GuiCast(this.pos, this.width / 2 - 120, 40, 240, 155);
        this.cast.setCast(actors, cameras);
        this.cast.setWorldAndResolution(this.mc, this.width, this.height);
    }

    /* Actions and handling */

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(null);
        }
        else if (button.id == 1)
        {
            Dispatcher.getInstance().sendToServer(new PacketDirectorReset(this.pos));
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        if (this.cast != null)
        {
            this.cast.handleMouseInput();
        }
    }

    /* GUI and drawing */

    @Override
    public void initGui()
    {
        int w = 200;
        int x = this.width / 2 - w / 2;

        this.done = new GuiButton(0, x, 205, 95, 20, I18n.format("blockbuster.gui.done"));
        this.reset = new GuiButton(1, x + 105, 205, 95, 20, I18n.format("blockbuster.gui.reset"));

        this.buttonList.add(this.done);
        this.buttonList.add(this.reset);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 0xffffffff);

        if (this.cast != null)
        {
            this.cast.drawScreen(mouseX, mouseY, partialTicks);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, this.noCast, this.width / 2, 80, 0xffffff);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}