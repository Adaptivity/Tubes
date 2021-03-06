package schmoller.tubes.gui;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import schmoller.tubes.ModTubes;
import schmoller.tubes.api.gui.GuiExtContainer;
import schmoller.tubes.api.helpers.CommonHelper;
import schmoller.tubes.definitions.TypeFilterTube;
import schmoller.tubes.network.packets.ModPacketSetColor;
import schmoller.tubes.network.packets.ModPacketSetFilterMode;
import schmoller.tubes.types.FilterTube;
import schmoller.tubes.types.FilterTube.Comparison;
import schmoller.tubes.types.FilterTube.Mode;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

public class FilterTubeGui extends GuiExtContainer
{
	private FilterTube mTube;
	public FilterTubeGui(FilterTube tube, EntityPlayer player)
	{
		super(new FilterTubeContainer(tube, player));
		
		mTube = tube;
		
		xSize = 176;
		ySize = 154;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer( int curX, int curY )
	{
		String s = StatCollector.translateToLocal("tubes.filter.name");
		fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        
        super.drawGuiContainerForegroundLayer(curX, curY);

		int xx = curX - (width - xSize) / 2;
		int yy = curY - (height - ySize) / 2;

		if(xx >= 153 && xx <= 167)
		{
			if(yy >= 19 && yy <= 33) // Mode button
			{
				drawHoveringText(Arrays.asList(StatCollector.translateToLocalFormatted("gui.filtertube.modestring", StatCollector.translateToLocal("gui.filtertube.mode." + mTube.getMode().name()))), xx, yy, fontRendererObj);
				RenderHelper.enableGUIStandardItemLighting();
			}
			else if(yy >= 35 && yy <= 49) // Comparison button
			{
				String text = StatCollector.translateToLocalFormatted("gui.filtertube.size." + mTube.getComparison().name(), StatCollector.translateToLocal("gui.filtertube.mode." + mTube.getMode().name()));
				
				int old = width;
				width -= (xx + curX); 
				drawHoveringText(Arrays.asList(text), xx, yy, fontRendererObj);
				RenderHelper.enableGUIStandardItemLighting();
				
				width = old;
			}
			else if(yy >= 51 && yy <= 65)
			{
				int colour = mTube.getColour();
				String text = StatCollector.translateToLocal("gui.colors.none");
				if(colour != -1)
					text = CommonHelper.getDyeName(colour);
				
				drawHoveringText(Arrays.asList(text), xx, yy, fontRendererObj);
				RenderHelper.enableGUIStandardItemLighting();
			}
		}
		

		
	}
	
	@Override
	protected void mouseClicked( int x, int y, int button )
	{
		int xx = x - (width - xSize) / 2;
		int yy = y - (height - ySize) / 2;
		
		if(xx >= 153 && xx <= 167)
		{
			if(yy >= 19 && yy <= 33) // Mode button
			{
				Mode current = mTube.getMode();
				int i = current.ordinal();
				if(button == 0)
					++i;
				else if(button == 1)
					--i;
				else if(button == 2)
					i = 0;
				
				if(i < 0)
					i = Mode.values().length - 1;
				else if(i >= Mode.values().length)
					i = 0;
				
				mTube.setMode(Mode.values()[i]);
				ModTubes.packetManager.sendPacketToServer(new ModPacketSetFilterMode(mTube.x(), mTube.y(), mTube.z(), Mode.values()[i]));
			}
			else if(yy >= 35 && yy <= 49) // Comparison button
			{
				Comparison current = mTube.getComparison();
				int i = current.ordinal();
				if(button == 0)
					++i;
				else if(button == 1)
					--i;
				else if(button == 2)
					i = 0;
				
				if(i < 0)
					i = Comparison.values().length - 1;
				else if(i >= Comparison.values().length)
					i = 0;
				
				mTube.setComparison(Comparison.values()[i]);
				ModTubes.packetManager.sendPacketToServer(new ModPacketSetFilterMode(mTube.x(), mTube.y(), mTube.z(), Comparison.values()[i]));
			}
			else if(yy >= 51 && yy <= 65)
			{
				int colour = mTube.getColour();
				
				if(button == 0)
					++colour;
				else if(button == 1)
					-- colour;
				else if(button == 2)
					colour = -1;
				
				if(colour > 15)
					colour = -1;
				if(colour < -1)
					colour = 15;
				
				mTube.setColour((short)colour);
				
				ModTubes.packetManager.sendPacketToServer(new ModPacketSetColor(mTube.x(), mTube.y(), mTube.z(), colour));
			}
		}
		super.mouseClicked(x, y, button);
	}
	
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer( float f, int i, int j )
	{
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		mc.renderEngine.bindTexture(TypeFilterTube.gui);
		
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		drawTexturedModalRect(x + 153, y + 19, xSize, 14 * mTube.getMode().ordinal(), 14, 14);
		drawTexturedModalRect(x + 153, y + 35, xSize + 14, 14 * mTube.getComparison().ordinal(), 14, 14);
		
		int colour = mTube.getColour();
		
		if(colour != -1)
		{
			drawRect(x + 156, y + 54, x + 164, y + 62, CommonHelper.getDyeColor(colour));
			GL11.glColor4f(1f, 1f, 1f, 1f);
		}
	}

}
